package com.sec.internal.ims.entitlement.nsds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NSDSSimEventManager extends Handler {
    private static final int EVENT_SIMMOBILITY_CHANGED = 2;
    private static final int EVENT_SIM_SUBSCRIBE_ID_CHANGED = 1;
    private static final int EVT_SIM_READY = 0;
    private static final int EVT_SIM_REFRESH = 3;
    private static final String LOG_TAG = "NSDSSimEventManager";
    public static final int NOTIFY_SIM_READY = 100;
    private static NSDSSimEventManager mInstance = null;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static final UriMatcher sUriMatcher;
    protected ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            IMSLog.i(NSDSSimEventManager.LOG_TAG, "Uri changed:" + uri);
            int phoneId = SimUtil.getDefaultPhoneId();
            if (uri.getFragment() != null && uri.getFragment().contains(ImsConstants.Uris.FRAGMENT_SIM_SLOT)) {
                phoneId = Character.getNumericValue(uri.getFragment().charAt(7));
                IMSLog.i(NSDSSimEventManager.LOG_TAG, "query : Exist simslot on uri: " + phoneId);
            }
            if (NSDSSimEventManager.sUriMatcher.match(uri) == 2) {
                NSDSSimEventManager.this.onSimMobilityChanged(phoneId);
            }
        }
    };
    private final Context mContext;
    protected BroadcastReceiver mDeviceReadyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IMSLog.i(NSDSSimEventManager.LOG_TAG, "DeviceReadyReceiver: " + intent.getAction());
            if (NSDSSimEventManager.this.isDeviceReady()) {
                for (ISimManager sm : NSDSSimEventManager.this.mSimManagers) {
                    NSDSSimEventManager.this.onEventSimReady(sm.getSimSlotIndex());
                }
            }
        }
    };
    private List<Messenger> mSimEvtMessengers = new ArrayList();
    private boolean mSimEvtRegistered = false;
    protected List<ISimManager> mSimManagers = new ArrayList();
    protected Map<Integer, Boolean> mSimMobilitystatus = new HashMap();

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(ImsConstants.SystemSettings.IMS_SIM_MOBILITY.getAuthority(), ImsConstants.SystemSettings.IMS_SIM_MOBILITY.getPath(), 2);
    }

    public NSDSSimEventManager(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        initSimManagers();
        registerContentObserver();
        registerDeviceReadyReceiver();
    }

    public static NSDSSimEventManager getInstance() {
        return mInstance;
    }

    public static NSDSSimEventManager createInstance(Looper looper, Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new NSDSSimEventManager(context, looper);
            }
        }
        return mInstance;
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_SIM_MOBILITY.getUri(), false, this.mContentObserver);
    }

    public ISimManager getSimManager(String imsi) {
        for (ISimManager simManager : this.mSimManagers) {
            if (imsi.equals(simManager.getImsi())) {
                return simManager;
            }
        }
        return null;
    }

    public ISimManager getSimManagerFromSimSlot(int simSlot) {
        for (ISimManager sm : this.mSimManagers) {
            if (sm.getSimSlotIndex() == simSlot) {
                return sm;
            }
        }
        IMSLog.i(LOG_TAG, "ISimManager[" + simSlot + "] is not exist. Return null..");
        return null;
    }

    public void registerSimEventMessenger(Messenger messenger, int simSlot) {
        synchronized (mLock) {
            if (messenger == null) {
                IMSLog.e(LOG_TAG, "registerSimEventMessenger: null messenger");
                return;
            }
            IMSLog.i(LOG_TAG, "registerSimEventMessenger size: " + this.mSimEvtMessengers.size());
            if (!this.mSimEvtMessengers.contains(messenger)) {
                this.mSimEvtMessengers.add(messenger);
            }
            notifyLazySimReady(messenger, simSlot);
        }
    }

    public void unregisterSimEventMessenger(Messenger evtMessenger) {
        synchronized (mLock) {
            if (evtMessenger == null) {
                IMSLog.e(LOG_TAG, "unregisterSimEventMessenger: messenger null");
                return;
            }
            IMSLog.i(LOG_TAG, "unregisterSimEventMessenger: " + this.mSimEvtMessengers.size());
            this.mSimEvtMessengers.remove(evtMessenger);
        }
    }

    private void registerDeviceReadyReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NSDSNamespaces.NSDSActions.DEVICE_READY_AFTER_BOOTUP);
        IMSLog.i(LOG_TAG, "registerDeviceReadyReceiver");
        this.mContext.registerReceiver(this.mDeviceReadyReceiver, filter);
    }

    private void unregisterDeviceReadyReceiver() {
        try {
            IMSLog.i(LOG_TAG, "unregisterDeviceReadyReceiver");
            this.mContext.unregisterReceiver(this.mDeviceReadyReceiver);
        } catch (IllegalArgumentException e) {
            IMSLog.e(LOG_TAG, "unregisterDeviceReadyReceiver: " + e.getMessage());
        }
    }

    public void handleMessage(Message msg) {
        IMSLog.i(LOG_TAG, "handleMessage:" + msg.what);
        int i = msg.what;
        if (i != 0) {
            if (i == 1) {
                onSimSubscribeIdChanged((SubscriptionInfo) ((AsyncResult) msg.obj).result);
                return;
            } else if (i != 3) {
                return;
            }
        }
        onEventSimReady(((Integer) ((AsyncResult) msg.obj).result).intValue());
    }

    private void initSimManagers() {
        this.mSimManagers.clear();
        this.mSimManagers.addAll(SimManagerFactory.getAllSimManagers());
        for (ISimManager sm : this.mSimManagers) {
            this.mSimMobilitystatus.put(Integer.valueOf(sm.getSimSlotIndex()), false);
        }
        if (!this.mSimEvtRegistered) {
            registerForSimEvents();
        }
    }

    /* access modifiers changed from: private */
    public void onEventSimReady(int phoneId) {
        ISimManager sm;
        IMSLog.i(LOG_TAG, phoneId, "onEventSimReady:");
        if (isDeviceReady() && (sm = getSimManagerFromSimSlot(phoneId)) != null) {
            boolean z = false;
            if ((phoneId < 0 || sm.hasNoSim()) || sm.hasVsim()) {
                z = true;
            }
            notifySimReady(z, phoneId);
        }
    }

    private void registerForSimEvents() {
        for (ISimManager sm : this.mSimManagers) {
            sm.registerForSimReady(this, 0, (Object) null);
            sm.registerForSimRefresh(this, 3, (Object) null);
            sm.registerForSimRemoved(this, 3, (Object) null);
        }
        SimManagerFactory.registerForSubIdChange(this, 1, (Object) null);
        this.mSimEvtRegistered = true;
    }

    private void onSimSubscribeIdChanged(SubscriptionInfo subInfo) {
        int simSlotIndex = subInfo.getSimSlotIndex();
        IMSLog.i(LOG_TAG, simSlotIndex, "onSimSubscribeIdChanged, subId: " + subInfo.getSubscriptionId());
        for (ISimManager sm : this.mSimManagers) {
            if (sm.getSimSlotIndex() == subInfo.getSimSlotIndex()) {
                sm.setSubscriptionInfo(subInfo);
            }
        }
    }

    private void notifySimReady(boolean absent, int phoneId) {
        IMSLog.i(LOG_TAG, "notifySimReady, isSimAbsent: " + absent);
        String deviceUid = DeviceIdHelper.getDeviceId(this.mContext, phoneId);
        boolean isSimSwapped = isSimSwapped(phoneId);
        IMSLog.i(LOG_TAG, phoneId, " isSimSwapped:" + isSimSwapped);
        IMSLog.c(LogClass.ES_CHECK_SIMSWAP, phoneId + ",SIMSWAP:" + isSimSwapped);
        if (isSimSwapped) {
            NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceUid);
            MnoNsdsStrategyCreator.resetMnoStrategy();
        }
        MnoNsdsConfigStrategyCreator.updateMnoStrategy(this.mContext, phoneId);
        notifyMessengerSimReady(getSimManagerFromSimSlot(phoneId));
    }

    private void notifyLazySimReady(Messenger messenger, int simSlot) {
        ISimManager sm = getSimManagerFromSimSlot(simSlot);
        if (sm != null && sm.isSimAvailable()) {
            try {
                messenger.send(obtainSimReadyMessage(sm));
            } catch (RemoteException e) {
                IMSLog.e(LOG_TAG, "notifyLazySimReady: " + e.getMessage());
                this.mSimEvtMessengers.remove(messenger);
            }
        }
    }

    private boolean isSimSwapped(int simSlot) {
        ISimManager simManager = getSimManagerFromSimSlot(simSlot);
        String prevImsiForSlot = NSDSSharedPrefHelper.getPrefForSlot(this.mContext, simSlot, "imsi");
        String currImsiForSlot = simManager == null ? null : simManager.getImsi();
        NSDSSharedPrefHelper.savePrefForSlot(this.mContext, simSlot, NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI, prevImsiForSlot);
        NSDSSharedPrefHelper.savePrefForSlot(this.mContext, simSlot, "imsi", currImsiForSlot);
        if (TextUtils.isEmpty(prevImsiForSlot) || prevImsiForSlot.equals(currImsiForSlot)) {
            return isSimSwapPending(simSlot);
        }
        Context context = this.mContext;
        NSDSSharedPrefHelper.save(context, DeviceIdHelper.getDeviceId(context, simSlot), NSDSNamespaces.NSDSSharedPref.PREF_PEDNING_SIM_SWAP, true);
        return true;
    }

    private boolean isSimSwapPending(int simSlot) {
        return NSDSSharedPrefHelper.isSimSwapPending(this.mContext, DeviceIdHelper.getDeviceId(this.mContext, simSlot));
    }

    private void notifyMessengerSimReady(ISimManager simManager) {
        synchronized (mLock) {
            for (int i = this.mSimEvtMessengers.size() - 1; i >= 0; i--) {
                try {
                    this.mSimEvtMessengers.get(i).send(obtainSimReadyMessage(simManager));
                } catch (RemoteException e) {
                    IMSLog.e(LOG_TAG, "notifyMessengerSimReady: dead messenger, removed" + e.getMessage());
                    this.mSimEvtMessengers.remove(i);
                }
            }
            IMSLog.i(LOG_TAG, "notifyMessengerSimReady: notified");
        }
    }

    private Bundle getSimEvtBundle(ISimManager simManager) {
        int phoneId = simManager.getSimSlotIndex();
        IMSLog.i(LOG_TAG, "getSimEvtBundle: phoneId " + phoneId);
        boolean noSim = phoneId < 0 || simManager.hasNoSim() || simManager.hasVsim();
        Bundle simEvtBundle = new Bundle();
        simEvtBundle.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, phoneId);
        simEvtBundle.putBoolean(NSDSNamespaces.NSDSExtras.SIM_ABSENT, noSim);
        simEvtBundle.putBoolean(NSDSNamespaces.NSDSExtras.SIM_SWAPPED, isSimSwapPending(phoneId));
        return simEvtBundle;
    }

    private Message obtainSimReadyMessage(ISimManager simManager) {
        Message msg = new Message();
        msg.what = 100;
        msg.obj = getSimEvtBundle(simManager);
        return msg;
    }

    public static void startIMSDeviceConfigService(Context context, ISimManager sm) {
        boolean startConfigService = false;
        boolean startNsdsService = false;
        Mno mno = sm.getSimMno();
        int phoneId = sm.getSimSlotIndex();
        IMSLog.i(LOG_TAG, phoneId, "startIMSDeviceConfigService : check CSC , Mnoname: " + sm.getSimMnoName());
        IMSLog.c(LogClass.ES_START_MNO, phoneId + ",START:" + sm.getSimMnoName());
        if (EntFeatureDetector.checkVSimFeatureEnabled("Nsds", phoneId)) {
            startConfigService = true;
            startNsdsService = true;
        } else if (EntFeatureDetector.checkVSimFeatureEnabled("Nsdsconfig", phoneId)) {
            startConfigService = true;
        }
        if (startConfigService || startNsdsService) {
            if ("".equalsIgnoreCase(NSDSConfigHelper.getConfigServer(phoneId))) {
                startConfigService = false;
                startNsdsService = false;
                IMSLog.i(LOG_TAG, phoneId, "startIMSDeviceConfigService : Not support ES server");
            }
            if (!ImsUtil.isSimMobilityActivated(phoneId)) {
                boolean isVoLteEnabled = true;
                if (DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.IMS, phoneId) == 1) {
                    if (DmConfigHelper.getImsSwitchValue(context, "volte", phoneId) != 1) {
                        isVoLteEnabled = false;
                    }
                    if (!isVoLteEnabled) {
                        if (mno == Mno.ATT || mno == Mno.TELEFONICA_UK || mno == Mno.TELEFONICA_UK_LAB) {
                            startNsdsService = false;
                            startConfigService = false;
                        }
                        IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : Nsds is disabled");
                    }
                    if (mno == Mno.TMOUS && startConfigService) {
                        String omcProperty = SemSystemProperties.get("ro.simbased.changetype", "");
                        String omcCode = OmcCode.get();
                        if (!mno.getMatchedSalesCode(omcCode).equals(omcCode) || ((DeviceUtil.isTablet() && !DeviceUtil.isSupport5G(context)) || (omcProperty.contains("SED") && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29))) {
                            startNsdsService = false;
                            startConfigService = false;
                            IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : ConfigService is disabled");
                        }
                    }
                } else {
                    IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : IMS is disabled");
                    startNsdsService = false;
                    startConfigService = false;
                }
            }
        }
        if (mno == Mno.GCI) {
            IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : GCI");
            startConfigService = true;
            startNsdsService = true;
        }
        if (mno == Mno.TELEFONICA_UK || mno == Mno.TELEFONICA_UK_LAB) {
            startConfigService = false;
            IMSLog.i(LOG_TAG, "startIMSDeviceConfigService : ConfigService is disabled for O2U");
        }
        IMSLog.i(LOG_TAG, phoneId, "startIMSDeviceConfigService : ConfigService [" + startConfigService + "], Nsds[" + startNsdsService + "]");
        IMSLog.c(LogClass.ES_START_SERVICE, phoneId + ",DC:" + startConfigService + ",NSDS:" + startNsdsService);
        if (startConfigService || startNsdsService) {
            if (startConfigService) {
                EntitlementConfigService.startEntitlementConfigService(context, phoneId);
            }
            if (startNsdsService) {
                NSDSMultiSimService.startNsdsMultiSimService(context, phoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onSimMobilityChanged(int phoneId) {
        ISimManager sm = getSimManagerFromSimSlot(phoneId);
        boolean simmoiblity = false;
        Iterator<ImsProfile> it = SlotBasedConfig.getInstance(phoneId).getProfiles().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().getSimMobility()) {
                    simmoiblity = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (sm != null && simmoiblity != this.mSimMobilitystatus.get(Integer.valueOf(phoneId)).booleanValue()) {
            IMSLog.i(LOG_TAG, phoneId, "onSimMobilityChanged to " + simmoiblity + " : Start again entitlement service");
            this.mSimMobilitystatus.put(Integer.valueOf(phoneId), Boolean.valueOf(simmoiblity));
            startIMSDeviceConfigService(this.mContext, sm);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceReady() {
        if (!NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            IMSLog.i(LOG_TAG, "isDeviceReady() User lock ");
            return false;
        }
        if (!MigrationHelper.checkMigrateDB(this.mContext)) {
            MigrationHelper.migrateDBToCe(this.mContext);
        }
        unregisterDeviceReadyReceiver();
        return true;
    }
}
