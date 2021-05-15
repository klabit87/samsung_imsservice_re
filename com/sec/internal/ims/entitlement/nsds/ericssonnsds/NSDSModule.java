package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.FcmNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.nsds.NSDSModuleBase;
import com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds.NsdsFcmListenerService;
import com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds.RegistrationIntentService;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.AkaTokenRetrievalFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.PushTokenUpdateFlow;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.DeviceNameHelper;
import com.sec.internal.ims.entitlement.util.E911AidValidator;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.entitlement.util.SimSwapNSDSConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.interfaces.ims.entitlement.nsds.IAkaTokenRetrievalFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NSDSModule extends NSDSModuleBase {
    protected static final String ACTION_CHECK_REG_STATE = "com.sec.vsim.ericssonnsds.CHECK_REG_STATE";
    protected static final String ACTION_E911_AID_EXP_CHECK_TIMEOUT = "com.sec.vsim.ericssonnsds.E911_AID_EXP_CHECK_TIMEOUT";
    protected static final String ACTION_REFRESH_DEVICE_INFO = "com.sec.vsim.ericssonnsds.ACTION_REFRESH_DEVICE_INFO";
    protected static final String ACTION_REFRESH_ENTITLEMENT_CHECK = "com.sec.vsim.ericssonnsds.ACTION_REFRESH_ENTITLEMENT_CHECK";
    protected static final String ACTION_REFRESH_TOKEN = "com.sec.vsim.ericssonnsds.REFRESH_TOKEN";
    private static final String ACTION_RETRY_ENTITLEMENT_CHECK = "com.sec.vsim.ericssonnsds.ACTION_RETRY_ENTITLEMENT_CHECK";
    protected static final String ACTION_SIM_DEVICE_ACTIVATION = "com.sec.vsim.ericssonnsds.ACTION_SIM_DEVICE_ACTIVATION";
    protected static final String ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT = "com.sec.vsim.ericssonnsds.ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT";
    protected static final String ACTION_SVC_PROVISION_CHECK_TIMEOUT = "com.sec.vsim.ericssonnsds.SVC_PROVISION_CHECK_TIMEOUT";
    private static final long INVALID_FINGERPRINT_EXPIRATION_TIME = 3600000;
    private static final int MAX_LENGTH_MSISDN = 11;
    private static final int PDN_RETRY_INTERVAL = 60000;
    private static final int PDN_RETRY_MAX_COUNT = 7;
    private static final long PDN_TIMEOUT = 10000;
    private static final long REFRESH_TOKEN_WAIT_TIME = 120000;
    private static final long RETRY_INTERVAL = 30000;
    private static final long RETRY_INTERVAL_AUTO_ON = 280000;
    private static final long RETRY_INTERVAL_HTTP_ERROR = 1800000;
    private static final long RETRY_INTERVAL_UNKNOWN_ERROR = 5000;
    private static String mLastImsi = "";
    /* access modifiers changed from: private */
    public static Looper sServiceLooper;
    /* access modifiers changed from: private */
    public static final UriMatcher sUriMatcher;
    /* access modifiers changed from: private */
    public String LOG_TAG = NSDSModule.class.getSimpleName();
    protected AirplaneModeObserver mAirplaneModeObserver;
    private IAkaTokenRetrievalFlow mAkaTokenRetrievalFlow;
    /* access modifiers changed from: private */
    public BaseFlowImpl mBaseFlowImpl;
    protected ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            String access$000 = NSDSModule.this.LOG_TAG;
            IMSLog.i(access$000, "Uri changed:" + uri);
            IMnoNsdsStrategy mnoStrategy = NSDSModule.this.getMnoNsdsStrategy();
            if (mnoStrategy == null || uri == null) {
                IMSLog.e(NSDSModule.this.LOG_TAG, "Uri changed: null mnoStrategy or null url");
                return;
            }
            int match = NSDSModule.sUriMatcher.match(uri);
            if (match == 40) {
                NSDSModule.this.performProceduresOnConfigRefreshComplete();
            } else if (match != 71) {
                if (match == 78) {
                    NSDSModule.this.queueUpdateDeviceName();
                } else if (match == 79) {
                    NSDSModule.this.performOnDeviceReadyIf();
                }
            } else if (NSDSModule.this.isDeviceReady()) {
                NSDSModule.this.handleSimSwapEvent("SimSwapCache is ready");
            } else {
                IMSLog.i(NSDSModule.this.LOG_TAG, "SIM swap will be handled after device is ready");
                if (mnoStrategy.getSimSwapFlow(NSDSModule.sServiceLooper, NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl, NSDSModule.this.mNSDSDatabaseHelper) != null) {
                    NSDSModule.this.mHandleSimSwapAfterDeviceIsReady = true;
                }
            }
            if (mnoStrategy.shouldChangedUriTriggerNsdsService(uri)) {
                NSDSModule.this.enableOrDisableNSDSService();
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    protected IEntitlementCheck mEntitlementCheckFlow;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    protected boolean mHandleSimSwapAfterDeviceIsReady;
    protected IImsRegistrationListener.Stub mImsRegistratinListner = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) {
            NSDSModule.this.editRegiListOnRegistered(reg);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(0, reg));
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errorCode) {
            NSDSModule.this.removeFromRegiListOnDeregistered(reg);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(1, errorCode.getSipErrorCode(), 0, reg));
        }
    };
    protected IImsService mImsService;
    /* access modifiers changed from: private */
    public Date mInvalidFingerPrintDate = null;
    private AtomicBoolean mIsAfterApm = new AtomicBoolean(false);
    protected boolean mIsSimSupported = false;
    protected boolean mIsXcapConnected = false;
    protected BroadcastReceiver mNSDSAppFlowReceiver = null;
    protected NSDSDatabaseHelper mNSDSDatabaseHelper;
    protected BroadcastReceiver mNSDSEventRequestReceiver = null;
    /* access modifiers changed from: private */
    public NSDSNetworkInfoManager mNSDSNetworkInfoManager;
    protected SharedPreferences.OnSharedPreferenceChangeListener mNSDSSharedPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            String access$000 = NSDSModule.this.LOG_TAG;
            IMSLog.i(access$000, "OnSharedPreferenceChangeListener: " + key + " changed.");
            if (key.contains(NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE)) {
                NSDSModule.this.queuePushTokenUpdateIf();
            } else if (key.contains(NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER)) {
                boolean isTokenSent = NSDSSharedPrefHelper.isGcmTokenSentToServer(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                String access$0002 = NSDSModule.this.LOG_TAG;
                IMSLog.i(access$0002, "isTokenSent: " + isTokenSent);
                if (isTokenSent) {
                    NSDSModule.this.updateGcmPushTokenInDb();
                    NSDSModule.this.queuePushTokenUpdateIf();
                    IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_REFRESH_TOKEN);
                    IMSLog.i(NSDSModule.this.LOG_TAG, "RefrehTokenTimer stopped");
                }
            } else if (key.contains(NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE)) {
                boolean isSvcProvCompleted = NSDSSharedPrefHelper.isVoWifiServiceProvisioned(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                String access$0003 = NSDSModule.this.LOG_TAG;
                IMSLog.i(access$0003, "isSvcProvCompleted: " + isSvcProvCompleted);
                if (isSvcProvCompleted) {
                    NSDSModule.this.scheduleServiceProvisionCheckTimer();
                } else {
                    IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_SVC_PROVISION_CHECK_TIMEOUT);
                }
            } else if (key.contains(NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS) && TextUtils.equals(NSDSSharedPrefHelper.get(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS), NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY)) {
                IMSLog.i(NSDSModule.this.LOG_TAG, "[ATT_AutoOn] AUTOON_RETRY");
                NSDSModule nSDSModule = NSDSModule.this;
                nSDSModule.scheduleRetryWFCAutoOnTimer(nSDSModule.mWfcAutoOnRetryCount);
            }
        }
    };
    private NsdsFcmListenerService mNsdsFcmListenerService = null;
    protected AtomicBoolean mOnSimSwapEvt = new AtomicBoolean(false);
    protected IPdnController mPdnController;
    PdnEventListener mPdnListener = new PdnEventListener() {
        public void onConnected(int networkType, Network network) {
            if (networkType != ConnectivityManagerExt.TYPE_MOBILE_XCAP || network == null) {
                IMSLog.i(NSDSModule.this.LOG_TAG, "networkType mismatched or null network!");
                return;
            }
            String access$000 = NSDSModule.this.LOG_TAG;
            IMSLog.i(access$000, "onConnected " + networkType + " with " + network);
            NSDSModule.this.mIsXcapConnected = true;
            NSDSModule.this.mNSDSNetworkInfoManager.setSocketFactory(network.getSocketFactory(), NSDSModule.this.mSimManager.getSimSlotIndex());
            NSDSModule.this.mNSDSNetworkInfoManager.setNetwork(network, NSDSModule.this.mSimManager.getSimSlotIndex());
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(22));
        }

        public void onDisconnected(int networkType, boolean isPdnUp) {
            String access$000 = NSDSModule.this.LOG_TAG;
            IMSLog.i(access$000, "onDisconnected " + networkType + " with " + isPdnUp);
            NSDSModule.this.mIsXcapConnected = false;
            NSDSModule.this.mNSDSNetworkInfoManager.clearNetworkInfo(NSDSModule.this.mSimManager.getSimSlotIndex());
        }

        public void onSuspended(int networkType) {
        }

        public void onPcscfAddressChanged(int networkType, List<String> list) {
        }

        public void onLocalIpChanged(int networkType, boolean isStackedIpChanged) {
        }

        public void onResumed(int networkType) {
        }

        public void onSuspendedBySnapshot(int networkType) {
        }

        public void onResumedBySnapshot(int networkType) {
        }

        public void onNetworkRequestFail() {
        }
    };
    private List<Message> mPendindMsgsForSimSwapCompletion = new ArrayList();
    protected PushTokenUpdateFlow mPushTokenUpdateFlow;
    protected final ArrayList<ImsRegistration> mRegistrationList = new ArrayList<>();
    private ISIMDeviceImplicitActivation mSIMDeviceActivationFlow;
    /* access modifiers changed from: private */
    public ISimManager mSimManager;
    private ISimSwapFlow mSimSwapFlow;
    /* access modifiers changed from: private */
    public int mSvcProvCheckRetryCount = 0;
    /* access modifiers changed from: private */
    public int mWfcAutoOnRetryCount = 0;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "nsds_configs", 40);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "sim_swap_nsds_configs", 71);
        sUriMatcher.addURI("settings", "global/device_name", 78);
        sUriMatcher.addURI("settings", "global/device_provisioned", 79);
    }

    /* access modifiers changed from: private */
    public void updateGcmPushTokenInDb() {
        IMSLog.i(this.LOG_TAG, "updateGcmPushTokenInDb()");
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        String senderId = mnoStrategy != null ? mnoStrategy.getGcmSenderId(this.mBaseFlowImpl.getDeviceId(), this.mSimManager.getImsi()) : null;
        String token = PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("GCM Token ready: " + token);
        this.mNSDSDatabaseHelper.insertOrUpdateGcmPushToken(senderId, token, "managePushToken", this.mBaseFlowImpl.getDeviceId());
    }

    protected class AirplaneModeObserver extends ContentObserver {
        public AirplaneModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int airPlaneModeOn = Settings.Global.getInt(NSDSModule.this.mContext.getContentResolver(), "airplane_mode_on", 1);
            String access$000 = NSDSModule.this.LOG_TAG;
            IMSLog.i(access$000, "AirpalneModeOn onChange: " + airPlaneModeOn);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(18, Integer.valueOf(airPlaneModeOn)));
        }
    }

    public NSDSModule(Looper looper, Context context, ISimManager simManager) {
        super(looper);
        sServiceLooper = looper;
        this.mContext = context;
        this.mSimManager = simManager;
        this.mNSDSDatabaseHelper = new NSDSDatabaseHelper(this.mContext);
        SimpleEventLog simpleEventLog = new SimpleEventLog(this.mContext, this.LOG_TAG, 100);
        this.mEventLog = simpleEventLog;
        simpleEventLog.logAndAdd("Create " + this.LOG_TAG);
        this.LOG_TAG += "<" + this.mSimManager.getSimSlotIndex() + ">";
        initialize();
    }

    private void initialize() {
        initNsdsAppFlows();
        registerNsdsContentObserver();
        registerAirplaneModeObserver();
        registerNsdsEventQueueReceiver();
        registerNsdsAppFlowReceiver();
        this.mPdnController = ImsRegistry.getPdnController();
        this.mNsdsFcmListenerService = new NsdsFcmListenerService();
        this.mNSDSNetworkInfoManager = NSDSNetworkInfoManager.getInstance();
        ImsRegistry.getFcmHandler().registerFcmEventListener(this.mNsdsFcmListenerService);
        connectImsService();
    }

    private void initSimSwapFlow() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            IMSLog.i(this.LOG_TAG, "initSimSwapFlow()");
            this.mSimSwapFlow = mnoStrategy.getSimSwapFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
        }
    }

    private void initNsdsAppFlows() {
        this.mBaseFlowImpl = new BaseFlowImpl(sServiceLooper, this.mContext, this.mSimManager);
        this.mPushTokenUpdateFlow = new PushTokenUpdateFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) {
        IMSLog.i(this.LOG_TAG, "registerImsRegistrationListener");
        try {
            this.mImsService.registerImsRegistrationListener(listener);
        } catch (RemoteException re) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "registerImsRegistrationListener " + re.getMessage());
        }
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) {
        IMSLog.i(this.LOG_TAG, "unregisterImsRegistrationListener");
        try {
            this.mImsService.unregisterImsRegistrationListener(listener);
        } catch (RemoteException re) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "unregisterImsRegistrationListener " + re.getMessage());
        }
    }

    private void connectImsService() {
        IMSLog.i(this.LOG_TAG, "connectImsService");
        if (this.mImsService == null) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.ImsService");
            ContextExt.bindServiceAsUser(this.mContext, serviceIntent, new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "Connected to ImsService.");
                    NSDSModule.this.mImsService = IImsService.Stub.asInterface(service);
                    NSDSModule nSDSModule = NSDSModule.this;
                    nSDSModule.registerImsRegistrationListener(nSDSModule.mImsRegistratinListner);
                }

                public void onServiceDisconnected(ComponentName name) {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "Disconnected from ImsService.");
                    NSDSModule nSDSModule = NSDSModule.this;
                    nSDSModule.unregisterImsRegistrationListener(nSDSModule.mImsRegistratinListner);
                    NSDSModule.this.mImsService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    /* access modifiers changed from: private */
    public void enableOrDisableNSDSService() {
        if (this.mOnSimSwapEvt.get()) {
            IMSLog.i(this.LOG_TAG, "add EVT_ENABLE_OR_DISABLE_SERVICE for sim swap complete:");
            this.mPendindMsgsForSimSwapCompletion.add(obtainMessage(42));
            return;
        }
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy == null || !mnoStrategy.isNsdsServiceEnabled()) {
            deactivateDeviceIfNsdsServiceDisabled();
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "enableOrDisableNSDSService: activate SIM device");
            queueSimDeviceActivation(11, 0);
        }
    }

    private void registerNsdsAppFlowReceiver() {
        if (this.mNSDSAppFlowReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
            filter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
            filter.addAction(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
            filter.addAction(NSDSNamespaces.NSDSActions.E911_AID_INFO_RECEIVED);
            filter.addAction(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
            filter.addAction(NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION);
            filter.addAction(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
            filter.addAction(NSDSNamespaces.NSDSActions.DEVICE_PUSH_TOKEN_READY);
            AnonymousClass6 r1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String autoActivate;
                    String access$000 = NSDSModule.this.LOG_TAG;
                    IMSLog.i(access$000, "onReceive: app flow result arrived " + intent.getAction());
                    int slotid = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                    if (slotid != NSDSModule.this.mSimManager.getSimSlotIndex()) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "SlotId isn't matched about intent");
                        return;
                    }
                    NSDSModule.this.scheduleNsdsAppFlowRetryIf(intent);
                    if (NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED.equals(intent.getAction())) {
                        IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_CHECK_REG_STATE);
                        if (intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_DEACTIVATION_CAUSE, 0) == 1) {
                            Date unused = NSDSModule.this.mInvalidFingerPrintDate = new Date(System.currentTimeMillis());
                        }
                    } else if (NSDSNamespaces.NSDSActions.E911_AID_INFO_RECEIVED.equals(intent.getAction())) {
                        NSDSModule.this.scheduleE911CheckTimer();
                        NSDSSharedPrefHelper.save(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
                    } else if (NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED.equals(intent.getAction())) {
                        NSDSModule.this.handleResultAfterEntitlementCheck(intent);
                    } else if (NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION.equals(intent.getAction())) {
                        ArrayList<String> eventList = intent.getStringArrayListExtra(NSDSNamespaces.NSDSExtras.EVENT_LIST);
                        String access$0002 = NSDSModule.this.LOG_TAG;
                        IMSLog.i(access$0002, "onReceive: entitlement push notification arrived " + eventList);
                        if (eventList != null) {
                            if (eventList.contains(NSDSNamespaces.NSDSGcmEventType.ENTMT_UPDATE)) {
                                NSDSModule.this.queueEntitlementCheck(9, 0);
                            } else if (eventList.contains(NSDSNamespaces.NSDSGcmEventType.E911_ADDR_UPDATE)) {
                                NSDSModule.this.queueEntitlementCheck(8, 0);
                            }
                        }
                    } else if (NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED.equals(intent.getAction())) {
                        int response = intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, -1);
                        SimpleEventLog access$2400 = NSDSModule.this.mEventLog;
                        int simSlotIndex = NSDSModule.this.mSimManager.getSimSlotIndex();
                        access$2400.logAndAdd(simSlotIndex, "ENTITLEMENT_CHECK_COMPLETED: Response[" + response + "], VoLTE[" + intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, false) + "], VoWiFi[" + intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, false) + "]");
                        NSDSModule.this.handlePollIntervalAfterEntitlementCheck(intent);
                        NSDSModule.this.handleEntitlementCheckCompleted(intent);
                        NSDSModule.this.removeMessages(24);
                        NSDSModule.this.removeMessages(46);
                        NSDSModule nSDSModule = NSDSModule.this;
                        nSDSModule.sendMessage(nSDSModule.obtainMessage(50));
                    } else if (NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED.equals(intent.getAction())) {
                        NSDSModule.this.handleResultAfterConfigRetrieval(intent);
                    } else if (NSDSNamespaces.NSDSActions.DEVICE_PUSH_TOKEN_READY.equals(intent.getAction())) {
                        String token = intent.getStringExtra("device_push_token");
                        NSDSModule.this.mNSDSDatabaseHelper.insertOrUpdateGcmPushToken(intent.getStringExtra("gcm_sender_id"), token, FcmNamespaces.BROADCAST_TO_ANDSF_APP, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                    }
                    if (EntFeatureDetector.checkWFCAutoOnEnabled(slotid) && NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED.equals(intent.getAction()) && (autoActivate = NSDSSharedPrefHelper.get(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS)) != null && !"completed".equals(autoActivate)) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "[ATT_AutoOn] onReceive: start VoWIFI Toggle AutoOn");
                        int unused2 = NSDSModule.this.mWfcAutoOnRetryCount = intent.getIntExtra("retry_count", -1);
                        NSDSModule.this.handleVoWifToggleOnEvent();
                        NSDSModule nSDSModule2 = NSDSModule.this;
                        nSDSModule2.scheduleRetryWFCAutoOnTimer(nSDSModule2.mWfcAutoOnRetryCount);
                    }
                }
            };
            this.mNSDSAppFlowReceiver = r1;
            this.mContext.registerReceiver(r1, filter);
        }
    }

    /* access modifiers changed from: private */
    public void handleResultAfterConfigRetrieval(Intent intent) {
        boolean success = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        int deviceEventType = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
        List<Integer> errorCodes = intent.getIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleResultAfterConfigRetrieval: " + success + " deviceEventType " + deviceEventType + " errorCodes " + errorCodes);
        if (success && errorCodes != null && !errorCodes.contains(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_NEW_CONFIG_UPDATED))) {
            performProceduresOnConfigRefreshComplete();
        }
    }

    private void registerNsdsEventQueueReceiver() {
        if (this.mNSDSEventRequestReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_REFRESH_TOKEN);
            filter.addAction(ACTION_REFRESH_DEVICE_INFO);
            filter.addAction(ACTION_SIM_DEVICE_ACTIVATION);
            filter.addAction(ACTION_CHECK_REG_STATE);
            filter.addAction(ACTION_E911_AID_EXP_CHECK_TIMEOUT);
            filter.addAction(ACTION_SVC_PROVISION_CHECK_TIMEOUT);
            filter.addAction(ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT);
            filter.addAction(ACTION_REFRESH_ENTITLEMENT_CHECK);
            filter.addAction(ACTION_RETRY_ENTITLEMENT_CHECK);
            AnonymousClass7 r1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String access$000 = NSDSModule.this.LOG_TAG;
                    IMSLog.i(access$000, "onReceive: event has been requested " + intent.getAction());
                    int retryCount = intent.getIntExtra("retry_count", 0);
                    int deviceEventType = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, -1);
                    if (intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0) != NSDSModule.this.mSimManager.getSimSlotIndex()) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "SlotId isn't matched about intent");
                    } else if (NSDSModule.ACTION_REFRESH_TOKEN.equals(intent.getAction())) {
                        NSDSModule.this.queueGcmTokenRetrieval();
                    } else if (NSDSModule.ACTION_REFRESH_DEVICE_INFO.equals(intent.getAction())) {
                        NSDSModule.this.queueRefreshDeviceAndServiceInfo(deviceEventType, retryCount);
                    } else if (NSDSModule.ACTION_SIM_DEVICE_ACTIVATION.equals(intent.getAction())) {
                        NSDSModule.this.queueSimDeviceActivation(deviceEventType, retryCount);
                    } else if (NSDSModule.ACTION_CHECK_REG_STATE.equals(intent.getAction())) {
                        List<String> prevReadyForUseMsisdns = NSDSModule.this.mNSDSDatabaseHelper.getReadyForUseMsisdns(NSDSModule.this.mBaseFlowImpl.getDeviceId());
                        String access$0002 = NSDSModule.this.LOG_TAG;
                        IMSLog.s(access$0002, "onReceive: ACTION_CHECK_REG_STATE timeout. prevRegMsisdns: " + prevReadyForUseMsisdns);
                        NSDSModule.this.broadcastLinesReadyStatusUpdated(new ArrayList(prevReadyForUseMsisdns), 0, 2);
                    } else if (NSDSModule.ACTION_E911_AID_EXP_CHECK_TIMEOUT.equals(intent.getAction())) {
                        NSDSModule.this.refreshEntitlementAndE911Info(6, 0);
                    } else if (NSDSModule.ACTION_SVC_PROVISION_CHECK_TIMEOUT.equals(intent.getAction())) {
                        NSDSModule.this.queueEntitlementCheck(4, 0);
                    } else if (NSDSModule.ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT.equals(intent.getAction())) {
                        IMnoNsdsStrategy mnoNsdsStrategy = NSDSModule.this.getMnoNsdsStrategy();
                        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isNsdsUIAppSwitchOn(NSDSModule.this.mBaseFlowImpl.getDeviceId())) {
                            NSDSModule nSDSModule = NSDSModule.this;
                            nSDSModule.queueEntitlementCheck(10, nSDSModule.mSvcProvCheckRetryCount);
                        }
                    } else if (NSDSModule.ACTION_REFRESH_ENTITLEMENT_CHECK.equals(intent.getAction()) || NSDSModule.ACTION_RETRY_ENTITLEMENT_CHECK.equals(intent.getAction())) {
                        IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_REFRESH_ENTITLEMENT_CHECK);
                        NSDSModule.this.queueEntitlementCheck(deviceEventType, retryCount);
                    }
                }
            };
            this.mNSDSEventRequestReceiver = r1;
            this.mContext.registerReceiver(r1, filter);
        }
    }

    private void initNsdsSharePref() {
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp != null) {
            sp.registerOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
            clearActivationProgressState();
        }
    }

    private void registerNsdsContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.NsdsConfigs.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(EntitlementConfigContract.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.SimSwapNsdsConfigs.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_NAME), false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED), false, this.mContentObserver);
    }

    private boolean checkSimReady() {
        String str = this.LOG_TAG;
        IMSLog.i(str, "checkSimReady: " + this.mIsSimSupported);
        return this.mIsSimSupported;
    }

    private void onFlightMode(int airPlaneModeOn) {
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onFlightMode: ");
        sb.append(airPlaneModeOn == 1);
        IMSLog.i(str, sb.toString());
        if (airPlaneModeOn != 1) {
            this.mIsAfterApm.set(true);
            if (isDeviceReady()) {
                refreshEntitlementAndE911Info(6, 0);
                this.mIsAfterApm.set(false);
            }
        }
    }

    private void registerAirplaneModeObserver() {
        if (this.mAirplaneModeObserver == null) {
            this.mAirplaneModeObserver = new AirplaneModeObserver(new Handler(sServiceLooper));
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this.mAirplaneModeObserver);
        }
    }

    /* access modifiers changed from: private */
    public void scheduleE911CheckTimer() {
        IMSLog.i(this.LOG_TAG, "schedule E911 aid expiration check timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        long millis = mnoNsdsStrategy == null ? -1 : mnoNsdsStrategy.getEntitlementCheckExpirationTime();
        if (millis > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_E911_AID_EXP_CHECK_TIMEOUT, (Bundle) null, millis);
        }
    }

    /* access modifiers changed from: private */
    public void scheduleServiceProvisionCheckTimer() {
        IMSLog.i(this.LOG_TAG, "schedule service provision check timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        long millis = mnoNsdsStrategy == null ? -1 : mnoNsdsStrategy.getEntitlementCheckExpirationTime() * 2;
        if (millis > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SVC_PROVISION_CHECK_TIMEOUT, (Bundle) null, millis);
        }
    }

    private void scheduleServiceProvisionCheckRetryTimer() {
        long millis;
        IMSLog.i(this.LOG_TAG, "schedule service provision check retry timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            millis = -1;
        } else {
            int i = this.mSvcProvCheckRetryCount + 1;
            this.mSvcProvCheckRetryCount = i;
            millis = mnoNsdsStrategy.calEntitlementCheckExpRetryTime(i);
        }
        if (millis > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT, (Bundle) null, millis);
        } else {
            this.mSvcProvCheckRetryCount = 0;
        }
    }

    private void scheduleEntitlementCheckPollInterval(int pollInterval) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "schedule entitlement check poll interval : poll interval " + pollInterval);
        Bundle extras = new Bundle();
        extras.putInt("retry_count", 0);
        extras.putInt(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 1);
        extras.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
        extras.putInt("phoneId", this.mSimManager.getSimSlotIndex());
        IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK, extras, ((long) pollInterval) * 60 * 60 * 1000);
    }

    /* access modifiers changed from: private */
    public void scheduleRetryWFCAutoOnTimer(int retryCount) {
        if (retryCount <= 1) {
            IMSLog.i(this.LOG_TAG, "[ATT_AutoOn] scheduleRetryWFCAutoOnTimer start in about 5 minutes");
            Bundle extras = new Bundle();
            extras.putInt("retry_count", retryCount);
            extras.putInt(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 1);
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_RETRY_ENTITLEMENT_CHECK, extras, RETRY_INTERVAL_AUTO_ON);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceReady() {
        return NetworkUtil.isConnected(this.mContext) || this.mIsXcapConnected;
    }

    private void clearActivationProgressState() {
        if (NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "clearActivationProgressState: SIM device");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        }
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "clearActivationProgressState: Entitlement");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        }
    }

    private void scheduleGetGcmRegistrationTokenIfTokenNotSent() {
        boolean isTokenSent = NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId());
        String str = this.LOG_TAG;
        IMSLog.i(str, "scheduleGetGcmRegistrationTokenIfTokenNotSent: isTokenSent:" + isTokenSent);
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (!isTokenSent && mnoStrategy != null && mnoStrategy.isGcmTokenRequired()) {
            new Thread(new Runnable(mnoStrategy.getGcmSenderId(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getImsi())) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NSDSModule.this.lambda$scheduleGetGcmRegistrationTokenIfTokenNotSent$0$NSDSModule(this.f$1);
                }
            }).start();
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_TOKEN, REFRESH_TOKEN_WAIT_TIME);
        }
    }

    public /* synthetic */ void lambda$scheduleGetGcmRegistrationTokenIfTokenNotSent$0$NSDSModule(String senderId) {
        getGcmTokenFromServer(senderId, "managePushToken");
    }

    /* access modifiers changed from: private */
    public void performProceduresOnConfigRefreshComplete() {
        if (this.mOnSimSwapEvt.get()) {
            IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete: pending due to SIM swap");
            this.mPendindMsgsForSimSwapCompletion.add(obtainMessage(41));
            return;
        }
        IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete");
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy == null || mnoStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId())) {
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
        } else {
            IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete: NSDS switch off");
        }
    }

    private void deactivateDeviceIfNsdsServiceDisabled() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null && !mnoStrategy.isNsdsServiceEnabled() && NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Deactivating device since nsds service is disabled");
            queueSimDeviceDeactivation(0);
        }
    }

    /* access modifiers changed from: private */
    public void handleSimSwapEvent(String sourceEvent) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "handleSimSwapEvent:" + sourceEvent);
        this.mHandleSimSwapAfterDeviceIsReady = false;
        stopForcedSimSwap();
        performSimSwapFlow();
    }

    public void initForDeviceReady() {
        initMnoBasedAppFlows();
        initNsdsSharePref();
    }

    public void onSimReady(boolean isSwapped) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "onSimReady: isSwapped " + isSwapped);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("onSimReady: isSwapped " + isSwapped);
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            this.mIsSimSupported = mnoStrategy.isSimSupportedForNsds(this.mSimManager);
        }
        if (isSwapped) {
            unregisterLoginReceiversAndStopTimers();
            initSimSwapFlow();
            initNsdsAppFlows();
        }
        initMnoBasedAppFlows();
        initNsdsSharePref();
        this.mOnSimSwapEvt.set(isSwapped || NSDSSharedPrefHelper.isSimSwapPending(this.mContext, this.mBaseFlowImpl.getDeviceId()));
        if (this.mOnSimSwapEvt.get()) {
            scheduleForForcedSimSwapIf();
            this.mNSDSDatabaseHelper.copyConfigEntriesForSimSwap(this.mBaseFlowImpl.getDeviceId(), NSDSSharedPrefHelper.getPrefForSlot(this.mContext, this.mSimManager.getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI), this.mSimManager.getSimSlotIndex());
            clearEntitlementServerUrl();
            SimSwapNSDSConfigHelper.clear();
            MnoNsdsStrategyCreator.resetMnoStrategy();
            performProcsOnSimSwapCompleted();
        }
        removeMessages(24);
        removeMessages(46);
        performOnDeviceReadyIf();
    }

    private void clearEntitlementServerUrl() {
        NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, this.mBaseFlowImpl.getDeviceId());
    }

    private void initMnoBasedAppFlows() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            this.mBaseFlowImpl.setSimAuthAppType(mnoStrategy.getSimAuthenticationType());
            this.mEntitlementCheckFlow = mnoStrategy.getEntitlementCheckImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
            this.mSIMDeviceActivationFlow = mnoStrategy.getSimDeviceActivationImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
            this.mAkaTokenRetrievalFlow = new AkaTokenRetrievalFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
            return;
        }
        IMSLog.e(this.LOG_TAG, "initMnoBasedAppFlows: mnoStrategy is null");
    }

    private void performSimSwapFlow() {
        ISimSwapFlow iSimSwapFlow = this.mSimSwapFlow;
        if (iSimSwapFlow != null) {
            iSimSwapFlow.handleSimSwap(new SimSwapCompletedListener() {
                public void onSimSwapCompleted() {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "performSimSwapHandling: onSimSwapCompleted");
                    NSDSSharedPrefHelper.clearSimSwapPending(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                    NSDSModule.this.mOnSimSwapEvt.set(false);
                    if (NSDSModule.this.getMnoNsdsStrategy() != null) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "performOnDeviceReadyIf after onSimSwapCompleted");
                        NSDSModule.this.performOnDeviceReadyIf();
                    }
                }
            });
        } else {
            IMSLog.e(this.LOG_TAG, "handleSimSwapEvent: flow not initiated, invalid request");
        }
    }

    private void performProcsOnSimSwapCompleted() {
        this.mOnSimSwapEvt.set(false);
        IMSLog.i(this.LOG_TAG, "performProcsOnSimSwapCompleted()");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && mnoNsdsStrategy.shouldIgnoreDeviceConfigValidity() && mnoNsdsStrategy.isNsdsServiceEnabled()) {
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
        }
        if (!checkSimReady()) {
            IMSLog.i(this.LOG_TAG, "performProcsOnSimSwapCompleted: SIM not supported");
            handleSimNotSupported();
        } else if (!this.mPendindMsgsForSimSwapCompletion.isEmpty()) {
            for (Message msg : this.mPendindMsgsForSimSwapCompletion) {
                sendMessage(msg);
            }
        }
    }

    private void scheduleForForcedSimSwapIf() {
        IMSLog.i(this.LOG_TAG, "scheduleForForcedSimSwapIfCacheNotReady");
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        long waitTime = 0;
        if (mnoVSimStrategy != null) {
            waitTime = mnoVSimStrategy.getWaitTimeForForcedSimSwap();
        }
        sendMessageDelayed(obtainMessage(40), waitTime);
    }

    private void stopForcedSimSwap() {
        IMSLog.i(this.LOG_TAG, "stopped forced SimSwap handling");
        removeMessages(40);
    }

    public void onSimNotAvailable() {
        IMSLog.i(this.LOG_TAG, "onSimNotAvailable()");
        this.mEventLog.add("onSimNotAvailable()");
        IntentScheduler.stopAllTimers(this.mContext);
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            mnoStrategy.handleSimNotAvailable(this.mBaseFlowImpl.getDeviceId(), this.mSimManager.getSimSlotIndex());
        }
        removeMessages(24);
        removeMessages(46);
        sendMessage(obtainMessage(50));
    }

    private void getGcmTokenFromServer(String senderId, String protocol) {
        IMSLog.i(this.LOG_TAG, "getGcmRegistrationToken()");
        Intent intent = new Intent(this.mContext, RegistrationIntentService.class);
        intent.putExtra("gcm_sender_id", senderId);
        intent.putExtra(NSDSNamespaces.NSDSExtras.GCM_PROTOCOL_TO_SERVER, protocol);
        intent.putExtra("device_id", this.mBaseFlowImpl.getDeviceId());
        this.mContext.startService(intent);
    }

    public void queueGcmTokenRetrieval() {
        sendEmptyMessage(43);
    }

    private void refreshDeviceConfigIf(int retryCount) {
        if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(this.mSimManager.getImsi())) {
            requestDeviceConfigRetrieval(14, retryCount);
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMnoNsdsStrategy nsdsStrategy = getMnoNsdsStrategy();
            if (nsdsStrategy != null && nsdsStrategy.isNsdsServiceEnabled()) {
                sendMessage(obtainMessage(3, 11, 0));
            }
        } else {
            IMSLog.i(this.LOG_TAG, "refreshDeviceConfig: getConfigRefreshOnPowerUp");
            requestDeviceConfigRetrieval(15, retryCount);
        }
    }

    public void activateSimDevice(int deviceEventType, int retryCount) {
        if (!checkSimReady()) {
            notifySimErrorForDeviceActivation();
        } else if (this.mInvalidFingerPrintDate != null && new Date().getTime() - this.mInvalidFingerPrintDate.getTime() <= INVALID_FINGERPRINT_EXPIRATION_TIME) {
            IMSLog.i(this.LOG_TAG, "do not try it");
        } else if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "activateSimDevice: activate SIM device");
            queueSimDeviceActivation(deviceEventType, retryCount);
        }
    }

    public void retrieveAkaToken(int deviceEventType, int retryCount) {
        if (!checkSimReady()) {
            notifySimErrorForDeviceActivation();
        } else if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "retrieveAkaToken: retrieve aka token");
            queueRetrieveAkaToken(deviceEventType, retryCount);
        }
    }

    public void deactivateSimDevice(int deactivationCause) {
        queueSimDeviceDeactivation(deactivationCause);
    }

    public void updateEntitlementUrl(String url) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "updateEntitlementUrl: url " + url);
        if (TextUtils.isEmpty(url)) {
            IMSLog.e(this.LOG_TAG, "updateEntitlementUrl: empty or null url");
            return;
        }
        this.mContext.getContentResolver().delete(EntitlementConfigContract.DeviceConfig.CONTENT_URI, (String) null, (String[]) null);
        this.mNSDSDatabaseHelper.deleteConfigAndResetDeviceAndAccountStatus(this.mBaseFlowImpl.getDeviceId(), this.mSimManager.getImsi(), this.mSimManager.getSimSlotIndex());
        NSDSSharedPrefHelper.setEntitlementServerUrl(this.mContext, this.mBaseFlowImpl.getDeviceId(), url);
    }

    private void queuePerformBootupProcedures() {
        removeMessages(45);
        sendEmptyMessageDelayed(45, 200);
    }

    public void queueRefreshDeviceConfig(int retryCount) {
        if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(this.mSimManager.getImsi()) || (!this.mOnSimSwapEvt.get() && NSDSConfigHelper.getConfigRefreshOnPowerUp(this.mContext, this.mSimManager.getImsi()))) {
            sendMessage(obtainMessage(14, Integer.valueOf(retryCount)));
        }
    }

    /* access modifiers changed from: protected */
    public void queueEntitlementCheck(int deviceEventType, int retry) {
        sendMessage(obtainMessage(15, deviceEventType, retry));
    }

    /* access modifiers changed from: protected */
    public void queueE911AddressUpdate(int deviceEventType) {
        sendMessage(obtainMessage(19, Integer.valueOf(deviceEventType)));
    }

    /* access modifiers changed from: protected */
    public void queueRemovePushToken(int deviceEventType) {
        sendMessage(obtainMessage(17, Integer.valueOf(deviceEventType)));
    }

    /* access modifiers changed from: private */
    public void queueSimDeviceActivation(int deviceEventType, int retryCount) {
        sendMessage(obtainMessage(3, deviceEventType, retryCount));
    }

    private void queueRetrieveAkaToken(int deviceEventType, int retryCount) {
        sendMessage(obtainMessage(49, deviceEventType, retryCount));
    }

    public void queueRefreshDeviceAndServiceInfo(int deviceEventType, int retryCount) {
        sendMessage(obtainMessage(13, deviceEventType, retryCount));
    }

    private void queueSimDeviceDeactivation(int deactivationCause) {
        sendMessage(obtainMessage(4, 0, 0, Integer.valueOf(deactivationCause)));
    }

    public void registerEventMessenger(Messenger messenger) {
        NSDSAppFlowBase.registerEventMessenger(messenger);
    }

    public void unregisterEventMessenger(Messenger messenger) {
        NSDSAppFlowBase.unregisterEventMessenger(messenger);
    }

    /* access modifiers changed from: private */
    public void queuePushTokenUpdateIf() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId()) && NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId()) && mnoStrategy != null && !mnoStrategy.supportEntitlementCheck()) {
            queuePushTokenUpdateInEntitlementServer();
        }
    }

    public void queuePushTokenUpdateInEntitlementServer() {
        sendEmptyMessage(21);
    }

    /* access modifiers changed from: private */
    public void queueUpdateDeviceName() {
        sendMessage(obtainMessage(6, false));
    }

    /* access modifiers changed from: private */
    public void refreshEntitlementAndE911Info(int deviceEventType, int retry) {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy == null || !mnoStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId()) || !NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(this.LOG_TAG, "refreshEntitlementAndE911Info: not ready to refresh");
            return;
        }
        if (E911AidValidator.validate(this.mNSDSDatabaseHelper.getNativeLineE911AidExp(this.mBaseFlowImpl.getDeviceId()))) {
            scheduleE911CheckTimer();
            if (deviceEventType == 6) {
                IMSLog.i(this.LOG_TAG, "refreshEntitlementAndE911Info: still valid, no refresh");
                return;
            }
        }
        queueEntitlementCheck(deviceEventType, retry);
    }

    private void refreshEntitlementAndE911InfoAutoOn(int deviceEventType, int retry) {
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy == null || !mnoVSimStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId()) || !mnoVSimStrategy.supportEntitlementCheck()) {
            IMSLog.e(this.LOG_TAG, "refreshEntitlementAndE911Info: not ready to refresh");
        } else {
            queueEntitlementCheck(deviceEventType, retry);
        }
    }

    private void notifySimErrorForDeviceActivation() {
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.INVALID_SIM_STATUS));
        translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void notifySimErrorForEntitlementAndLocTc() {
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.INVALID_SIM_STATUS));
        translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void performSimDeviceImplicitActivation(int deviceEventType, int retryCount) {
        IMSLog.i(this.LOG_TAG, "performSimDeviceImplicitActivation:");
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null && !mnoStrategy.isNsdsServiceEnabled()) {
            IMSLog.i(this.LOG_TAG, "NSDS is disabled, vail.");
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Device was not activated, activate it.");
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
            ISIMDeviceImplicitActivation iSIMDeviceImplicitActivation = this.mSIMDeviceActivationFlow;
            if (iSIMDeviceImplicitActivation != null) {
                iSIMDeviceImplicitActivation.performSimDeviceImplicitActivation(deviceEventType, retryCount);
            } else {
                IMSLog.e(this.LOG_TAG, "performSimDeviceImplicitActivation: flow not initiated, invalid request");
            }
        }
    }

    private void performAkaTokenRetrievalFlow(int deviceEventType, int retryCount) {
        IMSLog.i(this.LOG_TAG, "performAkaTokenRetrievalFlow:");
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null && !mnoStrategy.isNsdsServiceEnabled()) {
            IMSLog.i(this.LOG_TAG, "NSDS is disabled, vail.");
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Device was not activated, retrieve only aka token");
            IAkaTokenRetrievalFlow iAkaTokenRetrievalFlow = this.mAkaTokenRetrievalFlow;
            if (iAkaTokenRetrievalFlow != null) {
                iAkaTokenRetrievalFlow.performAkaTokenRetrieval(deviceEventType, retryCount);
            } else {
                IMSLog.e(this.LOG_TAG, "performAkaTokenRetrievalFlow: flow not initiated, invalid request");
            }
        }
    }

    private void performE911AddressUpdate(int deviceEventType) {
        IEntitlementCheck iEntitlementCheck = this.mEntitlementCheckFlow;
        if (iEntitlementCheck != null) {
            iEntitlementCheck.performE911AddressUpdate(deviceEventType);
        } else {
            IMSLog.e(this.LOG_TAG, "performE911AddressUpdate: flow not initiated, invalid request");
        }
    }

    private void performSimDeviceDeactivationFlow(int deactivationCause) {
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy != null) {
            mnoVSimStrategy.getSimDeviceDeactivationImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper).deactivateDevice(deactivationCause);
        } else {
            IMSLog.e(this.LOG_TAG, "performSimDeviceDeactivationFlow: mnoStrategy not initiated, invalid request");
        }
    }

    private void updatePushTokenInEntitlementServer() {
        this.mPushTokenUpdateFlow.updatePushToken();
    }

    private void performEntitlementCheck(int deviceEventType, int retryCount) {
        if (!checkEntitlementReadyStatus()) {
            return;
        }
        if (this.mEntitlementCheckFlow != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            int simSlotIndex = this.mSimManager.getSimSlotIndex();
            simpleEventLog.logAndAdd(simSlotIndex, "performEntitlementCheck: deviceEventType[" + deviceEventType + "], retryCount[" + retryCount + "]");
            this.mEntitlementCheckFlow.performEntitlementCheck(deviceEventType, retryCount);
            return;
        }
        IMSLog.e(this.LOG_TAG, "performEntitlementCheck: flow not initiated, invalid request");
    }

    private boolean checkEntitlementReadyStatus() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy == null) {
            IMSLog.e(this.LOG_TAG, "checkEntitlementReadyStatus: mnoStrategy null");
            return false;
        } else if (!mnoStrategy.supportEntitlementCheck()) {
            String str = this.LOG_TAG;
            IMSLog.i(str, "checkEntitlementReadyStatus: entitlement not required for " + this.mSimManager.getSimOperator());
            return false;
        } else if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "checkEntitlementReadyStatus: entitlement in progress");
            return false;
        } else if (mnoStrategy.needCheckEntitlementPollInterval() && IntentScheduler.hasActionPendingIntent(this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK)) {
            IMSLog.i(this.LOG_TAG, "checkEntitlementReadyStatus: entitlement poll interval");
            return false;
        } else if (!mnoStrategy.isSIMDeviceActivationRequired()) {
            IMSLog.i(this.LOG_TAG, "checkEntitlementReadyStatus: device activation not required");
            return true;
        } else if (NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId()) || !mnoStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId())) {
            return true;
        } else {
            IMSLog.e(this.LOG_TAG, "checkEntitlementReadyStatus: device cannot be inactive !!");
            if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
                queueSimDeviceActivation(11, 0);
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void performRemovePushToken(int deviceEventType) {
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy != null && mnoVSimStrategy.supportEntitlementCheck()) {
            IEntitlementCheck iEntitlementCheck = this.mEntitlementCheckFlow;
            if (iEntitlementCheck != null) {
                iEntitlementCheck.performRemovePushToken(deviceEventType);
            } else {
                IMSLog.e(this.LOG_TAG, "performRemovePushToken: flow not initiated, invalid request");
            }
        }
    }

    public void updateE911Address() {
        IMSLog.i(this.LOG_TAG, "updateE911Address()");
        if (!checkSimReady()) {
            notifySimErrorForEntitlementAndLocTc();
        } else {
            queueE911AddressUpdate(5);
        }
    }

    public void handleVoWifToggleOnEvent() {
        IMSLog.i(this.LOG_TAG, "handleVoWifToggleOnEvent()");
        if (!checkSimReady()) {
            notifySimErrorForEntitlementAndLocTc();
        } else {
            queueEntitlementCheck(2, 0);
        }
    }

    public void handleVoWifToggleOffEvent() {
        IMSLog.i(this.LOG_TAG, "handleVoWifToggleOffEvent()");
        queueRemovePushToken(3);
    }

    private void requestDeviceConfigRetrieval(int deviceEventType, int retryCount) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "requestDeviceConfigRetrieval: eventType " + deviceEventType + " retryCount " + retryCount);
        Intent intent = new Intent(this.mContext, EntitlementConfigService.class);
        intent.setAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
        this.mContext.startService(intent);
    }

    /* access modifiers changed from: private */
    public void handleResultAfterEntitlementCheck(Intent intent) {
        int deviceEventType = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, -1);
        boolean success = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        List<Integer> errors = intent.getIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleResultAfterEntitlementCheck: eventType " + deviceEventType + " success " + success + " errors " + errors);
        if (success) {
            if (deviceEventType == 4 || deviceEventType == 1) {
                scheduleServiceProvisionCheckTimer();
            }
        } else if (deviceEventType != 6) {
            if (deviceEventType == 7) {
                IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: init retry count");
                this.mSvcProvCheckRetryCount = 0;
            } else if (deviceEventType != 10) {
                IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: no retry");
                return;
            }
            if (errors != null && errors.contains(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE))) {
                scheduleServiceProvisionCheckRetryTimer();
            }
        } else {
            IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: init e911AID check timer");
            scheduleE911CheckTimer();
        }
    }

    /* access modifiers changed from: private */
    public void handleEntitlementCheckCompleted(Intent intent) {
        Intent intent2 = intent;
        boolean isRequest = intent2.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        boolean volteEnabled = intent2.getBooleanExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, false);
        Mno mno = this.mSimManager.getSimMno();
        String imsi = this.mSimManager.getImsi();
        String prefName = ImsSharedPrefHelper.USER_CONFIG;
        String lastVolteSwitch = "last_volte_switch_" + imsi;
        int phoneId = this.mSimManager.getSimSlotIndex();
        IUtServiceModule usm = ImsRegistry.getServiceModuleManager().getUtServiceModule();
        IMSLog.i(this.LOG_TAG, "handleEntitlementCheckCompleted: " + intent.getAction());
        if (mno == Mno.TELEFONICA_UK || mno == Mno.TELEFONICA_UK_LAB) {
            if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 28) {
                prefName = "imsswitch";
                lastVolteSwitch = "last_volte_switch_" + imsi.substring(5);
            }
            int lastVoLTESwitch = ImsSharedPrefHelper.getInt(phoneId, this.mContext, prefName, lastVolteSwitch, -1);
            IMSLog.i(this.LOG_TAG, "get lastVoLTESwitch: " + lastVoLTESwitch);
            int volteSwitch = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), phoneId);
            if (!isRequest || !volteEnabled) {
                if (lastVoLTESwitch == -1) {
                    ImsSharedPrefHelper.save(phoneId, this.mContext, prefName, lastVolteSwitch, volteSwitch);
                    IMSLog.i(this.LOG_TAG, "set lastVoLTESwitch: " + volteSwitch);
                }
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 1, phoneId);
                IMSLog.i(this.LOG_TAG, "setVoiceCallType using VOICE_CS (switch off)");
                if (usm != null) {
                    usm.enableUt(phoneId, false);
                    return;
                }
                return;
            }
            if (!mLastImsi.equals(imsi)) {
                IMSLog.i(this.LOG_TAG, "imsi is changed from " + IMSLog.checker(mLastImsi) + " to " + IMSLog.checker(imsi));
                ImsSharedPrefHelper.save(phoneId, this.mContext, prefName, lastVolteSwitch, volteSwitch);
                IMSLog.i(this.LOG_TAG, "set lastVoLTESwitch: " + volteSwitch);
                volteSwitch = -1;
                mLastImsi = imsi;
            }
            if (lastVoLTESwitch != -1) {
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, lastVoLTESwitch, phoneId);
                IMSLog.i(this.LOG_TAG, "setVoiceCallType to lastVoLTESwitch: " + lastVoLTESwitch);
                ImsSharedPrefHelper.save(phoneId, this.mContext, prefName, lastVolteSwitch, -1);
                IMSLog.i(this.LOG_TAG, "set lastVoLTESwitch: -1");
            } else if (volteSwitch != -1) {
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, volteSwitch, phoneId);
                IMSLog.i(this.LOG_TAG, "setVoiceCallType to volteSwitch: " + volteSwitch);
            } else {
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, phoneId);
                IMSLog.i(this.LOG_TAG, "setVoiceCallType to VOICE_VOLTE (default on)");
            }
            if (usm != null) {
                usm.enableUt(phoneId, true);
            }
            if (ImsRegistry.isReady()) {
                ImsRegistry.getRegistrationManager().requestTryRegister(phoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePollIntervalAfterEntitlementCheck(Intent intent) {
        boolean success = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        int errorCode = intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, -1);
        String str = this.LOG_TAG;
        IMSLog.i(str, "handlePollIntervalAfterEntitlementCheck: " + success + ", " + errorCode);
        if (success) {
            int pollInterval = intent.getIntExtra(NSDSNamespaces.NSDSExtras.POLL_INTERVAL, 24);
            if (pollInterval == 0) {
                IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK);
            } else {
                scheduleEntitlementCheckPollInterval(pollInterval);
            }
        } else if (errorCode != -1) {
            IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK);
        }
    }

    private void handleSimNotSupported() {
        NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, this.mBaseFlowImpl.getDeviceId());
        this.mOnSimSwapEvt.set(false);
        this.mNSDSDatabaseHelper.deleteNsdsConfigs(this.mSimManager.getImsi());
        NSDSConfigHelper.clear();
    }

    private void updateDeviceName() {
        Uri.Builder uriBuilder = NSDSContractExt.Devices.buildUpdateDeviceNameUri((long) this.mNSDSDatabaseHelper.getDeviceId(this.mBaseFlowImpl.getDeviceId())).buildUpon();
        uriBuilder.appendQueryParameter(NSDSContractExt.Devices.QUERY_PARAM_DEVICE_NAME, DeviceNameHelper.getDeviceName(this.mContext));
        this.mContext.getContentResolver().update(uriBuilder.build(), new ContentValues(), (String) null, (String[]) null);
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoNsdsStrategy() {
        MnoNsdsStrategyCreator mnoStrategyCreator = MnoNsdsStrategyCreator.getInstance(this.mContext, this.mSimManager.getSimSlotIndex());
        if (mnoStrategyCreator != null) {
            IMnoNsdsStrategy mnoStrategy = mnoStrategyCreator.getMnoStrategy();
            if (mnoStrategy == null) {
                return null;
            }
            String str = this.LOG_TAG;
            IMSLog.i(str, "getMnoNsdsStrategy: " + mnoStrategy.getClass().getName());
            return mnoStrategy;
        }
        IMSLog.i(this.LOG_TAG, "getMnoNsdsStrategy: mnoStrategyCreator is null");
        return null;
    }

    private void unregisterLoginReceiversAndStopTimers() {
        IntentScheduler.stopAllTimers(this.mContext);
    }

    /* access modifiers changed from: private */
    public void performOnDeviceReadyIf() {
        if (!checkSimReady()) {
            IMSLog.i(this.LOG_TAG, "SIM not supported...");
            handleSimNotSupported();
            return;
        }
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy == null) {
            IMSLog.e(this.LOG_TAG, "MNO Strategy has failed to be initiated...");
        } else if (!mnoStrategy.isDeviceProvisioned()) {
            IMSLog.i(this.LOG_TAG, "Waiting for OOBE setup complete...");
        } else if (isDeviceReady()) {
            if (this.mHandleSimSwapAfterDeviceIsReady) {
                IMSLog.i(this.LOG_TAG, "handling case when simswap cache was ready but device was not ready at that time");
                handleSimSwapEvent("Device is Ready");
                return;
            }
            queuePerformBootupProcedures();
        } else if (!mnoStrategy.isNsdsServiceViaXcap() || NetworkUtil.isMobileDataOn(this.mContext)) {
            IMSLog.i(this.LOG_TAG, "Device is still waiting to be ready...");
        } else {
            this.mPdnController.startPdnConnectivity(ConnectivityManagerExt.TYPE_MOBILE_XCAP, this.mPdnListener, this.mSimManager.getSimSlotIndex());
            removeMessages(24);
            sendMessageDelayed(obtainMessage(24, 0, 60000), 10000);
            IMSLog.i(this.LOG_TAG, "RequestNetwork via XCAP & Waiting for device readiness...");
        }
    }

    public void onDeviceReady() {
        IMSLog.s(this.LOG_TAG, "onDeviceReady");
        DeviceIdHelper.getDeviceId(this.mContext, this.mSimManager.getSimSlotIndex());
        removeMessages(24);
        removeMessages(46);
        performOnDeviceReadyIf();
    }

    private void performBootupProcedures() {
        removeMessages(24);
        removeMessages(46);
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            for (Integer intValue : mnoStrategy.getOperationsForBootupInit(this.mBaseFlowImpl.getDeviceId())) {
                sendEmptyMessage(intValue.intValue());
            }
            return;
        }
        IMSLog.e(this.LOG_TAG, "performBootupProcedures: mnoStrategy cannot be null !!!");
    }

    private void startPdnConnectivity(int retryCount, int retryTimer) {
        this.mPdnController.startPdnConnectivity(ConnectivityManagerExt.TYPE_MOBILE_XCAP, this.mPdnListener, this.mSimManager.getSimSlotIndex());
        removeMessages(24);
        sendMessageDelayed(obtainMessage(24, retryCount, retryTimer), 10000);
    }

    private void stopPdnConnectivity() {
        this.mPdnController.stopPdnConnectivity(ConnectivityManagerExt.TYPE_MOBILE_XCAP, this.mSimManager.getSimSlotIndex(), this.mPdnListener);
        this.mIsXcapConnected = false;
        this.mNSDSNetworkInfoManager.clearNetworkInfo(this.mSimManager.getSimSlotIndex());
    }

    private ImsRegistration getImsRegistration(int phoneId, boolean isEmergency) {
        Iterator<ImsRegistration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            ImsRegistration reg = it.next();
            if (reg != null && reg.getPhoneId() == phoneId && reg.getImsProfile().hasEmergencySupport() == isEmergency) {
                return reg;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void editRegiListOnRegistered(ImsRegistration regiInfo) {
        ImsRegistration reg;
        int oldRegiIndex = -1;
        Iterator<ImsRegistration> it = this.mRegistrationList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            reg = it.next();
            if (reg.getHandle() == regiInfo.getHandle() || (reg.getPhoneId() == regiInfo.getPhoneId() && TextUtils.equals(reg.getImsProfile().getName(), regiInfo.getImsProfile().getName()))) {
                oldRegiIndex = this.mRegistrationList.indexOf(reg);
            }
        }
        oldRegiIndex = this.mRegistrationList.indexOf(reg);
        if (oldRegiIndex == -1) {
            this.mRegistrationList.add(regiInfo);
        } else {
            this.mRegistrationList.set(oldRegiIndex, regiInfo);
        }
    }

    /* access modifiers changed from: private */
    public void removeFromRegiListOnDeregistered(ImsRegistration regiInfo) {
        Iterator<ImsRegistration> it = new ArrayList<>(this.mRegistrationList).iterator();
        while (it.hasNext()) {
            ImsRegistration reg = it.next();
            if (reg.getHandle() == regiInfo.getHandle()) {
                this.mRegistrationList.remove(reg);
            }
        }
    }

    private void onRegistration(ImsRegistration reg) {
        ImsProfile profile = reg.getImsProfile();
        if (profile == null || profile.hasEmergencySupport()) {
            IMSLog.i(this.LOG_TAG, "onRegistration: emergency registration, skip");
            return;
        }
        String str = this.LOG_TAG;
        IMSLog.i(str, "onRegistration: " + reg.toString());
        IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_CHECK_REG_STATE);
        List<String> registeredMsisdns = getMsisdnsFromImsRegistration(reg);
        List<String> prevReadyForUseMsisdns = this.mNSDSDatabaseHelper.getReadyForUseMsisdns(this.mBaseFlowImpl.getDeviceId());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "onRegistration: prevReadyForUseMsisdns " + prevReadyForUseMsisdns);
        this.mNSDSDatabaseHelper.updateRegistationStatusForLines(registeredMsisdns, 1, 0, 2);
        ArrayList<String> addedMsisdns = new ArrayList<>();
        for (String msisdn : registeredMsisdns) {
            if (!prevReadyForUseMsisdns.contains(msisdn)) {
                String str3 = this.LOG_TAG;
                IMSLog.s(str3, "onRegistration: add to added list:" + msisdn);
                addedMsisdns.add(msisdn);
            }
        }
        if (addedMsisdns.size() > 0) {
            broadcastLinesReadyStatusUpdated(addedMsisdns, 0, 2);
        } else {
            markLineDeregisteredIfRemovedInRereg(registeredMsisdns, prevReadyForUseMsisdns);
        }
    }

    private void markLineDeregisteredIfRemovedInRereg(List<String> registeredMsisdns, List<String> prevReadyForUseMsisdns) {
        ArrayList<String> deletedMsisdns = new ArrayList<>();
        for (String msisdn : prevReadyForUseMsisdns) {
            if (!registeredMsisdns.contains(msisdn)) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "markLineDeregisteredIfRemovedInRereg: add to deleted list:" + msisdn);
                deletedMsisdns.add(msisdn);
            }
        }
        if (deletedMsisdns.size() > 0) {
            this.mNSDSDatabaseHelper.updateRegistationStatusForLines(deletedMsisdns, 0, 2, 0);
            broadcastLinesReadyStatusUpdated(deletedMsisdns, 2, 0);
        }
    }

    private ArrayList<String> getMsisdnsFromImsRegistration(ImsRegistration reg) {
        List<NameAddr> nameAddrs = reg.getImpuList();
        ArrayList<String> registeredMsisdns = new ArrayList<>();
        for (NameAddr nameAddr : nameAddrs) {
            String msisdn = nameAddr.getUri().getMsisdn();
            if (msisdn != null) {
                if (msisdn.startsWith("+")) {
                    msisdn = msisdn.substring(1);
                }
                if (msisdn.length() <= 11 && !registeredMsisdns.contains(msisdn)) {
                    registeredMsisdns.add(msisdn);
                }
            }
        }
        String str = this.LOG_TAG;
        IMSLog.s(str, "getMsisdnsFromImsRegistration:" + registeredMsisdns);
        return registeredMsisdns;
    }

    /* access modifiers changed from: private */
    public void broadcastLinesReadyStatusUpdated(ArrayList<String> updatedMsisdns, int fromRegStatus, int toRegStatus) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.LINES_READY_STATUS_UPDATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.FROM_REG_STATUS, fromRegStatus);
        intent.putExtra(NSDSNamespaces.NSDSExtras.TO_REG_STATUS, toRegStatus);
        intent.putStringArrayListExtra(NSDSNamespaces.NSDSExtras.MSISDN_LIST, updatedMsisdns);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void broadcastStoredEntitlement() {
        int slotIdx = this.mSimManager.getSimSlotIndex();
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, true);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, NSDSSharedPrefHelper.getVoWiFiEntitlement(this.mContext, slotIdx));
        intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, NSDSSharedPrefHelper.getVoLteEntitlement(this.mContext, slotIdx));
        intent.putExtra(NSDSNamespaces.NSDSExtras.POLL_INTERVAL, 24);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotIdx);
        intent.putExtra("phoneId", slotIdx);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(slotIdx, "broadcastStoredEntitlement: " + intent.getExtras().toString());
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void onDeregistration(ImsRegistration reg) {
        ImsRegistration currReg;
        ImsProfile profile = reg.getImsProfile();
        if (profile == null || profile.hasEmergencySupport()) {
            IMSLog.i(this.LOG_TAG, "onDeregistration: emergency deregistration, skip");
            return;
        }
        String str = this.LOG_TAG;
        IMSLog.i(str, "onDeregistration: " + reg.toString());
        if (this.mRegistrationList.size() <= 0 || ((currReg = getImsRegistration(this.mSimManager.getSimSlotIndex(), false)) != null && TextUtils.equals(reg.getImsProfile().getName(), currReg.getImsProfile().getName()))) {
            IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_CHECK_REG_STATE);
            ArrayList<String> deregisteredMsisdns = getMsisdnsFromImsRegistration(reg);
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "onDeregistration: updated Msisdn list:" + deregisteredMsisdns.toString());
            this.mNSDSDatabaseHelper.updateRegistationStatusForLines(2, 0);
            broadcastLinesReadyStatusUpdated(deregisteredMsisdns, 2, 0);
            return;
        }
        IMSLog.i(this.LOG_TAG, "onDeregistration: abnormal deregistration, skip");
    }

    /* access modifiers changed from: private */
    public void scheduleNsdsAppFlowRetryIf(Intent intent) {
        List<Integer> errorCodes;
        int retryCount = intent.getIntExtra("retry_count", -1);
        int deviceEventType = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
        int errorCode = intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, -1);
        if (!intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false) && shouldRetry(errorCode, retryCount, deviceEventType)) {
            Bundle extras = new Bundle();
            extras.putInt("retry_count", retryCount + 1);
            extras.putInt(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, deviceEventType);
            if (NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED.equals(intent.getAction())) {
                Bundle extras2 = intent.getExtras();
                boolean retry = true;
                if (!(extras2 == null || (errorCodes = extras2.getIntegerArrayList(NSDSNamespaces.NSDSExtras.ERROR_CODES)) == null || !errorCodes.contains(1400))) {
                    retry = false;
                }
                String str = this.LOG_TAG;
                IMSLog.i(str, "Retry sim device implicit activation:" + retry);
                if (retry) {
                    IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SIM_DEVICE_ACTIVATION, extras2, 30000);
                }
            } else if (NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED.equals(intent.getAction())) {
                extras.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
                extras.putInt("phoneId", this.mSimManager.getSimSlotIndex());
                if (errorCode == -1) {
                    IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK, extras, RETRY_INTERVAL_UNKNOWN_ERROR);
                    IMSLog.i(this.LOG_TAG, "retry entitlement check after 5 seconds");
                    return;
                }
                IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_ENTITLEMENT_CHECK, extras, RETRY_INTERVAL_HTTP_ERROR);
                IMSLog.i(this.LOG_TAG, "retry entitlement check after 30 minutes");
            }
        }
    }

    private boolean shouldRetry(int errorCode, int retryCount, int deviceEventType) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "shouldRetry: errorCode " + errorCode + " retryCount " + retryCount + " deviceEventType " + deviceEventType);
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null && mnoStrategy.requireRetryBootupProcedure()) {
            if (retryCount > 4) {
                String str2 = this.LOG_TAG;
                IMSLog.i(str2, "shouldRetry: exceeded max retry " + retryCount);
                if (mnoStrategy.shouldRecoverStoredEntitlement()) {
                    broadcastStoredEntitlement();
                }
                return false;
            } else if (errorCode == -1 || errorCode >= 1001) {
                String str3 = this.LOG_TAG;
                IMSLog.i(str3, "shouldRetry: NSDS error, retry " + retryCount);
                return true;
            } else if (errorCode == 486 || errorCode == 408 || errorCode == 500 || errorCode == 503 || errorCode == 480) {
                String str4 = this.LOG_TAG;
                IMSLog.i(str4, "shouldRetry: HTTP error, retry " + retryCount);
                return true;
            }
        }
        return false;
    }

    private void retryPdnConnection(int retryCount, int retryTimer) {
        IMSLog.i(this.LOG_TAG, "XCAP PDN is not connected.");
        sendMessage(obtainMessage(50));
        if (retryCount > 7) {
            IMSLog.i(this.LOG_TAG, "Too many retries.. Give up retry pdn.");
        } else {
            sendMessageDelayed(obtainMessage(46, retryCount + 1, retryTimer * 2), (long) retryTimer);
        }
    }

    public void dump() {
        String str = this.LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(this.LOG_TAG);
        this.mEventLog.dump();
        this.mBaseFlowImpl.getNSDSClient().getResponseHandler().dump();
        IMSLog.decreaseIndent(this.LOG_TAG);
    }

    public void handleMessage(Message msg) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "handleMesasge: event " + msg.what);
        int i = msg.what;
        if (i == 0) {
            onRegistration((ImsRegistration) msg.obj);
        } else if (i == 1) {
            onDeregistration((ImsRegistration) msg.obj);
        } else if (i == 3) {
            performSimDeviceImplicitActivation(msg.arg1, msg.arg2);
        } else if (i == 4) {
            performSimDeviceDeactivationFlow(((Integer) msg.obj).intValue());
        } else if (i == 6) {
            updateDeviceName();
        } else if (i == 24) {
            retryPdnConnection(msg.arg1, msg.arg2);
        } else if (i == 14) {
            refreshDeviceConfigIf(((Integer) msg.obj).intValue());
        } else if (i != 15) {
            switch (i) {
                case 17:
                    performRemovePushToken(((Integer) msg.obj).intValue());
                    return;
                case 18:
                    onFlightMode(((Integer) msg.obj).intValue());
                    return;
                case 19:
                    performE911AddressUpdate(((Integer) msg.obj).intValue());
                    return;
                case 20:
                case 22:
                    break;
                case 21:
                    updatePushTokenInEntitlementServer();
                    return;
                default:
                    switch (i) {
                        case 40:
                            handleSimSwapEvent("Forced");
                            return;
                        case 41:
                            performProceduresOnConfigRefreshComplete();
                            return;
                        case 42:
                            enableOrDisableNSDSService();
                            return;
                        case 43:
                            scheduleGetGcmRegistrationTokenIfTokenNotSent();
                            return;
                        case 44:
                            refreshEntitlementAndE911Info(1, 0);
                            return;
                        case 45:
                            break;
                        case 46:
                            startPdnConnectivity(msg.arg1, msg.arg2);
                            return;
                        default:
                            switch (i) {
                                case 49:
                                    performAkaTokenRetrievalFlow(msg.arg1, msg.arg2);
                                    return;
                                case 50:
                                    stopPdnConnectivity();
                                    return;
                                case 51:
                                    refreshEntitlementAndE911InfoAutoOn(1, 0);
                                    return;
                                default:
                                    return;
                            }
                    }
            }
            performBootupProcedures();
        } else {
            performEntitlementCheck(msg.arg1, msg.arg2);
        }
    }
}
