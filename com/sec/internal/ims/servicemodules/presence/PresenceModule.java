package com.sec.internal.ims.servicemodules.presence;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.constants.ims.servicemodules.presence.PublishResponse;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PresenceModule extends ServiceModuleBase implements IPresenceModule, ICapabilityExchangeControl {
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT = 5000;
    private static final String LOG_TAG = "PresenceModule";
    static final String NAME = PresenceModule.class.getSimpleName();
    ICapabilityDiscoveryModule mCapabilityDiscovery = null;
    Context mContext = null;
    private SimpleEventLog mEventLog;
    ICapabilityEventListener mListener = null;
    protected Handler mModuleHandler = null;
    private int mPhoneCount = 0;
    private PresenceCacheController mPresenceCacheController;
    private PhoneIdKeyMap<PresenceConfig> mPresenceConfig;
    private PhoneIdKeyMap<PresenceModuleInfo> mPresenceModuleInfo;
    private Map<Integer, Boolean> mPresenceRegiInfoUpdater = new HashMap();
    private PresenceSharedPrefHelper mPresenceSp;
    private final RegistrantList mPublishRegistrants = new RegistrantList();
    IPresenceStackInterface mService;
    private List<ServiceTuple> mServiceTupleList = new ArrayList();
    private Map<String, PendingIntent> mSubscribeRetryList = new HashMap();
    UriGenerator mUriGenerator = null;
    private List<ImsUri> mUriToSubscribe = new ArrayList();
    PowerManager.WakeLock mWakeLock;

    static class PresenceModuleInfo {
        long mBackupPublishTimestamp = -1;
        PendingIntent mBadEventIntent;
        boolean mBadEventProgress = false;
        boolean mFirstPublish = true;
        long mLastBadEventTimestamp = -1;
        long mLastPublishTimestamp = -1;
        PresenceResponse.PresenceStatusCode mLastSubscribeStatusCode = PresenceResponse.PresenceStatusCode.NONE;
        boolean mLimitImmediateRetry;
        boolean mLimitReRegistration;
        Mno mMno = Mno.DEFAULT;
        PresenceResponse.PresenceFailureReason mOldPublishError;
        boolean mOwnInfoPublished;
        PresenceInfo mOwnPresenceInfo;
        boolean mParalysed;
        PendingIntent mPollingIntent;
        SocialPresenceCache mPresenceCache;
        int mPublishExpBackOffRetryCount;
        int mPublishNoResponseCount;
        int mPublishNotProvisionedCount;
        int mPublishRequestTimeout;
        ImsRegistration mRegInfo;
        PendingIntent mRetryPublishIntent;
        ISimManager mSimCardManager;
        boolean ongoingPublishErrRetry;

        PresenceModuleInfo() {
        }
    }

    public PresenceModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, NAME, 20);
        this.mPhoneCount = SimManagerFactory.getAllSimManagers().size();
        this.mPresenceSp = new PresenceSharedPrefHelper(this.mContext, this);
        this.mPresenceCacheController = new PresenceCacheController(this);
        this.mPresenceModuleInfo = new PhoneIdKeyMap<>(this.mPhoneCount, new PresenceModuleInfo());
        this.mPresenceConfig = new PhoneIdKeyMap<>(this.mPhoneCount, null);
        PresenceIntentReceiver presenceReceiver = new PresenceIntentReceiver(this);
        this.mContext.registerReceiver(PresenceIntentReceiver.mIntentReceiver, presenceReceiver.getIntentFilter());
        this.mContext.registerReceiver(PresenceIntentReceiver.mSubscribeRetryIntentReceiver, presenceReceiver.getSubscribeRetryIntentFilter());
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            PresenceModuleInfo presInfo = new PresenceModuleInfo();
            presInfo.mSimCardManager = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            presInfo.mOwnPresenceInfo = new PresenceInfo(phoneId);
            presInfo.mPresenceCache = new SocialPresenceCache(this.mContext, phoneId);
            this.mPresenceModuleInfo.put(phoneId, presInfo);
            this.mPresenceConfig.put(phoneId, new PresenceConfig(this.mContext, phoneId));
        }
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm != null) {
            PowerManager.WakeLock newWakeLock = pm.newWakeLock(1, LOG_TAG);
            this.mWakeLock = newWakeLock;
            newWakeLock.setReferenceCounted(false);
        }
        Log.i(LOG_TAG, "created");
    }

    public String[] getServicesRequiring() {
        return new String[]{"presence"};
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        IMSLog.i(LOG_TAG, phoneId, "onServiceSwitched:");
        updateFeatures(phoneId);
    }

    public void init() {
        super.init();
        Log.i(LOG_TAG, "init");
        IPresenceStackInterface presenceHandler = ImsRegistry.getHandlerFactory().getPresenceHandler();
        this.mService = presenceHandler;
        presenceHandler.registerForPresenceInfo(this, 10, (Object) null);
        this.mService.registerForWatcherInfo(this, 12, (Object) null);
        this.mService.registerForPublishFailure(this, 2, (Object) null);
        this.mCapabilityDiscovery = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            this.mPresenceModuleInfo.get(phoneId).mLastPublishTimestamp = this.mPresenceSp.loadPublishTimestamp(phoneId);
            this.mPresenceModuleInfo.get(phoneId).mLastBadEventTimestamp = this.mPresenceSp.loadBadEventTimestamp(phoneId);
        }
        HandlerThread moduleThread = new HandlerThread(LOG_TAG);
        moduleThread.start();
        this.mModuleHandler = new Handler(moduleThread.getLooper());
    }

    public void start() {
        super.start();
        Log.i(LOG_TAG, "start:");
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            this.mPresenceModuleInfo.get(phoneId).mPublishNotProvisionedCount = 0;
        }
    }

    public void stop() {
        super.stop();
        Log.i(LOG_TAG, "stop:");
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            this.mPresenceModuleInfo.get(phoneId).mOwnInfoPublished = false;
            this.mPresenceModuleInfo.get(phoneId).mBackupPublishTimestamp = 0;
            this.mPresenceSp.savePublishTimestamp(0, phoneId);
            stopPublishTimer(phoneId);
            stopSubscribeRetryTimer(phoneId);
            resetPublishErrorHandling(phoneId);
            setParalysed(false, phoneId);
            if (this.mPresenceModuleInfo.get(phoneId).mRegInfo != null) {
                sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
            }
        }
    }

    public void onConfigured(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onConfigured:");
        processConfigured(phoneId);
    }

    private void processConfigured(int phoneId) {
        this.mModuleHandler.post(new Runnable(phoneId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PresenceModule.this.lambda$processConfigured$0$PresenceModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$processConfigured$0$PresenceModule(int phoneId) {
        ImsProfile profile = ImsRegistry.getRegistrationManager().getImsProfile(phoneId, ImsProfile.PROFILE_TYPE.RCS);
        if (profile == null || !profile.hasService("presence")) {
            IMSLog.i(LOG_TAG, phoneId, "processConfigured: no Presence support.");
            return;
        }
        PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(phoneId);
        presInfo.mMno = presInfo.mSimCardManager.getSimMno();
        IMSLog.i(LOG_TAG, phoneId, "onConfigured: mno = " + presInfo.mMno);
        readConfig(phoneId);
        updateFeatures(phoneId);
        this.mPresenceSp.checkAndClearPresencePreferences(presInfo.mSimCardManager.getImsi(), phoneId);
    }

    private void updateFeatures(int phoneId) {
        this.mEnabledFeatures[phoneId] = 0;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "presence", phoneId) == 1 && this.mPresenceConfig.get(phoneId).getDefaultDisc() != 2) {
            this.mEnabledFeatures[phoneId] = (long) Capabilities.FEATURE_PRESENCE_DISCOVERY;
            if (this.mPresenceConfig.get(phoneId).isSocialPresenceSupport()) {
                long[] jArr = this.mEnabledFeatures;
                jArr[phoneId] = jArr[phoneId] | ((long) Capabilities.FEATURE_SOCIAL_PRESENCE);
            }
        }
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        Log.i(LOG_TAG, "onRegistered:");
        processRegistered(regiInfo);
    }

    private void processRegistered(ImsRegistration regiInfo) {
        this.mModuleHandler.post(new Runnable(regiInfo) {
            public final /* synthetic */ ImsRegistration f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PresenceModule.this.lambda$processRegistered$1$PresenceModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$processRegistered$1$PresenceModule(ImsRegistration regiInfo) {
        int phoneId = regiInfo.getPhoneId();
        PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(phoneId);
        presInfo.mMno = presInfo.mSimCardManager.getSimMno();
        IMSLog.i(LOG_TAG, phoneId, "processRegistered: mno = " + presInfo.mMno);
        readConfig(phoneId);
        ImsProfile profile = regiInfo.getImsProfile();
        if (presInfo.mRegInfo == null) {
            presInfo.mOwnPresenceInfo.setPublishGzipEnabled(profile.isPublishGzipEnabled());
        }
        presInfo.mRegInfo = regiInfo;
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(regiInfo.getPhoneId()), true);
        IMSLog.i(LOG_TAG, phoneId, "processRegistered: profile " + profile.getName());
        List<NameAddr> impus = regiInfo.getImpuList();
        if (impus == null || impus.isEmpty()) {
            IMSLog.e(LOG_TAG, phoneId, "processRegistered: impus is empty !!!");
            return;
        }
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(impus.get(0).getUri());
        if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            for (NameAddr addr : regiInfo.getImpuList()) {
                if (addr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    this.mUriGenerator = UriGeneratorFactory.getInstance().get(addr.getUri());
                    return;
                }
            }
        }
    }

    public void onDeregistering(ImsRegistration regiInfo) {
        Log.i(LOG_TAG, "onDeregistering:");
        processDeregistering(regiInfo);
    }

    private void processDeregistering(ImsRegistration regiInfo) {
        this.mModuleHandler.post(new Runnable(regiInfo) {
            public final /* synthetic */ ImsRegistration f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PresenceModule.this.lambda$processDeregistering$2$PresenceModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$processDeregistering$2$PresenceModule(ImsRegistration regiInfo) {
        if (regiInfo != null) {
            Log.i(LOG_TAG, "processDeregistering:");
            if (!regiInfo.getImsProfile().hasEmergencySupport()) {
                removeMessages(1, Integer.valueOf(regiInfo.getPhoneId()));
                if (isRunning()) {
                    unpublish(regiInfo.getPhoneId());
                }
            }
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        Log.i(LOG_TAG, "onDeregistered:");
        processDeregistered(regiInfo);
    }

    private void processDeregistered(ImsRegistration regiInfo) {
        this.mModuleHandler.post(new Runnable(regiInfo) {
            public final /* synthetic */ ImsRegistration f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PresenceModule.this.lambda$processDeregistered$3$PresenceModule(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$processDeregistered$3$PresenceModule(ImsRegistration regiInfo) {
        ImsProfile profile = regiInfo.getImsProfile();
        int phoneId = regiInfo.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered: profile " + profile.getName());
        removeMessages(1, Integer.valueOf(phoneId));
        removeMessages(10, Integer.valueOf(phoneId));
        removeMessages(12, Integer.valueOf(phoneId));
        removeMessages(11, Integer.valueOf(phoneId));
        removeMessages(9, Integer.valueOf(phoneId));
        setParalysed(false, phoneId);
        PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(phoneId);
        presInfo.mRegInfo = null;
        if (presInfo.mMno == Mno.TMOUS) {
            presInfo.mOwnPresenceInfo.setPublishGzipEnabled(profile.isPublishGzipEnabled());
        }
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(regiInfo.getPhoneId()), false);
        this.mUriGenerator = null;
        this.mPresenceModuleInfo.get(phoneId).mOwnInfoPublished = false;
        resetPublishErrorHandling(phoneId);
    }

    public void onSimChanged(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onSimChanged:");
        this.mPresenceCacheController.clearPresenceInfo(phoneId);
        setBadEventProgress(false, phoneId);
        this.mPresenceSp.saveBadEventTimestamp(0, phoneId);
    }

    public boolean isReadyToRequest(int phoneId) {
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mnoStrategy != null) {
            return mnoStrategy.isPresenceReadyToRequest(this.mPresenceModuleInfo.get(phoneId).mOwnInfoPublished, getParalysed(phoneId));
        }
        IMSLog.i(LOG_TAG, phoneId, "isReadyToRequest: mnoStrategy null");
        return false;
    }

    public void setOwnCapabilities(long features, int phoneId) {
        long j = features;
        int i = phoneId;
        if (isRunning()) {
            IMSLog.i(LOG_TAG, i, "setOwnCapabilities: features " + Capabilities.dumpFeature(features));
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.add("OwnCapabilities - set, features = " + j);
            IMSLog.c(LogClass.PM_SET_OWNCAPA, i + ",SET:" + j);
            PresenceInfo newPresenceInfo = new PresenceInfo(i);
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
            if (mnoStrategy != null) {
                mnoStrategy.changeServiceDescription();
            }
            PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(i);
            List<ServiceTuple> list = ServiceTuple.getServiceTupleList(features);
            for (ServiceTuple s : list) {
                IMSLog.i(LOG_TAG, i, "setOwnCapabilities: " + s);
                ServiceTuple tuple = presInfo.mOwnPresenceInfo.getServiceTuple(s.serviceId);
                if (tuple != null) {
                    s.tupleId = tuple.tupleId;
                } else if (presInfo.mMno.isKor()) {
                    String savedId = this.mPresenceSp.loadRandomTupleId(s.feature, i);
                    if (savedId != null) {
                        s.tupleId = savedId;
                    } else {
                        s.tupleId = StringGenerator.generateString(5, 10);
                        this.mPresenceSp.saveRandomTupleId(s.feature, s.tupleId, i);
                    }
                } else {
                    s.tupleId = StringGenerator.generateString(5, 10);
                }
                long j2 = features;
            }
            newPresenceInfo.setPhoneId(i);
            newPresenceInfo.addService(list);
            newPresenceInfo.setPublishGzipEnabled(presInfo.mOwnPresenceInfo.getPublishGzipEnabled());
            presInfo.mOwnPresenceInfo = newPresenceInfo;
            buildPresenceInfoForThirdParty(i);
            if (presInfo.mRegInfo != null) {
                presInfo.mOwnPresenceInfo.setUri(((NameAddr) presInfo.mRegInfo.getImpuList().get(0)).getUri().toString());
                IMSLog.s(LOG_TAG, i, "setOwnCapabilities: uri" + presInfo.mOwnPresenceInfo.getUri());
                if (getParalysed(i)) {
                    IMSLog.i(LOG_TAG, i, "setOwnCapabilities: paralysed");
                    return;
                }
                if (presInfo.mRetryPublishIntent != null) {
                    if (!presInfo.ongoingPublishErrRetry) {
                        IMSLog.i(LOG_TAG, i, "setOwnCapabilities: retry timer is running");
                        return;
                    }
                    this.mPresenceConfig.get(i).setPublishErrRetry((long) presInfo.mRegInfo.getImsProfile().getPublishErrRetryTimer());
                    IMSLog.i(LOG_TAG, i, "initialize PublishErrRetry: " + this.mPresenceConfig.get(i).getPublishErrRetry());
                }
                if (presInfo.mMno == Mno.VZW) {
                    sendMessageDelayed(obtainMessage(1, Integer.valueOf(phoneId)), 500);
                } else {
                    sendMessage(obtainMessage(1, Integer.valueOf(phoneId)));
                }
            }
        }
    }

    public void registerCapabilityEventListener(ICapabilityEventListener listener) {
        this.mListener = listener;
    }

    public int requestCapabilityExchange(List<ImsUri> uris, CapabilityConstants.RequestType type, int phoneId) {
        int requested;
        IMSLog.i(LOG_TAG, phoneId, "requestCapabilityExchange: list requestType " + type);
        if (!isReadyToRequest(phoneId)) {
            IMSLog.e(LOG_TAG, phoneId, "requestCapabilityExchange: PUBLISH is not completed. bail.");
            return 0;
        } else if (!checkModuleReady(phoneId)) {
            return 0;
        } else {
            synchronized (this.mUriToSubscribe) {
                if (this.mPresenceConfig.get(phoneId).getMaxUri() - this.mUriToSubscribe.size() < uris.size()) {
                    requested = this.mPresenceConfig.get(phoneId).getMaxUri() - this.mUriToSubscribe.size();
                    this.mUriToSubscribe.addAll(uris.subList(0, requested));
                    uris.removeAll(this.mUriToSubscribe);
                } else {
                    this.mUriToSubscribe.addAll(uris);
                    requested = uris.size();
                    uris.clear();
                }
                this.mPresenceCacheController.loadPresenceStorage(this.mUriToSubscribe, phoneId);
            }
            acquireWakeLock();
            sendMessage(obtainMessage(7, phoneId, 0, type));
            return requested;
        }
    }

    public boolean requestCapabilityExchange(ImsUri uri, ICapabilityExchangeControl.ICapabilityExchangeCallback callback, CapabilityConstants.RequestType type, boolean isAlwaysForce, long myFeatures, int phoneId, String extFeature) {
        IMSLog.s(LOG_TAG, phoneId, "requestCapabilityExchange: uri " + uri);
        IMSLog.i(LOG_TAG, phoneId, "requestCapabilityExchange: requestType " + type + ", isAlwaysForce: " + isAlwaysForce);
        if (!isReadyToRequest(phoneId)) {
            IMSLog.e(LOG_TAG, phoneId, "requestCapabilityExchange: PUBLISH is not completed. bail.");
            return false;
        }
        boolean isKoreaRcsOperator = false;
        if (this.mPresenceModuleInfo.get(phoneId).mMno.isKor()) {
            isKoreaRcsOperator = true;
        }
        if (this.mPresenceConfig.get(phoneId).getRlsUri() == null || this.mPresenceConfig.get(phoneId).getRlsUri().getScheme() == null || isKoreaRcsOperator) {
            sendMessage(obtainMessage(5, new PresenceSubscriptionController.SubscriptionRequest(uri, type, isAlwaysForce, phoneId)));
        } else {
            IMSLog.i(LOG_TAG, phoneId, "requestCapabilityExchange: adding uri to RCS list");
        }
        if (callback == null) {
            return true;
        }
        callback.onComplete((Capabilities) null);
        return true;
    }

    private void startPublishTimer(int phoneId) {
        if (this.mPresenceModuleInfo.get(phoneId).mPollingIntent != null) {
            IMSLog.e(LOG_TAG, phoneId, "startPublishTimer: PublishTimer is already running. Stopping it.");
            stopPublishTimer(phoneId);
        }
        long delay = this.mPresenceConfig.get(phoneId).getPublishTimer();
        if (PresenceUtil.getExtendedPublishTimerCond(phoneId, this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo.getServiceList())) {
            delay = this.mPresenceConfig.get(phoneId).getPublishTimerExtended();
        }
        IMSLog.i(LOG_TAG, phoneId, "startPublishTimer: PublishTimer " + delay + " sec");
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.publish_timeout");
        intent.putExtra("sim_slot_id", phoneId);
        this.mPresenceModuleInfo.get(phoneId).mPollingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(phoneId).mPollingIntent, 1000 * delay);
    }

    private void stopPublishTimer(int phoneId) {
        if (this.mPresenceModuleInfo.get(phoneId).mPollingIntent == null) {
            IMSLog.e(LOG_TAG, phoneId, "stopPublishTimer: PublishTimer is not running.");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "stopPublishTimer:");
        AlarmTimer.stop(this.mContext, this.mPresenceModuleInfo.get(phoneId).mPollingIntent);
        this.mPresenceModuleInfo.get(phoneId).mPollingIntent = null;
    }

    public PresenceInfo getOwnPresenceInfo(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "getOwnPresenceInfo");
        return this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo;
    }

    public PresenceInfo getPresenceInfo(ImsUri uri, int phoneId) {
        return this.mPresenceCacheController.getPresenceInfo(uri, phoneId);
    }

    public PresenceInfo getPresenceInfoByContactId(String contactId, int phoneId) {
        return this.mPresenceCacheController.getPresenceInfoByContactId(contactId, this.mCapabilityDiscovery.getPhonebook().getNumberlistByContactId(contactId), phoneId);
    }

    public void subscribe(PresenceSubscriptionController.SubscriptionRequest request, boolean isAnonymousFetch) {
        PresenceSubscription s;
        int phoneId = request.phoneId;
        IMSLog.s(LOG_TAG, phoneId, "subscribe: uri " + request.uri);
        IMSLog.i(LOG_TAG, phoneId, "subscribe: request type " + request.type);
        if (checkModuleReady(phoneId)) {
            PresenceSubscription s2 = PresenceSubscriptionController.getSubscription(request.uri, true, phoneId);
            if (s2 == null) {
                PresenceSubscription s3 = new PresenceSubscription(StringIdGenerator.generateSubscriptionId());
                s3.addUri(request.uri);
                s3.setRequestType(request.type);
                s3.setPhoneId(phoneId);
                PresenceSubscriptionController.addSubscription(s3);
                s = s3;
            } else {
                if (RcsPolicyManager.getRcsStrategy(phoneId).isSubscribeThrottled(s2, this.mPresenceConfig.get(phoneId).getSourceThrottleSubscribe() * 1000, request.type == CapabilityConstants.RequestType.REQUEST_TYPE_NONE || request.type == CapabilityConstants.RequestType.REQUEST_TYPE_LAZY, request.isAlwaysForce)) {
                    IMSLog.i(LOG_TAG, phoneId, "subscribe: single fetch has been already sent");
                    IMSLog.s(LOG_TAG, phoneId, "subscribe: throttled uri " + request.uri);
                    return;
                }
                s2.updateState(0);
                s2.updateTimestamp();
                s2.setRequestType(request.type);
                s = s2;
            }
            long delay = RcsPolicyManager.getRcsStrategy(phoneId).calSubscribeDelayTime(s);
            if (delay > 0) {
                IMSLog.i(LOG_TAG, phoneId, "subscribe: delayed for " + delay);
                s.updateState(5);
                sendMessageDelayed(obtainMessage(5, request), delay);
                return;
            }
            if (request.type == CapabilityConstants.RequestType.REQUEST_TYPE_LAZY) {
                PresenceSubscriptionController.addLazySubscription(this.mUriGenerator.normalize(request.uri));
            }
            int i = phoneId;
            this.mService.subscribe(PresenceUtil.convertUriType(request.uri, this.mPresenceConfig.get(phoneId).useSipUri(), getPresenceInfo(request.uri, phoneId), this.mPresenceModuleInfo.get(phoneId).mMno, this.mUriGenerator, i), isAnonymousFetch, obtainMessage(6, s), s.getSubscriptionId(), i);
        }
    }

    private void subscribe(List<ImsUri> uris, boolean isAnonymousFetch, CapabilityConstants.RequestType type, int phoneId) {
        ImsUri imsUri;
        List<ImsUri> list = uris;
        CapabilityConstants.RequestType requestType = type;
        int i = phoneId;
        IMSLog.s(LOG_TAG, i, "subscribe: uri list " + list);
        IMSLog.i(LOG_TAG, i, "subscribe: request type " + requestType);
        if (requestType == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC) {
            removeMessages(8);
            stopSubscribeRetryTimer(i);
        }
        if (checkModuleReady(i)) {
            PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(i);
            List<ImsUri> urisToSubscribe = new ArrayList<>();
            List<ImsUri> urisNotToSubscribe = new ArrayList<>();
            for (ImsUri uri : uris) {
                if (PresenceSubscriptionController.hasSubscription(uri)) {
                    IMSLog.i(LOG_TAG, i, "subscribe: subscription has been already sent");
                    IMSLog.s(LOG_TAG, i, "subscribe: subscribed uri " + uri);
                    urisNotToSubscribe.add(uri);
                } else {
                    if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.LIST_SUB_URI_TRANSLATION)) {
                        imsUri = PresenceUtil.convertUriType(uri, this.mPresenceConfig.get(i).useSipUri(), getPresenceInfo(uri, i), presInfo.mMno, this.mUriGenerator, phoneId);
                    } else {
                        imsUri = uri;
                    }
                    urisToSubscribe.add(imsUri);
                }
            }
            if (urisNotToSubscribe.size() > 0) {
                list.removeAll(urisNotToSubscribe);
            }
            if (urisToSubscribe.size() == 0) {
                IMSLog.i(LOG_TAG, i, "subscribe: no URI to subscribe.");
                return;
            }
            PresenceSubscription s = new PresenceSubscription(StringIdGenerator.generateSubscriptionId());
            s.addUriAll(list);
            s.setExpiry(PresenceUtil.getPollListSubExp(this.mContext, i));
            s.setRequestType(requestType);
            s.setSingleFetch(false);
            s.setPhoneId(i);
            if (presInfo.mMno == Mno.TMOUS) {
                s.addDropUriAll(list);
            }
            uris.clear();
            PresenceSubscriptionController.addSubscription(s);
            if (presInfo.mRegInfo != null) {
                List<ImsUri> list2 = urisToSubscribe;
                boolean z = isAnonymousFetch;
                this.mService.subscribeList(list2, z, obtainMessage(6, s), s.getSubscriptionId(), presInfo.mRegInfo.getImsProfile().isGzipEnabled(), s.getExpiry(), phoneId);
            }
        }
    }

    public void publish(PresenceInfo info, int phoneId) {
        PresenceInfo presenceInfo = info;
        int i = phoneId;
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (!isRunning() || mnoStrategy == null || this.mPresenceModuleInfo.get(i).mRegInfo == null) {
            IMSLog.i(LOG_TAG, i, "publish: not ready to publish");
            if (this.mPresenceModuleInfo.get(i).mRetryPublishIntent != null) {
                stopRetryPublishTimer(i);
                return;
            }
            return;
        }
        IMSLog.s(LOG_TAG, i, "publish: " + presenceInfo);
        removeMessages(1, Integer.valueOf(phoneId));
        stopPublishTimer(i);
        stopRetryPublishTimer(i);
        long throttleRetry = mnoStrategy.calThrottledPublishRetryDelayTime(this.mPresenceModuleInfo.get(i).mLastPublishTimestamp, this.mPresenceConfig.get(i).getSourceThrottlePublish());
        if (throttleRetry > 0) {
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(phoneId)), throttleRetry);
            return;
        }
        if (this.mPresenceSp.checkIfValidEtag(i)) {
            IMSLog.i(LOG_TAG, i, "valid etag, setting to " + this.mPresenceSp.getPublishETag(i));
            presenceInfo.setEtag(this.mPresenceSp.getPublishETag(i));
        } else {
            IMSLog.i(LOG_TAG, i, "not valid etag");
            presenceInfo.setEtag((String) null);
        }
        this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.update(presenceInfo);
        Date current = new Date();
        if (this.mPresenceModuleInfo.get(i).mFirstPublish && this.mPresenceConfig.get(i).getBadEventExpiry() != 0) {
            long remainBadEventTimer = (this.mPresenceModuleInfo.get(i).mLastBadEventTimestamp + (this.mPresenceConfig.get(i).getBadEventExpiry() * 1000)) - current.getTime();
            if (remainBadEventTimer > 0) {
                IMSLog.i(LOG_TAG, i, "publish: restart BadEventTimer");
                startBadEventTimer(remainBadEventTimer, false, i);
            }
        }
        long retry = mnoStrategy.isTdelay(this.mPresenceConfig.get(i).getTdelayPublish());
        if (retry != 0) {
            IMSLog.i(LOG_TAG, i, "publish: retry after " + retry + "ms");
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(phoneId)), retry);
            return;
        }
        if (PresenceUtil.getExtendedPublishTimerCond(i, this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.getServiceList())) {
            this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.setExtendedTimerFlag(true);
            long j = retry;
            presenceInfo.setExpireTime(Math.max(info.getMinExpires(), this.mPresenceConfig.get(i).getPublishTimerExtended()));
            presenceInfo.setExtendedTimerFlag(true);
        } else {
            this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.setExtendedTimerFlag(false);
            presenceInfo.setExpireTime(Math.max(info.getMinExpires(), this.mPresenceConfig.get(i).getPublishTimer()));
            presenceInfo.setExtendedTimerFlag(false);
        }
        acquireWakeLock();
        setServiceVersion();
        this.mService.publish(presenceInfo, obtainMessage(2, presenceInfo), i);
        if (this.mPresenceModuleInfo.get(i).mFirstPublish) {
            this.mPresenceModuleInfo.get(i).mFirstPublish = false;
        }
        this.mEventLog.add("Publish - sent");
        IMSLog.c(LogClass.PM_PUB, i + ",PUB-SENT");
    }

    public void unpublish(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "unpublish: ");
        stopPublishTimer(phoneId);
        stopRetryPublishTimer(phoneId);
        removeMessages(3, Integer.valueOf(phoneId));
        ImsRegistration regInfo = this.mPresenceModuleInfo.get(phoneId).mRegInfo;
        PresenceModuleInfo moduleInfo = this.mPresenceModuleInfo.get(phoneId);
        if (regInfo != null && !PresenceUtil.isRegProhibited(regInfo, phoneId)) {
            this.mService.unpublish(phoneId);
        }
        if (moduleInfo.mMno.isKor()) {
            IMSLog.i(LOG_TAG, phoneId, "unpublish: remain etag for Kor");
            if (moduleInfo.mLastPublishTimestamp > 0) {
                moduleInfo.mBackupPublishTimestamp = moduleInfo.mLastPublishTimestamp;
            }
        } else if (moduleInfo.mMno != Mno.ATT) {
            this.mPresenceSp.resetPublishEtag(phoneId);
        } else if (regInfo != null) {
            if (!ImsRegistry.getRegistrationManager().isPdnConnected(regInfo.getImsProfile(), phoneId)) {
                IMSLog.i(LOG_TAG, phoneId, "unpublish: PDN already disconnected");
                if (moduleInfo.mLastPublishTimestamp > 0) {
                    moduleInfo.mBackupPublishTimestamp = moduleInfo.mLastPublishTimestamp;
                }
            } else {
                this.mPresenceSp.resetPublishEtag(phoneId);
            }
        }
        if (moduleInfo.mOwnInfoPublished) {
            this.mEventLog.add("UnPublish");
            IMSLog.c(LogClass.PM_UNPUB, phoneId + ",UNPUB");
        }
        moduleInfo.mOwnInfoPublished = false;
        if (!moduleInfo.mMno.isKor()) {
            this.mPresenceSp.savePublishTimestamp(0, phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public PresenceModuleInfo getPresenceModuleInfo(int phoneId) {
        return this.mPresenceModuleInfo.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public PresenceConfig getPresenceConfig(int phoneId) {
        return this.mPresenceConfig.get(phoneId);
    }

    /* access modifiers changed from: package-private */
    public UriGenerator getUriGenerator() {
        return this.mUriGenerator;
    }

    public void setParalysed(boolean mode, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "mParalysed: " + mode);
        this.mPresenceModuleInfo.get(phoneId).mParalysed = mode;
    }

    public boolean getParalysed(int phoneId) {
        return this.mPresenceModuleInfo.get(phoneId).mParalysed;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0179  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNewPresenceInformation(com.sec.ims.presence.PresenceInfo r19, int r20) {
        /*
            r18 = this;
            r0 = r18
            r7 = r19
            r15 = r20
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onNewPresenceInformation: uri "
            r1.append(r2)
            java.lang.String r2 = r19.getUri()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "PresenceModule"
            com.sec.internal.log.IMSLog.s(r2, r15, r1)
            boolean r1 = r0.checkModuleReady(r15)
            if (r1 != 0) goto L_0x0028
            return
        L_0x0028:
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.lang.String r4 = r19.getUri()
            if (r4 != 0) goto L_0x003d
            java.lang.String r4 = r19.getTelUri()
            goto L_0x0041
        L_0x003d:
            java.lang.String r4 = r19.getUri()
        L_0x0041:
            r16 = r4
            java.lang.String r4 = r19.getSubscriptionId()
            com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription r14 = com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController.getSubscription(r4, r15)
            if (r14 != 0) goto L_0x0054
            java.lang.String r4 = "onNewPresenceInformation: failed to fetch subscription"
            com.sec.internal.log.IMSLog.e(r2, r15, r4)
            return
        L_0x0054:
            java.util.List r4 = r19.getServiceList()
            long r4 = com.sec.ims.presence.ServiceTuple.getFeatures(r4)
            long r8 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_ROLE
            boolean r4 = com.sec.internal.ims.servicemodules.options.CapabilityUtil.hasFeature(r4, r8)
            if (r4 == 0) goto L_0x008f
            java.util.Set r4 = r14.getUriList()
            java.util.Iterator r4 = r4.iterator()
            java.lang.Object r4 = r4.next()
            com.sec.ims.util.ImsUri r4 = (com.sec.ims.util.ImsUri) r4
            com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator r5 = com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator.getInstance()
            java.lang.String r6 = r4.getMsisdn()
            java.lang.String r8 = r19.getUri()
            r5.register(r6, r8)
            java.lang.String r5 = r4.toString()
            r7.setUri(r5)
            java.lang.String r5 = r4.toString()
            r7.setTelUri(r5)
        L_0x008f:
            boolean r4 = r19.isFetchSuccess()
            r8 = 0
            if (r4 == 0) goto L_0x00d4
            com.sec.internal.ims.util.UriGenerator r4 = r0.mUriGenerator
            java.lang.String r5 = r19.getTelUri()
            com.sec.ims.util.ImsUri r5 = com.sec.ims.util.ImsUri.parse(r5)
            com.sec.ims.util.ImsUri r4 = r4.normalize(r5)
            r3.add(r4)
            java.lang.String r4 = r19.getUri()
            if (r4 != 0) goto L_0x00c6
            java.lang.Object r4 = r3.get(r8)
            com.sec.ims.util.ImsUri r4 = (com.sec.ims.util.ImsUri) r4
            com.sec.ims.presence.PresenceInfo r4 = r0.getPresenceInfo(r4, r15)
            if (r4 == 0) goto L_0x00c6
            java.lang.String r5 = r4.getUri()
            if (r5 == 0) goto L_0x00c6
            java.lang.String r5 = r4.getUri()
            r7.setUri(r5)
        L_0x00c6:
            com.sec.ims.util.ImsUri r4 = com.sec.ims.util.ImsUri.parse(r16)
            if (r4 == 0) goto L_0x00f2
            com.sec.ims.util.ImsUri r4 = com.sec.ims.util.ImsUri.parse(r16)
            r1.add(r4)
            goto L_0x00f2
        L_0x00d4:
            if (r16 == 0) goto L_0x00f6
            com.sec.ims.util.ImsUri r4 = com.sec.ims.util.ImsUri.parse(r16)
            if (r4 == 0) goto L_0x00f6
            com.sec.ims.util.ImsUri r4 = com.sec.ims.util.ImsUri.parse(r16)
            r1.add(r4)
            com.sec.internal.ims.util.UriGenerator r4 = r0.mUriGenerator
            java.lang.Object r5 = r1.get(r8)
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            com.sec.ims.util.ImsUri r4 = r4.normalize(r5)
            r3.add(r4)
        L_0x00f2:
            r17 = r1
            r13 = r3
            goto L_0x0104
        L_0x00f6:
            java.util.ArrayList r4 = new java.util.ArrayList
            java.util.Set r5 = r14.getUriList()
            r4.<init>(r5)
            r1 = r4
            r3 = r1
            r17 = r1
            r13 = r3
        L_0x0104:
            boolean r1 = r14.isSingleFetch()
            if (r1 != 0) goto L_0x0136
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r1 = r0.mPresenceModuleInfo
            int r3 = r14.getPhoneId()
            java.lang.Object r1 = r1.get(r3)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r1 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r1
            com.sec.internal.constants.Mno r1 = r1.mMno
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS
            if (r1 != r3) goto L_0x0136
            java.util.Iterator r1 = r13.iterator()
        L_0x0120:
            boolean r3 = r1.hasNext()
            if (r3 == 0) goto L_0x0136
            java.lang.Object r3 = r1.next()
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            boolean r4 = r14.containsDropUri(r3)
            if (r4 == 0) goto L_0x0135
            r14.removeDropUri(r3)
        L_0x0135:
            goto L_0x0120
        L_0x0136:
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r1 = r14.getRequestType()
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r3 = com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants.RequestType.REQUEST_TYPE_LAZY
            if (r1 != r3) goto L_0x0179
            java.lang.Object r1 = r13.get(r8)
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            boolean r3 = r14.isLongLivedSubscription()
            boolean r1 = com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController.checkLazySubscription(r1, r3)
            if (r1 == 0) goto L_0x0179
            java.lang.String r1 = "onNewPresenceInformation: lazy subscription not in order"
            com.sec.internal.log.IMSLog.i(r2, r15, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "onNewPresenceInformation: delayed uri "
            r1.append(r3)
            java.lang.Object r3 = r13.get(r8)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r2, r15, r1)
            r1 = 11
            android.os.Message r1 = r0.obtainMessage(r1, r15, r8, r7)
            r2 = 1000(0x3e8, double:4.94E-321)
            r0.sendMessageDelayed(r1, r2)
            return
        L_0x0179:
            com.sec.internal.ims.servicemodules.presence.PresenceCacheController r1 = r0.mPresenceCacheController
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r4 = r0.mCapabilityDiscovery
            com.sec.internal.ims.util.UriGenerator r5 = r0.mUriGenerator
            r2 = r17
            r3 = r19
            r6 = r20
            r1.updatePresenceDatabase(r2, r3, r4, r5, r6)
            java.util.List r1 = r19.getServiceList()
            long r1 = com.sec.ims.presence.ServiceTuple.getFeatures(r1)
            java.lang.Object r3 = r13.get(r8)
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r4 = r0.mPresenceModuleInfo
            int r5 = r14.getPhoneId()
            java.lang.Object r4 = r4.get(r5)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r4 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r4
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r4 = r4.mLastSubscribeStatusCode
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$CapExResult r3 = com.sec.internal.ims.servicemodules.presence.PresenceUtil.translateToCapExResult(r7, r3, r1, r4)
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener r8 = r0.mListener
            if (r8 == 0) goto L_0x01bc
            java.lang.String r4 = r19.getPidf()
            r9 = r13
            r10 = r1
            r12 = r3
            r5 = r13
            r13 = r4
            r4 = r14
            r14 = r20
            r8.onCapabilityUpdate(r9, r10, r12, r13, r14)
            goto L_0x01be
        L_0x01bc:
            r5 = r13
            r4 = r14
        L_0x01be:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceModule.onNewPresenceInformation(com.sec.ims.presence.PresenceInfo, int):void");
    }

    /* access modifiers changed from: package-private */
    public void onNewWatcherInformation(PresenceInfo info, int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "onNewWatcherInformation: uri " + info.getUri());
        if (this.mUriGenerator == null) {
            IMSLog.i(LOG_TAG, phoneId, "onNewWatcherInformation: mUriGenerator is null");
            return;
        }
        List<ImsUri> list = null;
        if (info.isFetchSuccess()) {
            list = new ArrayList<>();
            list.add(ImsUri.parse(info.getUri()));
        } else {
            PresenceSubscription sub = PresenceSubscriptionController.getSubscription(info.getSubscriptionId(), phoneId);
            if (sub != null) {
                list = new ArrayList<>(sub.getUriList());
            }
        }
        if (list != null) {
            this.mPresenceCacheController.updatePresenceDatabase(list, info, this.mCapabilityDiscovery, this.mUriGenerator, phoneId);
            ImsUri telUri = this.mUriGenerator.normalize(list.get(0));
            long features = ServiceTuple.getFeatures(info.getServiceList());
            CapabilityConstants.CapExResult result = PresenceUtil.translateToCapExResult(info, telUri, features, this.mPresenceModuleInfo.get(phoneId).mLastSubscribeStatusCode);
            ICapabilityEventListener iCapabilityEventListener = this.mListener;
            if (iCapabilityEventListener != null) {
                iCapabilityEventListener.onCapabilityUpdate(list, features, result, info.getPidf(), phoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPublishComplete(PresenceResponse response, int phoneId) {
        ICapabilityEventListener iCapabilityEventListener;
        if (response == null) {
            IMSLog.i(LOG_TAG, phoneId, "onPublishComplete: response is null");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "onPublishComplete: success " + response.isSuccess());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("Publish - completed, response = " + response.getSipError());
        IMSLog.c(LogClass.PM_ONPUB_COMP, phoneId + "," + response.getSipError());
        PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(phoneId);
        clearWakeLock();
        if (response.isSuccess()) {
            boolean isRefreshPublish = false;
            presInfo.mOwnInfoPublished = true;
            stopBadEventTimer(phoneId);
            setParalysed(false, phoneId);
            resetPublishErrorHandling(phoneId);
            if (response instanceof PublishResponse) {
                PublishResponse publishResponse = (PublishResponse) response;
                IMSLog.i(LOG_TAG, phoneId, "getEtag:" + publishResponse.getEtag() + " getExpiresTimer:" + publishResponse.getExpiresTimer());
                this.mPresenceSp.savePublishETag(publishResponse.getEtag(), publishResponse.getExpiresTimer(), phoneId);
                isRefreshPublish = publishResponse.isRefresh();
                this.mPresenceSp.savePublishTimestamp(System.currentTimeMillis(), phoneId);
                IMSLog.i(LOG_TAG, phoneId, "onPublishComplete(), isRefresh : " + isRefreshPublish);
            }
            presInfo.mPublishNotProvisionedCount = 0;
            presInfo.mPublishExpBackOffRetryCount = 0;
            presInfo.mPublishRequestTimeout = 0;
            presInfo.mPublishNoResponseCount = 0;
            if (presInfo.mMno == Mno.VZW) {
                this.mPublishRegistrants.notifyResult(false);
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            }
            if (RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(phoneId)) {
                sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
            } else {
                if (!isRefreshPublish && (iCapabilityEventListener = this.mListener) != null) {
                    iCapabilityEventListener.onMediaReady(response.isSuccess(), true, phoneId);
                }
                if (!PresenceSubscriptionController.getPendingSubscription().isEmpty()) {
                    IMSLog.i(LOG_TAG, phoneId, "onPublishComplete, pending subscription");
                    for (PresenceSubscription obtainMessage : PresenceSubscriptionController.getPendingSubscription()) {
                        sendMessage(obtainMessage(8, obtainMessage));
                    }
                    PresenceSubscriptionController.clearPendingSubscription();
                }
            }
            if (!ImsProfile.isRcsUpProfile(this.mPresenceConfig.get(phoneId).getRcsProfile()) || presInfo.mMno == Mno.VZW) {
                IMSLog.i(LOG_TAG, phoneId, "onPublishComplete,start PublishTimer: " + this.mPresenceConfig.get(phoneId).getPublishTimer());
                startPublishTimer(phoneId);
            }
        } else if (response instanceof PublishResponse) {
            onPublishFailed((PublishResponse) response, phoneId);
        }
        if (this.mListener != null) {
            onEABPublishComplete(response);
        }
        ContentValues cv = new ContentValues();
        if (response.isSuccess()) {
            cv.put(DiagnosisConstants.DRCS_KEY_RCPC, 1);
        } else {
            cv.put(DiagnosisConstants.DRCS_KEY_RCPF, 1);
        }
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, cv);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x010a, code lost:
        if (onPublishRequireFull(r1, r4, r13) == false) goto L_0x0142;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onPublishFailed(com.sec.internal.constants.ims.servicemodules.presence.PublishResponse r12, int r13) {
        /*
            r11 = this;
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r13)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r1 = r11.mPresenceModuleInfo
            java.lang.Object r1 = r1.get(r13)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r1 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r1
            r2 = 0
            r1.mOwnInfoPublished = r2
            java.lang.String r3 = "PresenceModule"
            if (r0 != 0) goto L_0x001a
            java.lang.String r2 = "onPublishFailed: mnoStrategy is null."
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            return
        L_0x001a:
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceFailureReason r4 = r12.getReason()
            r5 = 1
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r6 = r0.handlePresenceFailure(r4, r5)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "onPublishFailed - statusCode: "
            r7.append(r8)
            r7.append(r6)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r3, r13, r7)
            r7 = 302579716(0x12090004, float:4.3229597E-28)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r13)
            java.lang.String r9 = ","
            r8.append(r9)
            int r10 = r6.ordinal()
            r8.append(r10)
            r8.append(r9)
            int r10 = r4.ordinal()
            r8.append(r10)
            r8.append(r9)
            int r10 = r12.getSipError()
            r8.append(r10)
            r8.append(r9)
            java.lang.String r9 = r12.getErrorDescription()
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.c(r7, r8)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r1 = r11.initPublishFailedInfos(r1, r6)
            com.sec.internal.constants.Mno r7 = r1.mMno
            boolean r7 = r7.isKor()
            if (r7 == 0) goto L_0x008b
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceFailureReason r7 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED
            if (r4 == r7) goto L_0x008b
            java.lang.String r7 = "onPublishFailed - remain etag for Kor"
            com.sec.internal.log.IMSLog.i(r3, r13, r7)
            goto L_0x0090
        L_0x008b:
            com.sec.internal.ims.servicemodules.presence.PresenceSharedPrefHelper r7 = r11.mPresenceSp
            r7.resetPublishEtag(r13)
        L_0x0090:
            com.sec.internal.constants.Mno r7 = r1.mMno
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.TMOUS
            if (r7 != r8) goto L_0x009b
            com.sec.ims.presence.PresenceInfo r7 = r1.mOwnPresenceInfo
            r7.setPublishGzipEnabled(r2)
        L_0x009b:
            int[] r2 = com.sec.internal.ims.servicemodules.presence.PresenceModule.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode
            int r7 = r6.ordinal()
            r2 = r2[r7]
            r7 = 1000(0x3e8, double:4.94E-321)
            switch(r2) {
                case 1: goto L_0x013e;
                case 2: goto L_0x0137;
                case 3: goto L_0x012d;
                case 4: goto L_0x0129;
                case 5: goto L_0x0106;
                case 6: goto L_0x010d;
                case 7: goto L_0x00f2;
                case 8: goto L_0x00ea;
                case 9: goto L_0x00e2;
                case 10: goto L_0x00d8;
                case 11: goto L_0x00c1;
                case 12: goto L_0x00c1;
                case 13: goto L_0x00b2;
                case 14: goto L_0x00aa;
                default: goto L_0x00a8;
            }
        L_0x00a8:
            goto L_0x0142
        L_0x00aa:
            java.lang.String r2 = "onPublishFailed: need to perform IMS re-registration"
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            goto L_0x0142
        L_0x00b2:
            java.lang.String r2 = "onPublishFailed: PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER"
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            long r2 = r12.getRetryAfter()
            r11.onPublishRetryAfter(r1, r2, r13)
            goto L_0x0142
        L_0x00c1:
            java.lang.String r2 = "onPublishFailed: PRESENCE_REQUIRE_RETRY_PUBLISH"
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceConfig> r2 = r11.mPresenceConfig
            java.lang.Object r2 = r2.get(r13)
            com.sec.internal.ims.servicemodules.presence.PresenceConfig r2 = (com.sec.internal.ims.servicemodules.presence.PresenceConfig) r2
            long r2 = r2.getPublishTimer()
            long r2 = r2 * r7
            r11.startRetryPublishTimer(r2, r13)
            goto L_0x0142
        L_0x00d8:
            java.lang.String r2 = "onPublishFailed: vzw default case... "
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            r11.onPublishDisableMode(r13)
            goto L_0x0142
        L_0x00e2:
            boolean r2 = r12.isRefresh()
            r11.onPublishNoResponse(r1, r2, r13)
            goto L_0x0142
        L_0x00ea:
            long r2 = r12.getRetryAfter()
            r11.onPublishRetryExpBackoff(r1, r2, r13)
            goto L_0x0142
        L_0x00f2:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceConfig> r2 = r11.mPresenceConfig
            java.lang.Object r2 = r2.get(r13)
            com.sec.internal.ims.servicemodules.presence.PresenceConfig r2 = (com.sec.internal.ims.servicemodules.presence.PresenceConfig) r2
            long r2 = r2.getBadEventExpiry()
            long r2 = r2 * r7
            r11.startBadEventTimer(r2, r5, r13)
            r11.setParalysed(r5, r13)
            goto L_0x0142
        L_0x0106:
            boolean r2 = r11.onPublishRequireFull(r1, r4, r13)
            if (r2 != 0) goto L_0x010d
            goto L_0x0142
        L_0x010d:
            int r2 = r12.getRetryTime()
            if (r2 <= 0) goto L_0x011d
            com.sec.ims.presence.PresenceInfo r2 = r1.mOwnPresenceInfo
            int r3 = r12.getRetryTime()
            long r7 = (long) r3
            r2.setMinExpires(r7)
        L_0x011d:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r13)
            android.os.Message r2 = r11.obtainMessage(r5, r2)
            r11.sendMessage(r2)
            goto L_0x0142
        L_0x0129:
            r11.onPublishRequestTimeout(r1, r13)
            goto L_0x0142
        L_0x012d:
            java.lang.String r2 = "onPublishFailed: PRESENCE_NOT_FOUND"
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            r11.setParalysed(r5, r13)
            goto L_0x0142
        L_0x0137:
            java.lang.String r2 = "onPublishFailed: PRESENCE_AT_NOT_REGISTERED"
            com.sec.internal.log.IMSLog.e(r3, r13, r2)
            goto L_0x0142
        L_0x013e:
            r11.onPublishNotProvisioned(r1, r13)
        L_0x0142:
            r11.notifyPublishError(r1, r6, r12, r13)
            android.content.Context r2 = r11.mContext
            int r3 = r12.getSipError()
            java.lang.String r5 = r12.getErrorDescription()
            com.sec.internal.ims.servicemodules.presence.PresenceUtil.sendRCSPPubInfoToHQM(r2, r3, r5, r13)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceModule.onPublishFailed(com.sec.internal.constants.ims.servicemodules.presence.PublishResponse, int):void");
    }

    /* renamed from: com.sec.internal.ims.servicemodules.presence.PresenceModule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode;

        static {
            int[] iArr = new int[PresenceResponse.PresenceStatusCode.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode = iArr;
            try {
                iArr[PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_AT_BAD_EVENT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    private PresenceModuleInfo initPublishFailedInfos(PresenceModuleInfo presInfo, PresenceResponse.PresenceStatusCode statusCode) {
        if (statusCode != PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF) {
            presInfo.mPublishExpBackOffRetryCount = 0;
        }
        if (statusCode != PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT) {
            presInfo.mPublishRequestTimeout = 0;
        }
        if (statusCode != PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE) {
            presInfo.mPublishNoResponseCount = 0;
        }
        return presInfo;
    }

    private void onPublishRequestTimeout(PresenceModuleInfo presInfo, int phoneId) {
        presInfo.mPublishRequestTimeout++;
        IMSLog.e(LOG_TAG, phoneId, "onPublishRequestTimeout: PRESENCE_REQUEST_TIMEOUT count = " + presInfo.mPublishRequestTimeout);
        long RequestTimeoutRetryTimer = PresenceUtil.getPublishExpBackOffRetryTime(phoneId, presInfo.mPublishRequestTimeout);
        if (RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(phoneId)) {
            sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
        } else if (RequestTimeoutRetryTimer != 0) {
            startRetryPublishTimer(1000 * RequestTimeoutRetryTimer, phoneId);
        } else {
            IMSLog.e(LOG_TAG, phoneId, "onPublishRequestTimeout: starting error retry ... ");
            if (this.mPresenceConfig.get(phoneId).getPublishErrRetry() != 0) {
                startRetryPublishTimer(this.mPresenceConfig.get(phoneId).getPublishErrRetry() * 1000, phoneId);
                this.mPresenceConfig.get(phoneId).setPublishErrRetry(0);
            }
        }
    }

    private void onPublishNoResponse(PresenceModuleInfo presInfo, boolean isRefresh, int phoneId) {
        if (!isRefresh) {
            presInfo.mPublishNoResponseCount++;
            IMSLog.e(LOG_TAG, phoneId, "onPublishNoResponse: count = " + presInfo.mPublishNoResponseCount + ", isSVLTE: " + SemSystemProperties.getBoolean("ro.ril.svlte1x", false));
            long NoResponseRetryTimer = PresenceUtil.getPublishExpBackOffRetryTime(phoneId, presInfo.mPublishNoResponseCount);
            if (NoResponseRetryTimer != 0) {
                startRetryPublishTimer(1000 * NoResponseRetryTimer, phoneId);
            } else {
                IMSLog.e(LOG_TAG, phoneId, "onPublishNoResponse: retry time end for NoResponse... ");
            }
        }
    }

    private boolean onPublishRequireFull(PresenceModuleInfo presInfo, PresenceResponse.PresenceFailureReason errorReason, int phoneId) {
        if (presInfo.mMno == Mno.TMOUS || presInfo.mMno.isKor()) {
            IMSLog.i(LOG_TAG, phoneId, "onPublishRequireFull: oldError = " + presInfo.mOldPublishError + ", newError = " + errorReason);
            if (!presInfo.mLimitImmediateRetry || errorReason == null || !errorReason.equals(presInfo.mOldPublishError)) {
                presInfo.mLimitImmediateRetry = true;
                presInfo.mOldPublishError = errorReason;
            } else {
                IMSLog.i(LOG_TAG, phoneId, "onPublishRequireFull: wait for the publish timer expiry");
                presInfo.mLimitImmediateRetry = false;
                presInfo.mOldPublishError = errorReason;
                startRetryPublishTimer(this.mPresenceConfig.get(phoneId).getPublishTimer() * 1000, phoneId);
                return false;
            }
        }
        return true;
    }

    private void onPublishRetryAfter(PresenceModuleInfo presInfo, long retryAfter, int phoneId) {
        if (retryAfter > 0) {
            IMSLog.e(LOG_TAG, phoneId, "onPublishRetryAfter: retry publish after " + retryAfter);
            startRetryPublishTimer(1000 * retryAfter, phoneId);
        } else if (presInfo.mMno == Mno.TMOUS) {
            startRetryPublishTimer((long) (((double) (this.mPresenceConfig.get(phoneId).getPublishTimer() * 1000)) * 0.85d), phoneId);
        } else {
            startRetryPublishTimer(this.mPresenceConfig.get(phoneId).getPublishTimer() * 1000, phoneId);
        }
    }

    private void onPublishRetryExpBackoff(PresenceModuleInfo presInfo, long retryAfter, int phoneId) {
        if (!presInfo.mMno.isKor() || retryAfter <= 0) {
            presInfo.mPublishExpBackOffRetryCount++;
            IMSLog.e(LOG_TAG, phoneId, "onPublishRetryExpBackoff: EXP_BACKOFF_RETRY count = " + presInfo.mPublishExpBackOffRetryCount);
            long expBackoffRetryTimer = PresenceUtil.getPublishExpBackOffRetryTime(phoneId, presInfo.mPublishExpBackOffRetryCount);
            if (RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(phoneId)) {
                sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
            } else if (expBackoffRetryTimer != 0) {
                startRetryPublishTimer(1000 * expBackoffRetryTimer, phoneId);
            } else {
                IMSLog.e(LOG_TAG, phoneId, "onPublishRetryExpBackoff: starting error retry ... ");
                if (this.mPresenceConfig.get(phoneId).getPublishErrRetry() != 0) {
                    startRetryPublishTimer(this.mPresenceConfig.get(phoneId).getPublishErrRetry() * 1000, phoneId);
                    this.mPresenceConfig.get(phoneId).setPublishErrRetry(0);
                }
            }
        } else {
            IMSLog.e(LOG_TAG, phoneId, "onPublishRetryExpBackoff: Use retryAfter, Retry publish after " + retryAfter);
            startRetryPublishTimer(1000 * retryAfter, phoneId);
            presInfo.mPublishExpBackOffRetryCount = 0;
        }
    }

    private void onPublishNotProvisioned(PresenceModuleInfo presInfo, int phoneId) {
        presInfo.mPublishNotProvisionedCount++;
        IMSLog.e(LOG_TAG, phoneId, "onPublishNotProvisioned: NOT_PROVISIONED count = " + presInfo.mPublishNotProvisionedCount);
        if (presInfo.mMno == Mno.VZW) {
            this.mCapabilityDiscovery.clearCapabilitiesCache(phoneId);
            PresenceUtil.triggerOmadmTreeSync(this.mContext, phoneId);
            setParalysed(true, phoneId);
            presInfo.mPublishNotProvisionedCount = 0;
        }
    }

    private void onPublishDisableMode(int phoneId) {
        if (RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(phoneId)) {
            sendMessage(obtainMessage(3, Integer.valueOf(phoneId)));
        }
    }

    private void notifyPublishError(PresenceModuleInfo presInfo, PresenceResponse.PresenceStatusCode statusCode, PublishResponse response, int phoneId) {
        IRegistrationGovernor regGov;
        if (presInfo.mRegInfo != null && (regGov = ImsRegistry.getRegistrationManager().getRegistrationGovernor(presInfo.mRegInfo.getHandle())) != null) {
            if (presInfo.mMno != Mno.TMOUS || statusCode != PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION) {
                regGov.onPublishError(new SipError(response.getSipError(), response.getErrorDescription()));
            } else if (!presInfo.mLimitReRegistration) {
                regGov.onPublishError(SipErrorBase.FORBIDDEN);
                presInfo.mLimitReRegistration = true;
            } else {
                IMSLog.i(LOG_TAG, phoneId, "notifyPublishError: maintain last IMS registration");
                presInfo.mLimitReRegistration = false;
            }
        }
    }

    private void startBadEventTimer(long millis, boolean needSaveTime, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "startBadEventTimer: millis " + millis);
        if (getBadEventProgress(phoneId)) {
            IMSLog.i(LOG_TAG, phoneId, "startBadEventTimer: BadEvent in progress");
            return;
        }
        if (this.mPresenceModuleInfo.get(phoneId).mBadEventIntent != null) {
            stopBadEventTimer(phoneId);
        }
        if (millis > 0) {
            Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.bad_event_timeout");
            intent.putExtra("sim_slot_id", phoneId);
            this.mPresenceModuleInfo.get(phoneId).mBadEventIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
            AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(phoneId).mBadEventIntent, millis);
            setBadEventProgress(true, phoneId);
            if (needSaveTime) {
                this.mPresenceSp.saveBadEventTimestamp(new Date().getTime(), phoneId);
            }
        }
    }

    private void stopBadEventTimer(int phoneId) {
        removeMessages(14, Integer.valueOf(phoneId));
        if (this.mPresenceModuleInfo.get(phoneId).mBadEventIntent == null) {
            IMSLog.e(LOG_TAG, phoneId, "stopBadEventTimer: BadEventExitTimer is not running.");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "stopBadEventTimer");
        AlarmTimer.stop(this.mContext, this.mPresenceModuleInfo.get(phoneId).mBadEventIntent);
        this.mPresenceModuleInfo.get(phoneId).mBadEventIntent = null;
        setBadEventProgress(false, phoneId);
        this.mPresenceSp.saveBadEventTimestamp(0, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onBadEventTimeout(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onBadEventTimeout: ");
        if (this.mPresenceModuleInfo.get(phoneId).mBadEventIntent != null) {
            stopBadEventTimer(phoneId);
            setParalysed(false, phoneId);
            sendMessage(obtainMessage(1, Integer.valueOf(phoneId)));
        }
    }

    private void startRetryPublishTimer(long millis, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "startRetryPublishTimer: millis " + millis);
        stopPublishTimer(phoneId);
        if (this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent != null) {
            stopRetryPublishTimer(phoneId);
        }
        if (millis > 0) {
            this.mPresenceModuleInfo.get(phoneId).ongoingPublishErrRetry = millis == this.mPresenceConfig.get(phoneId).getPublishErrRetry() * 1000;
            Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.retry_publish");
            intent.putExtra("sim_slot_id", phoneId);
            this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
            AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent, millis);
            if (!this.mPresenceModuleInfo.get(phoneId).mMno.isKor()) {
                this.mPresenceSp.savePublishTimestamp(0, phoneId);
            }
        }
    }

    private void stopRetryPublishTimer(int phoneId) {
        if (this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent == null) {
            IMSLog.e(LOG_TAG, phoneId, "stopRetryPublishTimer: mRetryPublishIntent is null.");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "stopRetryPublishTimer");
        AlarmTimer.stop(this.mContext, this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent);
        this.mPresenceModuleInfo.get(phoneId).mRetryPublishIntent = null;
    }

    private void startSubscribeRetryTimer(long millis, String subscriptionId, int phoneId) {
        Log.i(LOG_TAG, "startSubscribeRetryTimer: millis " + millis + ", subscriptionId " + subscriptionId);
        PendingIntent subscribeRetryIntent = this.mSubscribeRetryList.get(subscriptionId);
        if (subscribeRetryIntent != null) {
            AlarmTimer.stop(this.mContext, subscribeRetryIntent);
            this.mSubscribeRetryList.remove(subscriptionId);
        }
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.retry_subscribe");
        intent.setData(Uri.parse("urn:subscriptionid:" + subscriptionId));
        intent.putExtra("KEY_SUBSCRIPTION_ID", subscriptionId);
        intent.putExtra("KEY_PHONE_ID", phoneId);
        PendingIntent subscribeRetryIntent2 = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        AlarmTimer.start(this.mContext, subscribeRetryIntent2, millis);
        this.mSubscribeRetryList.put(subscriptionId, subscribeRetryIntent2);
    }

    private void stopSubscribeRetryTimer(int phoneId) {
        Iterator<String> it = this.mSubscribeRetryList.keySet().iterator();
        while (it.hasNext()) {
            String subscriptionId = it.next();
            PresenceSubscription s = PresenceSubscriptionController.getSubscription(subscriptionId, phoneId);
            if (s != null && s.getPhoneId() == phoneId) {
                AlarmTimer.stop(this.mContext, this.mSubscribeRetryList.get(subscriptionId));
                s.updateState(4);
                it.remove();
            }
        }
        IMSLog.i(LOG_TAG, phoneId, "stopSubscribeRetryTimer");
    }

    /* access modifiers changed from: package-private */
    public void onPeriodicPublish(int phoneId) {
        IMSLog.e(LOG_TAG, phoneId, "onPeriodicPublish:");
        publish(this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo, phoneId);
        startPublishTimer(phoneId);
    }

    private void acquireWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            synchronized (wakeLock) {
                this.mWakeLock.acquire();
                removeMessages(13);
                sendEmptyMessageDelayed(13, 5000);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean clearWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock == null) {
            return false;
        }
        synchronized (wakeLock) {
            if (!this.mWakeLock.isHeld()) {
                return false;
            }
            this.mWakeLock.release();
            removeMessages(13);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeRequested(PresenceSubscriptionController.SubscriptionRequest request) {
        subscribe(request, this.mPresenceConfig.get(request.phoneId).useAnonymousFetch());
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeComplete(PresenceSubscription subscription, PresenceResponse response) {
        int phoneId = subscription.getPhoneId();
        IMSLog.s(LOG_TAG, phoneId, "onSubscribeComplete: Uri " + subscription.getUriList() + " success " + response.isSuccess());
        clearWakeLock();
        if (RcsPolicyManager.getRcsStrategy(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "onSubscribeComplete: mnoStrategy is null.");
            return;
        }
        if (!response.isSuccess()) {
            subscription.updateState(6);
            onSubscribeFailed(subscription, response);
        } else {
            int expires = subscription.getExpiry();
            if (expires > 0) {
                subscription.updateState(1);
                if (subscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC && this.mListener != null) {
                    IMSLog.i(LOG_TAG, phoneId, "onSubscribeComplete: recover polling");
                    this.mListener.onPollingRequested(true, phoneId);
                }
                if (!subscription.isSingleFetch() && this.mPresenceModuleInfo.get(phoneId).mMno == Mno.TMOUS) {
                    IMSLog.i(LOG_TAG, phoneId, "onSubscribeComplete: subscription will be terminated after " + expires);
                    sendMessageDelayed(obtainMessage(9, subscription), ((long) (expires + 1)) * 1000);
                }
            } else {
                subscription.updateState(4);
            }
        }
        ContentValues cv = new ContentValues();
        if (response.isSuccess() || (!response.isSuccess() && (response.getSipError() == 403 || response.getSipError() == 404))) {
            cv.put("RCSC", 1);
        } else {
            cv.put(DiagnosisConstants.DRCS_KEY_RCSF, 1);
        }
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, cv);
        PresenceSubscriptionController.cleanExpiredSubscription();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0088, code lost:
        if (r4 != 15) goto L_0x014a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onSubscribeFailed(com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription r11, com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse r12) {
        /*
            r10 = this;
            int r0 = r11.getPhoneId()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r0)
            java.lang.String r2 = "PresenceModule"
            if (r1 != 0) goto L_0x0013
            java.lang.String r3 = "onSubscribeFailed: mnoStrategy is null."
            com.sec.internal.log.IMSLog.e(r2, r0, r3)
            return
        L_0x0013:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r3 = r10.mPresenceModuleInfo
            java.lang.Object r3 = r3.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r3 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r3
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceFailureReason r4 = r12.getReason()
            r5 = 0
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r4 = r1.handlePresenceFailure(r4, r5)
            r3.mLastSubscribeStatusCode = r4
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "onSubscribeFailed - statusCode: "
            r3.append(r4)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r4 = r10.mPresenceModuleInfo
            java.lang.Object r4 = r4.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r4 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r4
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r4 = r4.mLastSubscribeStatusCode
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r0, r3)
            com.sec.ims.presence.PresenceInfo r3 = new com.sec.ims.presence.PresenceInfo
            java.lang.String r4 = r11.getSubscriptionId()
            r3.<init>(r4, r0)
            com.sec.ims.presence.ServiceTuple r4 = new com.sec.ims.presence.ServiceTuple
            int r6 = com.sec.ims.options.Capabilities.FEATURE_NOT_UPDATED
            long r6 = (long) r6
            r8 = 0
            r4.<init>(r6, r8, r8)
            r3.addService(r4)
            r3.setFetchState(r5)
            int[] r4 = com.sec.internal.ims.servicemodules.presence.PresenceModule.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r6 = r10.mPresenceModuleInfo
            java.lang.Object r6 = r6.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r6 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r6
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r6 = r6.mLastSubscribeStatusCode
            int r6 = r6.ordinal()
            r4 = r4[r6]
            r6 = 1
            if (r4 == r6) goto L_0x010e
            r7 = 2
            if (r4 == r7) goto L_0x010a
            r7 = 3
            if (r4 == r7) goto L_0x00b7
            r7 = 4
            if (r4 == r7) goto L_0x00af
            r7 = 6
            r9 = 8
            if (r4 == r7) goto L_0x008c
            if (r4 == r9) goto L_0x00af
            r5 = 11
            if (r4 == r5) goto L_0x00b7
            r5 = 15
            if (r4 == r5) goto L_0x0130
            goto L_0x014a
        L_0x008c:
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r2 = r11.getRequestType()
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r4 = com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC
            if (r2 != r4) goto L_0x009b
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener r2 = r10.mListener
            if (r2 == 0) goto L_0x009b
            r2.onPollingRequested(r5, r0)
        L_0x009b:
            r2 = 5
            r11.updateState(r2)
            int r2 = r12.getRetryTime()
            r11.setExpiry(r2)
            android.os.Message r2 = r10.obtainMessage(r9, r11)
            r10.sendMessage(r2)
            goto L_0x014a
        L_0x00af:
            r11.retrySubscription()
            r10.handleExpBackOffRetry(r11)
            goto L_0x014a
        L_0x00b7:
            r3.clearService()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onSubscribeFailed - PRESENCE_NO_SUBSCRIBE: code : "
            r4.append(r5)
            int r5 = r12.getSipError()
            r4.append(r5)
            java.lang.String r5 = " errorReason : "
            r4.append(r5)
            java.lang.String r5 = r12.getErrorDescription()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r2, r0, r4)
            int r2 = r12.getSipError()
            r4 = 404(0x194, float:5.66E-43)
            if (r2 != r4) goto L_0x00fe
            java.lang.String r2 = r12.getErrorDescription()
            java.lang.String r4 = "isbot"
            boolean r2 = r4.equals(r2)
            if (r2 == 0) goto L_0x00fe
            com.sec.ims.presence.ServiceTuple r2 = new com.sec.ims.presence.ServiceTuple
            long r4 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_ROLE
            r2.<init>(r4, r8, r8)
            r3.addService(r2)
            goto L_0x014a
        L_0x00fe:
            com.sec.ims.presence.ServiceTuple r2 = new com.sec.ims.presence.ServiceTuple
            int r4 = com.sec.ims.options.Capabilities.FEATURE_NON_RCS_USER
            long r4 = (long) r4
            r2.<init>(r4, r8, r8)
            r3.addService(r2)
            goto L_0x014a
        L_0x010a:
            com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController.addPendingSubscription(r11)
            goto L_0x014a
        L_0x010e:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r4 = r10.mPresenceModuleInfo
            java.lang.Object r4 = r4.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r4 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r4
            com.sec.internal.constants.Mno r4 = r4.mMno
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW
            if (r4 != r5) goto L_0x0130
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r4 = r10.mCapabilityDiscovery
            r4.clearCapabilitiesCache(r0)
            android.content.Context r4 = r10.mContext
            com.sec.internal.ims.servicemodules.presence.PresenceUtil.triggerOmadmTreeSync(r4, r0)
            r10.setParalysed(r6, r0)
            java.lang.String r4 = "trigger OMA sync for 403 not provisioned"
            com.sec.internal.log.IMSLog.i(r2, r0, r4)
            goto L_0x014a
        L_0x0130:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r4 = r10.mPresenceModuleInfo
            java.lang.Object r4 = r4.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r4 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r4
            com.sec.internal.constants.Mno r4 = r4.mMno
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW
            if (r4 != r5) goto L_0x0143
            android.content.Context r4 = r10.mContext
            com.sec.internal.ims.servicemodules.presence.PresenceUtil.triggerOmadmTreeSync(r4, r0)
        L_0x0143:
            java.lang.String r4 = "onSubscribeFailed: for 403 forbidden response"
            com.sec.internal.log.IMSLog.i(r2, r0, r4)
        L_0x014a:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r2 = r10.mPresenceModuleInfo
            java.lang.Object r2 = r2.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r2 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r2
            com.sec.ims.ImsRegistration r2 = r2.mRegInfo
            if (r2 == 0) goto L_0x017e
            com.sec.internal.interfaces.ims.core.IRegistrationManager r2 = com.sec.internal.ims.registry.ImsRegistry.getRegistrationManager()
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r4 = r10.mPresenceModuleInfo
            java.lang.Object r4 = r4.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r4 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r4
            com.sec.ims.ImsRegistration r4 = r4.mRegInfo
            int r4 = r4.getHandle()
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r4 = r2.getRegistrationGovernor(r4)
            if (r4 == 0) goto L_0x017e
            com.sec.ims.util.SipError r5 = new com.sec.ims.util.SipError
            int r7 = r12.getSipError()
            java.lang.String r8 = r12.getErrorDescription()
            r5.<init>(r7, r8)
            r4.onSubscribeError(r6, r5)
        L_0x017e:
            r10.onNewPresenceInformation(r3, r0)
            android.content.Context r2 = r10.mContext
            int r4 = r12.getSipError()
            com.sec.internal.ims.servicemodules.presence.PresenceUtil.sendRCSPSubInfoToHQM(r2, r4, r0)
            r2 = 302579717(0x12090005, float:4.32296E-28)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r0)
            java.lang.String r5 = ","
            r4.append(r5)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r6 = r10.mPresenceModuleInfo
            java.lang.Object r6 = r6.get(r0)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r6 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r6
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r6 = r6.mLastSubscribeStatusCode
            r4.append(r6)
            r4.append(r5)
            int r5 = r12.getSipError()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r2, r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceModule.onSubscribeFailed(com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription, com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse):void");
    }

    private void handleExpBackOffRetry(PresenceSubscription subscription) {
        int phoneId = subscription.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "handleExpBackOffRetry: EXP_BACKOFF_RETRY count = " + subscription.getRetryCount());
        long expBackoffRetryTimer = 0;
        if (subscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC) {
            if (this.mListener != null && subscription.getRetryCount() == 1) {
                IMSLog.i(LOG_TAG, phoneId, "handleExpBackOffRetry: notifying polling failure");
                this.mListener.onPollingRequested(false, phoneId);
            }
            expBackoffRetryTimer = PresenceUtil.getListSubscribeExpBackOffRetryTime(phoneId, subscription.getRetryCount());
        } else if (subscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE) {
            expBackoffRetryTimer = PresenceUtil.getSubscribeExpBackOffRetryTime(phoneId, subscription.getRetryCount());
        }
        if (expBackoffRetryTimer != 0) {
            subscription.updateState(5);
            startSubscribeRetryTimer(1000 * expBackoffRetryTimer, subscription.getSubscriptionId(), phoneId);
            return;
        }
        subscription.updateState(4);
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeListRequested(CapabilityConstants.RequestType type, int phoneId) {
        synchronized (this.mUriToSubscribe) {
            subscribe(this.mUriToSubscribe, true, type, phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeRetry(PresenceSubscription s) {
        PresenceSubscription presenceSubscription = s;
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(s.getUriList());
        if (arrayList.size() > 1) {
            ArrayList arrayList2 = arrayList;
            this.mService.subscribeList(arrayList2, true, obtainMessage(6, presenceSubscription), s.getSubscriptionId(), this.mPresenceModuleInfo.get(s.getPhoneId()).mRegInfo.getImsProfile().isGzipEnabled(), s.getExpiry(), s.getPhoneId());
            return;
        }
        this.mService.subscribe((ImsUri) arrayList.get(0), true, obtainMessage(6, presenceSubscription), s.getSubscriptionId(), s.getPhoneId());
    }

    public void readConfig(int phoneId) {
        if (this.mPresenceConfig.get(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "readConfig: not ready");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "readConfig");
        this.mPresenceConfig.get(phoneId).load();
        this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo.setExpireTime(this.mPresenceConfig.get(phoneId).getRetryPublishTimer());
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int phoneId = SimUtil.getDefaultPhoneId();
        Log.i(LOG_TAG, "handleMessage: msg " + msg.what);
        if (!PresenceEvent.handleEvent(msg, this, phoneId)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + msg.what);
        }
    }

    private void setServiceVersion() {
        HashMap<String, String> svMap = new HashMap<>();
        ServiceTuple botMsg = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
        svMap.put("xbotmessage", botMsg.version);
        this.mService.updateServiceVersion(0, svMap);
        Log.i(LOG_TAG, "setServiceVersion: xbotmessage " + botMsg.version);
    }

    public void handleIntent(Intent intent) {
    }

    /* access modifiers changed from: package-private */
    public boolean checkModuleReady(int phoneId) {
        if (!isRunning()) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: module not running");
            return false;
        } else if (!isReadyToRequest(phoneId)) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: not ready to request");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: mnoStrategy is null.");
            return false;
        } else if (this.mUriGenerator != null) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: mUriGenerator is null");
            return false;
        }
    }

    private void onEABPublishComplete(PresenceResponse result) {
        this.mListener.onCapabilityAndAvailabilityPublished(result.getSipError(), result.getPhoneId());
    }

    public void registerService(String serviceId, String version, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "registerService: [" + serviceId + ":" + version + "]");
        ServiceTuple tuple = ServiceTuple.getServiceTuple(serviceId, version, (String[]) null);
        if (tuple != null) {
            IMSLog.i(LOG_TAG, phoneId, "registerService: valid service tuple");
            if (!this.mPresenceModuleInfo.get(phoneId).mOwnInfoPublished) {
                this.mServiceTupleList.add(tuple);
                return;
            }
            synchronized (this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo) {
                this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo.addService(tuple);
            }
            removeMessages(1, Integer.valueOf(phoneId));
            sendMessage(obtainMessage(1, Integer.valueOf(phoneId)));
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "advertise: not a valid service tuple, do nothing..");
    }

    public void deRegisterService(List<String> serviceIdList, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "deRegisterService: serviceIdList = " + serviceIdList);
        boolean isRemoved = false;
        for (String s : serviceIdList) {
            String[] list = s.split("#");
            ServiceTuple tuple = ServiceTuple.getServiceTuple(list[0], list[1], (String[]) null);
            if (tuple != null) {
                synchronized (this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo) {
                    this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo.removeService(tuple);
                }
                isRemoved = true;
            } else {
                IMSLog.e(LOG_TAG, phoneId, "deRegisterService: not a valid service tuple");
            }
        }
        if (isRemoved) {
            removeMessages(1, Integer.valueOf(phoneId));
            sendMessage(obtainMessage(1, Integer.valueOf(phoneId)));
        }
    }

    public void loadThirdPartyServiceTuples(List<ServiceTuple> tupleList) {
        Log.i(LOG_TAG, "loadThirdPartyServiceTuples");
        for (ServiceTuple st : tupleList) {
            synchronized (this.mServiceTupleList) {
                this.mServiceTupleList.add(st);
            }
        }
    }

    private void buildPresenceInfoForThirdParty(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "buildPresenceInfoForThirdParty");
        synchronized (this.mServiceTupleList) {
            if (!this.mServiceTupleList.isEmpty()) {
                for (ServiceTuple st : this.mServiceTupleList) {
                    this.mPresenceModuleInfo.get(phoneId).mOwnPresenceInfo.addService(st);
                }
            }
        }
    }

    private void setBadEventProgress(boolean mode, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "setBadEventProgress: " + mode);
        this.mPresenceModuleInfo.get(phoneId).mBadEventProgress = mode;
    }

    public boolean getBadEventProgress(int phoneId) {
        return this.mPresenceModuleInfo.get(phoneId).mBadEventProgress;
    }

    public boolean isOwnCapPublished() {
        return this.mPresenceModuleInfo.get(SimUtil.getDefaultPhoneId()).mOwnInfoPublished;
    }

    /* access modifiers changed from: package-private */
    public void onSubscriptionTerminated(PresenceSubscription s) {
        if (s == null) {
            Log.e(LOG_TAG, "onSubscriptionTerminated: subscription is null");
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(s.getDropUris());
        if (arrayList.size() > 0) {
            Log.i(LOG_TAG, "onSubscriptionTerminated: update capabilities for dropped " + arrayList.size() + " uris");
            ICapabilityEventListener iCapabilityEventListener = this.mListener;
            if (iCapabilityEventListener != null) {
                iCapabilityEventListener.onCapabilityUpdate(arrayList, (long) Capabilities.FEATURE_NOT_UPDATED, CapabilityConstants.CapExResult.SUCCESS, (String) null, s.getPhoneId());
            }
        }
    }

    public long getSupportFeature(int phoneId) {
        return this.mEnabledFeatures[phoneId];
    }

    public void reset(int phoneId) {
        this.mPresenceSp.savePublishTimestamp(0, phoneId);
        stopPublishTimer(phoneId);
        stopBadEventTimer(phoneId);
        stopSubscribeRetryTimer(phoneId);
        this.mPresenceSp.resetPublishEtag(phoneId);
    }

    public void removePresenceCache(List<ImsUri> uris, int phoneId) {
        this.mPresenceCacheController.removePresenceCache(uris, phoneId);
    }

    private void resetPublishErrorHandling(int phoneId) {
        PresenceModuleInfo presInfo = this.mPresenceModuleInfo.get(phoneId);
        presInfo.mLimitReRegistration = false;
        presInfo.mLimitImmediateRetry = false;
        presInfo.mOldPublishError = null;
    }

    public boolean getRegiInfoUpdater(int phoneId) {
        return this.mPresenceRegiInfoUpdater.getOrDefault(Integer.valueOf(phoneId), false).booleanValue();
    }

    public void setRegiInfoUpdater(int phoneId, boolean info) {
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(phoneId), Boolean.valueOf(info));
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Publish History: ");
        this.mEventLog.dump();
        for (PresenceConfig config : this.mPresenceConfig.values()) {
            if (config != null) {
                IMSLog.dump(LOG_TAG, config.toString());
            }
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
