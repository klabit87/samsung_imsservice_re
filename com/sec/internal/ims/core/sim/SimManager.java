package com.sec.internal.ims.core.sim;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsCscFeature;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SimManager extends Handler implements ISimManager {
    private static final String DELAYED_ISIM_LOAD = "DELAYED_ISIM_LOAD";
    protected static final int EVENT_DDS_CHANGED = 6;
    protected static final int EVENT_IMSSWITCH_UPDATED = 7;
    private static final int EVENT_LOAD_MNOMAP = 8;
    protected static final int EVENT_SIM_REFRESH = 3;
    protected static final int EVENT_SIM_STATE_CHANGED = 1;
    protected static final int EVENT_SOFTPHONE_AUTH_FAILED = 5;
    protected static final int EVENT_UICC_CHANGED = 2;
    private static final String LOG_TAG = "SimManager";
    private static final String SMF_MNONAME_PROP = "sys.smf.mnoname";
    protected static final String SOFTPHONE_OPERATOR_CODE = "310999";
    private static final Uri URI_UPDATE_GLOBAL = Uri.parse("content://com.sec.ims.settings/global");
    private static final Uri URI_UPDATE_MNO = Uri.parse("content://com.sec.ims.settings/mno");
    private static MnoMap mMnoMap = null;
    private static final String sInteractAcrossUsersFullPermission = "android.permission.INTERACT_ACROSS_USERS_FULL";
    private String OMCNW_CODE;
    private String OMC_CODE;
    private final AkaEventReceiver mAkaEventReceiver = new AkaEventReceiver();
    private final Context mContext;
    private final BroadcastReceiver mDDSIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(intent.getAction())) {
                int defaultPhoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(context));
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                int access$100 = SimManager.this.mSimSlot;
                IMSLog.i(SimManager.LOG_TAG, access$100, "DDS change intent received: defaultPhoneId=" + defaultPhoneId + ", subId=" + subId);
                SimManager simManager = SimManager.this;
                simManager.sendMessage(simManager.obtainMessage(6, defaultPhoneId, 0));
            }
        }
    };
    private Mno mDevMno = Mno.DEFAULT;
    private final List<ISimEventListener> mEventListeners = new ArrayList();
    private SimpleEventLog mEventLog;
    private final BroadcastReceiver mGtsAppInstallReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(intent.getData().getSchemeSpecificPart(), "com.google.android.gts.telephony")) {
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != 525384130) {
                    if (hashCode == 1544582882 && action.equals("android.intent.action.PACKAGE_ADDED")) {
                        c = 0;
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    c = 1;
                }
                if (c != 0) {
                    if (c == 1 && SimManager.this.getGtsAppInstalled()) {
                        Log.w(SimManager.LOG_TAG, "Remove GTS package, SendMessage SIM LOAD again");
                        SimManager.this.setGtsAppInstalled(false);
                        SimManager.this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                        SimManager simManager = SimManager.this;
                        simManager.sendMessage(simManager.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED));
                    }
                } else if (!SimManager.this.getGtsAppInstalled()) {
                    Log.w(SimManager.LOG_TAG, "ADD GTS package, SendMessage SIM LOAD again");
                    SimManager.this.setGtsAppInstalled(true);
                    SimManager.this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                    SimManager simManager2 = SimManager.this;
                    simManager2.sendMessage(simManager2.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED));
                }
            }
        }
    };
    private String mHighestPriorityEhplmn = "";
    private ContentObserver mImsServiceSwitchObserver = null;
    private String mImsi = "";
    private String mImsiFromImpi;
    private boolean mIsCrashSimEvent = false;
    private boolean mIsGtsAppInstalled = false;
    /* access modifiers changed from: private */
    public boolean mIsGuestMode = false;
    private boolean mIsOutBoundSIM = false;
    private boolean mIsRefresh = false;
    private boolean mIsimLoaded = false;
    private boolean mLabSimCard = false;
    private String mLastImsi = null;
    private ContentValues mMnoInfo = new ContentValues();
    private final BroadcastReceiver mMultiUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_BACKGROUND".equals(action)) {
                boolean unused = SimManager.this.mIsGuestMode = true;
            } else if ("android.intent.action.USER_FOREGROUND".equals(action)) {
                boolean unused2 = SimManager.this.mIsGuestMode = false;
            }
            int access$100 = SimManager.this.mSimSlot;
            IMSLog.i(SimManager.LOG_TAG, access$100, "IsGuestMode = " + SimManager.this.mIsGuestMode);
        }
    };
    private Mno mNetMno = Mno.DEFAULT;
    private String mOperatorFromImpi;
    private SimDataAdaptor mSimDataAdaptor = null;
    private final BroadcastReceiver mSimIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1);
            int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
            int access$100 = SimManager.this.mSimSlot;
            IMSLog.i(SimManager.LOG_TAG, access$100, "SimSimIntentReceiver: received action " + action + " subId=" + subId + " mSubId=" + SimManager.this.mSubscriptionId);
            if (SimManager.this.mSubscriptionId < 0 && subId != Integer.MAX_VALUE) {
                if (!SimUtil.isMultiSimSupported() || ((!ImsConstants.Intents.ACTION_SIM_REFRESH.equals(action) && !"android.intent.action.SIM_STATE_CHANGED".equals(action) && !ImsConstants.Intents.ACTION_SIM_ISIM_LOADED.equals(action)) || phoneId == SimManager.this.mSimSlot)) {
                    int unused = SimManager.this.mSubscriptionId = subId;
                } else {
                    Log.i(SimManager.LOG_TAG, "phoneId mismatch : " + action + ", " + phoneId);
                    return;
                }
            }
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                String iccState = intent.getStringExtra("ss");
                if (phoneId != SimManager.this.mSimSlot) {
                    int access$1002 = SimManager.this.mSimSlot;
                    IMSLog.i(SimManager.LOG_TAG, access$1002, "phoneId mismatch : ACTION_SIM_STATE_CHANGED, " + phoneId);
                    return;
                }
                SimManager simManager = SimManager.this;
                simManager.sendMessage(simManager.obtainMessage(1, iccState));
            } else if (ImsConstants.Intents.ACTION_SIM_ISIM_LOADED.equals(action)) {
                if (phoneId != SimManager.this.mSimSlot) {
                    int access$1003 = SimManager.this.mSimSlot;
                    IMSLog.i(SimManager.LOG_TAG, access$1003, "phoneId mismatch : ACTION_SIM_ISIM_LOADED, " + phoneId);
                    return;
                }
                SimManager simManager2 = SimManager.this;
                simManager2.sendMessage(simManager2.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED));
            } else if (ImsConstants.Intents.ACTION_SIM_ICCID_CHANGED.equals(action)) {
                SimManager.this.sendEmptyMessage(2);
            } else if (ImsConstants.Intents.ACTION_SIM_REFRESH.equals(action) && phoneId == SimManager.this.mSimSlot && !SimManager.this.hasVsim()) {
                SimManager.this.sendEmptyMessage(3);
            }
        }
    };
    private String mSimMnoName = "";
    protected final RegistrantList mSimReadyRegistrants = new RegistrantList();
    protected final RegistrantList mSimRefreshRegistrants = new RegistrantList();
    protected final RegistrantList mSimRemovedRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public int mSimSlot = 0;
    protected SimConstants.SIM_STATE mSimState = SimConstants.SIM_STATE.UNKNOWN;
    protected final RegistrantList mSimStateChangedRegistrants = new RegistrantList();
    private SimConstants.SIM_STATE mSimStatePrev = SimConstants.SIM_STATE.UNKNOWN;
    /* access modifiers changed from: private */
    public SoftphoneAccount mSoftphoneAccount;
    /* access modifiers changed from: private */
    public int mSubscriptionId = -1;
    private final ITelephonyManager mTelephonyManager;
    protected final RegistrantList mUiccChangedRegistrants = new RegistrantList();
    protected boolean notifySimReadyAlreadyDone = false;

    public enum ISIM_VALIDITY {
        IMPU_NOT_EXISTS(1),
        IMPU_INVALID(2),
        IMPI_NOT_EXIST(4),
        HOME_DOMAIN_NOT_EXIST(8);
        
        private int mCode;

        private ISIM_VALIDITY(int code) {
            this.mCode = 0;
            this.mCode = code;
        }

        public int getValue() {
            return this.mCode;
        }
    }

    public enum SIM_VALIDITY {
        GBA_NOT_SUPPORTED(1),
        MSISDN_INVALID(2);
        
        private int mCode;

        private SIM_VALIDITY(int code) {
            this.mCode = 0;
            this.mCode = code;
        }

        public int getValue() {
            return this.mCode;
        }
    }

    public SimManager(Looper looper, Context context, int simSlot, SubscriptionInfo subInfo, ITelephonyManager tm) {
        super(looper);
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 500);
        this.mContext = context;
        this.mSimSlot = simSlot;
        this.mTelephonyManager = tm;
        Log.i(LOG_TAG, "subId: " + this.mSubscriptionId + ", info: " + subInfo);
        if (subInfo != null) {
            this.mSubscriptionId = subInfo.getSubscriptionId();
            setSubscriptionInfo(subInfo);
        }
        String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        String omcNetworkCode = SimManagerUtils.getOmcNetworkCode(this.mSimSlot, this.OMC_CODE);
        this.OMCNW_CODE = omcNetworkCode;
        this.mNetMno = Mno.fromSalesCode(omcNetworkCode);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, "OMC_CODE(create): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i2 = this.mSimSlot;
        simpleEventLog2.logAndAdd(i2, "OMCNW_CODE(create): " + this.OMCNW_CODE + ", mNetMno: " + this.mNetMno.toString());
        setSimMno(this.mNetMno, false);
        this.mImsServiceSwitchObserver = new ImsServiceSwitchObserver(this);
        this.mContext.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_SWITCHES.getUri(), false, this.mImsServiceSwitchObserver);
    }

    class ImsServiceSwitchObserver extends ContentObserver {
        public ImsServiceSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.i(SimManager.LOG_TAG, "ImsServiceSwitch updated.");
            if (uri != null) {
                int phoneId = UriUtil.getSimSlotFromUri(uri);
                if (phoneId != SimManager.this.mSimSlot) {
                    Log.i(SimManager.LOG_TAG, "phoneId mismatch, No need to update");
                    return;
                }
                SimManager simManager = SimManager.this;
                simManager.sendMessage(simManager.obtainMessage(7, Integer.valueOf(phoneId)));
            }
        }
    }

    public void onImsSwitchUpdated(int phoneId) {
        SharedPreferences spSwitch = ImsSharedPrefHelper.getSharedPref(phoneId, this.mContext, "imsswitch", 0, false);
        for (String var : new String[]{ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE}) {
            this.mMnoInfo.put(var, Boolean.valueOf(spSwitch.getBoolean(var, false)));
        }
        this.mEventLog.logAndAdd(this.mSimSlot, this.mSimState + ", " + this.mSimMnoName + ", onImsSwitchUpdated : " + this.mMnoInfo);
    }

    private void updateGlobalSetting(int phoneId) {
        int switchType = CollectionUtils.getIntValue(this.mMnoInfo, ISimManager.KEY_IMSSWITCH_TYPE, 0);
        if (switchType == 4 || switchType == 5) {
            boolean isEnableIms = CollectionUtils.getBooleanValue(this.mMnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false);
            boolean isEnableVolte = CollectionUtils.getBooleanValue(this.mMnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false);
            boolean isEnableVowifi = CollectionUtils.getBooleanValue(this.mMnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false);
            if (!isEnableIms || (!isEnableVolte && !isEnableVowifi)) {
                IMSLog.i(LOG_TAG, phoneId, "updateGlobalSetting: enableIms or enableServiceVolte, enableServiceVowifi : disable");
                ContentValues disableVolte = new ContentValues();
                if (getDevMno().isAus()) {
                    disableVolte.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
                    disableVolte.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS");
                } else {
                    disableVolte.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
                    disableVolte.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "CS");
                }
                disableVolte.put(GlobalSettingsConstants.SS.DOMAIN, "CS_ALWAYS");
                disableVolte.put(GlobalSettingsConstants.Call.USSD_DOMAIN, "CS");
                Uri.Builder buildUpon = URI_UPDATE_GLOBAL.buildUpon();
                this.mContext.getContentResolver().update(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + phoneId).build(), disableVolte, (String) null, (String[]) null);
            }
        }
    }

    public void initSequentially() {
        IntentFilter simFilter = new IntentFilter();
        simFilter.addAction(ImsConstants.Intents.ACTION_SIM_ICCID_CHANGED);
        simFilter.addAction(ImsConstants.Intents.ACTION_SIM_REFRESH);
        simFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        simFilter.addAction(ImsConstants.Intents.ACTION_SIM_ISIM_LOADED);
        this.mContext.registerReceiver(this.mSimIntentReceiver, simFilter);
        IntentFilter packageIntentFilter = new IntentFilter();
        packageIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageIntentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mGtsAppInstallReceiver, packageIntentFilter);
        if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            IntentFilter ddsFilter = new IntentFilter();
            ddsFilter.addAction(ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
            this.mContext.registerReceiver(this.mDDSIntentReceiver, ddsFilter);
        }
        if (this.mContext.checkSelfPermission(sInteractAcrossUsersFullPermission) == 0) {
            IntentFilter mumFilter = new IntentFilter();
            mumFilter.addAction("android.intent.action.USER_BACKGROUND");
            mumFilter.addAction("android.intent.action.USER_FOREGROUND");
            this.mContext.registerReceiver(this.mMultiUserReceiver, mumFilter);
            IntentFilter akaFilter = new IntentFilter();
            akaFilter.addAction("com.sec.imsservice.AKA_CHALLENGE_COMPLETE");
            akaFilter.addAction("com.sec.imsservice.AKA_CHALLENGE_FAILED");
            ContextExt.registerReceiverAsUser(this.mContext.getApplicationContext(), this.mAkaEventReceiver, ContextExt.ALL, akaFilter, (String) null, (Handler) null);
        }
        Log.e(LOG_TAG, "init mno map");
        sendEmptyMessage(8);
    }

    static class AuthRequest {
        Message response;

        AuthRequest() {
        }
    }

    private class AkaEventReceiver extends BroadcastReceiver {
        private AkaEventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(SimManager.LOG_TAG, "Intent received : " + action);
            Log.i(SimManager.LOG_TAG, "id : " + intent.getIntExtra("id", -1));
            if ("com.sec.imsservice.AKA_CHALLENGE_COMPLETE".equals(action) && intent.getIntExtra("id", -1) == SimManager.this.mSoftphoneAccount.mId) {
                SimManager.this.onSoftphoneAuthDone(intent.getStringExtra("result"));
            } else if ("com.sec.imsservice.AKA_CHALLENGE_FAILED".equals(action)) {
                SimManager.this.onSoftphoneAuthDone("");
            }
        }
    }

    private static class SoftphoneAccount {
        int mId;
        String mImpi;
        String mNonce;
        Message mResponse;

        public SoftphoneAccount(String nonce, int id, String impi, Message response) {
            this.mNonce = nonce;
            this.mId = id;
            this.mImpi = impi;
            this.mResponse = response;
        }
    }

    public void registerForSimReady(Handler h, int what, Object obj) {
        IMSLog.i(LOG_TAG, this.mSimSlot, "Register for sim ready");
        Registrant register = new Registrant(h, what, obj);
        this.mSimReadyRegistrants.add(register);
        if (!this.notifySimReadyAlreadyDone) {
            return;
        }
        if (this.mSimState != SimConstants.SIM_STATE.UNKNOWN || SimManagerUtils.needImsUpOnUnknownState(this.mContext, this.mSimSlot)) {
            register.notifyResult(Integer.valueOf(this.mSimSlot));
        }
    }

    public void deregisterForSimReady(Handler h) {
        this.mSimReadyRegistrants.remove(h);
    }

    private void notifySimReady(String operator) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, "notifySimReady: state [" + this.mSimState + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(this.mSimSlot);
        sb.append(",NOTI SIM EVT");
        IMSLog.c(LogClass.SIM_NOTIFY_EVENT, sb.toString());
        boolean absent = true;
        this.notifySimReadyAlreadyDone = true;
        Intent intent = new Intent(ImsConstants.Intents.ACTION_IMS_ON_SIMLOADED);
        intent.addFlags(32);
        IMSLog.i(LOG_TAG, this.mSimSlot, "send ACTION_IMS_ON_SIMLOADED");
        IntentUtil.sendBroadcast(this.mContext, intent);
        if (this.mSimState == SimConstants.SIM_STATE.LOADED) {
            absent = false;
        }
        if (absent || this.mSimStatePrev != SimConstants.SIM_STATE.LOADED) {
            this.mSimReadyRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
        } else {
            SimDataAdaptor simDataAdaptor = this.mSimDataAdaptor;
            if (simDataAdaptor != null && simDataAdaptor.needHandleLoadedAgain(operator)) {
                int i2 = this.mSimSlot;
                IMSLog.i(LOG_TAG, i2, "SIM READY by needHandleLoadedAgain: " + operator);
                this.mSimReadyRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
            }
        }
        synchronized (this.mEventListeners) {
            for (ISimEventListener listener : this.mEventListeners) {
                listener.onReady(this.mSimSlot, absent);
            }
        }
    }

    public void registerForUiccChanged(Handler h, int what, Object obj) {
        this.mUiccChangedRegistrants.add(new Registrant(h, what, obj));
    }

    private void notifyUiccChanged() {
        this.mUiccChangedRegistrants.notifyRegistrants();
    }

    public void registerForSimRefresh(Handler h, int what, Object obj) {
        this.mSimRefreshRegistrants.add(new Registrant(h, what, obj));
    }

    public void deregisterForSimRefresh(Handler h) {
        this.mSimRefreshRegistrants.remove(h);
    }

    private void notifySimRefresh() {
        this.mSimRefreshRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public void registerForSimRemoved(Handler h, int what, Object obj) {
        this.mSimRemovedRegistrants.add(new Registrant(h, what, obj));
    }

    public void deregisterForSimRemoved(Handler h) {
        this.mSimRemovedRegistrants.remove(h);
    }

    private void notifySimRemoved() {
        this.mSimRemovedRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public void registerForSimStateChanged(Handler h, int what, Object obj) {
        this.mSimStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void deregisterForSimStateChanged(Handler h) {
        this.mSimStateChangedRegistrants.remove(h);
    }

    private void notifySimStateChanged() {
        this.mSimStateChangedRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public boolean isSimAvailable() {
        Log.i(LOG_TAG, "mSimState:" + this.mSimState + ", mIsimLoaded:" + this.mIsimLoaded + ", hasIsim():" + hasIsim());
        return this.mSimState == SimConstants.SIM_STATE.LOADED && (this.mIsimLoaded || !hasIsim());
    }

    public boolean hasNoSim() {
        return this.mSimState != SimConstants.SIM_STATE.LOADED;
    }

    public void setIsimLoaded() {
        onSimStateChange(IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
    }

    private boolean isISimAppLoaded() {
        if (getSimState() != 5 || getSubscriptionId() < 0 || !SimManagerUtils.isISimAppPresent(this.mSimSlot, this.mTelephonyManager)) {
            return false;
        }
        if (!ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM, true) || isISimDataValid()) {
            return true;
        }
        return false;
    }

    private boolean checkOutBoundSIM() {
        if (hasNoSim()) {
            Log.i(LOG_TAG, "isOutboundSim, SIM not ready");
            return false;
        } else if (DeviceUtil.isUnifiedSalesCodeInTSS()) {
            return !DeviceUtil.includedSimByTSS(this.mSimMnoName);
        } else {
            String operator = getSimOperator();
            if (!isLabSimCard() && !operator.equals("45001") && !DeviceUtil.getGcfMode()) {
                return CollectionUtils.isNullOrEmpty((Collection<?>) getNetworkNames());
            }
            Log.i(LOG_TAG, "isOutboundSim, GCF mode, LabSim card/ Test Bed SIM inserted.");
            return false;
        }
    }

    public List<String> getNetworkNames() {
        Mno mno = getSimMno();
        int subId = SimUtil.getSubId(this.mSimSlot);
        boolean onlyMccmnc = false;
        String gid1 = this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
        if ((TextUtils.isEmpty(gid1) || gid1.toUpperCase().startsWith("FF")) && getSimMno().isUSA()) {
            onlyMccmnc = true;
        }
        return CscParser.getNetworkNames(mno == Mno.RJIL ? getSimOperatorFromImpi() : getSimOperator(), mno == Mno.RJIL ? getImsiFromImpi() : getImsi(), this.mTelephonyManager.getGroupIdLevel1(subId), this.mTelephonyManager.getGid2(subId), this.mTelephonyManager.getSimOperatorName(subId), this.mSimSlot, onlyMccmnc);
    }

    public boolean hasIsim() {
        Mno mno = getSimMno();
        String simOp = getRilSimOperator();
        String hwVendor = SemSystemProperties.get("ro.boot.hardware", "");
        boolean hasIsim = false;
        if (mno == Mno.SKT && (("SKCTN".equals(simOp) || "SKCTD".equals(simOp)) && OmcCode.isKOROmcCode() && (hwVendor.contains("qcom") || hwVendor.contains("mt")))) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "hasIsim: watch data SIM. treat it as USIM(by SKT operator)");
            return false;
        } else if (mno == Mno.SAFARICOM_KENYA) {
            Log.i(LOG_TAG, "hasIsim safariCom_kenya : false");
            return false;
        } else {
            boolean hasIsim2 = SimManagerUtils.isISimAppPresent(this.mSimSlot, this.mTelephonyManager);
            int i = this.mSimSlot;
            IMSLog.i(LOG_TAG, i, "hasIsim: [" + hasIsim2 + "]");
            if (!ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.USE_USIM_ON_INVALID_ISIM, false)) {
                return hasIsim2;
            }
            if (hasIsim2 && (!this.mIsimLoaded || isISimDataValid())) {
                hasIsim = true;
            }
            return hasIsim;
        }
    }

    public String getRilSimOperator() {
        String simOp = this.mTelephonyManager.getTelephonyProperty(this.mSimSlot, "ril.simoperator", "ETC");
        Log.i(LOG_TAG, "getRilSimOperator: " + simOp);
        return simOp;
    }

    public boolean hasVsim() {
        return SimUtil.isSoftphoneEnabled();
    }

    public void setSimRefreshed() {
        Log.i(LOG_TAG, "setSimRefreshed:");
    }

    public String getSimOperator() {
        String operator = Mno.getMockOperatorCode();
        if (!TextUtils.isEmpty(operator)) {
            return operator;
        }
        if (SimUtil.isSoftphoneEnabled()) {
            return SOFTPHONE_OPERATOR_CODE;
        }
        String operator2 = this.mTelephonyManager.getSimOperator(getSubscriptionId());
        Log.i(LOG_TAG, "getSimOperator: value [" + operator2 + "]");
        return operator2;
    }

    public String getSimOperatorFromImpi() {
        if (TextUtils.isEmpty(this.mOperatorFromImpi)) {
            return getSimOperator();
        }
        return this.mOperatorFromImpi;
    }

    public boolean isLabSimCard() {
        Log.i(LOG_TAG, "isLabSimCard: state [" + this.mSimState + "] isLabSim [" + this.mLabSimCard + "]");
        return this.mSimState == SimConstants.SIM_STATE.LOADED && this.mLabSimCard;
    }

    public boolean isOutBoundSIM() {
        Log.i(LOG_TAG, "isOutBoundSIM: state [" + this.mSimState + "] isOutBoundSIM [" + this.mIsOutBoundSIM + "]");
        return this.mSimState == SimConstants.SIM_STATE.LOADED && this.mIsOutBoundSIM;
    }

    public boolean isGBASupported() {
        if ("45001".equals(getSimOperator())) {
            return true;
        }
        if (!hasIsim()) {
            return false;
        }
        boolean isGbaSupported = this.mTelephonyManager.isGbaSupported(getSubscriptionId());
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "isGbaSupported [" + isGbaSupported + "]");
        return isGbaSupported;
    }

    public boolean isISimDataValid() {
        return getISimDataValidity() == 0;
    }

    private int getISimDataValidity() {
        SimDataAdaptor simDataAdaptor;
        int isimValidity = 0;
        String impi = this.mTelephonyManager.getIsimImpi(getSubscriptionId());
        String homedomainName = this.mTelephonyManager.getIsimDomain(getSubscriptionId());
        String[] impuList = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        if (CollectionUtils.isNullOrEmpty((Object[]) impuList) || (simDataAdaptor = this.mSimDataAdaptor) == null) {
            isimValidity = 0 | ISIM_VALIDITY.IMPU_NOT_EXISTS.getValue();
            Log.e(LOG_TAG, "isIsimDataValid: " + ISIM_VALIDITY.IMPU_NOT_EXISTS);
        } else if (!isValidImpu(simDataAdaptor.getImpuFromList(Arrays.asList(impuList)))) {
            isimValidity = 0 | ISIM_VALIDITY.IMPU_INVALID.getValue();
            int i = this.mSimSlot;
            IMSLog.e(LOG_TAG, i, "isIsimDataValid: " + ISIM_VALIDITY.IMPU_INVALID);
        }
        if (TextUtils.isEmpty(impi)) {
            isimValidity |= ISIM_VALIDITY.IMPI_NOT_EXIST.getValue();
            Log.e(LOG_TAG, "isIsimDataValid: " + ISIM_VALIDITY.IMPI_NOT_EXIST);
        }
        if (!TextUtils.isEmpty(homedomainName)) {
            return isimValidity;
        }
        if (getSimMno() != Mno.TMOUS || this.mHighestPriorityEhplmn.isEmpty()) {
            int isimValidity2 = isimValidity | ISIM_VALIDITY.HOME_DOMAIN_NOT_EXIST.getValue();
            Log.e(LOG_TAG, "isIsimDataValid: " + ISIM_VALIDITY.HOME_DOMAIN_NOT_EXIST);
            return isimValidity2;
        }
        this.mEventLog.logAndAdd(this.mSimSlot, "Allow empty EF_HOMEDOMAIN only when the EHPLMN is available");
        return isimValidity;
    }

    public static boolean isValidImpu(String impu) {
        ImsUri uri = ImsUri.parse(impu);
        if (uri != null && uri.getUriType() == ImsUri.UriType.SIP_URI) {
            return true;
        }
        IMSLog.s(LOG_TAG, "invalid impu : " + impu);
        return false;
    }

    public String getIsimAuthentication(String nonce) {
        int appType = 0;
        if (isIsimLoaded()) {
            appType = 5;
        } else if (!isIsimLoaded() && isSimLoaded()) {
            appType = 2;
        }
        return getIsimAuthentication(nonce, appType);
    }

    public String getIsimAuthentication(String nonce, int appType) {
        if (appType == 0 || nonce == null || nonce.length() % 2 != 0) {
            Log.e(LOG_TAG, "Wrong parameter - AppType : " + appType + " nonce : " + nonce);
            return null;
        }
        Log.i(LOG_TAG, " getIsimAuthentication calling - AppType : " + appType);
        byte[] hexBytes = new byte[(nonce.length() / 2)];
        int j = 0;
        int i = 0;
        while (i < nonce.length()) {
            hexBytes[j] = (byte) (Integer.parseInt(nonce.substring(i, i + 2), 16) & 255);
            i += 2;
            j++;
        }
        IMSLog.c(LogClass.SIM_AKA_REQUEST, this.mSimSlot + ",REQ ISIM AUTH");
        String result = this.mTelephonyManager.getIccAuthentication(getSubscriptionId(), appType, 129, Base64.encodeToString(hexBytes, 2));
        Log.i(LOG_TAG, "result: " + result);
        if (getSimMno().isKor() && (TextUtils.equals(result, "2wQAAAAAAAA=") || TextUtils.isEmpty(result) || TextUtils.equals(result, "null"))) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i2 = this.mSimSlot;
            simpleEventLog.logAndAdd(i2, "getIsimAuthentication result:" + result);
            IMSLog.c(LogClass.SIM_AKA_RESPONSE, this.mSimSlot + ",getIsimAuthentication result:" + result);
            return "mGI=";
        } else if (TextUtils.isEmpty(result) || TextUtils.equals(result, "null")) {
            Log.e(LOG_TAG, "getIccAuthentication failed");
            return null;
        } else {
            IMSLog.c(LogClass.SIM_AKA_RESPONSE, this.mSimSlot + ",LEN:" + TextUtils.length(result));
            byte[] bArr = new byte[0];
            try {
                byte[] resultBytes = Base64.decode(result, 2);
                StringBuilder ret = new StringBuilder(resultBytes.length * 2);
                Log.i(LOG_TAG, "resultBytes.length: " + resultBytes.length);
                for (int i3 = 0; i3 < resultBytes.length; i3++) {
                    ret.append("0123456789abcdef".charAt((resultBytes[i3] >> 4) & 15));
                    ret.append("0123456789abcdef".charAt(resultBytes[i3] & 15));
                }
                String result2 = ret.toString();
                IMSLog.s(LOG_TAG, "decoded result : " + result2);
                return result2;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed to decode the AKA RESPONSE - retry as MAC ERROR");
                return "9862";
            }
        }
    }

    public void requestIsimAuthentication(String nonce, int appType, Message response) {
        String result = getIsimAuthentication(nonce, appType);
        if (result != null) {
            response.obj = new String(result.getBytes());
            response.sendToTarget();
        }
    }

    public void requestIsimAuthentication(String nonce, Message response) {
        String result = getIsimAuthentication(nonce);
        if (result != null) {
            response.obj = new String(result.getBytes());
            response.sendToTarget();
        }
    }

    public void requestSoftphoneAuthentication(String nonce, String impi, Message response, int id) {
        this.mSoftphoneAccount = new SoftphoneAccount(nonce, id, impi, response);
        Log.i(LOG_TAG, "requestSoftphoneAuthentication, id = " + id);
        IMSLog.c(LogClass.SIM_SOFTPHONE_AUTH_REQUEST, this.mSimSlot + ",REQ AUTH");
        Intent intent = new Intent("com.sec.imsservice.REQUEST_AKA_CHALLENGE");
        intent.putExtra(WwwAuthenticateHeader.HEADER_PARAM_NONCE, nonce);
        intent.putExtra("impi", impi);
        intent.putExtra("id", id);
        ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
    }

    /* access modifiers changed from: private */
    public void onSoftphoneAuthDone(String result) {
        IMSLog.s(LOG_TAG, "aka result : " + result);
        IMSLog.c(LogClass.SIM_SOFTPHONE_AUTH_RESPONSE, this.mSimSlot + ",LEN:" + TextUtils.length(result));
        if (TextUtils.isEmpty(result)) {
            Log.e(LOG_TAG, "aka failed");
            sendEmptyMessage(5);
        } else if (this.mSoftphoneAccount.mResponse != null) {
            this.mSoftphoneAccount.mResponse.obj = new String(result.getBytes());
            this.mSoftphoneAccount.mResponse.sendToTarget();
        }
    }

    private void onSoftphoneAuthFailed() {
        Log.i(LOG_TAG, "onSoftphoneAuthFailed");
        this.mSoftphoneAccount.mResponse.what = 46;
        this.mSoftphoneAccount.mResponse.sendToTarget();
    }

    private boolean updateSimState(SimConstants.SIM_STATE state) {
        SimConstants.SIM_STATE sim_state = this.mSimState;
        if (sim_state == state) {
            return false;
        }
        this.mSimStatePrev = sim_state;
        this.mSimState = state;
        if (state == SimConstants.SIM_STATE.LOADED) {
            return true;
        }
        this.mIsOutBoundSIM = false;
        return true;
    }

    private boolean isValidOperator(String operator) {
        return !TextUtils.isEmpty(operator) && operator.length() >= 5;
    }

    private boolean isValidImsi(String operator, String imsi) {
        return imsi != null && imsi.length() > operator.length();
    }

    private boolean useImsSwitch() {
        return getSimMno() != Mno.GCF && getSimMno() != Mno.SAMSUNG && !"GCF".equals(this.OMC_CODE) && !"SUP".equals(this.OMC_CODE) && !this.mLabSimCard;
    }

    /* access modifiers changed from: protected */
    public void onSimStateChange(String iccState) {
        String lastMno = "";
        String operator = getSimOperator();
        boolean changed = false;
        boolean isMultiSim = SimUtil.isMultiSimSupported();
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "onSimStateChange: [" + iccState + "], operator: [" + operator + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(this.mSimSlot);
        sb.append(",,EVT:");
        sb.append(iccState);
        IMSLog.c(LogClass.SIM_EVENT, sb.toString());
        if (hasVsim()) {
            handleVsim(operator, iccState);
            return;
        }
        if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
            lastMno = ImsRegistry.getString(this.mSimSlot, "mnoname", "");
            changed = handle_Loaded(operator);
        } else if (DELAYED_ISIM_LOAD.equals(iccState)) {
            changed = handle_Delayed_IsimLoaded();
        } else if (IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED.equals(iccState)) {
            changed = handle_IsimLoaded();
        } else if ("NOT_READY".equals(iccState) || "UNKNOWN".equals(iccState)) {
            handle_NotReadyUnknown(operator, iccState);
            return;
        } else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(iccState)) {
            handle_absent(operator, isMultiSim);
            return;
        } else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(iccState)) {
            handldle_Locked(operator);
            return;
        }
        if (changed) {
            handleSimStateChanged(lastMno, operator);
        }
    }

    private void handleVsim(String operator, String iccState) {
        if (this.mSimDataAdaptor == null) {
            this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
            Log.i(LOG_TAG, "Enable virtual SIM");
            updateSimState(SimConstants.SIM_STATE.LOADED);
            this.mIsimLoaded = true;
            this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
            this.mEventLog.add("VSIM LOADED");
            notifySimReady(operator);
        } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
            handleSubscriptionId();
        }
    }

    private boolean handleSubscriptionId() {
        SubscriptionInfo subInfo = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfoForSimSlotIndex(this.mSimSlot);
        if (subInfo == null) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange:[LOADED] subInfo is not created yet. retry in 1 sec.");
            IMSLog.c(LogClass.SIM_NO_SUBINFO, this.mSimSlot + ",NO SUBINFO");
            this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
            sendMessageDelayed(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED), 1000);
            return false;
        }
        SimManagerFactory.notifySubscriptionIdChanged(subInfo);
        setSubscriptionInfo(subInfo);
        return true;
    }

    private boolean handle_Loaded(String operator) {
        String gid2;
        String operator2 = operator;
        this.mEventLog.logAndAdd(this.mSimSlot, "LOADED : " + this.mSimState);
        removeMessages(1, IccCardConstants.INTENT_VALUE_ICC_LOADED);
        boolean changed = updateSimState(SimConstants.SIM_STATE.LOADED);
        if (!changed && !hasIsim() && !TextUtils.equals(this.mLastImsi, this.mTelephonyManager.getSubscriberId(getSubscriptionId()))) {
            changed = true;
        }
        SimDataAdaptor simDataAdaptor = this.mSimDataAdaptor;
        if (simDataAdaptor != null && simDataAdaptor.needHandleLoadedAgain(operator2)) {
            changed = true;
        }
        if (changed) {
            if (!isValidOperator(operator)) {
                IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange: [LOADED] but operator is invalid. retry in 1 sec.");
                IMSLog.c(LogClass.SIM_INVALID_OPERATOR, this.mSimSlot + ",INVLD OP:" + operator2);
                this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                sendMessageDelayed(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED), 1000);
                return false;
            } else if (!handleSubscriptionId()) {
                return false;
            } else {
                String imsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
                String impi = this.mTelephonyManager.getIsimImpi(getSubscriptionId());
                String gid1 = this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
                String spname = this.mTelephonyManager.getSimOperatorName(getSubscriptionId());
                String gid22 = this.mTelephonyManager.getGid2(getSubscriptionId());
                this.mEventLog.logAndAdd(this.mSimSlot, "imsi:" + IMSLog.checker(imsi) + " gid1:" + gid1 + " gid2:" + gid22 + " impi:" + IMSLog.checker(impi) + " spname:" + spname);
                if (!isValidImsi(operator2, imsi)) {
                    IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange: [LOADED] but imsi is invalid. retry in 1 sec.");
                    IMSLog.c(LogClass.SIM_INVALID_IMSI, this.mSimSlot + ",INVLD IMSI," + TextUtils.length(imsi));
                    this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                    sendMessageDelayed(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED), 1000);
                    return false;
                }
                String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
                this.OMC_CODE = str;
                if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str)) {
                    this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
                }
                this.mEventLog.logAndAdd(this.mSimSlot, "OMC_CODE(loaded): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
                String omcNetworkCode = SimManagerUtils.getOmcNetworkCode(this.mSimSlot, this.OMC_CODE);
                this.OMCNW_CODE = omcNetworkCode;
                this.mNetMno = Mno.fromSalesCode(omcNetworkCode);
                this.mEventLog.logAndAdd(this.mSimSlot, "OMCNW_CODE(loaded): " + this.OMCNW_CODE + ", mNetMno: " + this.mNetMno.toString());
                ImsCscFeature cscFeature = ImsCscFeature.getInstance();
                if (cscFeature == null) {
                    IMSLog.i(LOG_TAG, this.mSimSlot, "onSimStateChanged, cscFeature is absent.");
                    return false;
                }
                cscFeature.clear(this.mSimSlot);
                if (TextUtils.equals("CPW", this.OMCNW_CODE) && TextUtils.equals("00101", operator2)) {
                    IMSLog.i(LOG_TAG, this.mSimSlot, "CPW and 00101 sim card, Enable GCF mode");
                    DeviceUtil.setGcfMode(true);
                }
                this.mImsiFromImpi = "";
                boolean isGlobalGcEnabled = false;
                if (DeviceUtil.getGcfMode()) {
                    setSimMno(Mno.GCF, true);
                    String str2 = gid22;
                } else {
                    if (mMnoMap == null) {
                        this.mEventLog.logAndAdd(this.mSimSlot, "mnomap is empty");
                        mMnoMap = new MnoMap(this.mContext, this.mSimSlot);
                    }
                    if (impi == null || impi.startsWith(operator2)) {
                        gid2 = gid22;
                    } else {
                        String operatorFromImpi = SimManagerUtils.extractMnoFromImpi(impi);
                        String imsiFromImpi = imsi;
                        if (!impi.startsWith(imsi)) {
                            imsiFromImpi = SimManagerUtils.extractImsiFromImpi(impi, this.mTelephonyManager.getSubscriberId(getSubscriptionId()));
                        }
                        gid2 = gid22;
                        if (Mno.fromName(mMnoMap.getMnoName(operatorFromImpi, imsiFromImpi, gid1, gid2, spname)).isRjil()) {
                            operator2 = operatorFromImpi;
                            this.mOperatorFromImpi = operatorFromImpi;
                            imsi = imsiFromImpi;
                            this.mImsiFromImpi = imsiFromImpi;
                        }
                    }
                    String mnoname = mMnoMap.getMnoName(operator2, imsi, gid1, gid2, spname);
                    isGlobalGcEnabled = !mMnoMap.isGcBlockListContains(operator2) && !isMnoHasGcBlockExtension(mnoname);
                    IMSLog.i(LOG_TAG, this.mSimSlot, "isGlobalGcEnabled: " + isGlobalGcEnabled);
                    if (TextUtils.equals(mnoname, Mno.DEFAULT.getName())) {
                        isGlobalGcEnabled = false;
                    }
                    String mnoNameWithoutGcExtension = getMnoNameWithoutGcExtension(mnoname);
                    this.mSimMnoName = mnoNameWithoutGcExtension;
                    if ("LABSIM".equalsIgnoreCase(mnoNameWithoutGcExtension)) {
                        setSimMno(this.mNetMno, true);
                        this.mLabSimCard = true;
                        isGlobalGcEnabled = false;
                    } else {
                        setSimMno(Mno.fromName(this.mSimMnoName), false);
                        this.mLabSimCard = false;
                    }
                    if ("SUP".equalsIgnoreCase(this.OMC_CODE) && getSimMno() == Mno.DEFAULT) {
                        IMSLog.i(LOG_TAG, this.mSimSlot, "With SUP CSC, use GCF profile for GTS testing.");
                        setSimMno(Mno.GCF, true);
                    }
                    if (getSimMno() == Mno.DEFAULT && !"DEFAULT".equalsIgnoreCase(this.mSimMnoName)) {
                        this.mEventLog.logAndAdd(this.mSimSlot, "handle_Loaded: Mno.GENERIC Update Name, Country, Region");
                        Mno.updateGenerictMno(this.mSimMnoName);
                        setSimMno(Mno.GENERIC, false);
                    }
                }
                SemSystemProperties.set(SMF_MNONAME_PROP + this.mSimSlot, this.mSimMnoName + "|LOADED");
                this.mEventLog.logAndAdd(this.mSimSlot, "SIM PLMN: " + operator2 + ", mSimMno: " + getSimMno().toString() + "(" + this.mSimMnoName + ")");
                this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
                this.mMnoInfo.clear();
                this.mMnoInfo.put(ISimManager.KEY_HAS_SIM, true);
                this.mMnoInfo.put(ISimManager.KEY_GLOBALGC_ENABLED, Boolean.valueOf(isGlobalGcEnabled));
                this.mMnoInfo.put("mnoname", getSimMno().getName());
                this.mMnoInfo.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
                this.mMnoInfo.put("imsi", imsi);
                ImsLogAgentUtil.updateCommonHeader(this.mContext, this.mSimSlot, this.OMCNW_CODE, this.mSimMnoName, operator2);
                this.mIsOutBoundSIM = checkOutBoundSIM();
                List<ImsProfile> profiles = ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, this.mSimMnoName, this.mSimSlot);
                boolean isSimMobility = false;
                Iterator<ImsProfile> it = profiles.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().getSimMobility()) {
                            isSimMobility = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (getSimMno() == Mno.GENERIC) {
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 4);
                } else if (isSimMobility) {
                    IMSLog.i(LOG_TAG, this.mSimSlot, "isSimMobility true");
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 3);
                    this.mMnoInfo.putAll(ImsServiceSwitch.getSimMobilityImsSwitchSetting());
                    this.mMnoInfo.putAll(SimManagerUtils.getSimMobilityRcsSettings(this.mSimSlot, profiles));
                } else if ("XAS".equals(OmcCode.getNWCode(this.mSimSlot))) {
                    IMSLog.i(LOG_TAG, this.mSimSlot, "for XAS use internal IMSSetting");
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 4);
                    this.mMnoInfo.putAll(ImsServiceSwitch.getXasImsSwitchSetting());
                } else if (!useImsSwitch()) {
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
                } else if ("XAA".equals(OmcCode.getNWCode(this.mSimSlot)) && !DeviceUtil.isUSOpenDevice()) {
                    this.mEventLog.logAndAdd(this.mSimSlot, "Turned off all switches for OYN/XAA case");
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 5);
                } else if (this.mIsOutBoundSIM) {
                    this.mEventLog.logAndAdd(this.mSimSlot, "Turned off all switches for OutBoundSIM && not SimMo");
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 5);
                } else {
                    this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 4);
                    onImsSwitchUpdated(this.mSimSlot);
                }
                updateMno();
            }
        }
        return changed;
    }

    private boolean handle_Delayed_IsimLoaded() {
        if (this.mIsimLoaded || this.mSimState != SimConstants.SIM_STATE.LOADED) {
            return false;
        }
        this.mEventLog.logAndAdd(this.mSimSlot, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
        this.mIsimLoaded = true;
        return true;
    }

    private boolean handle_IsimLoaded() {
        this.mEventLog.logAndAdd(this.mSimSlot, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
        boolean changed = false;
        if (!this.mIsimLoaded) {
            changed = true;
        }
        if (this.mSimState == SimConstants.SIM_STATE.INVALID_ISIM && this.mSimStatePrev == SimConstants.SIM_STATE.LOADED) {
            changed = true;
            updateSimState(SimConstants.SIM_STATE.LOADED);
        }
        if (this.mSimState == SimConstants.SIM_STATE.LOADED && getSimMno() == Mno.BELL) {
            Log.i(LOG_TAG, "fix for exceptional case : LOADED notified before ISIM_LOADED");
            changed = true;
        }
        this.mIsimLoaded = true;
        return changed;
    }

    private void handle_NotReadyUnknown(String operator, String iccState) {
        if (this.mSimState == SimConstants.SIM_STATE.LOADED) {
            removeMessages(1, IccCardConstants.INTENT_VALUE_ICC_LOADED);
            onSimNotReady();
        } else if ("UNKNOWN".equals(iccState) && SimManagerUtils.needImsUpOnUnknownState(this.mContext, this.mSimSlot)) {
            this.OMC_CODE = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
            Mno salesCodeMno = Mno.DEFAULT;
            if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(this.OMC_CODE)) {
                salesCodeMno = Mno.fromSalesCode(this.OMC_CODE);
            }
            this.mDevMno = salesCodeMno;
            this.mEventLog.logAndAdd(this.mSimSlot, "SIM UNKNOWN");
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mSimSlot;
            simpleEventLog.logAndAdd(i, "OMC_CODE(unknown): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
            setSimMno(this.mDevMno, true);
            StringBuilder sb = new StringBuilder();
            sb.append(SMF_MNONAME_PROP);
            sb.append(this.mSimSlot);
            SemSystemProperties.set(sb.toString(), this.mSimMnoName);
            this.mMnoInfo.clear();
            this.mMnoInfo.put(ISimManager.KEY_HAS_SIM, false);
            this.mMnoInfo.put("mnoname", this.mDevMno.getName());
            this.mMnoInfo.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
            this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
            updateMno();
            notifySimReady(operator);
        }
    }

    private void handle_absent(String operator, boolean isMultiSim) {
        boolean changed = updateSimState(SimConstants.SIM_STATE.ABSENT);
        ImsLogAgentUtil.requestToSendStoredLog(this.mSimSlot, this.mContext, "DRPT");
        if (this.mSimStatePrev == SimConstants.SIM_STATE.LOADED || this.mSimStatePrev == SimConstants.SIM_STATE.LOCKED) {
            removeMessages(1, IccCardConstants.INTENT_VALUE_ICC_LOADED);
            this.mEventLog.logAndAdd(this.mSimSlot, "SIM REMOVED");
            onSimRemoved();
            String lastMnoName = this.mMnoInfo.getAsString("mnoname");
            this.mMnoInfo.clear();
            this.mMnoInfo.put(ISimManager.KEY_HAS_SIM, false);
            this.mMnoInfo.put("mnoname", lastMnoName);
            ContentValues contentValues = this.mMnoInfo;
            contentValues.put(ISimManager.KEY_MVNO_NAME, contentValues.getAsString(ISimManager.KEY_MVNO_NAME));
            this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
            updateMno();
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(this.mSimSlot + "SIM ABSENT");
        this.mIsimLoaded = false;
        this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
        if (changed) {
            notifySimReady(operator);
        }
        String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog2.logAndAdd(i, "OMC_CODE(absent): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        String omcNetworkCode = SimManagerUtils.getOmcNetworkCode(this.mSimSlot, this.OMC_CODE);
        this.OMCNW_CODE = omcNetworkCode;
        this.mNetMno = Mno.fromSalesCode(omcNetworkCode);
        SimpleEventLog simpleEventLog3 = this.mEventLog;
        int i2 = this.mSimSlot;
        simpleEventLog3.logAndAdd(i2, " OMCNW_CODE(absent): " + this.OMCNW_CODE + ", mNetMno: " + this.mNetMno.toString());
        setSimMno(this.mNetMno, true);
        StringBuilder sb = new StringBuilder();
        sb.append(SMF_MNONAME_PROP);
        sb.append(this.mSimSlot);
        String sb2 = sb.toString();
        SemSystemProperties.set(sb2, this.mSimMnoName + "|ABSENT");
        this.mMnoInfo.clear();
        this.mMnoInfo.put(ISimManager.KEY_HAS_SIM, false);
        this.mMnoInfo.put("mnoname", this.mNetMno.getName());
        this.mMnoInfo.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
        this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
        if (getSimMno() == Mno.RJIL) {
            int defaultPhoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SubscriptionManager.from(this.mContext));
            if (!isMultiSim || defaultPhoneId == this.mSimSlot) {
                updateMno();
            }
        } else if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || this.mTelephonyManager.getSimState() == 1) {
            updateMno();
        }
    }

    private void handldle_Locked(String operator) {
        boolean changed = updateSimState(SimConstants.SIM_STATE.LOCKED);
        String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        this.mEventLog.logAndAdd(this.mSimSlot, "SIM LOCKED");
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, "OMC_CODE(locked): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        setSimMno(this.mDevMno, true);
        StringBuilder sb = new StringBuilder();
        sb.append(SMF_MNONAME_PROP);
        sb.append(this.mSimSlot);
        SemSystemProperties.set(sb.toString(), this.mSimMnoName);
        this.mMnoInfo.clear();
        this.mMnoInfo.put(ISimManager.KEY_HAS_SIM, false);
        this.mMnoInfo.put("mnoname", this.mDevMno.getName());
        this.mMnoInfo.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
        this.mMnoInfo.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
        updateMno();
        if (!this.mSimStatePrev.isOneOf(SimConstants.SIM_STATE.LOADED, SimConstants.SIM_STATE.ABSENT) && changed) {
            notifySimReady(operator);
        }
    }

    private void handleSimStateChanged(String lastMno, String operator) {
        int isimValidity;
        if (isSimAvailable()) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "handleSimChange: SIM is ready.");
            if (getSimMno().isRjil()) {
                this.mLastImsi = getImsiFromImpi();
            } else {
                this.mLastImsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
            }
            ContentValues cvSIMI = new ContentValues();
            String gid1 = this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
            cvSIMI.put(DiagnosisConstants.SIMI_KEY_EVENT_TYPE, Integer.valueOf(DiagnosisConstants.getEventType(this.mSimStatePrev, this.mIsRefresh, TextUtils.equals(this.mSimMnoName, lastMno))));
            cvSIMI.put(DiagnosisConstants.SIMI_KEY_SUBSCRIPTION_ID, Integer.valueOf(Math.max(getSubscriptionId(), 0)));
            if (!TextUtils.isEmpty(gid1)) {
                cvSIMI.put(DiagnosisConstants.SIMI_KEY_GID1, gid1.substring(0, Math.min(16, gid1.length())));
            }
            cvSIMI.put(DiagnosisConstants.SIMI_KEY_ISIM_EXISTS, Integer.valueOf(this.mIsimLoaded ? 1 : 0));
            cvSIMI.put(DiagnosisConstants.COMMON_KEY_VOLTE_SETTINGS, Integer.valueOf(DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), this.mSimSlot)));
            cvSIMI.put(DiagnosisConstants.COMMON_KEY_VIDEO_SETTINGS, Integer.valueOf(DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), this.mSimSlot)));
            boolean validForNotify = true;
            int simValidity = 0;
            if (getSimMno() == Mno.TMOUS && !isGBASupported()) {
                simValidity = 0 | SIM_VALIDITY.GBA_NOT_SUPPORTED.getValue();
            }
            SimDataAdaptor simDataAdaptor = this.mSimDataAdaptor;
            if (simDataAdaptor != null && !simDataAdaptor.hasValidMsisdn()) {
                simValidity |= SIM_VALIDITY.MSISDN_INVALID.getValue();
                validForNotify = false;
            }
            if (simValidity > 0) {
                cvSIMI.put(DiagnosisConstants.SIMI_KEY_SIM_VALIDITY, DiagnosisConstants.intToHexStr(simValidity));
            }
            if (this.mIsimLoaded && (isimValidity = getISimDataValidity()) > 0) {
                cvSIMI.put(DiagnosisConstants.SIMI_KEY_ISIM_VALIDITY, DiagnosisConstants.intToHexStr(isimValidity));
                if (ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM, true)) {
                    IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange: invalid ISIM!");
                    updateSimState(SimConstants.SIM_STATE.INVALID_ISIM);
                    this.mEventLog.logAndAdd(this.mSimSlot, "INVALID_FIELD");
                    IMSLog.c(LogClass.SIM_INVALID_ISIM, this.mSimSlot + ",INVLD ISIM," + isimValidity);
                }
            }
            ImsLogAgentUtil.sendLogToAgent(this.mSimSlot, this.mContext, DiagnosisConstants.FEATURE_SIMI, cvSIMI);
            this.mIsRefresh = false;
            if (validForNotify) {
                notifySimReady(operator);
            }
        } else if (this.mSimState == SimConstants.SIM_STATE.LOADED && isISimAppLoaded()) {
            if (this.mIsCrashSimEvent) {
                this.mIsCrashSimEvent = false;
                sendMessage(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED));
                return;
            }
            sendMessageDelayed(obtainMessage(1, DELAYED_ISIM_LOAD), 10000);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0040, code lost:
        if (r1.equals("UNKNOWN") != false) goto L_0x0056;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initializeSimState() {
        /*
            r7 = this;
            r0 = 0
            r7.mIsCrashSimEvent = r0
            int r1 = r7.mSimSlot
            com.sec.internal.helper.os.ITelephonyManager r2 = r7.mTelephonyManager
            java.lang.String r1 = com.sec.internal.ims.core.sim.SimManagerUtils.readSimStateProperty(r1, r2)
            int r2 = r7.mSimSlot
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "initializeSimState (gsm.sim.state) : =  "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "SimManager"
            com.sec.internal.log.IMSLog.i(r4, r2, r3)
            int r2 = r1.hashCode()
            java.lang.String r3 = "LOADED"
            r4 = 3
            r5 = 2
            r6 = 1
            switch(r2) {
                case -2044189691: goto L_0x004d;
                case -2044123382: goto L_0x0043;
                case 433141802: goto L_0x003a;
                case 1924388665: goto L_0x0030;
                default: goto L_0x002f;
            }
        L_0x002f:
            goto L_0x0055
        L_0x0030:
            java.lang.String r0 = "ABSENT"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x002f
            r0 = r6
            goto L_0x0056
        L_0x003a:
            java.lang.String r2 = "UNKNOWN"
            boolean r2 = r1.equals(r2)
            if (r2 == 0) goto L_0x002f
            goto L_0x0056
        L_0x0043:
            java.lang.String r0 = "LOCKED"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x002f
            r0 = r5
            goto L_0x0056
        L_0x004d:
            boolean r0 = r1.equals(r3)
            if (r0 == 0) goto L_0x002f
            r0 = r4
            goto L_0x0056
        L_0x0055:
            r0 = -1
        L_0x0056:
            if (r0 == 0) goto L_0x0069
            if (r0 == r6) goto L_0x0069
            if (r0 == r5) goto L_0x0069
            if (r0 == r4) goto L_0x005f
            goto L_0x0071
        L_0x005f:
            r7.mIsCrashSimEvent = r6
            android.os.Message r0 = r7.obtainMessage(r6, r3)
            r7.sendMessage(r0)
            goto L_0x0071
        L_0x0069:
            android.os.Message r0 = r7.obtainMessage(r6, r1)
            r7.sendMessage(r0)
        L_0x0071:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.SimManager.initializeSimState():void");
    }

    private void setSimMno(Mno mno, boolean updateMnoName) {
        SimUtil.setSimMno(this.mSimSlot, mno);
        if (updateMnoName) {
            this.mSimMnoName = mno.getName();
        }
    }

    private void updateMno() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "updateMno:");
        this.mMnoInfo.put("phoneId", Integer.valueOf(this.mSimSlot));
        Integer imsSwitchType = this.mMnoInfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
        if (imsSwitchType == null) {
            imsSwitchType = 0;
        }
        if (TextUtils.isEmpty(this.mMnoInfo.getAsString("imsi"))) {
            this.mMnoInfo.put("imsi", "");
        }
        IMSLog.c(LogClass.SIM_UPDATE_MNO, this.mSimSlot + "," + this.mSimState + "," + this.mSimMnoName + "," + imsSwitchType);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, this.mSimState + ", " + this.mSimMnoName + ", " + this.mMnoInfo);
        if (imsSwitchType.intValue() != 0) {
            IMSLog.c(LogClass.SIM_MNO_INFO, this.mSimSlot + "," + SimManagerUtils.convertMnoInfoToString(this.mMnoInfo));
        }
        Uri.Builder buildUpon = URI_UPDATE_MNO.buildUpon();
        Uri uri = buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mSimSlot).build();
        int i2 = this.mSimSlot;
        IMSLog.i(LOG_TAG, i2, "updateMno [" + uri + "]");
        this.mContext.getContentResolver().update(uri, this.mMnoInfo, (String) null, (String[]) null);
    }

    private void onSimRefresh() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimRefresh");
        IMSLog.c(LogClass.SIM_REFRESH, this.mSimSlot + ",SIM REFRESH");
        this.mEventLog.logAndAdd(this.mSimSlot, "onSimRefresh");
        updateSimState(SimConstants.SIM_STATE.UNKNOWN);
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        if (!this.mIsRefresh) {
            this.mIsRefresh = true;
            notifySimRefresh();
        }
        this.mTelephonyManager.clearCache();
    }

    /* access modifiers changed from: protected */
    public void onSimRemoved() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimRemoved:");
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        notifySimRemoved();
        this.mTelephonyManager.clearCache();
    }

    /* access modifiers changed from: protected */
    public void onSimNotReady() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimNotReady");
        this.mEventLog.logAndAdd(this.mSimSlot, "onSimNotReady");
        updateSimState(SimConstants.SIM_STATE.UNKNOWN);
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        notifySimRemoved();
        this.mTelephonyManager.clearCache();
    }

    public void handleMessage(Message msg) {
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "handleMessage: what " + msg.what);
        switch (msg.what) {
            case 1:
                onSimStateChange((String) msg.obj);
                notifySimStateChanged();
                return;
            case 2:
                notifyUiccChanged();
                return;
            case 3:
                onSimRefresh();
                return;
            case 5:
                onSoftphoneAuthFailed();
                return;
            case 6:
                onDDSChanged(msg.arg1);
                return;
            case 7:
                onImsSwitchUpdated(((Integer) msg.obj).intValue());
                updateGlobalSetting(((Integer) msg.obj).intValue());
                return;
            case 8:
                if (mMnoMap == null) {
                    mMnoMap = new MnoMap(this.mContext, this.mSimSlot);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void notifyDDSChanged(int defaultPhoneId) {
        sendMessage(obtainMessage(6, defaultPhoneId, 0));
    }

    private void onDDSChanged(int defaultPhoneId) {
        if (!hasVsim() && defaultPhoneId == this.mSimSlot && this.mMnoInfo.size() > 0) {
            if (this.mSimState == SimConstants.SIM_STATE.LOADED || (this.mSimState == SimConstants.SIM_STATE.INVALID_ISIM && this.mSimStatePrev == SimConstants.SIM_STATE.LOADED)) {
                updateMno();
            }
        }
    }

    public int getSimSlotCount() {
        return this.mTelephonyManager.getPhoneCount();
    }

    public int getSubscriptionId() {
        if (this.mSubscriptionId < 0) {
            this.mSubscriptionId = SimUtil.getSubId(this.mSimSlot);
        }
        return this.mSubscriptionId;
    }

    public int getSimSlotIndex() {
        return this.mSimSlot;
    }

    public String getHighestPriorityEhplmn() {
        return this.mHighestPriorityEhplmn;
    }

    public synchronized void setSubscriptionInfo(SubscriptionInfo subInfo) {
        if (!hasVsim()) {
            int i = this.mSimSlot;
            IMSLog.i(LOG_TAG, i, "setSubscriptionInfo : mSubscriptionId : " + this.mSubscriptionId + " => " + subInfo.getSubscriptionId() + " mSimSlot : " + this.mSimSlot + " => " + subInfo.getSimSlotIndex());
            this.mSubscriptionId = subInfo.getSubscriptionId();
            this.mSimSlot = subInfo.getSimSlotIndex();
            this.mHighestPriorityEhplmn = SimManagerUtils.getEhplmn(subInfo);
            int i2 = this.mSimSlot;
            StringBuilder sb = new StringBuilder();
            sb.append("Stored EHPLMN [");
            sb.append(this.mHighestPriorityEhplmn);
            sb.append("]");
            IMSLog.i(LOG_TAG, i2, sb.toString());
        }
    }

    public boolean isSimLoaded() {
        return this.mSimState == SimConstants.SIM_STATE.LOADED;
    }

    public boolean isIsimLoaded() {
        Mno mno = getSimMno();
        String simOp = getRilSimOperator();
        String hwVendor = SemSystemProperties.get("ro.boot.hardware", "");
        if (mno == Mno.SKT && (("SKCTN".equals(simOp) || "SKCTD".equals(simOp)) && OmcCode.isKOROmcCode() && (hwVendor.contains("qcom") || hwVendor.contains("mt")))) {
            this.mEventLog.logAndAdd("isIsimLoaded: watch data SIM. treat it as USIM(by SKT operator)");
            return false;
        } else if (mno == Mno.SAFARICOM_KENYA) {
            return false;
        } else {
            if (!mno.isOneOf(Mno.CTC, Mno.CTCMO, Mno.H3G_SE, Mno.H3G_DK, Mno.AIRTEL, Mno.ROBI) && !mno.isHkMo() && !mno.isTw() && !mno.isLatin()) {
                return this.mIsimLoaded;
            }
            if (!this.mIsimLoaded || !isISimDataValid()) {
                return false;
            }
            return true;
        }
    }

    public String getMsisdn() {
        return this.mTelephonyManager.getMsisdn(getSubscriptionId());
    }

    public String getLine1Number() {
        return this.mTelephonyManager.getLine1Number();
    }

    public String getImsi() {
        String tmpImsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (!TextUtils.isEmpty(tmpImsi)) {
            this.mImsi = tmpImsi;
        }
        return this.mImsi;
    }

    public String getImsiFromImpi() {
        if (TextUtils.isEmpty(this.mImsiFromImpi)) {
            return getImsi();
        }
        return this.mImsiFromImpi;
    }

    public void registerSimCardEventListener(ISimEventListener listener) {
        synchronized (this.mEventListeners) {
            this.mEventListeners.add(listener);
        }
        if (this.notifySimReadyAlreadyDone && this.mSimState != SimConstants.SIM_STATE.UNKNOWN) {
            listener.onReady(this.mSimSlot, this.mSimState != SimConstants.SIM_STATE.LOADED);
        }
    }

    public void deRegisterSimCardEventListener(ISimEventListener listener) {
        synchronized (this.mEventListeners) {
            this.mEventListeners.remove(listener);
        }
    }

    public String getImpi() {
        return this.mTelephonyManager.getIsimImpi(getSubscriptionId());
    }

    public String getSimSerialNumber() {
        return this.mTelephonyManager.getSimSerialNumber();
    }

    public int getSimState() {
        if (this.mTelephonyManager == null) {
            return 0;
        }
        if (getSimSlotCount() == 1) {
            return this.mTelephonyManager.getSimState();
        }
        return this.mTelephonyManager.getSimState(getSimSlotIndex());
    }

    public String getDerivedImpuFromMsisdn() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpuFromMsisdn:");
        Mno mno = getSimMno();
        String msisdn = getMsisdn();
        if (TextUtils.isEmpty(msisdn)) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpuFromMsisdn: msisdn is not found");
            return null;
        }
        int[] mccmnc = SimManagerUtils.parseMccMnc(this.mSimSlot, getSimOperator());
        if (mccmnc == null) {
            int i = this.mSimSlot;
            IMSLog.e(LOG_TAG, i, "getDerivedImpi: operator is invalid. operator=" + getSimOperator());
            return "111@example.com";
        } else if (mno == Mno.BELL) {
            return String.format(Locale.US, "sip:%s@ims.bell.ca", new Object[]{msisdn});
        } else if (mno == Mno.LGU) {
            if (msisdn.startsWith("+82")) {
                msisdn = msisdn.replace("+82", "0");
            }
            return String.format(Locale.US, "sip:%s@lte-lguplus.co.kr", new Object[]{msisdn});
        } else {
            return String.format(Locale.US, "sip:%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{msisdn, Integer.valueOf(mccmnc[1]), Integer.valueOf(mccmnc[0])});
        }
    }

    public String getDerivedImpi() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpi:");
        Mno mno = getSimMno();
        String imsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (imsi == null || imsi.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpi: IMSI is not found. Using [sip:111@example.com]");
            return "111@example.com";
        }
        String operator = getSimOperator();
        if (!SimManagerUtils.isValidSimOperator(this.mSimSlot, operator)) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpi: operator is invalid");
            return null;
        }
        try {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (mno == Mno.LGU) {
                return String.format(Locale.US, "%s@lte-lguplus.co.kr", new Object[]{imsi});
            } else if (mno == Mno.TWM) {
                return String.format(Locale.US, "%s@ims.taiwanmobile.com", new Object[]{imsi});
            } else if (mno == Mno.CTC) {
                return String.format(Locale.US, "%s@ims.mnc011.mcc460.3gppnetwork.org", new Object[]{imsi});
            } else if (mno == Mno.CTCMO) {
                return String.format(Locale.US, "%s@ims.mnc007.mcc455.3gppnetwork.org", new Object[]{imsi});
            } else {
                return String.format(Locale.US, "%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{imsi, Integer.valueOf(mnc), Integer.valueOf(mcc)});
            }
        } catch (NumberFormatException e) {
            int i = this.mSimSlot;
            IMSLog.e(LOG_TAG, i, "getDerivedImpi: operator is invalid. operator=" + operator);
            return "111@example.com";
        }
    }

    public String getDerivedImpu() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpu:");
        Mno mno = getSimMno();
        String imsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (imsi == null || imsi.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpu: IMSI is not found.");
            return null;
        }
        int[] mccmnc = SimManagerUtils.parseMccMnc(this.mSimSlot, getSimOperator());
        if (mccmnc == null) {
            return null;
        }
        if (mno == Mno.TWM) {
            return String.format(Locale.US, "sip:%s@ims.taiwanmobile.com", new Object[]{imsi});
        } else if (mno == Mno.CTC) {
            return String.format(Locale.US, "sip:%s@ims.mnc011.mcc460.3gppnetwork.org", new Object[]{imsi});
        } else if (mno == Mno.CTCMO) {
            return String.format(Locale.US, "sip:%s@ims.mnc007.mcc455.3gppnetwork.org", new Object[]{imsi});
        } else {
            return String.format(Locale.US, "sip:%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{imsi, Integer.valueOf(mccmnc[1]), Integer.valueOf(mccmnc[0])});
        }
    }

    public List<String> getEfImpuList() {
        List<String> impus = new ArrayList<>();
        String[] efImpus = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        if (efImpus == null) {
            return impus;
        }
        for (String impu : efImpus) {
            if (!(impu == null || impu.length() == 0)) {
                impus.add(impu);
            }
        }
        return impus;
    }

    public String getImpuFromSim() {
        Mno mno = getSimMno();
        if (!hasIsim()) {
            return mno == Mno.LGU ? getDerivedImpuFromMsisdn() : getDerivedImpu();
        }
        String impu = this.mSimDataAdaptor.getImpuFromList(getEfImpuList());
        if (impu != null) {
            return impu;
        }
        return mno == Mno.LGU ? getDerivedImpuFromMsisdn() : getDerivedImpu();
    }

    public String getImpuFromIsim(int idx) {
        String[] impus = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        if (impus == null || impus.length < idx - 1) {
            return null;
        }
        return impus[idx];
    }

    public String getEmergencyImpu() {
        if (this.mSimDataAdaptor == null) {
            this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
        }
        String impu = this.mSimDataAdaptor.getEmergencyImpu(getEfImpuList());
        Mno mno = getSimMno();
        if (impu != null) {
            return impu;
        }
        if (hasNoSim()) {
            return "sip:anonymous@anonymous.invalid";
        }
        if (mno == Mno.BELL) {
            String impu2 = getDerivedImpuFromMsisdn();
            if (impu2 == null) {
                return "sip:anonymous@anonymous.invalid";
            }
            return impu2;
        } else if (mno == Mno.USCC) {
            return "sip:anonymous@anonymous.invalid";
        } else {
            return getDerivedImpu();
        }
    }

    public Mno getDevMno() {
        return this.mDevMno;
    }

    public Mno getNetMno() {
        return this.mNetMno;
    }

    public Mno getSimMno() {
        return SimUtil.getSimMno(this.mSimSlot);
    }

    public String getSimMnoName() {
        return this.mSimMnoName;
    }

    public void dump() {
        int i = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        int i2 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i2, "subID: " + this.mSubscriptionId);
        int i3 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i3, "mSimStatePrev: " + this.mSimStatePrev);
        int i4 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i4, "mSimState: " + this.mSimState);
        int i5 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i5, "mIsimLoaded: " + this.mIsimLoaded);
        int i6 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i6, "mIsOutBound: " + this.mIsOutBoundSIM);
        if (this.mSimDataAdaptor != null) {
            int i7 = this.mSimSlot;
            IMSLog.dump(LOG_TAG, i7, "mSimDataAdaptor : " + this.mSimDataAdaptor.getClass().getSimpleName());
        }
        if (this.mTelephonyManager != null) {
            if (!IMSLog.isShipBuild()) {
                int i8 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i8, "impi: " + this.mTelephonyManager.getIsimImpi(this.mSubscriptionId));
                int i9 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i9, "msisdn: " + this.mTelephonyManager.getMsisdn());
                int i10 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i10, "homedomainName: " + this.mTelephonyManager.getIsimDomain(this.mSubscriptionId));
                int i11 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i11, "impuFromSim[]: " + Arrays.toString(this.mTelephonyManager.getIsimImpu(this.mSubscriptionId)));
            }
            int i12 = this.mSimSlot;
            IMSLog.dump(LOG_TAG, i12, "operator: " + this.mTelephonyManager.getSimOperator(this.mSubscriptionId));
        }
        IMSLog.decreaseIndent(LOG_TAG);
        this.mEventLog.dump();
    }

    public SimpleEventLog getSimpleEventLog() {
        return this.mEventLog;
    }

    public ContentValues getMnoInfo() {
        return this.mMnoInfo;
    }

    /* access modifiers changed from: private */
    public void setGtsAppInstalled(boolean installed) {
        this.mIsGtsAppInstalled = installed;
    }

    /* access modifiers changed from: private */
    public boolean getGtsAppInstalled() {
        return this.mIsGtsAppInstalled;
    }

    private String getMnoNameWithoutGcExtension(String name) {
        String mnoname = name;
        int delimiterPos = mnoname.indexOf(Mno.GC_DELIMITER);
        if (delimiterPos != -1) {
            return mnoname.substring(0, delimiterPos);
        }
        return mnoname;
    }

    private boolean isMnoHasGcBlockExtension(String name) {
        return name.toUpperCase().endsWith(Mno.GC_BLOCK_EXTENSION);
    }
}
