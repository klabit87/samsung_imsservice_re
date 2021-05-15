package com.sec.internal.ims.servicemodules.im;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.SemEmergencyConstantsExt;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.constants.ims.servicemodules.im.ChatbotXmlUtils;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.MessageRevokeResponse;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeNotifyEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ReportChatbotAsSpamRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmLMMIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.ChatbotAnonymizeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ReportChatbotAsSpamParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.data.ImParticipantUri;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.IMessagingAppInfoListener;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.MessagingAppInfoReceiver;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImModule extends ServiceModuleBase implements IImModule, IGetter, IMessagingAppInfoListener {
    private static final long DEFAULT_WAKE_LOCK_TIMEOUT = 3000;
    private static final long DELAY_TIME_FOR_CACHE_CLEAR = 10000;
    /* access modifiers changed from: private */
    public static final String LOG_TAG;
    protected static final String NAME;
    private static final String[] sRequiredServices = {"im", "slm", "ft", "ft_http"};
    /* access modifiers changed from: private */
    public final ImCache mCache;
    private final List<ImsUri> mCallList;
    /* access modifiers changed from: private */
    public final PhoneIdKeyMap<ImConfig> mConfigs;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCountReconfiguration;
    private int mDefaultPhoneId;
    private final FeatureUpdater mFeatureUpdater;
    private final FtProcessor mFtProcessor;
    private final FtTranslation mFtTranslation;
    private final PhoneIdKeyMap<GroupChatRetrievingHandler> mGroupChatRetrievingHandlers;
    protected final PhoneIdKeyMap<Boolean> mHasIncomingSessionForA2P;
    private final ImDump mImDump;
    private final ImProcessor mImProcessor;
    private final IImServiceInterface mImService;
    private final ImSessionProcessor mImSessionProcessor;
    private final ImTranslation mImTranslation;
    /* access modifiers changed from: private */
    public boolean mInternetAvailable;
    private PhoneIdKeyMap<ConnectivityManager.NetworkCallback> mInternetListeners;
    private final PhoneIdKeyMap<Boolean> mIsDataRoamings;
    private final PhoneIdKeyMap<Boolean> mIsDataStateConnected;
    private final PhoneIdKeyMap<Boolean> mIsOutOfServices;
    private boolean mIsSetUpsmEventReceiver;
    /* access modifiers changed from: private */
    public boolean mIsWifiConnected;
    private MessagingAppInfoReceiver mMessagingAppInfoReceiver;
    protected final Set<Integer> mNeedToRemoveFromPendingList;
    private final PhoneIdKeyMap<String> mOwnPhoneNumbers;
    private String mRcsProfile;
    private final PhoneIdKeyMap<Integer> mRegistrationTypes;
    private IServiceAvailabilityEventListener mServiceAvailabilityEventListener;
    private final List<? extends ISimManager> mSimManagers;
    private final ISlmServiceInterface mSlmService;
    private final ThirdPartyTranslation mThirdPartyTranslation;
    private final BroadcastReceiver mUpsmEventReceiver;
    private final PhoneIdKeyMap<UriGenerator> mUriGenerators;
    private final PowerManager.WakeLock mWakeLock;

    static {
        Class<ImModule> cls = ImModule.class;
        NAME = cls.getSimpleName();
        LOG_TAG = cls.getSimpleName();
    }

    public ImModule(Looper looper, Context context, IImServiceInterface imService, ImCache imCache) {
        super(looper);
        this.mCallList = new ArrayList();
        this.mRcsProfile = "";
        this.mNeedToRemoveFromPendingList = new HashSet();
        this.mUpsmEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String access$000 = ImModule.LOG_TAG;
                Log.i(access$000, "Received UpsmEvent: " + intent.getAction() + " extra: " + intent.getExtras());
                ImModule.this.onUltraPowerSavingModeChanged();
            }
        };
        this.mCountReconfiguration = 0;
        this.mContext = context;
        this.mSimManagers = SimManagerFactory.getAllSimManagers();
        this.mCache = imCache;
        this.mImProcessor = new ImProcessor(this.mContext, this, imCache);
        this.mFtProcessor = new FtProcessor(this.mContext, imService, this, imCache);
        this.mImSessionProcessor = new ImSessionProcessor(this.mContext, imService, this, imCache);
        this.mImTranslation = new ImTranslation(this.mContext, this, this.mImSessionProcessor, this.mImProcessor);
        this.mFtTranslation = new FtTranslation(this.mContext, this, this.mFtProcessor);
        this.mThirdPartyTranslation = new ThirdPartyTranslation(this.mContext, this);
        this.mImService = imService;
        this.mSlmService = ImsRegistry.getHandlerFactory().getSlmHandler();
        setUpsmEventReceiver();
        this.mDefaultPhoneId = SimUtil.getSimSlotPriority();
        int phoneCount = this.mSimManagers.size();
        this.mConfigs = new PhoneIdKeyMap<>(phoneCount, null);
        this.mOwnPhoneNumbers = new PhoneIdKeyMap<>(phoneCount, null);
        this.mGroupChatRetrievingHandlers = new PhoneIdKeyMap<>(phoneCount, null);
        this.mRegistrationTypes = new PhoneIdKeyMap<>(phoneCount, null);
        this.mIsDataRoamings = new PhoneIdKeyMap<>(phoneCount, false);
        this.mIsDataStateConnected = new PhoneIdKeyMap<>(phoneCount, false);
        this.mIsOutOfServices = new PhoneIdKeyMap<>(phoneCount, false);
        this.mHasIncomingSessionForA2P = new PhoneIdKeyMap<>(phoneCount, false);
        this.mInternetListeners = new PhoneIdKeyMap<>(phoneCount, null);
        this.mUriGenerators = new PhoneIdKeyMap<>(phoneCount, null);
        this.mFeatureUpdater = new FeatureUpdater(this.mContext, this);
        this.mImDump = new ImDump(imCache);
        for (int i = 0; i < phoneCount; i++) {
            this.mConfigs.put(i, ImConfig.getInstance(i));
            this.mUriGenerators.put(i, UriGeneratorFactory.getInstance().get(i));
        }
        if (phoneCount > 1) {
            SimManagerFactory.registerForDDSChange(this, 29, (Object) null);
        }
        for (ISimManager sm : this.mSimManagers) {
            sm.registerForSimRefresh(this, 34, (Object) null);
        }
        PowerManager.WakeLock newWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, NAME);
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(true);
    }

    public ImModule(Looper looper, Context context, IImServiceInterface imServiceInterface) {
        this(looper, context, imServiceInterface, ImCache.getInstance());
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return sRequiredServices;
    }

    public void init() {
        super.init();
        Log.i(LOG_TAG, "init()");
        this.mCache.load(this);
        this.mCache.addImCacheActionListener(this.mImTranslation);
        this.mImSessionProcessor.init(this.mImProcessor, this.mFtProcessor, this.mImTranslation);
        this.mImProcessor.init(this.mImSessionProcessor, this.mImTranslation);
        this.mFtProcessor.init(this.mImSessionProcessor, this.mImTranslation);
    }

    public void start() {
        super.start();
        Log.i(LOG_TAG, "start()");
        this.mImService.registerForImIncomingSession(this, 1, (Object) null);
        this.mImService.registerForImSessionEstablished(this, 2, (Object) null);
        this.mImService.registerForImSessionClosed(this, 3, (Object) null);
        this.mImService.registerForImIncomingMessage(this, 4, (Object) null);
        this.mImService.registerForImIncomingFileTransfer(this, 5, (Object) null);
        this.mImService.registerForComposingNotification(this, 6, (Object) null);
        this.mImService.registerForImdnNotification(this, 7, (Object) null);
        this.mImService.registerForMessageFailed(this, 8, (Object) null);
        this.mImService.registerForConferenceInfoUpdate(this, 10, (Object) null);
        this.mImService.registerForImdnResponse(this, 21, (Object) null);
        this.mImService.registerForImdnFailed(this, 14, (Object) null);
        this.mImService.registerForTransferProgress(this, 20, (Object) null);
        this.mImService.registerForMessageRevokeResponse(this, 27, (Object) null);
        this.mImService.registerForSendMessageRevokeDone(this, 28, (Object) null);
        this.mImService.registerForChatbotAnonymizeResp(this, 31, (Object) null);
        this.mImService.registerForChatbotAnonymizeNotify(this, 32, (Object) null);
        this.mImService.registerForChatbotAsSpamNotify(this, 30, (Object) null);
        this.mSlmService.registerForSlmIncomingMessage(this, 11, (Object) null);
        this.mSlmService.registerForSlmIncomingFileTransfer(this, 12, (Object) null);
        this.mSlmService.registerForSlmImdnNotification(this, 13, (Object) null);
        this.mSlmService.registerForSlmTransferProgress(this, 20, (Object) null);
        this.mSlmService.registerForSlmLMMIncomingSession(this, 22, (Object) null);
    }

    public void stop() {
        super.stop();
        Log.i(LOG_TAG, "stop()");
        this.mImService.unregisterForImIncomingSession(this);
        this.mImService.unregisterForImSessionEstablished(this);
        this.mImService.unregisterForImSessionClosed(this);
        this.mImService.unregisterForImIncomingMessage(this);
        this.mImService.unregisterForImIncomingFileTransfer(this);
        this.mImService.unregisterForComposingNotification(this);
        this.mImService.unregisterForImdnNotification(this);
        this.mImService.unregisterForMessageFailed(this);
        this.mImService.unregisterForConferenceInfoUpdate(this);
        this.mImService.unregisterForImdnResponse(this);
        this.mImService.unregisterForImdnFailed(this);
        this.mImService.unregisterForTransferProgress(this);
        this.mImService.unregisterForMessageRevokeResponse(this);
        this.mImService.unregisterForSendMessageRevokeDone(this);
        this.mImService.unregisterForChatbotAnonymizeNotify(this);
        this.mImService.unregisterForChatbotAnonymizeResp(this);
        this.mImService.unregisterForChatbotAsSpamNotify(this);
        this.mSlmService.unregisterForSlmIncomingMessage(this);
        this.mSlmService.unregisterForSlmIncomingFileTransfer(this);
        this.mSlmService.unregisterForSlmImdnNotification(this);
        this.mSlmService.unregisterForSlmTransferProgress(this);
        this.mSlmService.unregisterForSlmLMMIncomingSession(this);
        handleEventDeregistered((ImsRegistration) null);
    }

    public void onConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "onConfigured: phoneId = " + phoneId);
        sendMessage(obtainMessage(17, Integer.valueOf(phoneId)));
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
        int phoneId = regiInfo.getPhoneId();
        String str = LOG_TAG;
        Log.i(str, "onRegistered() phoneId = " + phoneId + ", regiInfo = " + regiInfo);
        ImDump imDump = this.mImDump;
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistered: ");
        sb.append(regiInfo.getServices());
        imDump.addEventLogs(sb.toString());
        this.mUriGenerators.put(phoneId, UriGeneratorFactory.getInstance().get(regiInfo.getPreferredImpu().getUri()));
        this.mRegistrationTypes.put(phoneId, Integer.valueOf(ImsRegistry.getRegistrationManager().getCurrentNetworkByPhoneId(phoneId)));
        if (getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            Iterator it = regiInfo.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr addr = (NameAddr) it.next();
                if (addr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    this.mUriGenerators.put(phoneId, UriGeneratorFactory.getInstance().get(addr.getUri()));
                    break;
                }
            }
        }
        sendMessage(obtainMessage(15, regiInfo));
    }

    public void onDeregistering(ImsRegistration regiInfo) {
        super.onDeregistering(regiInfo);
        Log.i(LOG_TAG, "onDeregistering");
        int phoneId = this.mDefaultPhoneId;
        if (regiInfo != null) {
            phoneId = regiInfo.getPhoneId();
        }
        this.mRegistrationTypes.remove(phoneId);
        String imsi = getImsiFromPhoneId(phoneId);
        if (imsi != null) {
            for (ImSession session : this.mCache.getAllImSessions()) {
                if (imsi.equals(session.getOwnImsi())) {
                    session.forceCloseSession();
                }
            }
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        int phoneId = this.mDefaultPhoneId;
        if (regiInfo != null) {
            phoneId = regiInfo.getPhoneId();
        }
        String str = LOG_TAG;
        Log.i(str, "onDeregistered() phoneId : " + phoneId + ", errorCode :" + errorCode + ", regiInfo : " + regiInfo);
        ImDump imDump = this.mImDump;
        StringBuilder sb = new StringBuilder();
        sb.append("onDeregistered: ");
        sb.append(regiInfo.getServices());
        sb.append(", error=");
        sb.append(errorCode);
        imDump.addEventLogs(sb.toString());
        if (getImsRegistration(phoneId) == null) {
            Log.i(LOG_TAG, "onDeregistered() : already deregistered.");
            return;
        }
        this.mRegistrationTypes.remove(phoneId);
        this.mHasIncomingSessionForA2P.put(phoneId, false);
        sendMessage(obtainMessage(16, errorCode, 0, regiInfo));
        super.onDeregistered(regiInfo, errorCode);
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
        super.onNetworkChanged(event, phoneId);
        String str = LOG_TAG;
        Log.i(str, "onNetworkChanged phoneId:" + phoneId + ", to " + event);
        if (event.isWifiConnected != this.mIsWifiConnected && !getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED)) {
            onWifiConnectionChanged(event.isWifiConnected, phoneId);
        }
        if (event.isDataRoaming != this.mIsDataRoamings.get(phoneId).booleanValue()) {
            onDataRoamingChanged(event.isDataRoaming, phoneId);
        }
        if (event.outOfService != this.mIsOutOfServices.get(phoneId).booleanValue()) {
            onOutOfServiceChanged(event.outOfService, event.isDataStateConnected, phoneId);
        } else if (event.isDataStateConnected && !event.outOfService && this.mIsDataStateConnected.get(phoneId).booleanValue() != event.isDataStateConnected) {
            onOutOfServiceChanged(event.outOfService, event.isDataStateConnected, phoneId);
        }
        updateFeatures(phoneId);
    }

    /* access modifiers changed from: private */
    public void onWifiConnectionChanged(boolean isWifiConnected, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "onWifiConnectionChanged: " + isWifiConnected);
        this.mIsWifiConnected = isWifiConnected;
    }

    private void onDataRoamingChanged(boolean isDataRoaming, int phoneId) {
        this.mIsDataRoamings.put(phoneId, Boolean.valueOf(isDataRoaming));
        int accept = ImUserPreference.getInstance().getFtAutAccept(this.mContext, phoneId);
        String str = LOG_TAG;
        Log.i(str, "onDataRoamingChanged: ft aut accept=" + accept + " isRoaming=" + isDataRoaming);
        this.mConfigs.get(phoneId).setFtAutAccept(this.mContext, accept, isDataRoaming);
    }

    private void onOutOfServiceChanged(boolean outOfService, boolean dataConnectionState, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onOutOfServiceChanged:" + outOfService);
        this.mIsOutOfServices.put(phoneId, Boolean.valueOf(outOfService));
        this.mIsDataStateConnected.put(phoneId, Boolean.valueOf(dataConnectionState));
        boolean needToAccessIms = false;
        if (!outOfService && isRegistered(phoneId) && !getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
            boolean needToAccessInternet = this.mConfigs.get(phoneId).isFtHttpOverDefaultPdn() && isDefaultPdnConnected();
            if (!this.mConfigs.get(phoneId).isFtHttpOverDefaultPdn() && getRcsStrategy(phoneId).intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY) == 4 && isImsPdnConnected(phoneId)) {
                needToAccessIms = true;
            }
            if (needToAccessInternet || needToAccessIms) {
                for (ImSession session : this.mCache.getAllImSessions()) {
                    session.processPendingFtHttp(phoneId);
                }
            }
        } else if (outOfService && isRegistered(phoneId)) {
            for (ImSession session2 : this.mCache.getAllImSessions()) {
                session2.processCancelMessages(false, ImError.OUTOFSERVICE);
            }
        }
    }

    public void registerChatEventListener(IChatEventListener listener) {
        this.mImSessionProcessor.registerChatEventListener(listener);
    }

    public ImSession getImSession(String chatId) {
        return this.mCache.getImSession(chatId);
    }

    public Future<ImSession> createChat(List<ImsUri> participants, String subject, String sdpContentType, int threadId, String requestMessageId) {
        return this.mImSessionProcessor.createChat(participants, subject, sdpContentType, threadId, requestMessageId);
    }

    public void readMessages(String cid, List<String> list) {
        this.mImSessionProcessor.readMessages(cid, list);
    }

    public FutureTask<Boolean> deleteChats(List<String> list, boolean isLocalWipeout) {
        return this.mImSessionProcessor.deleteChats(list, isLocalWipeout);
    }

    public FtMessage getFtMessage(int ftMessageId) {
        return this.mCache.getFtMessage(ftMessageId);
    }

    public void setAutoAcceptFt(int accept) {
        this.mFtProcessor.setAutoAcceptFt(this.mDefaultPhoneId, accept);
    }

    public void setAutoAcceptFt(int phoneId, int accept) {
        this.mFtProcessor.setAutoAcceptFt(phoneId, accept);
    }

    public FutureTask<Boolean> deleteChatsForUnsubscribe() {
        return this.mImSessionProcessor.deleteChatsForUnsubscribe();
    }

    public void resendMessage(int msgId) {
        this.mImProcessor.resendMessage(msgId);
    }

    public void addParticipants(String chatId, List<ImsUri> participants) {
        this.mImSessionProcessor.addParticipants(chatId, participants);
    }

    public void closeChat(String cid) {
        this.mImSessionProcessor.closeChat(cid);
    }

    public void resumeSendingTransfer(int messageId, boolean isResizable) {
        this.mFtProcessor.resumeSendingTransfer(messageId, isResizable);
    }

    public void resumeReceivingTransfer(int messageId) {
        this.mFtProcessor.resumeReceivingTransfer(messageId);
    }

    public boolean hasEstablishedSession() {
        return this.mImSessionProcessor.hasEstablishedSession();
    }

    public void registerMessageEventListener(ImConstants.Type type, IMessageEventListener listener) {
        this.mImProcessor.registerMessageEventListener(type, listener);
    }

    public void registerFtEventListener(ImConstants.Type type, IFtEventListener listener) {
        this.mFtProcessor.registerFtEventListener(type, listener);
    }

    public ImConfig getImConfig() {
        return this.mConfigs.get(this.mDefaultPhoneId);
    }

    public ImConfig getImConfig(int phoneId) {
        return this.mConfigs.get(phoneId);
    }

    public Future<FtMessage> attachFileToSingleChat(int phoneId, String filePath, ImsUri contactUri, Set<NotificationStatus> dispositionNotification, String requestMessageId, String contentType, boolean isprotectedAccountMsg, boolean isResizable, boolean isExtraft, boolean isFtSms, String extInfo, FileDisposition fileDisposition) {
        return this.mFtProcessor.attachFileToSingleChat(phoneId, filePath, contactUri, dispositionNotification, requestMessageId, contentType, isprotectedAccountMsg, isResizable, isExtraft, isFtSms, extInfo, fileDisposition);
    }

    public Future<FtMessage> attachFileToGroupChat(String chatId, String filePath, Set<NotificationStatus> dispositionNotification, String requestMessageId, String contentType, boolean isResizable, boolean isBroadcast, boolean isExtraFt, boolean isFtSms, String extInfo, FileDisposition fileDisposition) {
        return this.mFtProcessor.attachFileToGroupChat(chatId, filePath, dispositionNotification, requestMessageId, contentType, isResizable, isBroadcast, isExtraFt, isFtSms, extInfo, fileDisposition);
    }

    public void sendFile(long messageId) {
        this.mFtProcessor.sendFile(messageId);
    }

    public void acceptFileTransfer(int messageId) {
        this.mFtProcessor.acceptFileTransfer(messageId);
    }

    public void cancelFileTransfer(int messageId) {
        this.mFtProcessor.cancelFileTransfer(messageId);
    }

    public FutureTask<Boolean> deleteMessages(List<String> list, boolean isLocalWipeout) {
        return this.mImProcessor.deleteMessages(list, isLocalWipeout);
    }

    public FutureTask<Boolean> deleteMessagesByImdnId(Map<String, Integer> imdnIds, boolean isLocalWipeout) {
        return this.mImProcessor.deleteMessagesByImdnId(imdnIds, isLocalWipeout);
    }

    public void rejectFileTransfer(int messageId) {
        this.mFtProcessor.rejectFileTransfer(messageId);
    }

    /* access modifiers changed from: protected */
    public IImServiceInterface getImHandler() {
        return this.mImService;
    }

    /* access modifiers changed from: protected */
    public Integer onRequestRegistrationType() {
        if (getImsRegistration() == null) {
            return null;
        }
        boolean isEpdg = false;
        if (this.mRegistrationTypes.get(this.mDefaultPhoneId) != null && this.mRegistrationTypes.get(this.mDefaultPhoneId).intValue() == 18) {
            isEpdg = true;
        }
        String str = LOG_TAG;
        Log.i(str, "is device registered over epdg: " + isEpdg);
        return this.mRegistrationTypes.get(this.mDefaultPhoneId);
    }

    /* access modifiers changed from: protected */
    public boolean isRegistered() {
        return getImsRegistration() != null;
    }

    /* access modifiers changed from: protected */
    public boolean isRegistered(int phoneId) {
        return getImsRegistration(phoneId) != null;
    }

    public boolean isServiceRegistered(int phoneId, String service) {
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration == null || service == null) {
            return false;
        }
        String str = LOG_TAG;
        Log.i(str, "isServiceRegistered:" + service + ":" + imsRegistration.getServices());
        return imsRegistration.hasService(service);
    }

    public void handleIntent(Intent intent) {
        if (intent.hasCategory(ImIntent.CATEGORY_ACTION)) {
            this.mImTranslation.handleIntent(intent);
        } else if (intent.hasCategory(FtIntent.CATEGORY_ACTION)) {
            this.mFtTranslation.handleIntent(intent);
        }
    }

    /* access modifiers changed from: protected */
    public int getPhoneIdByChatId(String cid) {
        ImSession session = this.mCache.getImSession(cid);
        if (session != null) {
            return getPhoneIdByIMSI(session.getOwnImsi());
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getPhoneIdByIMSI(String imsi) {
        if (imsi == null) {
            return this.mDefaultPhoneId;
        }
        int phoneId = SimManagerFactory.getPhoneId(imsi);
        return phoneId != -1 ? phoneId : this.mDefaultPhoneId;
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock(Object rawHandle) {
        String str = LOG_TAG;
        Log.i(str, "acquireWakeLock: " + rawHandle);
        this.mWakeLock.acquire(DEFAULT_WAKE_LOCK_TIMEOUT);
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock(Object rawHandle) {
        if (this.mWakeLock.isHeld()) {
            String str = LOG_TAG;
            Log.i(str, "releaseWakeLock: " + rawHandle);
            this.mWakeLock.release();
        }
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        boolean isSuccess = true;
        switch (msg.what) {
            case 1:
                this.mImSessionProcessor.onIncomingSessionReceived((ImIncomingSessionEvent) ((AsyncResult) msg.obj).result);
                return;
            case 2:
                this.mImSessionProcessor.onSessionEstablished((ImSessionEstablishedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 3:
                this.mImSessionProcessor.onSessionClosed((ImSessionClosedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 4:
                this.mImProcessor.onIncomingMessageReceived((ImIncomingMessageEvent) ((AsyncResult) msg.obj).result);
                return;
            case 5:
            case 12:
                this.mFtProcessor.onIncomingFileTransferReceived((FtIncomingSessionEvent) ((AsyncResult) msg.obj).result);
                return;
            case 6:
                this.mImSessionProcessor.onComposingNotificationReceived((ImComposingEvent) ((AsyncResult) msg.obj).result);
                return;
            case 7:
            case 13:
                this.mImSessionProcessor.onImdnNotificationReceived((ImdnNotificationEvent) ((AsyncResult) msg.obj).result);
                return;
            case 8:
                this.mImProcessor.onSendMessageHandleReportFailed((SendMessageFailedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 9:
                this.mImProcessor.onProcessPendingMessages(((Integer) msg.obj).intValue());
                return;
            case 10:
                this.mImSessionProcessor.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) ((AsyncResult) msg.obj).result);
                return;
            case 11:
                this.mImProcessor.onIncomingSlmMessage((SlmIncomingMessageEvent) ((AsyncResult) msg.obj).result);
                return;
            case 14:
                this.mImSessionProcessor.onSendImdnFailed((SendImdnFailedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 15:
                handleEventRegistered((ImsRegistration) msg.obj);
                return;
            case 16:
                handleEventDeregistered((ImsRegistration) msg.obj);
                return;
            case 17:
                handleEventConfigured(((Integer) msg.obj).intValue());
                return;
            case 18:
                handleEventMessageAppChanged();
                return;
            case 19:
                handleEventProcessRejoinGCSession(((Integer) msg.obj).intValue());
                return;
            case 20:
                this.mFtProcessor.handleFileTransferProgress((FtTransferProgressEvent) ((AsyncResult) msg.obj).result);
                return;
            case 21:
                String str = LOG_TAG;
                Log.i(str, "EVENT_IMDN_RESPONSE_RECEIVED : " + ((AsyncResult) msg.obj).result);
                return;
            case 22:
                this.mImSessionProcessor.onIncomingSlmLMMSessionReceived((SlmLMMIncomingSessionEvent) ((AsyncResult) msg.obj).result);
                return;
            case 23:
                handleEventResumePendingHttpFtOperations(((Integer) msg.obj).intValue());
                return;
            case 24:
                handleEventAbortOngoingHttpFtOperation(((Integer) msg.obj).intValue());
                return;
            case 25:
                this.mImSessionProcessor.handleEventBlocklistChanged();
                return;
            case 27:
                this.mImSessionProcessor.getImRevocationHandler().onMessageRevokeResponseReceived((MessageRevokeResponse) ((AsyncResult) msg.obj).result);
                return;
            case 28:
                this.mImSessionProcessor.getImRevocationHandler().onSendMessageRevokeRequestDone((MessageRevokeResponse) ((AsyncResult) msg.obj).result);
                return;
            case 29:
                handleDDSChange();
                return;
            case 30:
                ReportChatbotAsSpamRespEvent params = (ReportChatbotAsSpamRespEvent) ((AsyncResult) msg.obj).result;
                if (params.mError != ImError.SUCCESS) {
                    isSuccess = false;
                }
                this.mImTranslation.onReportChatbotAsSpamRespReceived(params.mUri, isSuccess, params.mRequestId);
                return;
            case 31:
                handleEventRequestChatbotAnonymizeResponse((ChatbotAnonymizeRespEvent) ((AsyncResult) msg.obj).result);
                return;
            case 32:
                ChatbotAnonymizeNotifyEvent params2 = (ChatbotAnonymizeNotifyEvent) ((AsyncResult) msg.obj).result;
                this.mImTranslation.onRequestChatbotAnonymizeNotiReceived(params2.mChatbotUri, params2.mResult, params2.mCommandId);
                return;
            case 33:
                if (ImsRegistry.isReady()) {
                    ImsRegistry.startAutoConfig(true, (Message) null);
                    return;
                }
                return;
            case 34:
                onSimRefresh(((Integer) ((AsyncResult) msg.obj).result).intValue());
                return;
            case 35:
                int msgId = ((Integer) msg.obj).intValue();
                if (this.mNeedToRemoveFromPendingList.remove(Integer.valueOf(msgId))) {
                    this.mCache.removeFromPendingList(msgId);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleEventConfigured(int phoneId) {
        ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (simManager != null && !TextUtils.isEmpty(simManager.getLine1Number())) {
            this.mOwnPhoneNumbers.put(phoneId, simManager.getLine1Number());
        }
        String str = LOG_TAG;
        IMSLog.s(str, "mSimCardManager own number is: " + this.mOwnPhoneNumbers.get(phoneId));
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (!(tm == null || tm.isNetworkRoaming() == this.mIsDataRoamings.get(phoneId).booleanValue())) {
            onDataRoamingChanged(tm.isNetworkRoaming(), phoneId);
        }
        ImsProfile profile = ImsRegistry.getRegistrationManager().getImsProfile(phoneId, ImsProfile.PROFILE_TYPE.CHAT);
        if (((Boolean) Optional.ofNullable(profile).map($$Lambda$ImModule$hIBmCEat4ubV0CUWhroSeXwK6w.INSTANCE).orElse(true)).booleanValue()) {
            Log.e(LOG_TAG, "profile is null, return !!!");
            return;
        }
        this.mRcsProfile = ConfigUtil.getRcsProfileWithFeature(this.mContext, phoneId, profile);
        this.mConfigs.get(phoneId).load(this.mContext, this.mRcsProfile, this.mIsDataRoamings.get(phoneId).booleanValue());
        String str2 = LOG_TAG;
        IMSLog.i(str2, "ImConfig loaded. " + this.mConfigs.get(phoneId));
        this.mCache.initializeLruCache(this.mConfigs.get(phoneId).getMaxConcurrentSession());
        updateFeatures(phoneId);
        if (this.mInternetListeners.get(phoneId) != null) {
            return;
        }
        if ((this.mConfigs.get(phoneId).isFtHttpOverDefaultPdn() && getRcsStrategy(phoneId).isFTHTTPAutoResumeAndCancelPerConnectionChange()) || getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED)) {
            setNetworkCallback(phoneId);
            registerDefaultNetworkCallback(phoneId);
        }
    }

    static /* synthetic */ Boolean lambda$handleEventConfigured$0(ImsProfile p) {
        return Boolean.valueOf(!p.hasService("im") && !p.hasService("slm"));
    }

    private void handleEventRegistered(ImsRegistration registration) {
        int phoneId = registration != null ? registration.getPhoneId() : this.mDefaultPhoneId;
        this.mDefaultPhoneId = SimUtil.getSimSlotPriority();
        if (registration != null) {
            updateOwnPhoneNumberOnRegi(phoneId, registration);
            if (this.mConfigs.get(phoneId).isEnableGroupChatListRetrieve()) {
                if (this.mGroupChatRetrievingHandlers.get(phoneId) == null) {
                    this.mGroupChatRetrievingHandlers.put(phoneId, new GroupChatRetrievingHandler(getLooper(), getContext(), this.mCache, this.mImTranslation, getImHandler(), getOwnPhoneNum(phoneId), getImsiFromPhoneId(phoneId)));
                }
                this.mGroupChatRetrievingHandlers.get(phoneId).startToRetrieveGroupChatList();
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, "mImRegistration own number is: " + this.mOwnPhoneNumbers.get(phoneId));
        if (this.mOwnPhoneNumbers.get(phoneId) == null || this.mOwnPhoneNumbers.get(phoneId).isEmpty()) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm != null) {
                this.mOwnPhoneNumbers.put(phoneId, sm.getImsi());
            }
            String str2 = LOG_TAG;
            IMSLog.s(str2, "When own number is not available through telephonyManager or RegistrationManager, we use imsi. TelephonyManager imsi: " + this.mOwnPhoneNumbers.get(phoneId));
        }
        if (isRequiredServicesRegistered(registration)) {
            if (!RcsUtils.DualRcs.isDualRcsReg() && getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.UPDATE_SESSION_AFTER_REGISTRATION)) {
                for (ImSession session : this.mCache.getActiveSessions()) {
                    if (session.getOwnImsi() != null && !session.getOwnImsi().equals(getImsiFromPhoneId(phoneId))) {
                        session.closeSession();
                    }
                }
            }
            this.mRcsProfile = ConfigUtil.getRcsProfileWithFeature(this.mContext, phoneId, registration.getImsProfile());
            if (getRegistration(phoneId) != null && !getRegistration(phoneId).isReRegi()) {
                if (getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USERAGENT_HAS_MSGAPPVERSION)) {
                    setAppVersionToSipUserAgent(registration);
                }
                processPendingMessagesOnRegi(phoneId, registration);
                this.mImSessionProcessor.getImRevocationHandler().startReconnectGuardTiemer(phoneId);
            }
            this.mCache.updateUriGenerator(phoneId);
        }
    }

    private void handleEventDeregistered(ImsRegistration registration) {
        int phoneId = this.mDefaultPhoneId;
        if (registration != null) {
            phoneId = registration.getPhoneId();
        }
        this.mImSessionProcessor.getImRevocationHandler().stopReconnectGuardTimer(phoneId);
        String imsi = getImsiFromPhoneId(phoneId);
        if (imsi != null) {
            for (ImSession session : this.mCache.getAllImSessions()) {
                if (imsi.equals(session.getOwnImsi())) {
                    session.processDeregistration();
                }
            }
        }
        this.mImService.unregisterAllFileTransferProgress();
        this.mSlmService.unregisterAllSLMFileTransferProgress();
        this.mCache.clear();
        this.mDefaultPhoneId = SimUtil.getSimSlotPriority();
        if (this.mMessagingAppInfoReceiver != null && getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USERAGENT_HAS_MSGAPPVERSION)) {
            this.mMessagingAppInfoReceiver.unregisterReceiver();
        }
    }

    private void handleEventMessageAppChanged() {
        Mno mMno = Mno.fromSalesCode(OmcCode.get());
        Log.i(LOG_TAG, "handleEventMessageAppChanged");
        if (!mMno.isEur() && !mMno.isMea()) {
            updateFeatures(this.mDefaultPhoneId);
        }
        if (!isDefaultMessageAppInUse()) {
            this.mImService.unregisterAllFileTransferProgress();
            this.mSlmService.unregisterAllSLMFileTransferProgress();
            for (ImSession session : this.mCache.getAllImSessions()) {
                session.closeSession();
                session.cancelPendingFilesInQueue();
            }
        }
    }

    private boolean isRequiredServicesRegistered(ImsRegistration registration) {
        if (registration != null) {
            for (String service : sRequiredServices) {
                if (registration.hasService(service)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateFeatures(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "updateFeatures: phoneId = " + phoneId);
        this.mEnabledFeatures[phoneId] = this.mFeatureUpdater.updateFeatures(phoneId, getImConfig(phoneId));
        updateExtendedBotMsgFeature(phoneId);
    }

    public void updateExtendedBotMsgFeature(int phoneId) {
        Mno simMno = SimUtil.getSimMno(phoneId);
        ServiceTuple botMsg = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
        String str = LOG_TAG;
        Log.i(str, "FEATURE_CHATBOT_EXTENDED_MSG enabled ver:" + botMsg.version);
        if (!simMno.isKor() || TextUtils.equals("0.0", botMsg.version)) {
            long[] jArr = this.mEnabledFeatures;
            jArr[phoneId] = jArr[phoneId] & (~Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
        } else if (CapabilityUtil.hasFeature(this.mEnabledFeatures[phoneId], Capabilities.FEATURE_CHATBOT_CHAT_SESSION) || CapabilityUtil.hasFeature(this.mEnabledFeatures[phoneId], Capabilities.FEATURE_CHATBOT_STANDALONE_MSG)) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[phoneId] = jArr2[phoneId] | Capabilities.FEATURE_CHATBOT_EXTENDED_MSG;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDeviceOutOfMemory() {
        ImTranslation imTranslation = this.mImTranslation;
        Objects.requireNonNull(imTranslation);
        post(new Runnable() {
            public final void run() {
                ImTranslation.this.onDeviceOutOfMemory();
            }
        });
    }

    /* access modifiers changed from: protected */
    public RoutingType getMsgRoutingType(ImsUri requestUri, ImsUri pAssertedId, ImsUri sender, ImsUri receiver, boolean isGroupchat, int phoneId) {
        if (requestUri == null || !requestUri.toString().contains(this.mOwnPhoneNumbers.get(phoneId))) {
            requestUri = ImsUri.parse("tel:" + this.mOwnPhoneNumbers.get(phoneId));
        }
        return getRcsStrategy(phoneId).getMsgRoutingType(requestUri, pAssertedId, sender, receiver, isGroupchat);
    }

    /* access modifiers changed from: protected */
    public String getOwnPhoneNum() {
        return this.mOwnPhoneNumbers.get(this.mDefaultPhoneId);
    }

    /* access modifiers changed from: protected */
    public String getOwnPhoneNum(int phoneId) {
        return this.mOwnPhoneNumbers.get(phoneId);
    }

    /* access modifiers changed from: protected */
    public boolean isOwnNumberChanged(ImSession session) {
        if (session == null) {
            Log.i(LOG_TAG, "isOwnNumberChanged: Invalid session.");
            return false;
        }
        int phoneId = getPhoneIdByIMSI(session.getOwnImsi());
        String ownImsi = getImsiFromPhoneId(phoneId);
        if (TextUtils.isEmpty(this.mOwnPhoneNumbers.get(phoneId)) || TextUtils.isEmpty(ownImsi)) {
            Log.i(LOG_TAG, "isOwnNumberChanged: Invalid value.");
            return false;
        }
        if (!TextUtils.equals(this.mOwnPhoneNumbers.get(phoneId), session.getOwnPhoneNum())) {
            List<String> dumps = new ArrayList<>();
            if (TextUtils.equals(ownImsi, this.mOwnPhoneNumbers.get(phoneId))) {
                dumps.add("IMSI");
                ImsUtil.listToDumpFormat(LogClass.IM_OWNNUMBER_CHANGED, phoneId, session.getChatId(), dumps);
                return true;
            } else if (!TextUtils.equals(ownImsi, session.getOwnPhoneNum())) {
                ImsUri ownUri = this.mUriGenerators.get(phoneId).getNormalizedUri(this.mOwnPhoneNumbers.get(phoneId), true);
                ImsUri prevUri = this.mUriGenerators.get(phoneId).getNormalizedUri(session.getOwnPhoneNum(), true);
                dumps.add("MDN");
                dumps.add((ownUri == null || ownUri.equals(prevUri)) ? "0" : "1");
                ImsUtil.listToDumpFormat(LogClass.IM_OWNNUMBER_CHANGED, phoneId, session.getChatId(), dumps);
                if (ownUri == null || ownUri.equals(prevUri)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isWifiConnected() {
        return this.mIsWifiConnected;
    }

    public ImsUri normalizeUri(ImsUri uri) {
        return normalizeUri(this.mDefaultPhoneId, uri);
    }

    /* access modifiers changed from: protected */
    public ImsUri normalizeUri(int phoneId, ImsUri uri) {
        if (this.mUriGenerators.get(phoneId) == null) {
            return uri;
        }
        ImsUri normalizedUri = this.mUriGenerators.get(phoneId).normalize(uri);
        if (normalizedUri != null && normalizedUri.getUriType() == ImsUri.UriType.TEL_URI) {
            normalizedUri.removeTelParams();
        }
        return normalizedUri;
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> normalizeUri(int phoneId, Collection<ImsUri> uris) {
        Set<ImParticipantUri> set = new HashSet<>();
        Set<ImsUri> ret = new HashSet<>();
        for (ImsUri uri : uris) {
            ImsUri normalizedUri = normalizeUri(phoneId, uri);
            if (normalizedUri == null || !set.add(new ImParticipantUri(normalizedUri))) {
                Log.e(LOG_TAG, "normalizeUri(Collection): normalized Uri is null. Ignored.");
            } else {
                if (normalizedUri.getUriType() == ImsUri.UriType.TEL_URI) {
                    normalizedUri.removeTelParams();
                }
                ret.add(normalizedUri);
            }
        }
        return ret;
    }

    public void registerServiceAvailabilityEventListener(IServiceAvailabilityEventListener listener) throws NullPointerException, IllegalStateException {
        Preconditions.checkNotNull(listener);
        Preconditions.checkState(this.mServiceAvailabilityEventListener == null, "ServiceAvailabilityEventListener is already registered");
        this.mServiceAvailabilityEventListener = listener;
        Log.i(LOG_TAG, "registered ServiceAvailabilityEventListener");
    }

    public void unregisterServiceAvailabilityEventListener(IServiceAvailabilityEventListener listener) throws IllegalStateException, IllegalArgumentException {
        boolean z = true;
        Preconditions.checkState(this.mServiceAvailabilityEventListener != null, "There is no ServiceAvailabilityEventListener registered");
        if (this.mServiceAvailabilityEventListener != listener) {
            z = false;
        }
        Preconditions.checkArgument(z, "It is not possible to unregister different instance of a listener than previously registered");
        this.mServiceAvailabilityEventListener = null;
        Log.i(LOG_TAG, "ServiceAvailabilityEventListener unregistered");
    }

    /* access modifiers changed from: protected */
    public void updateServiceAvailability(String ownIdentity, ImsUri remoteUri, Date timestamp) {
        IServiceAvailabilityEventListener iServiceAvailabilityEventListener;
        if (ownIdentity == null || remoteUri == null || timestamp == null || (iServiceAvailabilityEventListener = this.mServiceAvailabilityEventListener) == null) {
            String str = LOG_TAG;
            Log.i(str, "Service availability cannot be updated, ownIdentity = " + IMSLog.checker(ownIdentity) + ", remoteUri = " + IMSLog.checker(remoteUri) + ", timestamp = " + timestamp + ", mServiceAvailabilityEventListener = " + this.mServiceAvailabilityEventListener);
            return;
        }
        iServiceAvailabilityEventListener.onServiceAvailabilityUpdate(ownIdentity, remoteUri, timestamp);
    }

    /* access modifiers changed from: protected */
    public void setConfig(ImConfig config) {
        this.mConfigs.put(this.mDefaultPhoneId, config);
    }

    /* access modifiers changed from: protected */
    public UriGenerator getUriGenerator() {
        return getUriGenerator(this.mDefaultPhoneId);
    }

    /* access modifiers changed from: protected */
    public UriGenerator getUriGenerator(int phoneId) {
        return this.mUriGenerators.get(phoneId);
    }

    /* access modifiers changed from: protected */
    public String getUserAlias(int phoneId) {
        String alias = this.mConfigs.get(phoneId).getUserAlias();
        IMnoStrategy mnoStrategy = getRcsStrategy(phoneId);
        if (mnoStrategy == null || !mnoStrategy.dropUnsupportedCharacter(alias)) {
            return alias;
        }
        return "";
    }

    public void setUserAlias(int phoneId, String alias) {
        if (!RcsUtils.DualRcs.isDualRcsSettings()) {
            phoneId = this.mDefaultPhoneId;
        }
        this.mConfigs.get(phoneId).setUserAlias(this.mContext, alias);
    }

    public String getUserAliasFromPreference(int phoneId) {
        return this.mConfigs.get(phoneId).getUserAliasFromPreference(this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean notifyRCSMessages() {
        IMnoStrategy mnoStrategy = getRcsStrategy();
        return mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.NOTIFY_RCS_MSG);
    }

    /* access modifiers changed from: protected */
    public void sendModuleResponse(Message msg, int result, Object obj) {
        Message resp = (Message) msg.getData().getParcelable("callback_msg");
        if (resp != null && (resp.obj instanceof ModuleChannel.Listener)) {
            resp.arg1 = result;
            resp.obj = new Object[]{(ModuleChannel.Listener) resp.obj, obj};
            resp.sendToTarget();
        }
    }

    private void setNetworkCallback(final int phoneId) {
        this.mInternetListeners.put(phoneId, new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                if (phoneId == SimUtil.getDefaultPhoneId()) {
                    String access$000 = ImModule.LOG_TAG;
                    Log.i(access$000, "INET  : onAvailable, phoneId : " + phoneId);
                    boolean unused = ImModule.this.mInternetAvailable = true;
                    if (ImModule.this.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED)) {
                        handleNetworkForCancelFtWiFiDisconnection(network, true);
                    } else if (((ImConfig) ImModule.this.mConfigs.get(phoneId)).isFtHttpOverDefaultPdn()) {
                        ImModule imModule = ImModule.this;
                        imModule.sendMessage(imModule.obtainMessage(23, Integer.valueOf(phoneId)));
                    }
                }
            }

            public void onLost(Network network) {
                if (phoneId == SimUtil.getDefaultPhoneId()) {
                    String access$000 = ImModule.LOG_TAG;
                    Log.i(access$000, "INET : onLost, phoneId : " + phoneId);
                    boolean unused = ImModule.this.mInternetAvailable = false;
                    if (ImModule.this.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED)) {
                        handleNetworkForCancelFtWiFiDisconnection(network, false);
                    } else if (((ImConfig) ImModule.this.mConfigs.get(phoneId)).isFtHttpOverDefaultPdn()) {
                        ImModule imModule = ImModule.this;
                        imModule.sendMessageDelayed(imModule.obtainMessage(24, Integer.valueOf(phoneId)), ImModule.DEFAULT_WAKE_LOCK_TIMEOUT);
                    }
                }
            }

            private void handleNetworkForCancelFtWiFiDisconnection(Network network, boolean isAvailable) {
                NetworkCapabilities nc = ((ConnectivityManager) ImModule.this.mContext.getSystemService("connectivity")).getNetworkCapabilities(network);
                if (nc != null) {
                    boolean isWifi = nc.hasTransport(1);
                    String access$000 = ImModule.LOG_TAG;
                    int i = phoneId;
                    IMSLog.i(access$000, i, "handleNetworkForCancelFtWiFiDisconnection: isWifi=" + isWifi + ", isAvailable=" + isAvailable);
                    if (!ImModule.this.mIsWifiConnected && isWifi && isAvailable) {
                        ImModule.this.onWifiConnectionChanged(true, phoneId);
                    } else if (!ImModule.this.mIsWifiConnected) {
                    } else {
                        if ((isWifi && !isAvailable) || (!isWifi && isAvailable)) {
                            ImModule.this.onWifiConnectionChanged(false, phoneId);
                            for (ImSession session : ImModule.this.mCache.getAllImSessions()) {
                                session.forceCancelFt(false, CancelReason.WIFI_DISCONNECTED);
                            }
                        }
                    }
                }
            }
        });
    }

    private void registerDefaultNetworkCallback(int phoneId) {
        Log.i(LOG_TAG, "INET  : registerDefaultNetworkCallback");
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerDefaultNetworkCallback(this.mInternetListeners.get(phoneId));
    }

    public void handleEventDefaultAppChanged() {
        sendEmptyMessage(18);
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultMessageAppInUse() {
        IMnoStrategy mnoStrategy = getRcsStrategy();
        if (mnoStrategy == null || !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.ALWAYS_RCS_ON)) {
            String currentPackage = null;
            String samsungPackage = PackageUtils.getMsgAppPkgName(this.mContext);
            try {
                currentPackage = Telephony.Sms.getDefaultSmsPackage(this.mContext);
            } catch (Exception e) {
                String str = LOG_TAG;
                Log.e(str, "Failed to currentPackage: " + e);
            }
            boolean result = TextUtils.equals(currentPackage, samsungPackage);
            String str2 = LOG_TAG;
            Log.i(str2, "isDefaultMessageAppInUse : " + result);
            return result;
        }
        Log.i(LOG_TAG, "isDefaultMessageAppInUse : always true");
        return true;
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        Log.i(LOG_TAG, "onServiceSwitched");
        updateFeatures(phoneId);
    }

    public void registerImSessionListener(IImSessionListener listener) {
        this.mImSessionProcessor.registerImSessionListenerByPhoneId(listener, this.mDefaultPhoneId);
    }

    public void registerImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        this.mImSessionProcessor.registerImSessionListenerByPhoneId(listener, phoneId);
    }

    public void unregisterImSessionListener(IImSessionListener listener) {
        this.mImSessionProcessor.unregisterImSessionListenerByPhoneId(listener, this.mDefaultPhoneId);
    }

    public void unregisterImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        this.mImSessionProcessor.unregisterImSessionListenerByPhoneId(listener, phoneId);
    }

    public void registerImsOngoingFtListener(IImsOngoingFtEventListener listener) {
        this.mFtProcessor.registerImsOngoingFtListenerByPhoneId(listener, this.mDefaultPhoneId);
    }

    public void registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        this.mFtProcessor.registerImsOngoingFtListenerByPhoneId(listener, phoneId);
    }

    public void unregisterImsOngoingListener(IImsOngoingFtEventListener listener) {
        this.mFtProcessor.unregisterImsOngoingListenerByPhoneId(listener, this.mDefaultPhoneId);
    }

    public void unregisterImsOngoingListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        this.mFtProcessor.unregisterImsOngoingListenerByPhoneId(listener, phoneId);
    }

    public Future<ImMessage> sendMessage(String cid, String body, Set<NotificationStatus> disposition, String contentType, String requestMessageId, int messageNumber, boolean isBroadcastMsg, boolean isprotectedAccountMsg, boolean isGLSMsg, String deviceName, String reliableMessage, String xmsMessage, List<ImsUri> ccList, boolean isTemporary, String maapTrafficType, String referenceMessageId, String referenceMessageType, String referenceMessageValue) {
        return this.mImProcessor.sendMessage(cid, body, disposition, contentType, requestMessageId, messageNumber, isBroadcastMsg, isprotectedAccountMsg, isGLSMsg, deviceName, reliableMessage, xmsMessage, ccList, isTemporary, maapTrafficType, referenceMessageId, referenceMessageType, referenceMessageValue);
    }

    /* access modifiers changed from: protected */
    public boolean isNativeLine(int phoneId, ImsUri uri) {
        if (uri == null) {
            return false;
        }
        if (!isRegistered(phoneId)) {
            return true;
        }
        ImsUri myImpu = this.mUriGenerators.get(phoneId).getNormalizedUri(this.mOwnPhoneNumbers.get(phoneId), true);
        if (myImpu == null || myImpu.equals(normalizeUri(phoneId, uri))) {
            return true;
        }
        return false;
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        for (ImConfig config : this.mConfigs.values()) {
            IMSLog.dump(LOG_TAG, config.toString());
        }
        this.mImDump.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    public IMnoStrategy getRcsStrategy() {
        return RcsPolicyManager.getRcsStrategy(this.mDefaultPhoneId);
    }

    public IMnoStrategy getRcsStrategy(int phoneId) {
        return RcsPolicyManager.getRcsStrategy(phoneId);
    }

    public MessageBase getMessage(int id) {
        return this.mCache.getMessage(id);
    }

    public MessageBase getMessage(String imdnId, ImDirection direction) {
        return this.mCache.getMessage(imdnId, direction);
    }

    public List<MessageBase> getMessages(Collection<String> ids) {
        return this.mCache.getMessages(ids);
    }

    public MessageBase getPendingMessage(int id) {
        return this.mCache.getPendingMessage(id);
    }

    public List<MessageBase> getAllPendingMessages(String chatId) {
        return this.mCache.getAllPendingMessages(chatId);
    }

    public List<String> getMessageIdsForDisplayAggregation(String chatId, ImDirection direction, Long timestamp) {
        return this.mImSessionProcessor.getMessageIdsForDisplayAggregation(chatId, direction, timestamp);
    }

    public String onRequestIncomingFtTransferPath() {
        return this.mFtProcessor.onRequestIncomingFtTransferPath();
    }

    public void onMessagingAppPackageReplaced() {
        post(new Runnable() {
            public final void run() {
                ImModule.this.lambda$onMessagingAppPackageReplaced$1$ImModule();
            }
        });
    }

    public /* synthetic */ void lambda$onMessagingAppPackageReplaced$1$ImModule() {
        if (this.mMessagingAppInfoReceiver != null && getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.USERAGENT_HAS_MSGAPPVERSION)) {
            String version = this.mMessagingAppInfoReceiver.getMessagingAppVersion();
            String str = LOG_TAG;
            Log.i(str, "onMessagingAppPackageReplaced: " + version);
            Iterator it = this.mRegistrationList.iterator();
            while (it.hasNext()) {
                Registration reg = (Registration) it.next();
                if (!TextUtils.isEmpty(version)) {
                    this.mImService.setMoreInfoToSipUserAgent(version, reg.getImsRegi().getHandle());
                }
            }
        }
    }

    private boolean isImsPdnConnected(int phoneId) {
        Network network = null;
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration != null) {
            network = imsRegistration.getNetwork();
        }
        NetworkCapabilities nc = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkCapabilities(network);
        return nc != null && nc.hasCapability(4) && nc.hasTransport(0);
    }

    private boolean isDefaultPdnConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (networkCapabilities == null || !networkCapabilities.hasCapability(12) || (!networkCapabilities.hasTransport(0) && !networkCapabilities.hasTransport(1))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public Network getNetwork(boolean sFtHttpOverDefaultPdn, int phoneId) {
        ImsRegistration imsRegistration;
        if (sFtHttpOverDefaultPdn || getRcsStrategy(phoneId).intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY) != 4 || (imsRegistration = getImsRegistration(phoneId)) == null || sFtHttpOverDefaultPdn) {
            return null;
        }
        return imsRegistration.getNetwork();
    }

    public Network getNetwork(int phoneId) {
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration == null || getImConfig(phoneId).isFtHttpOverDefaultPdn()) {
            return null;
        }
        return imsRegistration.getNetwork();
    }

    public Set<ImsUri> getOwnUris(int phoneId) {
        Set<ImsUri> ownUris = new HashSet<>();
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration != null) {
            for (NameAddr addr : imsRegistration.getImpuList()) {
                ownUris.add(normalizeUri(phoneId, addr.getUri()));
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, "getOwnUris: " + ownUris);
        return ownUris;
    }

    public void reconfiguration(long[] timer) {
        if (!hasMessages(33) && timer.length > this.mCountReconfiguration) {
            sendMessageDelayed(obtainMessage(33), timer[this.mCountReconfiguration]);
            this.mCountReconfiguration++;
        }
    }

    private void setUpsmEventReceiver() {
        Log.i(LOG_TAG, "setUpsmEventReceiver.");
        if (!this.mIsSetUpsmEventReceiver) {
            Log.i(LOG_TAG, "register upsm event receiver.");
            IntentFilter upsmIntentFilter = new IntentFilter();
            upsmIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_STATE_CHANGED");
            upsmIntentFilter.addAction(SemEmergencyConstantsExt.EMERGENCY_CHECK_ABNORMAL_STATE);
            upsmIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_START_SERVICE_BY_ORDER");
            this.mContext.registerReceiver(this.mUpsmEventReceiver, upsmIntentFilter);
            this.mIsSetUpsmEventReceiver = true;
            SemEmergencyManager emergencyManager = SemEmergencyManager.getInstance(this.mContext);
            if (emergencyManager != null && SemEmergencyManager.isEmergencyMode(this.mContext) && SystemUtil.checkUltraPowerSavingMode(emergencyManager)) {
                Log.i(LOG_TAG, "upsm is already set, so send upsm event.");
                onUltraPowerSavingModeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUltraPowerSavingModeChanged() {
        postDelayed(new Runnable() {
            public final void run() {
                ImModule.this.lambda$onUltraPowerSavingModeChanged$2$ImModule();
            }
        }, 500);
    }

    public /* synthetic */ void lambda$onUltraPowerSavingModeChanged$2$ImModule() {
        Log.i(LOG_TAG, "onUltraPowerSavingModeChanged: update features");
        updateFeatures(this.mDefaultPhoneId);
    }

    public void requestChatbotAnonymize(int phoneId, ImsUri chatbotUri, String action, String commandId) {
        post(new Runnable(chatbotUri, phoneId, commandId, action) {
            public final /* synthetic */ ImsUri f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ String f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                ImModule.this.lambda$requestChatbotAnonymize$3$ImModule(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$requestChatbotAnonymize$3$ImModule(ImsUri chatbotUri, int phoneId, String commandId, String action) {
        String str = LOG_TAG;
        Log.i(str, "requestChatbotAnonymize : uri = " + IMSLog.checker(chatbotUri));
        if (ImsProfile.getRcsProfileType(this.mRcsProfile) < ImsProfile.RCS_PROFILE.UP_2_2.ordinal()) {
            String AnonymizeXml = ChatbotXmlUtils.getInstance().composeAnonymizeXml(action, commandId);
            ChatbotAnonymizeParams param = new ChatbotAnonymizeParams(phoneId != -1 ? phoneId : this.mDefaultPhoneId, chatbotUri, AnonymizeXml, commandId);
            String str2 = LOG_TAG;
            IMSLog.s(str2, "requestChatbotAnonymize : xml = " + AnonymizeXml);
            this.mImService.requestChatbotAnonymize(param);
        } else if (getImConfig(phoneId).getBotPrivacyDisable()) {
            Log.e(LOG_TAG, "requestChatbotAnonymize Privacy is disabled, Anonymization session doesnt exist");
        } else {
            this.mImService.requestChatbotAnonymize(new ChatbotAnonymizeParams(phoneId != -1 ? phoneId : this.mDefaultPhoneId, chatbotUri, "", commandId));
        }
    }

    public void reportChatbotAsSpam(int phoneId, String request_id, ImsUri chatbotUri, List<String> msgIds, String spamType, String freeText) {
        post(new Runnable(chatbotUri, request_id, msgIds, spamType, freeText, phoneId) {
            public final /* synthetic */ ImsUri f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ List f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ int f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            public final void run() {
                ImModule.this.lambda$reportChatbotAsSpam$4$ImModule(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
    }

    public /* synthetic */ void lambda$reportChatbotAsSpam$4$ImModule(ImsUri chatbotUri, String request_id, List msgIds, String spamType, String freeText, int phoneId) {
        if (chatbotUri == null) {
            this.mImTranslation.onReportChatbotAsSpamRespReceived((String) null, false, request_id);
            return;
        }
        String spamXml = ChatbotXmlUtils.getInstance().composeSpamXml(chatbotUri.toString(), msgIds, spamType, freeText);
        ReportChatbotAsSpamParams param = new ReportChatbotAsSpamParams(phoneId != -1 ? phoneId : this.mDefaultPhoneId, request_id, chatbotUri, spamXml);
        String str = LOG_TAG;
        Log.i(str, "reportChatbotAsSpam : uri = " + chatbotUri.toStringLimit() + ", xml = " + IMSLog.checker(spamXml));
        this.mImService.reportChatbotAsSpam(param);
    }

    /* access modifiers changed from: protected */
    public boolean hasChatbotParticipant(String chatId) {
        ImSession session = this.mCache.getImSession(chatId);
        return session != null && !session.isGroupChat() && ChatbotUriUtil.hasChatbotUri(session.getParticipantsUri());
    }

    public void onCallStateChanged(int phoneId, List<ICall> calls) {
        int nConnectedCalls = 0;
        this.mCallList.clear();
        for (ICall call : calls) {
            if (call.isConnected()) {
                String str = LOG_TAG;
                IMSLog.s(str, "Connected Call Number = " + call);
                nConnectedCalls++;
                ImsUri uri = this.mUriGenerators.get(phoneId).getNormalizedUri(call.getNumber(), true);
                if (uri != null && !this.mCallList.contains(uri)) {
                    this.mCallList.add(uri);
                }
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "nConnectedCalls = " + nConnectedCalls);
        if (nConnectedCalls > 1) {
            this.mCallList.clear();
        }
    }

    /* access modifiers changed from: protected */
    public boolean getActiveCall(ImsUri uri) {
        for (ImsUri callUri : this.mCallList) {
            if (callUri != null && callUri.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private void handleDDSChange() {
        int phoneId = SimUtil.getDefaultPhoneId();
        String str = LOG_TAG;
        Log.i(str, "handleDDSChange: current dds phoneId:" + phoneId);
        if (isRegistered(phoneId)) {
            Log.i(LOG_TAG, "handleDDSChange: registered, return;");
            return;
        }
        for (ImSession session : this.mCache.getActiveSessions()) {
            session.closeSession();
        }
    }

    private void onSimRefresh(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onSimRefresh:");
        for (ImSession session : this.mCache.getAllImSessions()) {
            session.onSimRefresh(phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public String getImsiFromPhoneId(int phoneId) {
        return SimManagerFactory.getImsiFromPhoneId(phoneId);
    }

    public void cleanUp() {
        stop();
    }

    /* access modifiers changed from: protected */
    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    /* access modifiers changed from: protected */
    public void setCountReconfiguration(int value) {
        this.mCountReconfiguration = value;
    }

    /* access modifiers changed from: protected */
    public boolean isDataRoaming(int phoneId) {
        return this.mIsDataRoamings.get(phoneId).booleanValue();
    }

    /* access modifiers changed from: protected */
    public void removeReconfigurationEvent() {
        removeMessages(33);
    }

    public ImSessionProcessor getImSessionProcessor() {
        return this.mImSessionProcessor;
    }

    /* access modifiers changed from: protected */
    public ImProcessor getImProcessor() {
        return this.mImProcessor;
    }

    /* access modifiers changed from: protected */
    public FtProcessor getFtProcessor() {
        return this.mFtProcessor;
    }

    /* access modifiers changed from: protected */
    public ImDump getImDump() {
        return this.mImDump;
    }

    private void handleEventProcessRejoinGCSession(int phoneId) {
        Log.i(LOG_TAG, "EVENT_PROCESS_REJOIN_GC_SESSION");
        if (getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_AUTO_REJOIN)) {
            this.mCache.loadImSessionForAutoRejoin();
        }
        this.mImSessionProcessor.processRejoinGCSession(phoneId);
    }

    private void handleEventResumePendingHttpFtOperations(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "EVENT_RESUME_PENDING_HTTP_FT_OPERATIONS mInternetAvailable: " + this.mInternetAvailable);
        if (this.mInternetAvailable) {
            for (ImSession session : this.mCache.getAllImSessions()) {
                int id = getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
                if (id != -1 && isRegistered(id)) {
                    session.processPendingFtHttp(phoneId);
                }
            }
        }
    }

    private void handleEventAbortOngoingHttpFtOperation(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "EVENT_ABORT_ONGOING_HTTP_FT_OPERATIONS isRegistered: " + isRegistered(phoneId) + ", mInternetAvailable: " + this.mInternetAvailable);
        if (isRegistered(phoneId) && !this.mInternetAvailable) {
            for (ImSession session : this.mCache.getAllImSessions()) {
                session.abortAllHttpFtOperations();
            }
        }
    }

    private void handleEventRequestChatbotAnonymizeResponse(ChatbotAnonymizeRespEvent params) {
        boolean isSuccess = params.mError == ImError.SUCCESS;
        if (ImsProfile.getRcsProfileType(this.mRcsProfile) >= ImsProfile.RCS_PROFILE.UP_2_2.ordinal() && isSuccess) {
            ImsUri chatbotUri = ImsUri.parse(params.mChatbotUri);
            for (ImSession session : this.mCache.getActiveSessions()) {
                if (session.getRemoteUri().equals(chatbotUri) && session.getIsTokenUsed() && session.isChatbotRole()) {
                    session.closeSession();
                }
            }
        }
        this.mImTranslation.onRequestChatbotAnonymizeResponse(params.mChatbotUri, isSuccess, params.mCommandId, params.mRetryAfter);
    }

    private void updateOwnPhoneNumberOnRegi(int phoneId, ImsRegistration registration) {
        String ownPhoneNumber = registration.getPreferredImpu().getUri().getMsisdn();
        if (ownPhoneNumber != null) {
            ownPhoneNumber = ownPhoneNumber.replace("+", "");
        }
        this.mOwnPhoneNumbers.put(phoneId, ownPhoneNumber);
        String str = LOG_TAG;
        IMSLog.s(str, "handleEventRegistered, mOwnImsi=" + getImsiFromPhoneId(phoneId) + ", mOwnPhoneNumber=" + this.mOwnPhoneNumbers.get(phoneId));
        if (getImsiFromPhoneId(phoneId) != null && getImsiFromPhoneId(phoneId).equals(this.mOwnPhoneNumbers.get(phoneId))) {
            String ownNumber = null;
            String str2 = LOG_TAG;
            IMSLog.s(str2, "handleEventRegistered, registration.getImpuList()=" + registration.getImpuList());
            Iterator it = registration.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr addr = (NameAddr) it.next();
                if (!getImsiFromPhoneId(phoneId).equals(addr.getUri().getMsisdn()) && addr.getUri().getUriType() == ImsUri.UriType.TEL_URI) {
                    ownNumber = addr.getUri().getMsisdn();
                    break;
                }
            }
            if (TextUtils.isEmpty(ownNumber)) {
                Iterator it2 = registration.getImpuList().iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    NameAddr addr2 = (NameAddr) it2.next();
                    if (!getImsiFromPhoneId(phoneId).equals(addr2.getUri().getMsisdn())) {
                        ownNumber = addr2.getUri().getMsisdn();
                        break;
                    }
                }
            }
            this.mOwnPhoneNumbers.put(phoneId, ownNumber);
        }
    }

    private void setAppVersionToSipUserAgent(ImsRegistration registration) {
        if (this.mMessagingAppInfoReceiver == null) {
            this.mMessagingAppInfoReceiver = new MessagingAppInfoReceiver(this.mContext, this);
        }
        this.mMessagingAppInfoReceiver.registerReceiver();
        String version = this.mMessagingAppInfoReceiver.getMessagingAppVersion();
        if (!TextUtils.isEmpty(version)) {
            this.mImService.setMoreInfoToSipUserAgent(version, registration.getHandle());
        }
    }

    private void processPendingMessagesOnRegi(int phoneId, ImsRegistration registration) {
        this.mCache.loadImSessionWithPendingMessages();
        if (this.mConfigs.get(phoneId).getEnableFtAutoResumable()) {
            this.mCache.loadImSessionWithFailedFTMessages();
        }
        Collection<ImSession> allSessions = this.mCache.getAllImSessions();
        String str = LOG_TAG;
        Log.i(str, allSessions.size() + " session(s) in cache");
        for (ImSession session : allSessions) {
            session.updateNetworkForPendingMessage(registration.getNetwork(), getNetwork(this.mConfigs.get(phoneId).isFtHttpOverDefaultPdn(), phoneId));
        }
        sendMessage(obtainMessage(9, Integer.valueOf(phoneId)));
        sendMessage(obtainMessage(19, Integer.valueOf(phoneId)));
    }

    /* access modifiers changed from: protected */
    public void removeFromPendingListWithDelay(int msgId) {
        this.mNeedToRemoveFromPendingList.add(Integer.valueOf(msgId));
        sendMessageDelayed(obtainMessage(35, Integer.valueOf(msgId)), 10000);
    }

    public boolean hasIncomingSessionForA2P(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "hasIncomingSessionForA2P: phoneId = " + phoneId);
        return this.mHasIncomingSessionForA2P.get(phoneId).booleanValue();
    }
}
