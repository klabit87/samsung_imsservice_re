package com.sec.internal.ims.servicemodules.options;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;
import com.sec.internal.ims.servicemodules.options.ContactCache;
import com.sec.internal.ims.servicemodules.presence.PresenceModule;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CapabilityDiscoveryModule extends ServiceModuleBase implements ICapabilityDiscoveryModule {
    private static final int CONTACT_CHANGED_DELAY = 500;
    private static final int EVT_EXCHANGE_CAPABILITIES_FOR_VSH_DELAY = 500;
    private static final long LAST_SEEN_ACTIVE = 0;
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityDiscModule";
    private static final String NAME = CapabilityDiscoveryModule.class.getSimpleName();
    private static final int SET_OWN_CAPABILITIES_DELAY = 500;
    private static final int SET_OWN_CAPABILITIES_DELAY_ON_REG = 100;
    private boolean forcePollingGuard;
    private boolean isOfflineAddedContact;
    private ImsUri mActiveCallRemoteUri;
    /* access modifiers changed from: private */
    public int mAvailablePhoneId;
    protected Handler mBackgroundHandler;
    private String[] mCallNumber;
    final Map<Integer, CapabilitiesCache> mCapabilitiesMapList = new HashMap();
    CapabilityEventListener mCapabilityEventListener;
    private CapabilityExchange mCapabilityExchange;
    protected CapabilityForIncall mCapabilityForIncall;
    private boolean mCapabilityModuleOn = true;
    private CapabilityQuery mCapabilityQuery;
    private CapabilityRegistration mCapabilityRegistration;
    CapabilityServiceEventListener mCapabilityServiceEventListener;
    private CapabilityUpdate mCapabilityUpdate;
    private CapabilityUtil mCapabilityUtil;
    private PhoneIdKeyMap<CapabilityConfig> mConfigs;
    ContactCache mContactList;
    private final ContactCache.ContactEventListener mContactListener = new ContactCache.ContactEventListener() {
        public final void onChanged() {
            CapabilityDiscoveryModule.this.lambda$new$0$CapabilityDiscoveryModule();
        }
    };
    Context mContext;
    PhoneIdKeyMap<ICapabilityExchangeControl> mControl;
    private SimpleEventLog mEventLog;
    private Map<Integer, Boolean> mHasVideoOwn = new HashMap();
    private IImModule mImModule;
    private Map<Integer, ImsRegistration> mImsRegInfoList = new HashMap();
    private boolean mInitialQuery = true;
    private Map<Integer, Boolean> mIsConfigured;
    private Map<Integer, Boolean> mIsConfiguredOnCapability;
    /* access modifiers changed from: private */
    public boolean mIsInCall;
    private Map<Integer, CapabilityConstants.CapExResult> mLastCapExResult;
    private long mLastListSubscribeStamp = -1;
    private long mLastPollTimestamp = -1;
    private int mNetworkClass;
    private NetworkEvent mNetworkEvent;
    private int mNetworkType;
    private Map<Integer, Long> mOldFeature;
    OptionsModule mOptionsModule;
    private Map<Integer, Boolean> mOptionsSwitchOnList = new HashMap();
    /* access modifiers changed from: private */
    public Map<Integer, Capabilities> mOwnList = new HashMap();
    private final PhoneStateListenerForCapability mPhoneStateListener;
    private final List<Date> mPollingHistory = new ArrayList();
    protected PendingIntent mPollingIntent = null;
    PresenceModule mPresenceModule;
    private Map<Integer, Boolean> mPresenceSwitchOnList = new HashMap();
    private Map<Integer, String> mRcsProfile = new HashMap();
    IRegistrationManager mRegMan;
    private int mRetrySyncContactCount;
    private final ServiceAvailabilityEventListenerWrapper mServiceAvailabilityEventListenerWrapper;
    List<ServiceTuple> mServiceTupleList = new ArrayList();
    private SimEventListener mSimEventListener = null;
    private final TelephonyManager mTelephonyManager;
    protected PendingIntent mThrottledIntent = null;
    private UriGenerator mUriGenerator = null;
    Map<Integer, List<ImsUri>> mUrisToRequestList = new HashMap();
    private PhoneIdKeyMap<Long> mUserLastActive;

    public /* synthetic */ void lambda$new$0$CapabilityDiscoveryModule() {
        removeMessages(2);
        sendMessageDelayed(obtainMessage(2), 500);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CapabilityDiscoveryModule(Looper looper, Context context, OptionsModule option, PresenceModule presence, IRegistrationManager rm, IImModule imModule) {
        super(looper);
        Context context2 = context;
        IRegistrationManager iRegistrationManager = rm;
        boolean z = false;
        this.mIsInCall = false;
        this.mIsConfigured = new HashMap();
        this.mIsConfiguredOnCapability = new HashMap();
        this.mCallNumber = new String[]{null, null};
        this.mLastCapExResult = new HashMap();
        this.mBackgroundHandler = null;
        this.mNetworkEvent = null;
        this.mNetworkClass = 0;
        this.mActiveCallRemoteUri = null;
        this.mNetworkType = 0;
        this.mAvailablePhoneId = 0;
        this.isOfflineAddedContact = false;
        this.mOldFeature = new HashMap();
        this.forcePollingGuard = false;
        this.mRetrySyncContactCount = 0;
        this.mContext = context2;
        this.mRegMan = iRegistrationManager;
        this.mEventLog = new SimpleEventLog(context2, LOG_TAG, 100);
        int phoneCount = SimUtil.getPhoneCount();
        int phoneId = 0;
        while (phoneId < phoneCount) {
            this.mUrisToRequestList.put(Integer.valueOf(phoneId), new ArrayList());
            this.mOptionsSwitchOnList.put(Integer.valueOf(phoneId), true);
            this.mPresenceSwitchOnList.put(Integer.valueOf(phoneId), true);
            this.mOwnList.put(Integer.valueOf(phoneId), new Capabilities());
            this.mRcsProfile.put(Integer.valueOf(phoneId), "");
            this.mHasVideoOwn.put(Integer.valueOf(phoneId), z);
            this.mIsConfigured.put(Integer.valueOf(phoneId), z);
            this.mIsConfiguredOnCapability.put(Integer.valueOf(phoneId), z);
            this.mLastCapExResult.put(Integer.valueOf(phoneId), CapabilityConstants.CapExResult.SUCCESS);
            this.mOldFeature.put(Integer.valueOf(phoneId), Long.valueOf((long) Capabilities.FEATURE_NOT_UPDATED));
            phoneId++;
            z = z;
        }
        initContactCache(phoneCount);
        this.mOptionsModule = option;
        this.mPresenceModule = presence;
        this.mCapabilityEventListener = new CapabilityEventListener(this);
        this.mServiceAvailabilityEventListenerWrapper = new ServiceAvailabilityEventListenerWrapper(this);
        this.mCapabilityServiceEventListener = new CapabilityServiceEventListener();
        this.mImModule = imModule;
        if (!IMSLog.isShipBuild()) {
            Preconditions.checkState(this.mImModule != null, "Shall not happen! Wrong order of modules instantiation in ServiceModuleManager");
        } else if (this.mImModule == null) {
            Log.e(LOG_TAG, "Shall not happen! Wrong order of modules instantiation in ServiceModuleManager");
        }
        CapabilityIntentReceiver intentReceiver = new CapabilityIntentReceiver(this);
        this.mContext.registerReceiver(intentReceiver, intentReceiver.getIntentFilter());
        this.mPhoneStateListener = new PhoneStateListenerForCapability();
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        this.mTelephonyManager = telephonyManager;
        telephonyManager.listen(this.mPhoneStateListener, 96);
        if (this.mUriGenerator == null) {
            UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get();
            this.mUriGenerator = uriGenerator;
            this.mContactList.setUriGenerator(uriGenerator);
        }
        if (phoneCount > 1) {
            SimManagerFactory.registerForDDSChange(this, 15, (Object) null);
        }
        this.mConfigs = new PhoneIdKeyMap<>(phoneCount, null);
        this.mControl = new PhoneIdKeyMap<>(phoneCount, null);
        this.mUserLastActive = new PhoneIdKeyMap<>(phoneCount, -1L);
        this.mCapabilityUtil = new CapabilityUtil(this, this.mEventLog);
        this.mCapabilityForIncall = new CapabilityForIncall(this, this.mCapabilityUtil, iRegistrationManager);
        CapabilityExchange capabilityExchange = new CapabilityExchange(this, this.mCapabilityUtil, this.mEventLog);
        this.mCapabilityExchange = capabilityExchange;
        this.mCapabilityQuery = new CapabilityQuery(this, this.mCapabilityUtil, capabilityExchange);
        this.mCapabilityUpdate = new CapabilityUpdate(this, this.mCapabilityUtil, iRegistrationManager, this.mEventLog);
        this.mCapabilityRegistration = new CapabilityRegistration(this, this.mCapabilityUtil, iRegistrationManager);
        IMSLog.i(LOG_TAG, "created");
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"options", "presence", "lastseen"};
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        this.mCapabilityUtil.onServiceSwitched(phoneId, switchStatus, this.mPresenceSwitchOnList, this.mOptionsSwitchOnList, this.mCapabilityModuleOn);
    }

    public void init() {
        IMSLog.i(LOG_TAG, "init");
        super.init();
        HandlerThread thread = new HandlerThread("BackgroundHandler", 10);
        thread.start();
        this.mBackgroundHandler = new Handler(thread.getLooper());
        loadCapabilityStorage();
        this.mLastPollTimestamp = loadPollTimestamp();
    }

    public void onConfigured(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onConfigured");
        processConfigured(phoneId);
    }

    private void processConfigured(int phoneId) {
        post(new Runnable(phoneId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                CapabilityDiscoveryModule.this.lambda$processConfigured$1$CapabilityDiscoveryModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$processConfigured$1$CapabilityDiscoveryModule(int phoneId) {
        int defaultPhoneId = SimUtil.getDefaultPhoneId();
        this.mAvailablePhoneId = defaultPhoneId;
        if (defaultPhoneId == -1) {
            this.mAvailablePhoneId = 0;
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, phoneId)) {
            IMSLog.i(LOG_TAG, phoneId, "omit not data sim onConfigured!");
            return;
        }
        try {
            this.mConfigs.put(phoneId, new CapabilityConfig(this.mContext, phoneId));
            loadConfig(phoneId);
            if (!this.mContactList.isReady() && this.mConfigs.get(phoneId) != null && !this.mConfigs.get(phoneId).isDisableInitialScan()) {
                IMSLog.i(LOG_TAG, phoneId, "onConfigured: start ContactCache");
                if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, phoneId)) {
                    IMSLog.i(LOG_TAG, phoneId, "onConfigured: ignore to start ContactCache because of opposite sim");
                } else if (this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
                    syncContact();
                }
            }
            if (this.mConfigs.get(phoneId) == null || !this.mConfigs.get(phoneId).usePresence()) {
                this.mControl.put(phoneId, this.mOptionsModule);
            } else {
                this.mControl.put(phoneId, this.mPresenceModule);
                Capabilities ownCap = this.mOwnList.get(Integer.valueOf(phoneId));
                ownCap.addFeature((long) Capabilities.FEATURE_PRESENCE_DISCOVERY);
                this.mOwnList.put(Integer.valueOf(phoneId), ownCap);
            }
            this.mRcsProfile.put(Integer.valueOf(phoneId), this.mConfigs.get(phoneId) != null ? this.mConfigs.get(phoneId).getRcsProfile() : "");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        loadThirdPartyServiceTuples(phoneId);
        onImsSettingsUpdate(phoneId);
        sendMessage(obtainMessage(9, phoneId, 0, (Object) null));
        sendMessage(obtainMessage(52, phoneId, 0));
    }

    /* access modifiers changed from: package-private */
    public void loadConfig(int phoneId) {
        if (this.mConfigs.get(phoneId) == null) {
            IMSLog.s(LOG_TAG, phoneId, "Config not ready");
        } else {
            this.mConfigs.get(phoneId).load();
        }
    }

    public void start() {
        IMSLog.i(LOG_TAG, "start");
        if (this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
            super.start();
            this.mContactList.registerListener(this.mContactListener);
            this.mContactList.start();
            this.mOptionsModule.registerCapabilityEventListener(this.mCapabilityEventListener);
            this.mPresenceModule.registerCapabilityEventListener(this.mCapabilityEventListener);
            registerSimCardEventListener();
            updateMsgAppInfo(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMsgAppInfo(boolean initialize) {
        try {
            String msgAppPkgName = PackageUtils.getMsgAppPkgName(this.mContext);
            ApplicationInfo info = this.mContext.getPackageManager().getApplicationInfo(msgAppPkgName, 128);
            String msgAppXbotVer = info.metaData.getString("Xbot.Version");
            if (msgAppXbotVer == null) {
                IMSLog.i(LOG_TAG, "Xbot.Version" + " is null");
                msgAppXbotVer = Float.toString(info.metaData.getFloat("Xbot.Version"));
            }
            ServiceTuple xbotMsgTuple = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
            ServiceTuple chatbotTuple = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_CHAT_SESSION);
            IMSLog.i(LOG_TAG, "updateMsgAppInfo: msgAppPkgName:" + msgAppPkgName + "cur:" + xbotMsgTuple + ", new:" + msgAppXbotVer);
            if (!TextUtils.equals(msgAppXbotVer, xbotMsgTuple.version)) {
                ServiceTuple.setServiceVersion(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG, msgAppXbotVer);
                if (!initialize) {
                    for (Map.Entry<Integer, ImsRegistration> entry : this.mImsRegInfoList.entrySet()) {
                        int phoneId = entry.getKey().intValue();
                        if (entry.getValue().hasRcsService()) {
                            PresenceInfo ownPresenceInfo = this.mPresenceModule.getOwnPresenceInfo(phoneId);
                            if (ownPresenceInfo.getServiceTuple(chatbotTuple.serviceId) == null) {
                                IMSLog.i(LOG_TAG, phoneId, "updateMsgAppInfo: chatbot not registered");
                            } else if (ownPresenceInfo.getServiceTuple(xbotMsgTuple.serviceId) == null || xbotMsgTuple.version.equals("0.0")) {
                                this.mOldFeature.put(Integer.valueOf(phoneId), Long.valueOf(this.mOwnList.get(Integer.valueOf(phoneId)).getFeature()));
                                IMSLog.i(LOG_TAG, phoneId, "updateMsgAppInfo: update REGISTER");
                                this.mImModule.updateExtendedBotMsgFeature(phoneId);
                                this.mRegMan.sendReRegister(phoneId, entry.getValue().getNetworkType());
                            } else {
                                IMSLog.i(LOG_TAG, phoneId, "updateMsgAppInfo: re PUBLISH");
                                ownPresenceInfo.removeService(xbotMsgTuple);
                                xbotMsgTuple.version = msgAppXbotVer;
                                ownPresenceInfo.addService(xbotMsgTuple);
                                this.mPresenceModule.removeMessages(1);
                                this.mPresenceModule.sendEmptyMessage(1);
                            }
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "error retrieving msgapp(" + "" + ") details");
        }
    }

    public void onPackageUpdated(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            IMSLog.i(LOG_TAG, "onPackageUpdated: invalid packageName");
        } else {
            sendEmptyMessage(40);
        }
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
        this.mCapabilityUtil.onNetworkChanged(this.mContext, event, phoneId, this.mAvailablePhoneId, this.mImsRegInfoList, this.mNetworkEvent, this.mNetworkClass);
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        ImsRegistration imsRegistration = regiInfo;
        this.mCapabilityRegistration.onRegistered(this.mContext, imsRegistration, this.mImsRegInfoList, this.mLastCapExResult.get(Integer.valueOf(regiInfo.getPhoneId())), this.mOldFeature.get(Integer.valueOf(regiInfo.getPhoneId())).longValue());
    }

    public void stop() {
        super.stop();
        processStop();
    }

    private void processStop() {
        post(new Runnable() {
            public final void run() {
                CapabilityDiscoveryModule.this.lambda$processStop$2$CapabilityDiscoveryModule();
            }
        });
    }

    public /* synthetic */ void lambda$processStop$2$CapabilityDiscoveryModule() {
        Log.i(LOG_TAG, "processStop");
        stopPollingTimer();
        if (this.mCapabilityUtil.isCapabilityDiscoveryDisabled(this.mContext, this.mDefaultPhoneId)) {
            savePollTimestamp(LAST_SEEN_ACTIVE);
        }
        this.mImsRegInfoList.clear();
        for (Map.Entry<Integer, Capabilities> e : this.mOwnList.entrySet()) {
            Integer phoneId = e.getKey();
            Capabilities ownCap = this.mOwnList.get(phoneId);
            if (ownCap.isAvailable()) {
                ownCap.setAvailiable(false);
                this.mOwnList.put(phoneId, ownCap);
                notifyOwnCapabilitiesChanged(phoneId.intValue());
            }
        }
        this.mContactList.stop();
        this.mContactList.unregisterListener(this.mContactListener);
        deregisterSimCardEventListener();
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        this.mCapabilityRegistration.onDeregistered(regiInfo, this.mImsRegInfoList);
    }

    public void onDeregistering(ImsRegistration reg) {
        super.onDeregistering(reg);
        this.mCapabilityRegistration.onDeregistering(reg, this.mImsRegInfoList);
    }

    public void onSimChanged(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onSimChanged: clear cache and init poll timer");
        clearCapabilitiesCache(phoneId);
        this.mInitialQuery = true;
        if (!isRunning()) {
            IMSLog.i(LOG_TAG, phoneId, "onSimChanged: isRunning() is false.");
        } else if (this.mControl.get(phoneId) != null && this.mControl.get(phoneId).isReadyToRequest(phoneId)) {
            sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
        }
    }

    public void onCallStateChanged(int phoneId, List<ICall> calls) {
        if (this.mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
            this.mCapabilityForIncall.processCallStateChanged(phoneId, new CopyOnWriteArrayList(calls), this.mImsRegInfoList);
        } else {
            this.mCapabilityForIncall.processCallStateChangedOnDeregi(phoneId, new CopyOnWriteArrayList(calls));
        }
    }

    public void setCallNumber(int phoneId, String CallNumber) {
        this.mCallNumber[phoneId] = CallNumber;
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int phoneId) {
        this.mCapabilityUpdate.onOwnCapabilitiesChanged(phoneId);
    }

    public void updateOwnCapabilities(int phoneId) {
        this.mCapabilityUpdate.updateOwnCapabilities(this.mContext, this.mImsRegInfoList, phoneId, this.mIsConfiguredOnCapability.get(Integer.valueOf(phoneId)).booleanValue(), this.mNetworkType);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.i(LOG_TAG, "handleMessage: evt " + msg.what);
        if (!isRunning() && msg.what != 15) {
            Log.i(LOG_TAG, "CapabilityDiscoveryModule disabled.");
        } else if (!CapabilityEvent.handleEvent(msg, this, this.mCapabilityUtil, this.mServiceAvailabilityEventListenerWrapper, this.mAvailablePhoneId)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + msg.what);
        }
    }

    public void handleModuleChannelRequest(Message msg) {
        int i = msg.what;
        if (i == 8001) {
            enableFeature(((Long) msg.obj).longValue(), false);
            sendModuleResponse(msg, 1, (Object) null);
        } else if (i == 8002) {
            disableFeature(((Long) msg.obj).longValue(), false);
            sendModuleResponse(msg, 1, (Object) null);
        }
    }

    public void handleIntent(Intent intent) {
    }

    public void onImsConifgChanged(int phoneId, String dmUri) {
        IMSLog.i(LOG_TAG, phoneId, "onChange: config changed : " + dmUri);
        removeMessages(7, Integer.valueOf(phoneId));
        sendMessageDelayed(obtainMessage(7, Integer.valueOf(phoneId)), 600);
    }

    public void registerListener(ICapabilityServiceEventListener listener, int phoneId) {
        this.mCapabilityServiceEventListener.registerListener(listener, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(ICapabilityServiceEventListener listener, int phoneId) {
        this.mCapabilityServiceEventListener.unregisterListener(listener, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void enableFeature(long feature, boolean forced) {
        Log.i(LOG_TAG, "enableFeature: forced " + forced + " feature " + Capabilities.dumpFeature(feature));
        for (Map.Entry<Integer, Capabilities> entry : this.mOwnList.entrySet()) {
            int phoneId = entry.getKey().intValue();
            Capabilities ownCap = this.mOwnList.get(Integer.valueOf(phoneId));
            ownCap.addFeature(feature);
            this.mOwnList.put(Integer.valueOf(phoneId), ownCap);
            if (isRunning() && forced) {
                removeMessages(5, Integer.valueOf(phoneId));
                sendMessageDelayed(obtainMessage(5, 1, 0, Integer.valueOf(phoneId)), 500);
            }
        }
    }

    private void disableFeature(long feature, boolean forced) {
        Log.i(LOG_TAG, "disableFeature: forced " + forced + " feature " + Capabilities.dumpFeature(feature));
        for (Map.Entry<Integer, Capabilities> entry : this.mOwnList.entrySet()) {
            int phoneId = entry.getKey().intValue();
            Capabilities ownCap = this.mOwnList.get(Integer.valueOf(phoneId));
            ownCap.removeFeature(feature);
            this.mOwnList.put(Integer.valueOf(phoneId), ownCap);
            if (isRunning() && forced) {
                removeMessages(5, Integer.valueOf(phoneId));
                sendMessageDelayed(obtainMessage(5, 1, 0, Integer.valueOf(phoneId)), 500);
            }
        }
    }

    public Capabilities getCapabilities(int id, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(id, phoneId);
    }

    public Capabilities getCapabilities(String number, CapabilityRefreshType refreshType, boolean lazyQuery, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(number, refreshType, lazyQuery, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public Capabilities getCapabilities(String number, long features, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(number, features, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public Capabilities getCapabilities(ImsUri uri, long features, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(uri, features, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public Capabilities[] getCapabilities(List<ImsUri> uris, CapabilityRefreshType refreshType, long features, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(uris, refreshType, features, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public Capabilities getCapabilities(ImsUri uri, CapabilityRefreshType refreshType, int phoneId) {
        return this.mCapabilityQuery.getCapabilities(uri, refreshType, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public Capabilities[] getCapabilitiesByContactId(String contactId, CapabilityRefreshType refreshType, int phoneId) {
        return this.mCapabilityQuery.getCapabilitiesByContactId(contactId, refreshType, phoneId, this.mRcsProfile.get(Integer.valueOf(phoneId)));
    }

    public void exchangeCapabilities(String number, long myFeatures, int phoneId, String extFeature) {
        this.mCapabilityExchange.exchangeCapabilities(this.mImsRegInfoList, this.mRegMan, number, myFeatures, phoneId, extFeature, this.mCallNumber[phoneId]);
    }

    public void exchangeCapabilitiesForVSH(int phoneId, boolean enable) {
        this.mCapabilityForIncall.exchangeCapabilitiesForVSH(phoneId, enable, this.mImsRegInfoList);
    }

    /* access modifiers changed from: package-private */
    public void ddsChangedCheckRcsSwitch() {
        int phoneId = SimUtil.getDefaultPhoneId();
        if (this.mDefaultPhoneId == phoneId) {
            Log.i(LOG_TAG, "Current default phoneId = " + this.mDefaultPhoneId);
            return;
        }
        this.mDefaultPhoneId = phoneId;
        this.mAvailablePhoneId = phoneId;
        if (phoneId == -1) {
            this.mAvailablePhoneId = 0;
        }
        if (!this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
            stop();
        } else if (isReady()) {
            start();
        }
    }

    /* access modifiers changed from: package-private */
    public void setUserActive(boolean isActive, int phoneId) {
        Log.i(LOG_TAG, "IPC successful user activity" + isActive);
        if (isActive) {
            this.mUserLastActive.put(phoneId, Long.valueOf(LAST_SEEN_ACTIVE));
        } else {
            this.mUserLastActive.put(phoneId, Long.valueOf(System.currentTimeMillis()));
        }
        saveUserLastActiveTimeStamp(System.currentTimeMillis(), phoneId);
        Log.i(LOG_TAG, "IPC successful user activity: " + this.mUserLastActive);
    }

    /* access modifiers changed from: package-private */
    public void addFakeCapabilityInfo(List<ImsUri> uris, boolean feature, int phoneId) {
        if (uris != null) {
            IMSLog.s(LOG_TAG, "addFakeCapabilityInfo: uri " + uris.toString());
            Bundle b = new Bundle();
            b.putParcelableArrayList("URIS", new ArrayList(uris));
            b.putLong("FEATURES", feature ? Capabilities.FEATURE_ALL : (long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            b.putInt("PHONEID", phoneId);
            sendMessage(obtainMessage(4, CapabilityConstants.CapExResult.SUCCESS.ordinal(), -1, b));
        }
    }

    public void clearCapabilitiesCache(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "clearCapabilitiesCache");
        synchronized (this.mCapabilitiesMapList) {
            this.mCapabilitiesMapList.get(Integer.valueOf(phoneId)).clear();
        }
        savePollTimestamp(LAST_SEEN_ACTIVE);
        this.mContactList.resetRefreshTime();
    }

    public void changeParalysed(boolean mode, int phoneId) {
        this.mCapabilityUtil.changeParalysed(mode, phoneId);
    }

    public ImsUri getNetworkPreferredUri(ImsUri uri) {
        return this.mCapabilityUtil.getNetworkPreferredUri(uri);
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getAllCapabilities(int phoneId) {
        return this.mCapabilityQuery.getAllCapabilities(phoneId);
    }

    public Capabilities getOwnCapabilitiesBase(int phoneId) {
        return this.mCapabilityQuery.getOwnCapabilitiesBase(phoneId, this.mOwnList.get(Integer.valueOf(phoneId)));
    }

    public Capabilities getOwnCapabilities() {
        return getOwnCapabilities(this.mDefaultPhoneId);
    }

    public Capabilities getOwnCapabilities(int phoneId) {
        return this.mCapabilityQuery.getOwnCapabilities(phoneId, this.mAvailablePhoneId, this.mImsRegInfoList, this.mRegMan, this.mNetworkType, this.mIsInCall, this.mCallNumber[phoneId], this.mOwnList.get(Integer.valueOf(phoneId)));
    }

    public void setOwnCapabilities(int phoneId, boolean notifyToRm) {
        this.mCapabilityUpdate.setOwnCapabilities(this.mContext, phoneId, notifyToRm, this.mImsRegInfoList, this.mNetworkType, this.mIsInCall, this.mCallNumber[phoneId]);
    }

    /* access modifiers changed from: package-private */
    public void prepareResponse(List<ImsUri> uris, long availFeatures, String txId, int phoneId, String extDestFeature) {
        this.mCapabilityUpdate.prepareResponse(this.mContext, uris, availFeatures, txId, phoneId, extDestFeature, this.mImsRegInfoList, this.mNetworkType, this.mCallNumber[phoneId]);
    }

    public boolean hasVideoOwnCapability(int phoneId) {
        return this.mHasVideoOwn.get(Integer.valueOf(phoneId)).booleanValue();
    }

    public ContactCache getPhonebook() {
        return this.mContactList;
    }

    public CapabilitiesCache getCapabilitiesCache() {
        return getCapabilitiesCache(this.mAvailablePhoneId);
    }

    public CapabilitiesCache getCapabilitiesCache(int phoneId) {
        CapabilitiesCache capabilitiesCache;
        synchronized (this.mCapabilitiesMapList) {
            capabilitiesCache = this.mCapabilitiesMapList.get(Integer.valueOf(phoneId));
        }
        return capabilitiesCache;
    }

    /* access modifiers changed from: package-private */
    public CapabilityConfig getCapabilityConfig(int phoneId) {
        return this.mConfigs.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public ICapabilityExchangeControl getCapabilityControl(int phoneId) {
        return this.mControl.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void putCapabilityControlForOptionsModule(int phoneId, OptionsModule options) {
        this.mControl.put(phoneId, options);
    }

    public UriGenerator getUriGenerator() {
        return this.mUriGenerator;
    }

    public PresenceModule getPresenceModule() {
        return this.mPresenceModule;
    }

    /* access modifiers changed from: package-private */
    public void onContactChanged(boolean initial) {
        this.mCapabilityUpdate.onContactChanged(initial, this.mAvailablePhoneId, this.isOfflineAddedContact, this.mLastListSubscribeStamp);
    }

    public int requestCapabilityExchange(List<ImsUri> uris, CapabilityConstants.RequestType requestType, int phoneId) {
        return this.mCapabilityExchange.requestCapabilityExchange(uris, requestType, phoneId);
    }

    public boolean requestCapabilityExchange(ImsUri uri, CapabilityConstants.RequestType type, boolean isAlwaysForce, int phoneId) {
        return this.mCapabilityExchange.requestCapabilityExchange(uri, type, isAlwaysForce, phoneId, this.mOwnList.get(Integer.valueOf(phoneId)), this.mRegMan, this.mImsRegInfoList, this.mCallNumber[phoneId], this.mNetworkType);
    }

    /* access modifiers changed from: package-private */
    public boolean updatePollList(ImsUri uri, boolean needAdd, int phoneId) {
        return this.mCapabilityExchange.updatePollList(uri, needAdd, phoneId);
    }

    /* access modifiers changed from: package-private */
    public boolean isPollingInProgress(Date current, int phoneId) {
        return this.mCapabilityUpdate.isPollingInProgress(current, phoneId, this.mPollingHistory);
    }

    /* access modifiers changed from: package-private */
    public void requestInitialCapabilitiesQuery(int phoneId) {
        this.mCapabilityExchange.requestInitialCapabilitiesQuery(phoneId, this.mInitialQuery, this.mLastPollTimestamp);
    }

    /* access modifiers changed from: package-private */
    public void startPoll(Date current, int phoneId) {
        long delay = (this.mLastPollTimestamp + (((long) this.mConfigs.get(phoneId).getPollingPeriod()) * 1000)) - current.getTime();
        if (delay > LAST_SEEN_ACTIVE) {
            sendMessage(obtainMessage(1, false));
            startPollingTimer(delay);
            return;
        }
        sendMessage(obtainMessage(1, true));
    }

    public void poll(boolean isPeriodic, int phoneId) {
        this.mCapabilityExchange.poll(this.mContext, isPeriodic, phoneId, this.mImsRegInfoList, this.mPollingHistory);
    }

    public boolean setLegacyLatching(ImsUri uri, boolean isLatching, int phoneId) {
        return this.mCapabilityUpdate.setLegacyLatching(this.mContext, uri, isLatching, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onUpdateCapabilities(List<ImsUri> uris, long availFeatures, CapabilityConstants.CapExResult result, String pidf, int lastSeen, List<ImsUri> paidList, int phoneId, boolean isTokenUsed, String extFeature) {
        this.mCapabilityUpdate.onUpdateCapabilities(uris, availFeatures, result, pidf, lastSeen, paidList, phoneId, isTokenUsed, extFeature, this.mCallNumber[phoneId]);
    }

    /* access modifiers changed from: package-private */
    public void notifyOwnCapabilitiesChanged(int phoneId) {
        this.mBackgroundHandler.post(new Runnable(phoneId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                CapabilityDiscoveryModule.this.lambda$notifyOwnCapabilitiesChanged$3$CapabilityDiscoveryModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyOwnCapabilitiesChanged$3$CapabilityDiscoveryModule(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "notifyOwnCapabilitiesChanged:");
        this.mCapabilityServiceEventListener.notifyOwnCapabilitiesChanged(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void notifyCapabilitiesChanged(List<ImsUri> uris, Capabilities capex, int phoneId) {
        this.mBackgroundHandler.post(new Runnable(phoneId, uris, capex) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ List f$2;
            public final /* synthetic */ Capabilities f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                CapabilityDiscoveryModule.this.lambda$notifyCapabilitiesChanged$4$CapabilityDiscoveryModule(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$notifyCapabilitiesChanged$4$CapabilityDiscoveryModule(int phoneId, List uris, Capabilities capex) {
        IMSLog.i(LOG_TAG, phoneId, "notifyCapabilitiesChanged:");
        String[] strArr = this.mCallNumber;
        if (strArr[phoneId] != null) {
            this.mActiveCallRemoteUri = UriUtil.parseNumber(strArr[phoneId]);
        }
        this.mCapabilityServiceEventListener.notifyCapabilitiesChanged(uris, capex, this.mActiveCallRemoteUri, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void startPollingTimer(int phoneId) {
        long delay = ((long) this.mConfigs.get(phoneId).getPollingPeriod()) * 1000;
        if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_RAND_DELAY_PERIODIC_POLL)) {
            delay = this.mCapabilityUtil.getRandomizedDelayForPeriodicPolling(phoneId, delay);
        }
        if (delay != LAST_SEEN_ACTIVE) {
            startPollingTimer(delay);
        }
    }

    /* access modifiers changed from: package-private */
    public void startPollingTimer(long millis) {
        Log.i(LOG_TAG, "startPollingTimer: millis " + millis);
        if (this.mPollingIntent != null) {
            stopPollingTimer();
        }
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.sec.internal.ims.servicemodules.options.poll_timeout"), 134217728);
        this.mPollingIntent = broadcast;
        AlarmTimer.start(this.mContext, broadcast, millis);
    }

    /* access modifiers changed from: package-private */
    public void stopPollingTimer() {
        Log.i(LOG_TAG, "stopPollingTimer");
        PendingIntent pendingIntent = this.mPollingIntent;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mPollingIntent = null;
        }
    }

    private long loadPollTimestamp() {
        long lastPollTimestamp = this.mContext.getSharedPreferences("capdiscovery", 0).getLong("pollTimestamp", LAST_SEEN_ACTIVE);
        if (lastPollTimestamp <= new Date().getTime()) {
            return lastPollTimestamp;
        }
        Log.i(LOG_TAG, "loadPollTimestamp: abnormal case, clear lastPollTime " + lastPollTimestamp + " to 0");
        savePollTimestamp(LAST_SEEN_ACTIVE);
        return LAST_SEEN_ACTIVE;
    }

    /* access modifiers changed from: package-private */
    public void savePollTimestamp(long timestamp) {
        SharedPreferences sp = this.mContext.getSharedPreferences("capdiscovery", 0);
        this.mLastPollTimestamp = timestamp;
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("pollTimestamp", timestamp);
        editor.apply();
    }

    private void saveUserLastActiveTimeStamp(long timestamp, int phoneId) {
        SharedPreferences sp = this.mContext.getSharedPreferences("capdiscovery", 0);
        IMSLog.i(LOG_TAG, phoneId, "save last seen active");
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("lastseenactive_" + SimManagerFactory.getImsiFromPhoneId(phoneId), timestamp);
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public void onImsSettingsUpdate(int phoneId) {
        this.mCapabilityUtil.onImsSettingsUpdate(this.mContext, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void syncContact() {
        Log.i(LOG_TAG, "syncContact:");
        ISimManager sm = SimManagerFactory.getSimManager();
        if (sm == null || !sm.isSimLoaded()) {
            Log.i(LOG_TAG, "syncContact: sim is not loaded.");
            this.mContactList.setIsBlockedContactChange(true);
            return;
        }
        _syncContact(sm.getSimMno());
    }

    /* access modifiers changed from: package-private */
    public void _syncContact(Mno mno) {
        this.mCapabilityUpdate._syncContact(mno);
    }

    public boolean isConfigured(int phoneId) {
        return this.mIsConfigured.get(Integer.valueOf(phoneId)).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public boolean isOwnInfoPublished() {
        boolean isPublished = this.mPresenceModule.isOwnCapPublished();
        Log.i(LOG_TAG, "isOwnInfoPublished: " + isPublished);
        return isPublished;
    }

    /* access modifiers changed from: package-private */
    public void registerService(String serviceId, String version) {
        Log.i(LOG_TAG, "registerService: called for vzw api layer");
        if (this.mControl.get(this.mDefaultPhoneId) == null) {
            Log.i(LOG_TAG, "registerService: adding service tuple to list");
            ServiceTuple st = ServiceTuple.getServiceTuple(serviceId, version, (String[]) null);
            synchronized (this.mServiceTupleList) {
                this.mServiceTupleList.add(st);
            }
        } else if (this.mControl.get(this.mDefaultPhoneId) == this.mPresenceModule) {
            Log.i(LOG_TAG, "registerService: calling presence module api");
            this.mControl.get(this.mDefaultPhoneId).registerService(serviceId, version, this.mDefaultPhoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void deRegisterService(List<String> serviceIdList) {
        Log.i(LOG_TAG, "deRegisterService: called for vzw api layer");
        if (this.mControl.get(this.mDefaultPhoneId) != null && this.mControl.get(this.mDefaultPhoneId) == this.mPresenceModule) {
            this.mControl.get(this.mDefaultPhoneId).deRegisterService(serviceIdList, this.mDefaultPhoneId);
        }
    }

    private void loadThirdPartyServiceTuples(int phoneId) {
        PresenceModule presenceModule;
        Log.i(LOG_TAG, "loadThirdPartyServiceTuples");
        if (this.mControl.get(phoneId) != null && this.mControl.get(phoneId) == (presenceModule = this.mPresenceModule)) {
            presenceModule.loadThirdPartyServiceTuples(this.mServiceTupleList);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyEABServiceAdvertiseResult(int errorCode, int phoneId) {
        Log.i(LOG_TAG, "notifyEABServiceAdvertiseResult: error[" + errorCode + "]");
        this.mCapabilityServiceEventListener.notifyEABServiceAdvertiseResult(errorCode, phoneId);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        this.mContactList.dump();
        IMSLog.dump(LOG_TAG, "mLastPollTimestamp: " + new Date(this.mLastPollTimestamp));
        this.mEventLog.dump();
        for (CapabilityConfig config : this.mConfigs.values()) {
            if (config != null) {
                IMSLog.dump(LOG_TAG, config.toString());
            }
        }
        try {
            getCapabilitiesCache().dump();
        } catch (NullPointerException e) {
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }

    private final class PhoneStateListenerForCapability extends PhoneStateListener {
        int mNetworkType;

        private PhoneStateListenerForCapability() {
            this.mNetworkType = 0;
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i(CapabilityDiscoveryModule.LOG_TAG, "onCallStateChanged: " + state);
            if (!CapabilityDiscoveryModule.this.mIsInCall && state != 0) {
                boolean unused = CapabilityDiscoveryModule.this.mIsInCall = true;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mDefaultPhoneId) && incomingNumber != null) {
                    fetchCapabilities(CapabilityDiscoveryModule.this.mDefaultPhoneId);
                }
            } else if (CapabilityDiscoveryModule.this.mIsInCall && state == 0) {
                boolean unused2 = CapabilityDiscoveryModule.this.mIsInCall = false;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mDefaultPhoneId) && incomingNumber != null) {
                    fetchCapabilities(CapabilityDiscoveryModule.this.mDefaultPhoneId);
                }
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            CapabilityDiscoveryModule.this.post(new Runnable(networkType, state) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    CapabilityDiscoveryModule.PhoneStateListenerForCapability.this.lambda$onDataConnectionStateChanged$0$CapabilityDiscoveryModule$PhoneStateListenerForCapability(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onDataConnectionStateChanged$0$CapabilityDiscoveryModule$PhoneStateListenerForCapability(int networkType, int state) {
            if (networkType != this.mNetworkType) {
                this.mNetworkType = networkType;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mDefaultPhoneId) && CapabilityDiscoveryModule.this.mIsInCall) {
                    Log.i(CapabilityDiscoveryModule.LOG_TAG, "onDataConnectionStateChanged(): state=" + state + ", networkType=" + networkType);
                    fetchCapabilities(CapabilityDiscoveryModule.this.mDefaultPhoneId);
                }
            }
        }

        private boolean hasCshFeature(int phoneId) {
            return ((Capabilities) CapabilityDiscoveryModule.this.mOwnList.get(Integer.valueOf(phoneId))).hasFeature(Capabilities.FEATURE_ISH) || ((Capabilities) CapabilityDiscoveryModule.this.mOwnList.get(Integer.valueOf(phoneId))).hasFeature(Capabilities.FEATURE_VSH);
        }

        private void fetchCapabilities(int phoneId) {
            CapabilityDiscoveryModule.this.post(new Runnable(phoneId) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    CapabilityDiscoveryModule.PhoneStateListenerForCapability.this.lambda$fetchCapabilities$1$CapabilityDiscoveryModule$PhoneStateListenerForCapability(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$fetchCapabilities$1$CapabilityDiscoveryModule$PhoneStateListenerForCapability(int phoneId) {
            CapabilityDiscoveryModule.this.updateOwnCapabilities(phoneId);
            CapabilityDiscoveryModule.this.setOwnCapabilities(phoneId, true);
        }
    }

    private class SimEventListener implements ISimEventListener {
        private SimEventListener() {
        }

        public void onReady(int phoneId, boolean absent) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            Mno mno = sm == null ? Mno.DEFAULT : sm.getSimMno();
            String imsi = sm == null ? "" : sm.getImsi();
            SharedPreferences pref = CapabilityDiscoveryModule.this.mContext.getSharedPreferences("capdiscovery", 0);
            SharedPreferences.Editor editor = pref.edit();
            if (!TextUtils.equals(pref.getString("imsi", ""), imsi) && mno == Mno.TMOUS) {
                Log.i(CapabilityDiscoveryModule.LOG_TAG, "imsi changed");
                CapabilityDiscoveryModule.this.clearCapabilitiesCache(phoneId);
                editor.putString("imsi", imsi);
            }
            IMSLog.s(CapabilityDiscoveryModule.LOG_TAG, phoneId, "SimEventListener,onReady,EVT_SYNC_CONTACT");
            editor.apply();
            int unused = CapabilityDiscoveryModule.this.mAvailablePhoneId = SimUtil.getDefaultPhoneId();
            if (CapabilityDiscoveryModule.this.mAvailablePhoneId == -1) {
                int unused2 = CapabilityDiscoveryModule.this.mAvailablePhoneId = 0;
            }
            if (!RcsUtils.DualRcs.isDualRcsReg() || CapabilityDiscoveryModule.this.mAvailablePhoneId == phoneId) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = CapabilityDiscoveryModule.this;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(10, mno), 3000);
                return;
            }
            IMSLog.s(CapabilityDiscoveryModule.LOG_TAG, phoneId, "SimEventListener : contact sync of opposite sim is blocked.");
        }
    }

    public void exchangeCapabilitiesForVSHOnRegi(boolean enable, int phoneId) {
        sendMessageDelayed(obtainMessage(14, phoneId, 0, Boolean.valueOf(enable)), 500);
    }

    /* access modifiers changed from: package-private */
    public void triggerCapexForIncallRegiDeregi(int phoneId, ImsRegistration regiInfo) {
        this.mCapabilityForIncall.triggerCapexForIncallRegiDeregi(phoneId, regiInfo);
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        if (this.mCapabilityUtil.isPhoneLockState(this.mContext)) {
            Log.i(LOG_TAG, "onBootCompleted : not required sync contact");
            return;
        }
        Log.i(LOG_TAG, "onBootCompleted: try sync contact");
        this.mRetrySyncContactCount = 0;
        sendMessage(obtainMessage(13));
    }

    /* access modifiers changed from: package-private */
    public void onRetrySyncContact() {
        this.mCapabilityUpdate.onRetrySyncContact(this.mRetrySyncContactCount);
    }

    /* access modifiers changed from: package-private */
    public void handleDelayedSetOwnCapabilities(int phoneId) {
        Log.i(LOG_TAG, "handleMessage: EVT_DELAYED_SET_OWN_CAPABILITIES");
        if (this.mPresenceModule.getRegiInfoUpdater(phoneId)) {
            removeMessages(5, Integer.valueOf(phoneId));
            sendMessage(obtainMessage(5, 0, 0, Integer.valueOf(phoneId)));
            this.mPresenceModule.setRegiInfoUpdater(phoneId, false);
            return;
        }
        Log.i(LOG_TAG, "EVT_DELAYED_SET_OWN_CAPABILITIES : Delayed for a while");
        sendMessageDelayed(obtainMessage(53, 0, 0, Integer.valueOf(phoneId)), 100);
    }

    /* access modifiers changed from: package-private */
    public void deleteNonRcsDataFromContactDB(int phoneId) {
        this.mEventLog.logAndAdd(phoneId, "deleteNonRcsDataFromContactDB");
        getCapabilitiesCache(phoneId).deleteNonRcsDataFromContactDB();
    }

    private void initContactCache(int phoneCount) {
        synchronized (this.mCapabilitiesMapList) {
            for (int i = 0; i < phoneCount; i++) {
                this.mCapabilitiesMapList.put(Integer.valueOf(i), new CapabilitiesCache(this.mContext, i));
            }
            this.mContactList = new ContactCache(this.mContext, this.mCapabilitiesMapList);
        }
    }

    private void loadCapabilityStorage() {
        synchronized (this.mCapabilitiesMapList) {
            for (Map.Entry<Integer, CapabilitiesCache> e : this.mCapabilitiesMapList.entrySet()) {
                this.mCapabilitiesMapList.get(e.getKey()).loadCapabilityStorage();
            }
        }
    }

    private void registerSimCardEventListener() {
        if (this.mSimEventListener == null) {
            this.mSimEventListener = new SimEventListener();
            if (RcsUtils.DualRcs.isDualRcsReg()) {
                for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                    sm.registerSimCardEventListener(this.mSimEventListener);
                }
                return;
            }
            SimManagerFactory.getSimManager().registerSimCardEventListener(this.mSimEventListener);
        }
    }

    private void deregisterSimCardEventListener() {
        if (this.mSimEventListener == null) {
            return;
        }
        if (RcsUtils.DualRcs.isDualRcsReg()) {
            for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                sm.deRegisterSimCardEventListener(this.mSimEventListener);
            }
            return;
        }
        SimManagerFactory.getSimManager().deRegisterSimCardEventListener(this.mSimEventListener);
    }

    /* access modifiers changed from: package-private */
    public void setIsOfflineAddedContact(boolean value) {
        this.isOfflineAddedContact = value;
    }

    /* access modifiers changed from: package-private */
    public void setNetworkEvent(NetworkEvent value) {
        this.mNetworkEvent = value;
    }

    /* access modifiers changed from: package-private */
    public void setNetworkClass(int value) {
        this.mNetworkClass = value;
    }

    /* access modifiers changed from: package-private */
    public void setNetworkType(int value) {
        this.mNetworkType = value;
    }

    /* access modifiers changed from: package-private */
    public void setforcePollingGuard(boolean value) {
        this.forcePollingGuard = value;
    }

    /* access modifiers changed from: package-private */
    public void addPollingHistory(Date value) {
        this.mPollingHistory.add(value);
    }

    /* access modifiers changed from: package-private */
    public void setLastListSubscribeStamp(long value) {
        this.mLastListSubscribeStamp = value;
    }

    /* access modifiers changed from: package-private */
    public void setInitialQuery(boolean value) {
        this.mInitialQuery = value;
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, Capabilities> getOwnList() {
        return this.mOwnList;
    }

    /* access modifiers changed from: package-private */
    public void putOwnList(int phoneId, Capabilities ownCap) {
        this.mOwnList.put(Integer.valueOf(phoneId), ownCap);
    }

    /* access modifiers changed from: package-private */
    public void setIsConfigured(boolean value, int phoneId) {
        this.mIsConfigured.put(Integer.valueOf(phoneId), Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void setIsConfiguredOnCapability(boolean value, int phoneId) {
        this.mIsConfiguredOnCapability.put(Integer.valueOf(phoneId), Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void putUrisToRequestList(int phoneId, List<ImsUri> urisToRequest) {
        this.mUrisToRequestList.put(Integer.valueOf(phoneId), urisToRequest);
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, List<ImsUri>> getUrisToRequest() {
        return this.mUrisToRequestList;
    }

    /* access modifiers changed from: package-private */
    public OptionsModule getOptionsModule() {
        return this.mOptionsModule;
    }

    /* access modifiers changed from: package-private */
    public boolean getForcePollingGuard() {
        return this.forcePollingGuard;
    }

    /* access modifiers changed from: package-private */
    public void setHasVideoOwnCapability(boolean value, int phoneId) {
        this.mHasVideoOwn.put(Integer.valueOf(phoneId), Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getThrottledIntent() {
        return this.mThrottledIntent;
    }

    /* access modifiers changed from: package-private */
    public void setThrottledIntent(PendingIntent intent) {
        this.mThrottledIntent = intent;
    }

    /* access modifiers changed from: package-private */
    public void setLastCapExResult(CapabilityConstants.CapExResult value, int phoneId) {
        this.mLastCapExResult.put(Integer.valueOf(phoneId), value);
    }

    /* access modifiers changed from: package-private */
    public void setUriGenerator(UriGenerator value) {
        this.mUriGenerator = value;
    }

    /* access modifiers changed from: package-private */
    public void setOldFeature(long value, int phoneId) {
        this.mOldFeature.put(Integer.valueOf(phoneId), Long.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public int getDefaultPhoneId() {
        return this.mDefaultPhoneId;
    }

    /* access modifiers changed from: package-private */
    public void setAvailablePhoneId(int value) {
        this.mAvailablePhoneId = value;
    }

    /* access modifiers changed from: package-private */
    public long getUserLastActive(int phoneId) {
        return this.mUserLastActive.get(phoneId).longValue();
    }

    /* access modifiers changed from: package-private */
    public void putUserLastActive(int phoneId, long value) {
        this.mUserLastActive.put(phoneId, Long.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void removeImsRegInfoList(int phoneId) {
        this.mImsRegInfoList.remove(Integer.valueOf(phoneId));
    }

    /* access modifiers changed from: package-private */
    public void setImsRegInfoList(int phoneId, ImsRegistration imsRegInfo) {
        this.mImsRegInfoList.put(Integer.valueOf(phoneId), imsRegInfo);
    }

    /* access modifiers changed from: package-private */
    public void setRetrySyncContactCount(int value) {
        this.mRetrySyncContactCount = value;
    }

    /* access modifiers changed from: package-private */
    public void settOptionsSwitch(int phoneId, boolean value) {
        this.mOptionsSwitchOnList.put(Integer.valueOf(phoneId), Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void setPresenceSwitch(int phoneId, boolean value) {
        this.mPresenceSwitchOnList.put(Integer.valueOf(phoneId), Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void setCapabilityModuleOn(boolean value) {
        this.mCapabilityModuleOn = value;
    }
}
