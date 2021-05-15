package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ImSession extends StateMachine {
    private static final long DEFAULT_WAKE_LOCK_TIMEOUT = 3000;
    protected static final int DEFER_WITHOUT_STARTSESSION = 0;
    protected static final int DEFER_WITH_STARTSESSION = 1;
    private static final String LOG_TAG = ImSession.class.getSimpleName();
    private static final int MESSAGE_REVOKE_OPERATION_TIME = 10000;
    private static final int REQUEST_THRESHOLD_TIME = 5000;
    private static final int SEND_MESSAGE_THRESHOLD_TIME = 300;
    protected final List<String> mAcceptTypes;
    protected final List<String> mAcceptWrappedTypes;
    private final ChatData mChatData;
    private ChatFallbackMech mChatFallbackMech;
    private final String mChatId;
    protected ImSessionClosedEvent mClosedEvent;
    protected ImSessionClosedReason mClosedReason;
    protected final ImSessionClosedState mClosedState;
    private final ImSessionClosingState mClosingState;
    private final Set<ImsUri> mComposingActiveUris;
    protected int mComposingNotificationInterval;
    private ConferenceInfoUpdater mConferenceInfoUpdater;
    protected final ImConfig mConfig;
    protected final List<MessageBase> mCurrentMessages;
    private final ImSessionDefaultState mDefaultState;
    private final Map<IState, SessionState> mDetailedStateMap;
    private String mDeviceId;
    protected final ArrayList<ImSessionInfo> mEstablishedImSessionInfo;
    private final ImSessionEstablishedState mEstablishedState;
    protected final IGetter mGetter;
    protected final List<ImSessionInfo> mImSessionInfoList;
    protected final IImServiceInterface mImsService;
    protected Set<Message> mInProgressRequestCallbacks;
    private String mInReplyToContributionId;
    protected List<ImIncomingMessageEvent> mIncomingMessageEvents;
    private final ImSessionInitialState mInitialState;
    private ImsUri mInitiator;
    private String mInitiatorAlias;
    protected boolean mIsComposing;
    protected boolean mIsOfflineGCInvitation;
    protected boolean mIsRevokeTimerRunning;
    private boolean mIsTimerExpired;
    private boolean mIsTokenUsed;
    protected String mLeaderParticipant;
    protected final ImSessionListener mListener;
    protected final ArrayDeque<MessageBase> mMessagesToSendDisplayNotification;
    private final Map<String, Integer> mNeedToRevokeMessages;
    protected ImsUri mNewContactValueUri;
    private String mOwnImsi;
    protected final HashMap<ImsUri, ImParticipant> mParticipants;
    protected List<Message> mPendingEvents;
    protected final ArrayList<FtMessage> mPendingFileTransfer;
    private int mPhoneId;
    protected final ArrayList<FtMessage> mProcessingFileTransfer;
    private Object mRawHandle;
    protected List<String> mRemoteAcceptTypes;
    protected List<String> mRemoteAcceptWrappedTypes;
    private String mRequestMessageId;
    protected int mRetryTimer;
    private String mSdpContentType;
    private int mSendMessageResponseTimeout;
    private final String mServiceId;
    private final ISlmServiceInterface mSlmService;
    protected final ImSessionStartingState mStartingState;
    protected EnumSet<SupportedFeature> mSupportedFeatures;
    protected boolean mSwapUriType;
    private int mThreadId;
    protected UriGenerator mUriGenerator;
    private final PowerManager.WakeLock mWakeLock;

    protected enum ChatFallbackMech {
        NONE,
        MESSAGE_REVOCATION,
        NETWORK_INTERWORKING
    }

    public enum SessionState {
        INITIAL,
        STARTING,
        ESTABLISHED,
        CLOSING,
        CLOSED,
        FAILED_MEDIA
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected ImSession(com.sec.internal.ims.servicemodules.im.ImSessionBuilder r15) {
        /*
            r14 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "ImSession#"
            r0.append(r1)
            com.sec.internal.constants.ims.servicemodules.im.ChatData r1 = r15.mChatData
            r2 = 4
            r3 = 0
            if (r1 != 0) goto L_0x0013
            java.lang.String r1 = r15.mChatId
            goto L_0x0019
        L_0x0013:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r1 = r15.mChatData
            java.lang.String r1 = r1.getChatId()
        L_0x0019:
            java.lang.String r1 = r1.substring(r3, r2)
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.os.Looper r1 = r15.mLooper
            r14.<init>((java.lang.String) r0, (android.os.Looper) r1)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mProcessingFileTransfer = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mPendingFileTransfer = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mImSessionInfoList = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mEstablishedImSessionInfo = r0
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            r14.mParticipants = r0
            java.util.HashSet r0 = new java.util.HashSet
            r0.<init>()
            r14.mComposingActiveUris = r0
            android.util.ArrayMap r0 = new android.util.ArrayMap
            r0.<init>()
            r14.mDetailedStateMap = r0
            java.util.ArrayDeque r0 = new java.util.ArrayDeque
            r0.<init>()
            r14.mMessagesToSendDisplayNotification = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mCurrentMessages = r0
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            r14.mNeedToRevokeMessages = r0
            r0 = -1
            r14.mThreadId = r0
            r14.mRetryTimer = r0
            java.lang.String r0 = ""
            r14.mOwnImsi = r0
            r14.mPhoneId = r3
            com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason.NONE
            r14.mClosedReason = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mIncomingMessageEvents = r0
            r0 = 120(0x78, float:1.68E-43)
            r14.mComposingNotificationInterval = r0
            com.sec.internal.ims.servicemodules.im.ImSession$ChatFallbackMech r0 = com.sec.internal.ims.servicemodules.im.ImSession.ChatFallbackMech.NONE
            r14.mChatFallbackMech = r0
            java.util.HashSet r0 = new java.util.HashSet
            r0.<init>()
            r14.mInProgressRequestCallbacks = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r14.mPendingEvents = r0
            com.sec.internal.ims.servicemodules.im.listener.ImSessionListener r0 = r15.mListener
            r14.mListener = r0
            com.sec.internal.ims.servicemodules.im.ImConfig r0 = r15.mConfig
            r14.mConfig = r0
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r0 = r15.mImsService
            r14.mImsService = r0
            com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface r0 = r15.mSlmService
            r14.mSlmService = r0
            com.sec.internal.ims.util.UriGenerator r0 = r15.mUriGenerator
            r14.mUriGenerator = r0
            com.sec.internal.constants.ims.servicemodules.im.ChatData r0 = r15.mChatData
            if (r0 == 0) goto L_0x00b8
            com.sec.internal.constants.ims.servicemodules.im.ChatData r0 = r15.mChatData
            r14.mChatData = r0
            goto L_0x00d8
        L_0x00b8:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r0 = new com.sec.internal.constants.ims.servicemodules.im.ChatData
            java.lang.String r2 = r15.mChatId
            java.lang.String r3 = r15.mOwnNumber
            java.lang.String r4 = r15.mOwnGroupAlias
            java.lang.String r5 = r15.mSubject
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r6 = r15.mChatType
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r7 = r15.mDirection
            java.lang.String r8 = r15.mConversationId
            java.lang.String r9 = r15.mContributionId
            java.lang.String r10 = r15.mOwnIMSI
            java.lang.String r11 = r15.mIconPath
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r12 = r15.mChatMode
            com.sec.ims.util.ImsUri r13 = r15.mSessionUri
            r1 = r0
            r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
            r14.mChatData = r0
        L_0x00d8:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r0 = r14.mChatData
            java.lang.String r0 = r0.getChatId()
            r14.mChatId = r0
            java.util.HashMap<com.sec.ims.util.ImsUri, com.sec.internal.constants.ims.servicemodules.im.ImParticipant> r0 = r14.mParticipants
            java.util.Map<com.sec.ims.util.ImsUri, com.sec.internal.constants.ims.servicemodules.im.ImParticipant> r1 = r15.mParticipants
            r0.putAll(r1)
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r0 = r15.mDirection
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r1 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            if (r0 != r1) goto L_0x010f
            java.util.Set<com.sec.ims.util.ImsUri> r0 = r15.mParticipantsUri
            java.util.Iterator r0 = r0.iterator()
        L_0x00f3:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x010e
            java.lang.Object r1 = r0.next()
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            java.util.HashMap<com.sec.ims.util.ImsUri, com.sec.internal.constants.ims.servicemodules.im.ImParticipant> r2 = r14.mParticipants
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r3 = new com.sec.internal.constants.ims.servicemodules.im.ImParticipant
            java.lang.String r4 = r14.mChatId
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r5 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED
            r3.<init>(r4, r5, r1)
            r2.put(r1, r3)
            goto L_0x00f3
        L_0x010e:
            goto L_0x0130
        L_0x010f:
            java.util.Set<com.sec.ims.util.ImsUri> r0 = r15.mParticipantsUri
            java.util.Iterator r0 = r0.iterator()
        L_0x0115:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0130
            java.lang.Object r1 = r0.next()
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            java.util.HashMap<com.sec.ims.util.ImsUri, com.sec.internal.constants.ims.servicemodules.im.ImParticipant> r2 = r14.mParticipants
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r3 = new com.sec.internal.constants.ims.servicemodules.im.ImParticipant
            java.lang.String r4 = r14.mChatId
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r5 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INITIAL
            r3.<init>(r4, r5, r1)
            r2.put(r1, r3)
            goto L_0x0115
        L_0x0130:
            int r0 = r14.getPhoneId()
            r14.mPhoneId = r0
            java.lang.String r0 = r15.mSdpContentType
            r14.mSdpContentType = r0
            int r0 = r15.mThreadId
            r14.mThreadId = r0
            java.lang.String r0 = r15.mRequestMessageId
            r14.mRequestMessageId = r0
            java.lang.Object r0 = r15.mRawHandle
            r14.mRawHandle = r0
            com.sec.internal.ims.servicemodules.im.interfaces.IGetter r0 = r15.mGetter
            r14.mGetter = r0
            int r0 = r14.mPhoneId
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = r14.getRcsStrategy(r0)
            java.lang.String r1 = "sendmsg_resp_timeout"
            int r0 = r0.intSetting(r1)
            r14.mSendMessageResponseTimeout = r0
            java.lang.String r0 = r15.mServiceId
            r14.mServiceId = r0
            java.util.List<java.lang.String> r0 = r15.mAcceptTypes
            r14.mAcceptTypes = r0
            java.util.List<java.lang.String> r0 = r15.mAcceptWrappedTypes
            r14.mAcceptWrappedTypes = r0
            java.util.Map<java.lang.String, java.lang.Integer> r0 = r14.mNeedToRevokeMessages
            java.util.Map<java.lang.String, java.lang.Integer> r1 = r15.mNeedToRevokeMessages
            r0.putAll(r1)
            android.content.Context r0 = r14.getContext()
            java.lang.String r1 = "power"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.os.PowerManager r0 = (android.os.PowerManager) r0
            java.lang.String r1 = r14.getName()
            r2 = 1
            android.os.PowerManager$WakeLock r1 = r0.newWakeLock(r2, r1)
            r14.mWakeLock = r1
            r1.setReferenceCounted(r2)
            com.sec.internal.ims.servicemodules.im.ImSessionDefaultState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionDefaultState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mDefaultState = r1
            com.sec.internal.ims.servicemodules.im.ImSessionInitialState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionInitialState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mInitialState = r1
            com.sec.internal.ims.servicemodules.im.ImSessionStartingState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionStartingState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mStartingState = r1
            com.sec.internal.ims.servicemodules.im.ImSessionEstablishedState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionEstablishedState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mEstablishedState = r1
            com.sec.internal.ims.servicemodules.im.ImSessionClosingState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionClosingState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mClosingState = r1
            com.sec.internal.ims.servicemodules.im.ImSessionClosedState r1 = new com.sec.internal.ims.servicemodules.im.ImSessionClosedState
            int r2 = r14.mPhoneId
            r1.<init>(r2, r14)
            r14.mClosedState = r1
            r1 = 0
            r14.mConferenceInfoUpdater = r1
            r14.initState()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSession.<init>(com.sec.internal.ims.servicemodules.im.ImSessionBuilder):void");
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock(Object rawHandle) {
        logi("acquireWakeLock: " + getChatId() + " : " + rawHandle);
        this.mWakeLock.acquire(DEFAULT_WAKE_LOCK_TIMEOUT);
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock(Object rawHandle) {
        if (this.mWakeLock.isHeld()) {
            logi("releaseWakeLock: " + getChatId() + " : " + rawHandle);
            this.mWakeLock.release();
        }
    }

    /* access modifiers changed from: protected */
    public void updateSessionInfo(ImSessionInfo info) {
        setRawHandle(info.mRawHandle);
        setContributionId(info.mContributionId);
        setConversationId(info.mConversationId);
        this.mInReplyToContributionId = info.mInReplyToContributionId;
        this.mSdpContentType = info.mSdpContentType;
        setSessionUri(info.mSessionUri);
        setDirection(info.mDirection);
        this.mChatData.triggerObservers(ImCacheAction.UPDATED);
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mGetter.getContext();
    }

    /* access modifiers changed from: protected */
    public ChatData getChatData() {
        return this.mChatData;
    }

    public String getChatId() {
        return this.mChatId;
    }

    /* access modifiers changed from: protected */
    public String getOwnPhoneNum() {
        return this.mChatData.getOwnPhoneNum();
    }

    /* access modifiers changed from: protected */
    public void setOwnPhoneNum(String ownNum) {
        this.mChatData.setOwnPhoneNum(ownNum);
    }

    public String getOwnImsi() {
        return this.mChatData.getOwnIMSI();
    }

    /* access modifiers changed from: protected */
    public void setOwnImsi(String ownImsi) {
        this.mChatData.setOwnIMSI(ownImsi);
    }

    public int getId() {
        return this.mChatData.getId();
    }

    public int getChatStateId() {
        return this.mChatData.getState().getId();
    }

    /* access modifiers changed from: protected */
    public void updateChatState(ChatData.State state) {
        this.mChatData.updateState(state);
    }

    /* access modifiers changed from: protected */
    public boolean isChatState(ChatData.State state) {
        return getChatStateId() == state.getId();
    }

    public boolean isGroupChat() {
        return this.mChatData.isGroupChat();
    }

    /* access modifiers changed from: protected */
    public int getPhoneId() {
        int mDefaultPhoneId = SimUtil.getSimSlotPriority();
        int phoneId = SimManagerFactory.getPhoneId(this.mChatData.getOwnIMSI());
        return phoneId != -1 ? phoneId : mDefaultPhoneId;
    }

    /* access modifiers changed from: protected */
    public boolean isChatbotRole() {
        return this.mChatData.isChatbotRole();
    }

    /* access modifiers changed from: protected */
    public boolean isBotSessionAnonymized() {
        return !this.mConfig.getBotPrivacyDisable() && isChatbotRole() && getIsTokenUsed();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0017, code lost:
        r0 = r2.mInitiator;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isChatbotManualAcceptUsed() {
        /*
            r2 = this;
            boolean r0 = r2.isChatbotRole()
            if (r0 == 0) goto L_0x0025
            int r0 = r2.getPhoneId()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = r2.getRcsStrategy(r0)
            java.lang.String r1 = "use_chatbot_manualaccept"
            boolean r0 = r0.boolSetting(r1)
            if (r0 == 0) goto L_0x0025
            com.sec.ims.util.ImsUri r0 = r2.mInitiator
            if (r0 == 0) goto L_0x0025
            com.sec.ims.util.ImsUri$UriType r0 = r0.getUriType()
            com.sec.ims.util.ImsUri$UriType r1 = com.sec.ims.util.ImsUri.UriType.SIP_URI
            if (r0 != r1) goto L_0x0025
            r0 = 1
            goto L_0x0026
        L_0x0025:
            r0 = 0
        L_0x0026:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSession.isChatbotManualAcceptUsed():boolean");
    }

    /* access modifiers changed from: protected */
    public void updateIsChatbotRole(boolean isChatbotRole) {
        ImsUri uri = getRemoteUri();
        if (!(isChatbotRole == this.mChatData.isChatbotRole() || uri == null)) {
            if (isChatbotRole) {
                ImCache.getInstance().addToChatbotRoleUris(uri);
            } else {
                ImCache.getInstance().removeFromChatbotRoleUris(uri);
            }
        }
        this.mChatData.updateIsChatbotRole(isChatbotRole);
    }

    /* access modifiers changed from: protected */
    public ChatData.ChatType getChatType() {
        return this.mChatData.getChatType();
    }

    /* access modifiers changed from: protected */
    public void updateChatType(ChatData.ChatType chatType) {
        this.mChatData.updateChatType(chatType);
    }

    public ChatMode getChatMode() {
        return this.mChatData.getChatMode();
    }

    public String getSubject() {
        return this.mChatData.getSubject();
    }

    private void setSubject(String subject) {
        this.mChatData.setSubject(subject);
    }

    /* access modifiers changed from: protected */
    public ImSubjectData getSubjectData() {
        return this.mChatData.getSubjectData();
    }

    /* access modifiers changed from: protected */
    public ImIconData getIconData() {
        return this.mChatData.getIconData();
    }

    /* access modifiers changed from: protected */
    public String getInitiatorAlias() {
        return this.mInitiatorAlias;
    }

    /* access modifiers changed from: protected */
    public void setInitiatorAlias(String alias) {
        this.mInitiatorAlias = alias;
    }

    public boolean getIsTokenUsed() {
        return this.mIsTokenUsed;
    }

    /* access modifiers changed from: protected */
    public void setIsTokenUsed(boolean isTokenUsed) {
        this.mIsTokenUsed = isTokenUsed;
    }

    /* access modifiers changed from: protected */
    public String getDeviceId() {
        return this.mDeviceId;
    }

    /* access modifiers changed from: protected */
    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    /* access modifiers changed from: protected */
    public String getUserAlias() {
        String alias = this.mConfig.getUserAlias();
        return getRcsStrategy().dropUnsupportedCharacter(alias) ? "" : alias;
    }

    /* access modifiers changed from: protected */
    public boolean isMuted() {
        return this.mChatData.isMuted();
    }

    /* access modifiers changed from: protected */
    public String getContributionId() {
        return this.mChatData.getContributionId();
    }

    /* access modifiers changed from: protected */
    public void setContributionId(String contributionId) {
        this.mChatData.setContributionId(contributionId);
    }

    /* access modifiers changed from: protected */
    public boolean isBroadcastMsg(MessageBase msg) {
        return this.mChatData.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT || (msg != null && msg.isBroadcastMsg());
    }

    /* access modifiers changed from: protected */
    public ImsUri getSessionUri() {
        return this.mChatData.getSessionUri();
    }

    /* access modifiers changed from: protected */
    public void setSessionUri(ImsUri uri) {
        this.mChatData.setSessionUri(uri);
    }

    public ImSessionClosedEvent getImSessionClosedEvent() {
        return this.mClosedEvent;
    }

    public Set<ImParticipant> getParticipants() {
        return new HashSet(this.mParticipants.values());
    }

    public List<String> getParticipantsString() {
        List<String> ret = new ArrayList<>();
        for (ImsUri uri : this.mParticipants.keySet()) {
            ret.add(uri.toString());
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public ImParticipant getParticipant(ImsUri uri) {
        if (uri != null) {
            return this.mParticipants.get(uri);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getRequestMessageId() {
        return this.mRequestMessageId;
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getParticipantsUri() {
        return new HashSet(this.mParticipants.keySet());
    }

    /* access modifiers changed from: protected */
    public int getParticipantsSize() {
        return this.mParticipants.size();
    }

    public ImsUri getRemoteUri() {
        if (this.mParticipants.size() == 1) {
            return this.mParticipants.keySet().iterator().next();
        }
        return null;
    }

    public int getMaxParticipantsCount() {
        return this.mChatData.getMaxParticipantsCount();
    }

    /* access modifiers changed from: protected */
    public int getThreadId() {
        return this.mThreadId;
    }

    /* access modifiers changed from: protected */
    public String getSdpContentType() {
        return this.mSdpContentType;
    }

    /* access modifiers changed from: protected */
    public void updateSubjectData(ImSubjectData subjectData) {
        this.mChatData.updateSubjectData(subjectData);
    }

    /* access modifiers changed from: protected */
    public void updateIconData(ImIconData iconData) {
        this.mChatData.updateIconData(iconData);
    }

    /* access modifiers changed from: protected */
    public String getConversationId() {
        return this.mChatData.getConversationId();
    }

    /* access modifiers changed from: protected */
    public void setConversationId(String conversationId) {
        this.mChatData.setConversationId(conversationId);
    }

    /* access modifiers changed from: protected */
    public String getInReplyToContributionId() {
        return this.mInReplyToContributionId;
    }

    /* access modifiers changed from: protected */
    public void setInReplyToContributionId(String inReplyToContributionId) {
        this.mInReplyToContributionId = inReplyToContributionId;
    }

    public ImDirection getDirection() {
        return this.mChatData.getDirection();
    }

    /* access modifiers changed from: protected */
    public void setDirection(ImDirection imDirection) {
        this.mChatData.setDirection(imDirection);
    }

    /* access modifiers changed from: protected */
    public void updateParticipantsStatus(ImParticipant.Status status) {
        List<ImParticipant> updatedParticipants = new ArrayList<>();
        for (ImParticipant p : this.mParticipants.values()) {
            if (p.getStatus() != status) {
                p.setStatus(status);
                updatedParticipants.add(p);
            }
        }
        if (!updatedParticipants.isEmpty()) {
            this.mListener.onParticipantsUpdated(this, updatedParticipants);
        }
    }

    /* access modifiers changed from: protected */
    public void updateParticipantAlias(String alias, ImParticipant participant) {
        if (participant == null) {
            IMSLog.e(LOG_TAG, "updateParticipantAlias, skipping update");
        } else if (!hasImSessionInfo(ImSessionInfo.SessionType.NORMAL) && TextUtils.isEmpty(alias)) {
            IMSLog.i(LOG_TAG, "updateParticipantAlias, SnF session and alias empty - do not update");
        } else if (participant.getUserAlias() == null || !participant.getUserAlias().equals(alias)) {
            participant.setUserAlias(alias);
            List<ImParticipant> updatedParticipants = new ArrayList<>();
            updatedParticipants.add(participant);
            this.mListener.onParticipantsUpdated(this, updatedParticipants);
            this.mListener.onParticipantAliasUpdated(this.mChatId, participant);
        } else {
            IMSLog.i(LOG_TAG, "updateParticipantAlias, participant alias is up to date");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReusable() {
        return this.mChatData.isReusable();
    }

    /* access modifiers changed from: protected */
    public boolean isRejoinable() {
        return isGroupChat() && getSessionUri() != null;
    }

    public boolean hasImSessionInfo(Object rawHandle) {
        return getImSessionInfo(rawHandle) != null;
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mStartingState, this.mDefaultState);
        addState(this.mEstablishedState, this.mDefaultState);
        addState(this.mClosingState, this.mDefaultState);
        addState(this.mClosedState, this.mInitialState);
        setInitialState(this.mInitialState);
        start();
        this.mDetailedStateMap.put(this.mInitialState, SessionState.INITIAL);
        this.mDetailedStateMap.put(this.mStartingState, SessionState.STARTING);
        this.mDetailedStateMap.put(this.mEstablishedState, SessionState.ESTABLISHED);
        this.mDetailedStateMap.put(this.mClosingState, SessionState.CLOSING);
        this.mDetailedStateMap.put(this.mClosedState, SessionState.CLOSED);
    }

    public SessionState getDetailedState() {
        return this.mDetailedStateMap.get(getCurrentState());
    }

    /* access modifiers changed from: protected */
    public IState getCurrentSessionState() {
        return getCurrentState();
    }

    public void startSession() {
        if (isBroadcastMsg((MessageBase) null)) {
            logi("broadcast message just use SLM, should never start session");
        } else {
            sendMessage(obtainMessage(1001));
        }
    }

    public void processIncomingSession(ImIncomingSessionEvent event) {
        if (event.mRawHandle != null) {
            acquireWakeLock(event.mRawHandle);
            if (event.mIsDeferred) {
                sendMessage(obtainMessage(1010, (Object) event));
            } else {
                sendMessage(obtainMessage(1005, (Object) event));
            }
        }
    }

    public void acceptSession(boolean explicit) {
        sendMessage(obtainMessage(1006, (Object) Boolean.valueOf(explicit)));
    }

    public void rejectSession() {
        sendMessage(obtainMessage(1008));
    }

    /* access modifiers changed from: protected */
    public void rejectSession(int reason) {
        sendMessage(obtainMessage(1008, (Object) Integer.valueOf(reason)));
    }

    public void receiveSessionEstablished(ImSessionEstablishedEvent event) {
        sendMessage(obtainMessage(1003, (Object) event));
    }

    public void receiveSessionClosed(ImSessionClosedEvent event) {
        sendMessage(obtainMessage(1014, (Object) event));
    }

    /* access modifiers changed from: protected */
    public void receiveConferenceInfo(ImSessionConferenceInfoUpdateEvent event) {
        sendMessage(obtainMessage((int) ImSessionEvent.CONFERENCE_INFO_UPDATED, (Object) event));
    }

    /* access modifiers changed from: protected */
    public void receiveComposingNotification(ImComposingEvent event) {
        if (event.mInterval != 0) {
            this.mComposingNotificationInterval = event.mInterval;
        }
        ImsUri remoteUri = this.mGetter.normalizeUri(ImsUri.parse(event.mUri));
        if (event.mIsComposing) {
            this.mComposingActiveUris.add(remoteUri);
            removeMessages(ImSessionEvent.RECEIVE_ISCOMPOSING_TIMEOUT);
            sendMessageDelayed((int) ImSessionEvent.RECEIVE_ISCOMPOSING_TIMEOUT, ((long) this.mComposingNotificationInterval) * 1000);
            checkAndUpdateSessionTimeout();
            return;
        }
        this.mComposingActiveUris.remove(remoteUri);
    }

    /* access modifiers changed from: protected */
    public void restartSession(int threadId, String requestMessageId, String subject) {
        this.mThreadId = threadId;
        this.mRequestMessageId = requestMessageId;
        setSubject(subject);
    }

    public void closeSession() {
        closeSession(true, getRcsStrategy(this.mPhoneId).getSessionStopReason(isGroupChat()));
    }

    /* access modifiers changed from: protected */
    public void closeSession(boolean reuse, ImSessionStopReason closeReason) {
        this.mChatData.updateIsReusable(reuse);
        this.mClosedState.mStopReason = closeReason;
        if (closeReason == ImSessionStopReason.VOLUNTARILY) {
            forceCancelFt(true, CancelReason.CANCELED_BY_USER);
        }
        sendMessage(obtainMessage(1012, (Object) closeReason));
    }

    /* access modifiers changed from: protected */
    public void forceCloseSession() {
        sendMessage(obtainMessage(1015));
    }

    /* access modifiers changed from: protected */
    public void addParticipants(List<ImsUri> participants) {
        if (isGroupChat()) {
            sendMessage(obtainMessage(ImSessionEvent.ADD_PARTICIPANTS, 0, 0, participants));
            return;
        }
        startSession();
        sendMessage(obtainMessage((int) ImSessionEvent.EXTEND_TO_GROUP_CHAT, (Object) participants));
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(List<ImsUri> participants) {
        if (isGroupChat()) {
            sendMessage(obtainMessage(ImSessionEvent.REMOVE_PARTICIPANTS, 0, 0, participants));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String subject) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_SUBJECT, (Object) subject));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(String icon_path) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_ICON, (Object) icon_path));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String alias) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GROUP_ALIAS, (Object) alias));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(List<ImsUri> participants) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_LEADER, (Object) participants));
        }
    }

    /* access modifiers changed from: protected */
    public void receiveDeliveryTimeout() {
        sendMessage(obtainMessage(ImSessionEvent.DELIVERY_TIMEOUT));
    }

    /* access modifiers changed from: protected */
    public ImsUri getInitiator() {
        return this.mInitiator;
    }

    /* access modifiers changed from: protected */
    public void setInitiator(ImsUri initiator) {
        this.mInitiator = initiator;
    }

    /* access modifiers changed from: protected */
    public void addParticipant(Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            this.mParticipants.put(p.getUri(), p);
        }
    }

    /* access modifiers changed from: protected */
    public void deleteParticipant(Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            this.mParticipants.remove(p.getUri());
        }
    }

    public void sendComposing(boolean isTyping, int interval) {
        this.mComposingNotificationInterval = interval;
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION, (Object) Boolean.valueOf(isTyping)));
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getComposingActiveUris() {
        return this.mComposingActiveUris;
    }

    public void sendImMessage(MessageBase msg) {
        logi("sendImMessage: ChatbotMessagingTech = " + msg.getChatbotMessagingTech());
        if (msg.getBody() != null) {
            if (msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.UNKNOWN) {
                msg.setChatbotMessagingTech(getRcsStrategy(this.mPhoneId).checkChatbotMessagingTech(this.mConfig, isGroupChat(), getParticipantsUri()));
            }
            boolean isSlm = true;
            if (msg.getChatbotMessagingTech() != ImConstants.ChatbotMessagingTech.NONE) {
                if (msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.NOT_AVAILABLE) {
                    msg.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
                    return;
                }
                msg.setSlmSvcMsg(msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING);
            }
            if (!ChatbotUriUtil.hasChatbotUri(getParticipantsUri())) {
                if (this.mConfig.getChatEnabled() || this.mConfig.getSlmAuth() != ImConstants.SlmAuth.ENABLED) {
                    isSlm = false;
                }
                msg.setSlmSvcMsg(isSlm);
            }
            if (msg.getIsSlmSvcMsg()) {
                sendMessage(obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) msg));
            } else {
                sendMessage(obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) msg));
            }
            if ((msg instanceof ImMessage) && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_SENDMSG_RESP_TIMEOUT)) {
                sendMessageDelayed(obtainMessage((int) ImSessionEvent.SEND_MESSAGE_RESPONSE_TIMEOUT, (Object) msg), ((long) this.mSendMessageResponseTimeout) * 1000);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setSessionTimeoutThreshold(MessageBase msg) {
        Preconditions.checkNotNull(msg, "msg cannot be null");
        if (msg instanceof ImMessage) {
            sendMessageDelayed(obtainMessage(1019, (Object) msg), 300000);
        }
    }

    /* access modifiers changed from: protected */
    public void receiveMessage(MessageBase msg, Object rawHandle) {
        ImSessionInfo info;
        if (!(msg == null || rawHandle == null || (info = getImSessionInfo(rawHandle)) == null || !info.isSnFSession())) {
            info.mReceivedMessageIds.add(Integer.valueOf(msg.getId()));
        }
        sendMessage(obtainMessage((int) ImSessionEvent.RECEIVE_MESSAGE, (Object) msg));
    }

    /* access modifiers changed from: protected */
    public void readMessages(List<String> messageIds) {
        Long displayedTimestamp = Long.valueOf(System.currentTimeMillis());
        boolean isAggregation = this.mConfig.isAggrImdnSupported() && isGroupChat();
        List<String> ids = new ArrayList<>(messageIds);
        if (isAggregation) {
            List<String> l = this.mGetter.getMessageIdsForDisplayAggregation(getChatId(), ImDirection.INCOMING, displayedTimestamp);
            l.removeAll(ids);
            ids.addAll(l);
        }
        List<MessageBase> messages = this.mGetter.getMessages(ids);
        messages.sort($$Lambda$ImSession$PrMtWDsBW8rwoSn9q_uHztqNscg.INSTANCE);
        boolean isRespondDisplay = isRespondDisplay();
        for (MessageBase m : messages) {
            if ((m instanceof FtHttpIncomingMessage) || m.getStatus() != ImConstants.Status.FAILED) {
                m.updateStatus(ImConstants.Status.READ);
                m.updateDisplayedTimestamp(displayedTimestamp.longValue());
                if (m.isDisplayedNotificationRequired() && isRespondDisplay) {
                    m.updateDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                    this.mMessagesToSendDisplayNotification.add(m);
                }
            } else {
                loge("Do not update message with status FAILED: " + m.getId());
            }
        }
        if (!this.mMessagesToSendDisplayNotification.isEmpty()) {
            sendMessage(obtainMessage(ImSessionEvent.SEND_DISPLAYED_NOTIFICATION));
        }
    }

    static /* synthetic */ int lambda$readMessages$0(MessageBase m1, MessageBase m2) {
        long value = m1.getInsertedTimestamp() - m2.getInsertedTimestamp();
        if (value == 0) {
            if (m1.getId() < m2.getId()) {
                return -1;
            }
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent event, MessageBase msg) {
        logi("onSendImdnFailed event: " + event + ", msg: " + msg);
        if ((msg instanceof ImMessage) || (msg instanceof FtHttpIncomingMessage)) {
            this.mClosedState.handleCloseSession(event.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
            transitionToProperState();
        }
        NotificationStatus current = msg.getNotificationStatus();
        if (current == NotificationStatus.DELIVERED || current == NotificationStatus.DISPLAYED) {
            msg.sendDeliveredNotification((Object) null, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) msg), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
            if (current == NotificationStatus.DISPLAYED && isRespondDisplay()) {
                msg.sendDisplayedNotification((Object) null, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) msg.toList()), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSendMessageHandleReportFailed(SendMessageFailedEvent event, MessageBase msg) {
        Message callback = obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) msg);
        AsyncResult.forMessage(callback, new SendMessageResult(event.mRawHandle, event.mResult), (Throwable) null);
        callback.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void receiveSlmMessage(MessageBase msg) {
        sendMessage(obtainMessage((int) ImSessionEvent.RECEIVE_SLM_MESSAGE, (Object) msg));
    }

    private boolean isRespondDisplay() {
        return isGroupChat() || this.mConfig.getRespondDisplay();
    }

    /* access modifiers changed from: protected */
    public void processPendingMessages(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        this.mOwnImsi = sm == null ? "" : sm.getImsi();
        logi("processPendingMessages phoneId = " + phoneId);
        this.mProcessingFileTransfer.clear();
        this.mListener.onProcessingFileTransferChanged(this);
        Map<Long, MessageBase> orderedPendingMessages = new TreeMap<>();
        if (TextUtils.isEmpty(this.mOwnImsi)) {
            loge("processPendingMessages: ownImsi is not loaded.");
            return;
        }
        for (MessageBase m : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (TextUtils.isEmpty(m.getOwnIMSI())) {
                m.updateOwnIMSI(this.mOwnImsi);
                orderedPendingMessages.put(Long.valueOf(m.getInsertedTimestamp()), m);
            } else if (m.getOwnIMSI().equals(this.mOwnImsi)) {
                orderedPendingMessages.put(Long.valueOf(m.getInsertedTimestamp()), m);
            }
        }
        for (MessageBase m2 : orderedPendingMessages.values()) {
            if (m2 instanceof ImMessage) {
                processPendingImMessage((ImMessage) m2);
            } else if (m2 instanceof FtMessage) {
                processPendingFtMessage((FtMessage) m2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processPendingFtHttp(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        this.mOwnImsi = sm == null ? "" : sm.getImsi();
        logi("processPendingFtHttp");
        this.mProcessingFileTransfer.clear();
        this.mListener.onProcessingFileTransferChanged(this);
        List<MessageBase> orderedPendingMessages = new ArrayList<>();
        for (MessageBase m : this.mGetter.getAllPendingMessages(this.mChatId)) {
            String ownImsi = m.getOwnIMSI();
            logi("IMSI of SIM sent this message = " + IMSLog.numberChecker(ownImsi) + ", IMSI of current SIM = " + IMSLog.numberChecker(this.mOwnImsi));
            if (TextUtils.isEmpty(ownImsi)) {
                logi("current status of this message = " + m.getStatus());
                if (m.getStatus() == ImConstants.Status.SENDING || m.getStatus() == ImConstants.Status.TO_SEND) {
                    m.updateStatus(ImConstants.Status.FAILED);
                }
            } else if (ownImsi.equals(this.mOwnImsi)) {
                orderedPendingMessages.add(m);
            }
        }
        orderedPendingMessages.sort($$Lambda$ImSession$P0H322xlBdslh7IByTbbM8XV27M.INSTANCE);
        for (MessageBase m2 : orderedPendingMessages) {
            if (m2 instanceof FtMessage) {
                processPendingFtMessage((FtMessage) m2);
            }
        }
    }

    static /* synthetic */ int lambda$processPendingFtHttp$1(MessageBase m1, MessageBase m2) {
        long value = m1.getInsertedTimestamp() - m2.getInsertedTimestamp();
        if (value == 0) {
            if (m1.getId() < m2.getId()) {
                return -1;
            }
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public void processPendingNotifications(List<MessageBase> pendingNotifications) {
        boolean isRespondDisplay = isRespondDisplay();
        List<MessageBase> orderedMessages = new ArrayList<>(pendingNotifications);
        orderedMessages.sort($$Lambda$ImSession$7tfU2p2KFQzRMgD806Dwbz0bwT4.INSTANCE);
        List<MessageBase> deliveryNotifications = new ArrayList<>();
        for (MessageBase m : orderedMessages) {
            if (m.getDirection() == ImDirection.INCOMING) {
                NotificationStatus current = m.getNotificationStatus();
                NotificationStatus desired = m.getDesiredNotificationStatus();
                logi("sendDispositionNotification current : " + current + " desired : " + desired);
                if (desired == NotificationStatus.DELIVERED && current == NotificationStatus.NONE) {
                    deliveryNotifications.add(m);
                } else if (desired == NotificationStatus.DISPLAYED) {
                    m.updateStatus(ImConstants.Status.READ);
                    if (current == NotificationStatus.DELIVERED && isRespondDisplay) {
                        this.mMessagesToSendDisplayNotification.add(m);
                    } else if (current == NotificationStatus.NONE) {
                        deliveryNotifications.add(m);
                        if (isRespondDisplay) {
                            this.mMessagesToSendDisplayNotification.add(m);
                        }
                    }
                }
            }
        }
        for (MessageBase m2 : deliveryNotifications) {
            sendDeliveredNotification(m2);
        }
        if (!this.mMessagesToSendDisplayNotification.isEmpty()) {
            sendMessage(obtainMessage(ImSessionEvent.SEND_DISPLAYED_NOTIFICATION));
        }
    }

    static /* synthetic */ int lambda$processPendingNotifications$2(MessageBase m1, MessageBase m2) {
        long value = m1.getInsertedTimestamp() - m2.getInsertedTimestamp();
        if (value == 0) {
            if (m1.getId() < m2.getId()) {
                return -1;
            }
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private void processPendingImMessage(ImMessage msg) {
        ImConstants.Status status = msg.getStatus();
        if (!msg.isOutgoing()) {
            return;
        }
        if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
            sendImMessage(msg);
        }
    }

    private void processPendingFtMessage(FtMessage msg) {
        if (!msg.isAutoResumable()) {
            return;
        }
        if (msg.isOutgoing() && msg.getStateId() == 2) {
            resumeTransferFile(msg);
        } else if ((msg instanceof FtHttpIncomingMessage) && msg.getStateId() == 2) {
            receiveTransfer(msg, (FtIncomingSessionEvent) null, true);
        } else if ((msg instanceof FtHttpOutgoingMessage) && msg.getStateId() == 3) {
            ImConstants.Status status = msg.getStatus();
            if (!msg.isOutgoing()) {
                return;
            }
            if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
                sendImMessage(msg);
            }
        } else if (!RcsUtils.UiUtils.isPctMode() && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.AUTO_RESEND_FAILED_FT) && msg.isOutgoing() && msg.getCancelReason() != CancelReason.CANCELED_BY_USER) {
            resumeTransferFile(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void attachFile(FtMessage msg) {
        sendMessage(obtainMessage((int) ImSessionEvent.ATTACH_FILE, (Object) msg));
    }

    /* access modifiers changed from: protected */
    public void processRejoinGCSession() {
        if (isRejoinable() && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_AUTO_REJOIN) && (isChatState(ChatData.State.ACTIVE) || isChatState(ChatData.State.CLOSED_INVOLUNTARILY))) {
            logi("processRejoinGCSession : " + getChatId());
            sendMessage(obtainMessage(1020));
        } else if (isRejoinable() && isChatState(ChatData.State.CLOSED_VOLUNTARILY) && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_AUTO_REJOIN_FOR_BYE)) {
            logi("processRejoinGCSession for bye : " + getChatId());
            sendMessage(obtainMessage(1021));
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAutoRejoinSession() {
        if (isRejoinable() && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_AUTO_REJOIN) && (isChatState(ChatData.State.ACTIVE) || isChatState(ChatData.State.CLOSED_INVOLUNTARILY))) {
            return true;
        }
        if (!isRejoinable() || !isChatState(ChatData.State.CLOSED_VOLUNTARILY) || !getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_AUTO_REJOIN_FOR_BYE)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onMessageSending(MessageBase msg) {
        Preconditions.checkNotNull(msg, "msg cannot be null");
        if (!(msg.getStatus() == ImConstants.Status.SENDING || msg.getStatus() == ImConstants.Status.SENT)) {
            msg.updateStatus(ImConstants.Status.SENDING);
        }
        if (!this.mIsComposing || !isBroadcastMsg(msg)) {
            this.mIsComposing = false;
            removeMessages(ImSessionEvent.SEND_ISCOMPOSING_REFRESH);
            removeMessages(ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT);
            return;
        }
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION, (Object) false));
    }

    /* access modifiers changed from: protected */
    public void onSendSlmMessage(MessageBase msg) {
        ImConstants.MessagingTech messagingTech;
        MessageBase messageBase = msg;
        Preconditions.checkNotNull(messageBase, "msg cannot be null");
        logi("onSendSlmMessage");
        Set<ImsUri> participants = getRcsStrategy(this.mPhoneId).getNetworkPreferredUri(this.mUriGenerator, getParticipantsUri());
        if (msg.getType() == ImConstants.Type.TEXT_PUBLICACCOUNT) {
            Set<ImsUri> participants1 = new HashSet<>();
            for (ImsUri uri : participants) {
                participants1.add(PublicAccountUri.convertToPublicAccountUri(uri.toString()));
            }
            participants = participants1;
        }
        if (!msg.getContentType().contains(ImMultipart.SUGGESTION_RESPONSE_CONTENT_TYPE) && !msg.getContentType().contains(ImMultipart.SHARED_CLIENT_DATA_CONTENT_TYPE)) {
            this.mInReplyToContributionId = null;
        }
        SendSlmMessageParams slmMessageParams = new SendSlmMessageParams(msg.getId(), this.mChatId, msg.getBody(), msg.getContentType(), msg.getUserAlias(), msg.getImdnId(), new Date(), msg.getDispositionNotification(), StringIdGenerator.generateContributionId(), this.mChatData.getConversationId(), this.mInReplyToContributionId, participants, obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE_DONE, (Object) messageBase), msg.getType() == ImConstants.Type.TEXT_PUBLICACCOUNT, isBroadcastMsg(msg), msg.getDeviceName(), msg.getReliableMessage(), getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.EXTRA_FT_FOR_NS), this.mChatData.getOwnIMSI(), !isGroupChat() && ChatbotUriUtil.hasChatbotUri(getParticipantsUri()), msg.getMaapTrafficType());
        if (msg.getReportMsgParams() != null) {
            slmMessageParams.mReportMsgParams = msg.getReportMsgParams();
        }
        if (msg.getBody().length() > this.mConfig.getPagerModeLimit()) {
            messagingTech = ImConstants.MessagingTech.SLM_LARGE_MODE;
        } else {
            messagingTech = ImConstants.MessagingTech.SLM_PAGER_MODE;
        }
        msg.setMessagingTech(messagingTech);
        this.mSlmService.sendSlmMessage(slmMessageParams);
        onMessageSending(msg);
        setSessionTimeoutThreshold(msg);
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(MessageBase msg) {
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION, (Object) msg));
    }

    /* access modifiers changed from: protected */
    public void onAddParticipantsSucceeded(List<ImsUri> participants) {
        List<ImParticipant> insertedParticipants = new ArrayList<>();
        for (ImsUri uri : participants) {
            ImsUri normalizedUri = this.mGetter.normalizeUri(uri);
            if (normalizedUri != null && getParticipant(normalizedUri) == null) {
                insertedParticipants.add(new ImParticipant(this.mChatId, ImParticipant.Status.INVITED, normalizedUri));
            }
        }
        if (!isGroupChat() && getParticipantsSize() > 1) {
            updateChatType(ChatData.ChatType.REGULAR_GROUP_CHAT);
        }
        if (!insertedParticipants.isEmpty()) {
            this.mListener.onParticipantsInserted(this, insertedParticipants);
        }
        this.mListener.onAddParticipantsSucceeded(this.mChatId, participants);
    }

    /* access modifiers changed from: protected */
    public void onAddParticipantsFailed(List<ImsUri> participants, ImErrorReason reason) {
        this.mListener.onAddParticipantsFailed(this.mChatId, participants, reason);
    }

    /* access modifiers changed from: protected */
    public void onRemoveParticipantsFailed(List<ImsUri> participants, ImErrorReason reason) {
        this.mListener.onRemoveParticipantsFailed(this.mChatId, participants, reason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatLeaderFailed(List<ImsUri> participants, ImErrorReason reason) {
        this.mListener.onChangeGroupChatLeaderFailed(this.mChatId, participants, reason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatSubjectFailed(String subject, ImErrorReason reason) {
        this.mListener.onChangeGroupChatSubjectFailed(this.mChatId, subject, reason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatIconFailed(String icon_path, ImErrorReason reason) {
        this.mListener.onChangeGroupChatIconFailed(this.mChatId, icon_path, reason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupAliasFailed(String alias, ImErrorReason reason) {
        this.mListener.onChangeGroupAliasFailed(this.mChatId, alias, reason);
    }

    /* access modifiers changed from: protected */
    public void onConferenceInfoUpdated(ImSessionConferenceInfoUpdateEvent event) {
        if (this.mConferenceInfoUpdater == null) {
            ImsUri ownUri = this.mUriGenerator.getNormalizedUri(getOwnPhoneNum(), true);
            int i = this.mPhoneId;
            this.mConferenceInfoUpdater = new ConferenceInfoUpdater(this, i, ownUri, getRcsStrategy(i), this.mUriGenerator, this.mListener);
        }
        this.mConferenceInfoUpdater.onConferenceInfoUpdated(event, this.mLeaderParticipant);
    }

    /* access modifiers changed from: protected */
    public void onIncomingSessionProcessed(ImIncomingMessageEvent msgEvent, boolean notify) {
        this.mListener.onIncomingSessionProcessed(msgEvent, this, notify);
    }

    /* access modifiers changed from: protected */
    public void failCurrentMessages(Object rawHandle, Result result) {
        failCurrentMessages(rawHandle, result, (String) null);
    }

    /* access modifiers changed from: protected */
    public void failCurrentMessages(Object rawHandle, Result result, String allowedMethods) {
        for (MessageBase message : this.mCurrentMessages) {
            Message callback = obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) message);
            AsyncResult.forMessage(callback, new SendMessageResult(rawHandle, result, allowedMethods), (Throwable) null);
            callback.sendToTarget();
        }
        this.mCurrentMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void updateNetworkForPendingMessage(Network network, Network ftNetwork) {
        List<MessageBase> list = this.mGetter.getAllPendingMessages(this.mChatId);
        String str = LOG_TAG;
        Log.i(str, "updateNetworkForPendingMessage: " + list.size() + " pended message(s) in " + this.mChatId + " with " + network + ", " + ftNetwork);
        for (MessageBase m : list) {
            if ((m instanceof FtHttpOutgoingMessage) || (m instanceof FtHttpIncomingMessage)) {
                m.setNetwork(ftNetwork);
            } else {
                m.setNetwork(network);
            }
        }
    }

    /* access modifiers changed from: protected */
    public FtMessage findFtMessage(String fileName, long fileSize, String fileTransferId) {
        Preconditions.checkNotNull(fileName);
        Preconditions.checkNotNull(fileTransferId);
        for (MessageBase m : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (m instanceof FtMessage) {
                FtMessage ftMsg = (FtMessage) m;
                if (fileName.equals(ftMsg.getFileName()) && fileSize == ftMsg.getFileSize() && fileTransferId.equals(ftMsg.getFileTransferId())) {
                    return ftMsg;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void processDeregistration() {
        logi("processDeregistration :" + getChatId());
        forceCloseSession();
        if (isMsgRevocationSupported() && !this.mNeedToRevokeMessages.isEmpty()) {
            Collection<MessageBase> messages = new ArrayList<>();
            for (String imdnId : this.mNeedToRevokeMessages.keySet()) {
                MessageBase msg = this.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
                if (msg != null && msg.getRevocationStatus() == ImConstants.RevocationStatus.SENT) {
                    messages.add(msg);
                    stopMsgRevokeOperationTimer(imdnId);
                }
            }
            this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.SUCCESS, messages, this);
            this.mIsRevokeTimerRunning = false;
            this.mNeedToRevokeMessages.clear();
            PreciseAlarmManager.getInstance(getContext()).removeMessage(obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED));
        }
        processCancelMessages(true, (ImError) null);
    }

    /* access modifiers changed from: protected */
    public void abortAllHttpFtOperations() {
        logi("abortAllHttpFtOperations :" + getChatId());
        for (MessageBase m : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (m instanceof FtHttpOutgoingMessage) {
                if (((FtMessage) m).getStateId() == 2) {
                    logi("processDeregistration : mPendingMessages FtMessage.getStateId() = " + ((FtMessage) m).getStateId());
                    ((FtMessage) m).setFtCompleteCallback((Message) null);
                    ((FtMessage) m).cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
                }
            } else if ((m instanceof FtHttpIncomingMessage) && ((FtMessage) m).getStateId() == 2) {
                logi("processDeregistration : mPendingMessages FtMessage.getStateId() = " + ((FtMessage) m).getStateId());
                ((FtMessage) m).cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
        synchronized (this.mPendingFileTransfer) {
            for (int i = 0; i < this.mPendingFileTransfer.size(); i++) {
                FtMessage m2 = this.mPendingFileTransfer.get(i);
                logi("cancel pending file transfer : " + m2.getId());
                m2.setFtCompleteCallback((Message) null);
                m2.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void forceCancelFt(boolean cancelPending, CancelReason cancelReason) {
        this.mClosedState.forceCancelFt(cancelPending, cancelReason, false);
    }

    /* access modifiers changed from: protected */
    public void processCancelMessages(boolean isFallback, ImError error) {
        logi("processCancelMessages :" + getChatId());
        cancelInProgressMessages(isFallback, error == null ? ImError.UNKNOWN_ERROR : error);
        cancelPendingFilesInQueue();
    }

    /* access modifiers changed from: protected */
    public void cancelInProgressMessages(boolean isFallback, ImError error) {
        for (MessageBase m : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (m instanceof ImMessage) {
                cancelInProgressChatMsg((ImMessage) m, isFallback, error);
            } else if (m instanceof FtHttpOutgoingMessage) {
                cancelInProgressFTOutGoingMsg((FtMessage) m, isFallback, error);
            } else if (m instanceof FtHttpIncomingMessage) {
                cancelInProgressFTInComingMsg((FtMessage) m, isFallback, error);
            }
        }
    }

    private void cancelInProgressChatMsg(ImMessage imMsg, boolean isFallback, ImError error) {
        if (imMsg.getDirection() != ImDirection.OUTGOING) {
            return;
        }
        if ((imMsg.getStatus() == ImConstants.Status.TO_SEND || imMsg.getStatus() == ImConstants.Status.SENDING) && error != ImError.OUTOFSERVICE) {
            logi("cancelInProgressChatMsg : mark msg failed " + imMsg.getId());
            if (!isFallback) {
                imMsg.onSendMessageDone(new Result(error, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            } else if (isGroupChat() || !ChatbotUriUtil.hasChatbotUri(getParticipantsUri())) {
                imMsg.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
            } else {
                logi("cancelInProgressChatMsg : no fallback in case of chatbots");
                imMsg.onSendMessageDone(new Result(error, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            }
        }
    }

    private void cancelInProgressFTInComingMsg(FtMessage ftMsg, boolean isFallback, ImError error) {
        if (ftMsg.getStateId() == 2) {
            logi("cancelInProgressFTInComingMsg : mPendingMessages FtMessage.getStateId() = " + ftMsg.getStateId());
            if (ftMsg.mIsWifiUsed) {
                ftMsg.cancelTransfer(CancelReason.WIFI_DISCONNECTED);
            } else {
                ftMsg.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    private void cancelInProgressFTOutGoingMsg(FtMessage ftMsg, boolean isFallback, ImError error) {
        if (ftMsg.getStateId() == 2) {
            logi("cancelInProgressFTOutGoingMsg : mPendingMessages FtMessage.getStateId() = " + ftMsg.getStateId());
            ftMsg.setFtCompleteCallback((Message) null);
            if (ftMsg.mIsWifiUsed) {
                ftMsg.cancelTransfer(CancelReason.WIFI_DISCONNECTED);
            } else {
                ftMsg.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        } else if (ftMsg.getStateId() == 3 && ftMsg.getStatus() != ImConstants.Status.SENT && !ftMsg.isFtSms()) {
            if (!isFallback || (!isGroupChat() && ChatbotUriUtil.hasChatbotUri(getParticipantsUri()))) {
                ftMsg.onSendMessageDone(new Result(error, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            } else {
                ftMsg.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void cancelPendingFilesInQueue() {
        synchronized (this.mPendingFileTransfer) {
            for (int i = 0; i < this.mPendingFileTransfer.size(); i++) {
                FtMessage m = this.mPendingFileTransfer.get(i);
                logi("cancel pending file transfer : " + m.getId());
                m.setFtCompleteCallback((Message) null);
                m.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendFile(FtMessage msg) {
        logi("sendFile::entering .... queue size: " + this.mProcessingFileTransfer.size());
        if (msg instanceof FtHttpOutgoingMessage) {
            if (!msg.isFtSms()) {
                msg.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            }
            msg.sendFile();
        } else if (this.mProcessingFileTransfer.isEmpty()) {
            msg.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_FILE, (Object) msg));
            addToProcessingFileTransfer(msg);
            this.mListener.onProcessingFileTransferChanged(this);
        } else if (!this.mPendingFileTransfer.contains(msg) && !this.mProcessingFileTransfer.contains(msg)) {
            msg.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            this.mPendingFileTransfer.add(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void resumeTransferFile(FtMessage msg) {
        Preconditions.checkNotNull(msg);
        logi("resumeTransferFile: " + msg.getId() + " mProcessingFileTransfer size: " + this.mProcessingFileTransfer.size());
        msg.setConversationId(getConversationId());
        msg.setContributionId(getContributionId());
        msg.setIsResuming(true);
        msg.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
        if (msg instanceof FtHttpOutgoingMessage) {
            if (isVoluntaryDeparture()) {
                msg.cancelTransfer(CancelReason.CANCELED_BY_USER);
            } else if (isGroupChat()) {
                attachFile(msg);
            } else {
                msg.sendFile();
            }
        } else if (this.mProcessingFileTransfer.isEmpty()) {
            if (isGroupChat()) {
                attachFile(msg);
            } else {
                msg.sendFile();
            }
            addToProcessingFileTransfer(msg);
            this.mListener.onProcessingFileTransferChanged(this);
        } else if (!this.mProcessingFileTransfer.contains(msg) && !this.mPendingFileTransfer.contains(msg)) {
            msg.updateQueued();
            this.mPendingFileTransfer.add(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void receiveTransfer(FtMessage msg, FtIncomingSessionEvent event, boolean resume) {
        logi("receiveTransfer: mProcessingFileTransfer size: " + this.mProcessingFileTransfer.size());
        msg.receiveTransfer(obtainMessage(ImSessionEvent.FILE_COMPLETE), event, resume);
        if ((msg instanceof FtMsrpMessage) && !this.mProcessingFileTransfer.contains(msg)) {
            this.mPendingFileTransfer.remove(msg);
            addToProcessingFileTransfer(msg);
            this.mListener.onProcessingFileTransferChanged(this);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isFirstMessageInStart(String body) {
        return getRcsStrategy(this.mPhoneId).isFirstMsgInvite(this.mConfig.isFirstMsgInvite()) && (!isGroupChat() || getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.FIRSTMSG_GROUPCHAT_INVITE)) && !getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_MSRP);
    }

    private boolean isSessionTimeoutSupported() {
        return this.mConfig.getTimerIdle() != 0 && !isGroupChat() && isSessionTimeoutRequired();
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateSessionTimeout() {
        if (isSessionTimeoutSupported()) {
            removeMessages(1018);
            if ("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\"".equalsIgnoreCase(this.mServiceId)) {
                logi("checkAndUpdateSessionTimeout serviceId = " + this.mServiceId + ", " + (this.mConfig.getCallComposerTimerIdle() * 1000));
                if (this.mConfig.getCallComposerTimerIdle() > 0) {
                    sendMessageDelayed(obtainMessage(1018), ((long) this.mConfig.getCallComposerTimerIdle()) * 1000);
                    return;
                }
                return;
            }
            logi("checkAndUpdateSessionTimeout " + (this.mConfig.getTimerIdle() * 1000));
            sendMessageDelayed(obtainMessage(1018), ((long) this.mConfig.getTimerIdle()) * 1000);
        }
    }

    private boolean isSessionTimeoutRequired() {
        return getServiceId() == null || (!getServiceId().equalsIgnoreCase("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\"") && !getServiceId().equalsIgnoreCase("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""));
    }

    /* access modifiers changed from: protected */
    public void addToProcessingFileTransfer(FtMessage ftMsg) {
        if (!this.mProcessingFileTransfer.contains(ftMsg)) {
            this.mProcessingFileTransfer.add(ftMsg);
            ftMsg.startFileTransferTimer();
        }
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo addImSessionInfo(ImIncomingSessionEvent event, ImSessionInfo.ImSessionState state) {
        ImSessionInfo info = new ImSessionInfo(event.mRawHandle, state, ImDirection.INCOMING, event.mSessionUri, event.mContributionId, event.mConversationId, (String) null, event.mSdpContentType);
        if (event.mIsDeferred) {
            info.mSessionType = event.mIsForStoredNoti ? ImSessionInfo.SessionType.SNF_NOTIFICATION_SESSION : ImSessionInfo.SessionType.SNF_SESSION;
        }
        addImSessionInfo(info);
        return info;
    }

    /* access modifiers changed from: protected */
    public void addImSessionInfo(ImSessionInfo info) {
        this.mImSessionInfoList.add(0, info);
    }

    /* access modifiers changed from: protected */
    public void handleAcceptSession(ImSessionInfo info) {
        if (info != null) {
            acquireWakeLock(info.mRawHandle);
            boolean isSnF = info.mSessionType != ImSessionInfo.SessionType.NORMAL;
            info.mState = ImSessionInfo.ImSessionState.ACCEPTING;
            this.mImsService.acceptImSession(new AcceptImSessionParams(this.mChatId, getUserAlias(), info.mRawHandle, isSnF, obtainMessage(isSnF ? 1011 : 1007)));
        }
    }

    /* access modifiers changed from: protected */
    public void handleCloseAllSession(ImSessionStopReason closeReason) {
        for (ImSessionInfo info : new ArrayList<>(this.mImSessionInfoList)) {
            this.mClosedState.handleCloseSession(info.mRawHandle, closeReason);
        }
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getImSessionInfo(Object rawHandle) {
        if (rawHandle == null) {
            return null;
        }
        for (ImSessionInfo info : this.mImSessionInfoList) {
            if (rawHandle.equals(info.mRawHandle)) {
                return info;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo removeImSessionInfo(Object rawHandle) {
        ImSessionInfo info = getImSessionInfo(rawHandle);
        if (info == null) {
            return null;
        }
        this.mImSessionInfoList.remove(info);
        return info;
    }

    /* access modifiers changed from: protected */
    public boolean removeImSessionInfo(ImSessionInfo info) {
        return this.mImSessionInfoList.remove(info);
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getLatestActiveImSessionInfo() {
        for (ImSessionInfo info : this.mImSessionInfoList) {
            if (!info.isSnFSession() && info.mState != ImSessionInfo.ImSessionState.PENDING_INVITE && info.mState != ImSessionInfo.ImSessionState.CLOSING) {
                return info;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hasActiveImSessionInfo() {
        return getLatestActiveImSessionInfo() != null;
    }

    private boolean hasImSessionInfo(ImSessionInfo.ImSessionState state) {
        for (ImSessionInfo info : this.mImSessionInfoList) {
            if (info.mState == state) {
                return true;
            }
        }
        return false;
    }

    private boolean hasImSessionInfo(ImSessionInfo.SessionType type) {
        for (ImSessionInfo info : this.mImSessionInfoList) {
            if (info.mSessionType == type) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getImSessionInfoByMessageId(int msgId) {
        for (ImSessionInfo info : this.mImSessionInfoList) {
            if (info.mReceivedMessageIds.contains(Integer.valueOf(msgId))) {
                return info;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void transitionToProperState() {
        IState state;
        Set<ImSessionInfo.ImSessionState> list = new HashSet<>();
        for (ImSessionInfo info : this.mImSessionInfoList) {
            logi("transitionToProperState : ImSessionInfo = " + info);
            if (!info.isSnFSession()) {
                list.add(info.mState);
            }
        }
        if (list.isEmpty()) {
            state = this.mClosedState;
        } else if (list.contains(ImSessionInfo.ImSessionState.ESTABLISHED)) {
            state = this.mEstablishedState;
        } else if (list.contains(ImSessionInfo.ImSessionState.ACCEPTING) || list.contains(ImSessionInfo.ImSessionState.INITIAL) || list.contains(ImSessionInfo.ImSessionState.STARTED) || list.contains(ImSessionInfo.ImSessionState.STARTING)) {
            state = this.mStartingState;
        } else if (list.contains(ImSessionInfo.ImSessionState.CLOSING)) {
            state = this.mClosingState;
        } else {
            state = this.mClosedState;
        }
        if (state != getCurrentState()) {
            transitionTo(state);
        }
    }

    /* access modifiers changed from: protected */
    public void onSimRefresh(int phoneId) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSimRefresh : " + phoneId);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String imsi = sm == null ? null : sm.getImsi();
        if (this.mPhoneId != phoneId && getOwnImsi().equals(imsi)) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "update previous phoneId : " + this.mPhoneId + "to :" + phoneId);
            this.mPhoneId = phoneId;
        }
    }

    public String toString() {
        return "ImSession [mChatData=" + this.mChatData + ", mSdpContentType=" + this.mSdpContentType + ", mThreadId=" + this.mThreadId + ", mSupportedFeatures=" + this.mSupportedFeatures + ", mRemoteAcceptTypes=" + this.mRemoteAcceptTypes + ", mRemoteAcceptWrappedTypes=" + this.mRemoteAcceptWrappedTypes + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mIsComposing=" + this.mIsComposing + ", mParticipants=" + IMSLog.checker(this.mParticipants) + ", mRawHandle=" + this.mRawHandle + ", mClosedReason=" + this.mClosedReason + ", mComposingNotificationInterval=" + this.mComposingNotificationInterval + ", mComposingActiveUris=" + this.mComposingActiveUris + ", mProcessingFileTransfer=" + this.mProcessingFileTransfer + ", mPendingFileTransfer=" + this.mPendingFileTransfer + ", mRequestMessageId=" + this.mRequestMessageId + ", mCurrentMessages=" + this.mCurrentMessages + ", mRawHandle=" + this.mRawHandle + ", mServiceId=" + this.mServiceId + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + "]";
    }

    /* access modifiers changed from: protected */
    public String toStringForDump() {
        return "ImSession [ChatId=" + this.mChatData.getChatId() + ", ConvId=" + this.mChatData.getConversationId() + ", ContId=" + this.mChatData.getContributionId() + ", ChatType=" + this.mChatData.getChatType() + ", Participants=" + IMSLog.checker(this.mParticipants) + ", Status=" + this.mChatData.getState() + ", ClosedReason=" + this.mClosedReason + "]";
    }

    public int hashCode() {
        int i = 31 * 1;
        ChatData chatData = this.mChatData;
        return i + (chatData == null ? 0 : chatData.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImSession other = (ImSession) obj;
        ChatData chatData = this.mChatData;
        if (chatData != null) {
            return chatData.equals(other.mChatData);
        }
        if (other.mChatData == null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateUriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
    }

    /* access modifiers changed from: protected */
    public boolean needToUseGroupChatInvitationUI() {
        boolean ret = isGroupChat() && this.mGetter.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_INVITATIONUI_USED) && !this.mConfig.isAutAcceptGroupChat();
        logi("needToUseGroupChatInvitationUI, ChatState=" + this.mChatData.getState() + ", ret=" + ret);
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean isAutoAccept() {
        if (getRcsStrategy(this.mPhoneId) != null && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.FORCE_AUTO_ACCEPT)) {
            return true;
        }
        if (isGroupChat()) {
            return this.mConfig.isAutAcceptGroupChat();
        }
        return this.mConfig.isAutAccept();
    }

    /* access modifiers changed from: protected */
    public boolean isEstablishedState() {
        return getCurrentState() == this.mEstablishedState;
    }

    /* access modifiers changed from: protected */
    public boolean isEmptySession() {
        return isChatState(ChatData.State.NONE) && !hasImSessionInfo(ImSessionInfo.ImSessionState.PENDING_INVITE);
    }

    /* access modifiers changed from: protected */
    public boolean isVoluntaryDeparture() {
        return isGroupChat() && isChatState(ChatData.State.CLOSED_VOLUNTARILY);
    }

    private IMnoStrategy getRcsStrategy() {
        return this.mGetter.getRcsStrategy();
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy getRcsStrategy(int phoneId) {
        return this.mGetter.getRcsStrategy(phoneId);
    }

    /* access modifiers changed from: protected */
    public Message getFtCompleteCallback() {
        return obtainMessage(ImSessionEvent.FILE_COMPLETE);
    }

    /* access modifiers changed from: protected */
    public void setNetworkFallbackMech(boolean isMsgFallbackSupported, boolean isMsgRevokeSupported) {
        this.mChatFallbackMech = ChatFallbackMech.NONE;
        if (!isGroupChat()) {
            if (isMsgFallbackSupported) {
                this.mChatFallbackMech = ChatFallbackMech.NETWORK_INTERWORKING;
            } else if (isMsgRevokeSupported && this.mConfig.getChatRevokeTimer() > 0) {
                this.mChatFallbackMech = ChatFallbackMech.MESSAGE_REVOCATION;
            }
        }
        logi("setNetworkFallbackMech: isMsgFallbackSupported=" + isMsgFallbackSupported + ", isMsgRevokeSupported=" + isMsgRevokeSupported + ", isGroupChat()=" + isGroupChat() + ", getChatRevokeTimer()=" + this.mConfig.getChatRevokeTimer() + ", mChatFallbackMech=" + this.mChatFallbackMech);
    }

    /* access modifiers changed from: protected */
    public void setNetworkFallbackMech(ChatFallbackMech mech) {
        this.mChatFallbackMech = mech;
        logi("setNetworkFallbackMech: mChatFallbackMech=" + this.mChatFallbackMech);
    }

    /* access modifiers changed from: protected */
    public boolean isMsgFallbackSupported() {
        return this.mChatFallbackMech == ChatFallbackMech.NETWORK_INTERWORKING;
    }

    /* access modifiers changed from: protected */
    public boolean isMsgRevocationSupported() {
        return this.mChatFallbackMech == ChatFallbackMech.MESSAGE_REVOCATION;
    }

    public boolean isTimerExpired() {
        return this.mIsTimerExpired;
    }

    /* access modifiers changed from: protected */
    public void setIsTimerExpired(boolean isTimerExpired) {
        this.mIsTimerExpired = isTimerExpired;
    }

    /* access modifiers changed from: protected */
    public void removeMsgFromListForRevoke(String imdnId) {
        removeMsgFromListForRevoke((Collection<String>) Collections.singletonList(imdnId));
    }

    /* access modifiers changed from: protected */
    public void removeMsgFromListForRevoke(Collection<String> imdnIds) {
        this.mNeedToRevokeMessages.keySet().removeAll(imdnIds);
        this.mListener.removeFromRevokingMessages(imdnIds);
        logi("removeMsgFromListForRevoke() : msg imdnId : " + imdnIds + ", remaining list size : " + this.mNeedToRevokeMessages.size());
        if (this.mNeedToRevokeMessages.isEmpty()) {
            this.mIsRevokeTimerRunning = false;
            PreciseAlarmManager.getInstance(getContext()).removeMessage(obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED));
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, Integer> getNeedToRevokeMessages() {
        return this.mNeedToRevokeMessages;
    }

    /* access modifiers changed from: protected */
    public void reconnectGuardTimerExpired() {
        if (!this.mIsRevokeTimerRunning) {
            sendMessage((int) ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED);
        }
    }

    /* access modifiers changed from: protected */
    public void messageRevocationRequestAll(boolean userSelectResult, int userSelectType) {
        messageRevocationRequest(new ArrayList<>(this.mNeedToRevokeMessages.keySet()), userSelectResult, userSelectType);
    }

    /* access modifiers changed from: protected */
    public void messageRevocationRequest(List<String> imdnIds, boolean userSelectResult, int userSelectType) {
        Collection<MessageBase> messages = new ArrayList<>();
        logi("messageRevocationRequest() : imdnIds : " + imdnIds + " userSelectResult : " + userSelectResult + " userSelectType : " + userSelectType);
        if (userSelectResult) {
            if (userSelectType == 1) {
                for (String imdnId : imdnIds) {
                    MessageBase msg = this.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
                    if (!(msg instanceof ImMessage) && msg != null && msg.getRevocationStatus() == ImConstants.RevocationStatus.PENDING) {
                        messages.add(msg);
                    }
                }
            } else if (userSelectType == 2) {
                for (String imdnId2 : imdnIds) {
                    MessageBase msg2 = this.mGetter.getMessage(imdnId2, ImDirection.OUTGOING);
                    if (!(msg2 instanceof FtMessage) && msg2 != null && msg2.getRevocationStatus() == ImConstants.RevocationStatus.PENDING) {
                        messages.add(msg2);
                    }
                }
            }
            if (!messages.isEmpty()) {
                this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, messages, this);
            }
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST, (Object) imdnIds));
            return;
        }
        for (String imdnId3 : imdnIds) {
            MessageBase msg3 = this.mGetter.getMessage(imdnId3, ImDirection.OUTGOING);
            if (msg3 != null && msg3.getRevocationStatus() == ImConstants.RevocationStatus.PENDING) {
                messages.add(msg3);
            }
        }
        if (!messages.isEmpty()) {
            this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, messages, this);
        }
    }

    /* access modifiers changed from: protected */
    public void startMsgRevokeOperationTimer(String imdnId) {
        logi("startMsgRevokeOperationTimer() : imdnId : " + imdnId);
        sendMessageDelayed(obtainMessage((int) ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT, (Object) imdnId), 10000);
    }

    /* access modifiers changed from: protected */
    public void stopMsgRevokeOperationTimer(String imdnId) {
        logi("stopMsgRevokeOperationTimer() : imdnId : " + imdnId);
        getHandler().removeMessages(ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT, imdnId);
    }

    /* access modifiers changed from: protected */
    public void handleSendingStateRevokeMessages() {
        sendMessage((int) ImSessionEvent.RESEND_MESSAGE_REVOKE_REQUEST);
    }

    /* access modifiers changed from: protected */
    public void onSendDisplayedNotification(List<MessageBase> l) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSendDisplayedNotification : messages = " + l);
        for (MessageBase message : l) {
            ImSessionInfo info = getImSessionInfoByMessageId(message.getId());
            Object rawHandle = this.mRawHandle;
            if (info != null && info.isSnFSession() && info.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
                rawHandle = info.mRawHandle;
            }
            Object rawHandle2 = rawHandle;
            message.sendDisplayedNotification(rawHandle2, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) message.toList()), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
        }
        if (!this.mMessagesToSendDisplayNotification.isEmpty()) {
            sendMessageDelayed((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION, 1500);
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSession$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        static {
            int[] iArr = new int[IMnoStrategy.StatusCode.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = iArr;
            try {
                iArr[IMnoStrategy.StatusCode.FALLBACK_TO_SLM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_SLM_FILE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleUploadedFileFallback(FtHttpOutgoingMessage msg) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[getRcsStrategy(this.mPhoneId).getUploadedFileFallbackSLMTech().getStatusCode().ordinal()];
        if (i == 1) {
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) msg));
        } else if (i == 2) {
            msg.attachSlmFile();
        } else if (i == 3) {
            msg.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
        }
    }

    /* access modifiers changed from: protected */
    public void onEstablishmentTimeOut(Object rawHandle) {
        ImSessionInfo info = getImSessionInfo(rawHandle);
        logi("SESSION_ESTABLISHMENT_TIMEOUT : " + info);
        if (info != null && info.mState != ImSessionInfo.ImSessionState.ESTABLISHED) {
            this.mClosedState.handleCloseSession(info.mRawHandle, ImSessionStopReason.NO_RESPONSE);
        }
    }

    /* access modifiers changed from: protected */
    public Object getRawHandle() {
        return this.mRawHandle;
    }

    /* access modifiers changed from: protected */
    public void setRawHandle(Object handle) {
        this.mRawHandle = handle;
    }

    /* access modifiers changed from: protected */
    public void transitionToStartingState() {
        transitionTo(this.mStartingState);
    }

    /* access modifiers changed from: protected */
    public void addInProgressRequestCallback(Message cb) {
        logi("addInProgressRequestCallback: " + cb.what);
        removeMessages(ImSessionEvent.EVENT_REQUEST_TIMEOUT);
        sendMessageDelayed(obtainMessage(ImSessionEvent.EVENT_REQUEST_TIMEOUT), 5000);
        this.mInProgressRequestCallbacks.add(cb);
    }

    /* access modifiers changed from: protected */
    public void removeInProgressRequestCallback(Message cb) {
        logi("removeInProgressRequestCallback: " + cb.what);
        this.mInProgressRequestCallbacks.remove(cb);
        if (this.mInProgressRequestCallbacks.isEmpty()) {
            removeMessages(ImSessionEvent.EVENT_REQUEST_TIMEOUT);
            handlePendingEvents();
        }
    }

    /* access modifiers changed from: protected */
    public void handleRequestTimeout() {
        logi("handleRequestTimeout: " + this.mInProgressRequestCallbacks);
        for (Message msg : this.mInProgressRequestCallbacks) {
            if (msg.what != 2009) {
                logi("handleRequestTimeout: Unexpected event " + msg.what);
            } else {
                onChangeGroupChatLeaderFailed((List) msg.obj, ImErrorReason.ENGINE_ERROR);
            }
        }
        this.mInProgressRequestCallbacks.clear();
        handlePendingEvents();
    }

    /* access modifiers changed from: protected */
    public void handlePendingEvents() {
        logi("handlePendingEvents: " + this.mPendingEvents);
        for (Message msg : this.mPendingEvents) {
            sendMessage(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void leaveSessionWithReject(Object rawHandle) {
        IMSLog.c(LogClass.IM_INCOMING_SESSION_ERR, "User left");
        this.mImsService.rejectImSession(new RejectImSessionParams(this.mChatId, rawHandle, ImSessionRejectReason.VOLUNTARILY, (Message) null));
        this.mClosedReason = ImSessionClosedReason.CLOSED_BY_LOCAL;
        handleCloseAllSession(ImSessionStopReason.VOLUNTARILY);
        updateChatState(ChatData.State.NONE);
        this.mListener.onChatDeparted(this);
        transitionToProperState();
        releaseWakeLock(rawHandle);
    }
}
