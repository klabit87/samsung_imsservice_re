package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageRevokeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendReportMsgParams;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.ImExtensionMNOHeadersHelper;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.interfaces.IModuleInterface;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ThumbnailTool;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public abstract class MessageBase extends Observable {
    public static final int FLAG_FT_SMS = 1;
    public static final int FLAG_TEMPORARY = 2;
    protected static final String LOG_TAG = MessageBase.class.getSimpleName();
    protected String mBody;
    protected final String mChatId;
    private ImConstants.ChatbotMessagingTech mChatbotMessagingTech = ImConstants.ChatbotMessagingTech.UNKNOWN;
    protected final ImConfig mConfig;
    protected String mContentType;
    protected String mContributionId;
    protected String mConversationId;
    protected int mCurrentRetryCount;
    protected long mDeliveredTimestamp;
    protected NotificationStatus mDesiredNotificationStatus = NotificationStatus.NONE;
    protected String mDeviceId;
    protected String mDeviceName;
    protected final ImDirection mDirection;
    protected Set<NotificationStatus> mDispNotification;
    protected long mDisplayedTimestamp;
    protected String mExtInfo;
    protected boolean mExtraFt;
    protected int mFlagMask;
    protected int mId;
    protected final String mImdnId;
    protected String mImdnOriginalTo;
    protected List<ImImdnRecRoute> mImdnRecRouteList;
    protected final IImServiceInterface mImsService;
    protected final long mInsertedTimestamp;
    protected boolean mIsBroadcastMsg;
    protected boolean mIsRoutingMsg;
    protected boolean mIsSlmSvcMsg;
    protected boolean mIsVM2TextMsg;
    protected long mLastDisplayedTimestamp;
    protected NotificationStatus mLastNotificationType = NotificationStatus.NONE;
    protected String mMaapTrafficType;
    protected ImConstants.MessagingTech mMessagingTech;
    protected IMnoStrategy mMnoStrategy;
    private final IModuleInterface mModule;
    protected Network mNetwork;
    protected int mNotDisplayedCounter;
    protected ImsUri mNotificationParticipant;
    protected NotificationStatus mNotificationStatus = NotificationStatus.NONE;
    protected String mRcsTrafficType;
    protected String mReferenceId;
    protected String mReferenceType;
    protected String mReferenceValue;
    protected String mReliableMessage;
    protected ImsUri mRemoteUri;
    protected SendReportMsgParams mReportMsgParams;
    protected String mRequestMessageId;
    protected ImConstants.RevocationStatus mRevocationStatus = ImConstants.RevocationStatus.NONE;
    protected RoutingType mRoutingType = RoutingType.NONE;
    protected long mSentTimestamp;
    protected String mSimIMSI;
    protected final ISlmServiceInterface mSlmService;
    protected ImConstants.Status mStatus;
    protected String mSuggestion;
    protected final ThumbnailTool mThumbnailTool;
    protected ImConstants.Type mType;
    protected UriGenerator mUriGenerator;
    protected String mUserAlias;
    protected String mXmsMessage;

    public abstract String getServiceTag();

    protected MessageBase(Builder<?> builder) {
        Preconditions.checkNotNull(builder.mModule);
        Preconditions.checkNotNull(builder.mModule.getContext());
        Preconditions.checkNotNull(builder.mImsService);
        Preconditions.checkNotNull(builder.mSlmService);
        Preconditions.checkNotNull(builder.mConfig);
        Preconditions.checkNotNull(builder.mUriGenerator);
        this.mModule = builder.mModule;
        this.mImsService = builder.mImsService;
        this.mSlmService = builder.mSlmService;
        this.mConfig = builder.mConfig;
        this.mThumbnailTool = builder.mThumbnailTool;
        this.mUriGenerator = builder.mUriGenerator;
        this.mId = builder.mId;
        this.mChatId = builder.mChatId;
        this.mImdnId = builder.mImdnId;
        this.mImdnOriginalTo = builder.mImdnOriginalTo;
        this.mImdnRecRouteList = builder.mImdnRecRouteList;
        this.mType = builder.mType;
        this.mIsSlmSvcMsg = builder.mIsSlmSvcMsg;
        this.mBody = builder.mBody;
        this.mSuggestion = builder.mSuggestion;
        this.mContentType = builder.mContentType;
        this.mStatus = builder.mStatus;
        this.mDirection = builder.mDirection;
        this.mInsertedTimestamp = builder.mInsertedTimestamp;
        this.mSentTimestamp = builder.mSentTimestamp;
        this.mDeliveredTimestamp = builder.mDeliveredTimestamp;
        this.mDisplayedTimestamp = builder.mDisplayedTimestamp;
        this.mRemoteUri = builder.mRemoteUri;
        this.mUserAlias = builder.mUserAlias;
        this.mDispNotification = builder.mDispNotification;
        this.mNotificationStatus = builder.mNotificationStatus;
        this.mDesiredNotificationStatus = builder.mDesiredNotificationStatus;
        this.mNotDisplayedCounter = builder.mNotDisplayedCounter;
        this.mRequestMessageId = builder.mRequestMessageId;
        this.mIsBroadcastMsg = builder.mIsBroadcastMsg;
        this.mIsVM2TextMsg = builder.mIsVM2TextMsg;
        this.mIsRoutingMsg = builder.mIsRoutingMsg;
        this.mRoutingType = builder.mRoutingType;
        this.mDeviceName = builder.mDeviceName;
        this.mReliableMessage = builder.mReliableMessage;
        this.mExtraFt = builder.mExtraFt;
        this.mXmsMessage = builder.mXmsMessage;
        this.mMnoStrategy = builder.mMnoStrategy;
        this.mNetwork = builder.mNetwork;
        this.mExtInfo = builder.mExtInfo;
        this.mConversationId = builder.mConversationId;
        this.mContributionId = builder.mContributionId;
        this.mDeviceId = builder.mDeviceId;
        this.mSimIMSI = builder.mSimIMSI;
        this.mFlagMask = builder.mFlagMask;
        this.mRevocationStatus = builder.mRevocationStatus;
        this.mMaapTrafficType = builder.mMaapTraficType;
        this.mMessagingTech = builder.mMessagingTech;
        this.mReferenceId = builder.mReferenceId;
        this.mReferenceType = builder.mReferenceType;
        this.mReferenceValue = builder.mReferenceValue;
        this.mRcsTrafficType = builder.mRcsTrafficType;
    }

    public static ImConstants.Type getType(String contentType) {
        if (contentType == null || (!contentType.contains(MIMEContentType.LOCATION_PUSH) && !contentType.contains(MIMEContentType.LOCATION_PULL))) {
            return ImConstants.Type.TEXT;
        }
        return ImConstants.Type.LOCATION;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mModule.getContext();
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        return this.mModule.isWifiConnected();
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getBody() {
        return this.mBody;
    }

    public String getSuggestion() {
        return this.mSuggestion;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public ImConstants.Type getType() {
        return this.mType;
    }

    public boolean getIsSlmSvcMsg() {
        return this.mIsSlmSvcMsg;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public String getImdnId() {
        return this.mImdnId;
    }

    public String getImdnOriginalTo() {
        return this.mImdnOriginalTo;
    }

    public List<ImImdnRecRoute> getImdnRecRouteList() {
        return this.mImdnRecRouteList;
    }

    public void setImdnRecRouteList(List<ImImdnRecRoute> imdnRecRouteList) {
        this.mImdnRecRouteList = imdnRecRouteList;
    }

    public long getInsertedTimestamp() {
        return this.mInsertedTimestamp;
    }

    public long getSentTimestamp() {
        return this.mSentTimestamp;
    }

    public void setSentTimestamp(long time) {
        this.mSentTimestamp = time;
    }

    public long getDeliveredTimestamp() {
        return this.mDeliveredTimestamp;
    }

    public void setDeliveredTimestamp(long time) {
        this.mDeliveredTimestamp = time;
    }

    public Long getDisplayedTimestamp() {
        return Long.valueOf(this.mDisplayedTimestamp);
    }

    public void setDisplayedTimestamp(long time) {
        this.mDisplayedTimestamp = time;
    }

    public Long getLastDisplayedTimestamp() {
        return Long.valueOf(this.mLastDisplayedTimestamp);
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public String getUserAlias() {
        return this.mUserAlias;
    }

    public ImDirection getDirection() {
        return this.mDirection;
    }

    public ImConstants.Status getStatus() {
        return this.mStatus;
    }

    public void setStatus(ImConstants.Status status) {
        this.mStatus = status;
    }

    public Set<NotificationStatus> getDispositionNotification() {
        return this.mDispNotification;
    }

    public NotificationStatus getNotificationStatus() {
        return this.mNotificationStatus;
    }

    public NotificationStatus getDesiredNotificationStatus() {
        return this.mDesiredNotificationStatus;
    }

    public void setDesiredNotificationStatus(NotificationStatus status) {
        this.mDesiredNotificationStatus = status;
    }

    public NotificationStatus getLastNotificationType() {
        return this.mLastNotificationType;
    }

    public int getNotDisplayedCounter() {
        return this.mNotDisplayedCounter;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public ImsUri getNotificationParticipant() {
        return this.mNotificationParticipant;
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    public void updateStatus(ImConstants.Status status) {
        if (this.mStatus != status) {
            this.mStatus = status;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateExtInfo(String extinfo) {
        this.mExtInfo = extinfo;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDeliveredTimestamp(long time) {
        this.mDeliveredTimestamp = time;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDisplayedTimestamp(long time) {
        this.mDisplayedTimestamp = time;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDesiredNotificationStatus(NotificationStatus status) {
        if (this.mDesiredNotificationStatus != status) {
            this.mDesiredNotificationStatus = status;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public String getRequestMessageId() {
        return this.mRequestMessageId;
    }

    public void updateNotificationStatus(NotificationStatus status) {
        if (status != this.mNotificationStatus) {
            this.mNotificationStatus = status;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateRevocationStatus(ImConstants.RevocationStatus status) {
        if (status != this.mRevocationStatus) {
            this.mRevocationStatus = status;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public boolean isDeliveredNotificationRequired() {
        return this.mDirection == ImDirection.INCOMING && this.mDispNotification.contains(NotificationStatus.DELIVERED);
    }

    public boolean isDisplayedNotificationRequired() {
        return this.mDirection == ImDirection.INCOMING && this.mDispNotification.contains(NotificationStatus.DISPLAYED);
    }

    public List<MessageBase> toList() {
        List<MessageBase> l = new ArrayList<>();
        l.add(this);
        return l;
    }

    public SendImdnParams.ImdnData getNewImdnData(NotificationStatus status) {
        return new SendImdnParams.ImdnData(status, this.mImdnId, getNewDate(this.mSentTimestamp), this.mImdnRecRouteList, this.mImdnOriginalTo);
    }

    public boolean isBroadcastMsg() {
        return this.mIsBroadcastMsg;
    }

    public boolean isVM2TextMsg() {
        return this.mIsVM2TextMsg;
    }

    public boolean isRoutingMsg() {
        return this.mIsRoutingMsg;
    }

    public RoutingType getRoutingType() {
        return this.mRoutingType;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public String getReliableMessage() {
        return this.mReliableMessage;
    }

    public void setReliableMessage(String reliableMessage) {
        this.mReliableMessage = reliableMessage;
    }

    public boolean getExtraFt() {
        return this.mExtraFt;
    }

    public void setExtraFt(boolean extraFt) {
        this.mExtraFt = extraFt;
    }

    public boolean isFtSms() {
        return (this.mFlagMask & 1) == 1;
    }

    public void setFtSms(boolean ftSms) {
        int i = this.mFlagMask;
        this.mFlagMask = ftSms ? i | 1 : i & -2;
    }

    public boolean isTemporary() {
        return (this.mFlagMask & 2) == 2;
    }

    public void setTemporary(boolean isTemporary) {
        int i = this.mFlagMask;
        this.mFlagMask = isTemporary ? i | 2 : i & -3;
    }

    public void setSlmSvcMsg(boolean isSlmSvcMsg) {
        this.mIsSlmSvcMsg = isSlmSvcMsg;
    }

    public ImConstants.MessagingTech getMessagingTech() {
        return this.mMessagingTech;
    }

    public void setMessagingTech(ImConstants.MessagingTech messagingTech) {
        this.mMessagingTech = messagingTech;
    }

    public ImConstants.ChatbotMessagingTech getChatbotMessagingTech() {
        return this.mChatbotMessagingTech;
    }

    public void setChatbotMessagingTech(ImConstants.ChatbotMessagingTech chatbotMessagingTech) {
        this.mChatbotMessagingTech = chatbotMessagingTech;
    }

    public int getFlagMask() {
        return this.mFlagMask;
    }

    public ImConstants.RevocationStatus getRevocationStatus() {
        return this.mRevocationStatus;
    }

    public void setRevocationStatus(ImConstants.RevocationStatus status) {
        this.mRevocationStatus = status;
    }

    public String getXmsMessage() {
        return this.mXmsMessage;
    }

    public boolean isOutgoing() {
        return this.mDirection == ImDirection.OUTGOING;
    }

    public boolean isIncoming() {
        return this.mDirection == ImDirection.INCOMING;
    }

    public void setSpamInfo(ImsUri spamFrom, ImsUri spamTo, String spamDate, int spamMsgId) {
        this.mReportMsgParams = new SendReportMsgParams(spamFrom, spamTo, spamDate, spamMsgId);
    }

    public SendReportMsgParams getReportMsgParams() {
        return this.mReportMsgParams;
    }

    public String getConversationId() {
        return this.mConversationId;
    }

    public String getContributionId() {
        return this.mContributionId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getOwnIMSI() {
        return this.mSimIMSI;
    }

    public void updateOwnIMSI(String imsi) {
        if (imsi != null && !"".equals(imsi) && !imsi.equals(this.mSimIMSI)) {
            this.mSimIMSI = imsi;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateRemoteUri(ImsUri remoteUri) {
        this.mRemoteUri = remoteUri;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public String getMaapTrafficType() {
        return this.mMaapTrafficType;
    }

    public ImConstants.ChatbotTrafficType getChatbotTrafficType() {
        return ImConstants.ChatbotTrafficType.NONE;
    }

    public String getReferenceId() {
        return this.mReferenceId;
    }

    public String getReferenceType() {
        return this.mReferenceType;
    }

    public String getReferenceValue() {
        return this.mReferenceValue;
    }

    public String getRcsTrafficType() {
        return this.mRcsTrafficType;
    }

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
    }

    private ImsUri getParticipantsNetworkPreferredUri(ImsUri uri) {
        ICapabilityDiscoveryModule discoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        ImsUri uriToUse = discoveryModule != null ? discoveryModule.getNetworkPreferredUri(uri) : null;
        if (uriToUse == null) {
            return this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri);
        }
        return uriToUse;
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
        if (this.mRemoteUri == null) {
            onSendDeliveredNotificationDone();
            return;
        }
        ImsUri remoteUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, getParticipantsNetworkPreferredUri(this.mRemoteUri));
        String str = this.mChatId;
        String str2 = this.mConversationId;
        String str3 = str2 == null ? conversationId : str2;
        String str4 = this.mContributionId;
        SendImdnParams sendImdnParams = new SendImdnParams(rawHandle, remoteUri, str, str3, str4 == null ? contributionId : str4, ownImsi, onComplete, this.mDeviceId, getNewImdnData(NotificationStatus.DELIVERED), isGroupchat, new Date(), isBotSessionAnonymized);
        if (this.mIsSlmSvcMsg) {
            if (isVM2TextMsg()) {
                sendImdnParams.addImExtensionMNOHeaders(ImExtensionMNOHeadersHelper.addVM2TextHeaders());
            }
            this.mSlmService.sendSlmDeliveredNotification(sendImdnParams);
            return;
        }
        this.mImsService.sendDeliveredNotification(sendImdnParams);
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object rawHandle, String conversationId, String contributionId, Message onComplete, String ownImsi, boolean isGroupchat, boolean isBotSessionAnonymized) {
        if (this.mRemoteUri == null) {
            onSendDisplayedNotificationDone();
            return;
        }
        ImsUri remoteUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, getParticipantsNetworkPreferredUri(this.mRemoteUri));
        String str = this.mChatId;
        String str2 = this.mConversationId;
        String str3 = str2 == null ? conversationId : str2;
        String str4 = this.mContributionId;
        SendImdnParams sendImdnParams = new SendImdnParams(rawHandle, remoteUri, str, str3, str4 == null ? contributionId : str4, ownImsi, onComplete, this.mDeviceId, getNewImdnData(NotificationStatus.DISPLAYED), isGroupchat, new Date(), isBotSessionAnonymized);
        if (this.mIsSlmSvcMsg) {
            if (isVM2TextMsg()) {
                sendImdnParams.addImExtensionMNOHeaders(ImExtensionMNOHeadersHelper.addVM2TextHeaders());
            }
            this.mSlmService.sendSlmDisplayedNotification(sendImdnParams);
            return;
        }
        this.mImsService.sendDisplayedNotification(sendImdnParams);
    }

    /* access modifiers changed from: protected */
    public void onSendDeliveredNotificationDone() {
        if (this.mNotificationStatus != NotificationStatus.DISPLAYED) {
            updateNotificationStatus(NotificationStatus.DELIVERED);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendDisplayedNotificationDone() {
        updateNotificationStatus(NotificationStatus.DISPLAYED);
    }

    public void onImdnNotificationReceived(ImdnNotificationEvent event) {
        if (this.mDirection != ImDirection.OUTGOING) {
            Log.e(LOG_TAG, "Incoming message received imdn notification, ignore.");
            return;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[event.mStatus.ordinal()];
        if (i == 1) {
            this.mNotificationStatus = NotificationStatus.DELIVERED;
            this.mLastNotificationType = NotificationStatus.DELIVERED;
            setDeliveredTimestamp(event.mCpimDate.getTime());
            setRevocationStatus(ImConstants.RevocationStatus.NONE);
            this.mNotificationParticipant = event.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        } else if (i == 2) {
            if (this.mNotificationStatus == NotificationStatus.NONE) {
                this.mNotificationStatus = NotificationStatus.DELIVERED;
                setRevocationStatus(ImConstants.RevocationStatus.NONE);
                setDeliveredTimestamp(event.mCpimDate.getTime());
            }
            this.mLastNotificationType = NotificationStatus.DISPLAYED;
            this.mLastDisplayedTimestamp = event.mCpimDate.getTime();
            if (this.mNotDisplayedCounter > 0) {
                String str = LOG_TAG;
                Log.i(str, "onImdnNotificationReceived: Decrease mNotDisplayedCounter " + this.mNotDisplayedCounter);
                this.mNotDisplayedCounter = this.mNotDisplayedCounter - 1;
            }
            if (this.mNotDisplayedCounter == 0) {
                this.mNotificationStatus = NotificationStatus.DISPLAYED;
                setDisplayedTimestamp(event.mCpimDate.getTime());
            }
            this.mNotificationParticipant = event.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        } else if (i != 3) {
            if (i == 4 && this.mNotificationStatus != NotificationStatus.DELIVERED && this.mNotificationStatus != NotificationStatus.DISPLAYED) {
                this.mNotificationStatus = NotificationStatus.INTERWORKING_MMS;
                this.mLastNotificationType = NotificationStatus.INTERWORKING_MMS;
                setDeliveredTimestamp(event.mCpimDate.getTime());
                setRevocationStatus(ImConstants.RevocationStatus.NONE);
                this.mNotificationParticipant = event.mRemoteUri;
                triggerObservers(ImCacheAction.UPDATED);
            }
        } else if (this.mNotificationStatus != NotificationStatus.DELIVERED && this.mNotificationStatus != NotificationStatus.DISPLAYED) {
            this.mNotificationStatus = NotificationStatus.INTERWORKING_SMS;
            this.mLastNotificationType = NotificationStatus.INTERWORKING_SMS;
            setDeliveredTimestamp(event.mCpimDate.getTime());
            setRevocationStatus(ImConstants.RevocationStatus.NONE);
            this.mNotificationParticipant = event.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.MessageBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus;

        static {
            int[] iArr = new int[NotificationStatus.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = iArr;
            try {
                iArr[NotificationStatus.DELIVERED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.DISPLAYED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.INTERWORKING_SMS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.INTERWORKING_MMS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public int getCurrentRetryCount() {
        return this.mCurrentRetryCount;
    }

    public void incrementRetryCount() {
        this.mCurrentRetryCount++;
    }

    public void triggerObservers(ImCacheAction action) {
        setChanged();
        notifyObservers(action);
    }

    public IMnoStrategy getRcsStrategy() {
        return this.mMnoStrategy;
    }

    public int hashCode() {
        int i = 31 * 1;
        String str = this.mChatId;
        return (31 * (i + (str == null ? 0 : str.hashCode()))) + this.mId;
    }

    public String toString() {
        return "MessageBase [mChatId=" + this.mChatId + ", mId=" + this.mId + ", mBody=" + IMSLog.checker(this.mBody) + ", mImdnId=" + this.mImdnId + ", mRemoteUri=" + IMSLog.numberChecker(this.mRemoteUri) + ", mType=" + this.mType + ", mContentType=" + this.mContentType + ", mImdnOriginalTo=" + IMSLog.checker(this.mImdnOriginalTo) + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mStatus=" + this.mStatus + ", mInsertedTimestamp=" + this.mInsertedTimestamp + ", mSentTimestamp=" + this.mSentTimestamp + ", mDeliveredTimestamp=" + this.mDeliveredTimestamp + ", mDisplayedTimestamp=" + this.mDisplayedTimestamp + ", mDirection=" + this.mDirection + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mCurrentRetryCount=" + this.mCurrentRetryCount + ", mDispNotification=" + this.mDispNotification + ", mNotificationStatus=" + this.mNotificationStatus + ", mDesiredNotificationStatus=" + this.mDesiredNotificationStatus + ", mNotDisplayedCounter=" + this.mNotDisplayedCounter + ", mIsBroadcastMsg=" + this.mIsBroadcastMsg + ", mDeviceId=" + this.mDeviceId + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mReferenceId=" + this.mReferenceId + ", mReferenceType=" + this.mReferenceType + ", mReferenceValue=" + this.mReferenceValue + ", mRcsTrafficType=" + this.mRcsTrafficType + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageBase other = (MessageBase) obj;
        String str = this.mChatId;
        if (str == null) {
            if (other.mChatId != null) {
                return false;
            }
        } else if (!str.equals(other.mChatId)) {
            return false;
        }
        if (this.mId == other.mId) {
            return true;
        }
        return false;
    }

    public void sendMessageRevokeRequest(String conversationId, String contributionId, Message onComplete, String ownImsi) {
        if (this.mRemoteUri == null) {
            Log.e(LOG_TAG, "remote uri is null");
            return;
        }
        this.mImsService.sendMessageRevokeRequest(new SendMessageRevokeParams(this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mRemoteUri.getMsisdn(), (String) null), this.mImdnId, onComplete, conversationId, contributionId, ownImsi));
    }

    public Date getNewDate(long timeStamp) {
        return new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp);
    }

    public static abstract class Builder<T extends Builder<T>> {
        /* access modifiers changed from: private */
        public String mBody;
        /* access modifiers changed from: private */
        public String mChatId;
        /* access modifiers changed from: private */
        public ImConfig mConfig;
        /* access modifiers changed from: private */
        public String mContentType;
        /* access modifiers changed from: private */
        public String mContributionId;
        /* access modifiers changed from: private */
        public String mConversationId;
        /* access modifiers changed from: private */
        public long mDeliveredTimestamp;
        /* access modifiers changed from: private */
        public NotificationStatus mDesiredNotificationStatus = NotificationStatus.NONE;
        /* access modifiers changed from: private */
        public String mDeviceId;
        /* access modifiers changed from: private */
        public String mDeviceName;
        /* access modifiers changed from: private */
        public ImDirection mDirection;
        /* access modifiers changed from: private */
        public final Set<NotificationStatus> mDispNotification = new HashSet();
        /* access modifiers changed from: private */
        public long mDisplayedTimestamp;
        /* access modifiers changed from: private */
        public String mExtInfo;
        /* access modifiers changed from: private */
        public boolean mExtraFt;
        /* access modifiers changed from: private */
        public int mFlagMask;
        /* access modifiers changed from: private */
        public int mId;
        /* access modifiers changed from: private */
        public String mImdnId;
        /* access modifiers changed from: private */
        public String mImdnOriginalTo;
        /* access modifiers changed from: private */
        public List<ImImdnRecRoute> mImdnRecRouteList;
        /* access modifiers changed from: private */
        public IImServiceInterface mImsService;
        /* access modifiers changed from: private */
        public long mInsertedTimestamp;
        /* access modifiers changed from: private */
        public boolean mIsBroadcastMsg;
        /* access modifiers changed from: private */
        public boolean mIsRoutingMsg;
        /* access modifiers changed from: private */
        public boolean mIsSlmSvcMsg;
        /* access modifiers changed from: private */
        public boolean mIsVM2TextMsg;
        /* access modifiers changed from: private */
        public String mMaapTraficType;
        /* access modifiers changed from: private */
        public ImConstants.MessagingTech mMessagingTech = ImConstants.MessagingTech.NORMAL;
        /* access modifiers changed from: private */
        public IMnoStrategy mMnoStrategy;
        /* access modifiers changed from: private */
        public IModuleInterface mModule;
        /* access modifiers changed from: private */
        public Network mNetwork;
        /* access modifiers changed from: private */
        public int mNotDisplayedCounter;
        /* access modifiers changed from: private */
        public NotificationStatus mNotificationStatus = NotificationStatus.NONE;
        private ImsUri mPreferredUri;
        /* access modifiers changed from: private */
        public String mRcsTrafficType;
        /* access modifiers changed from: private */
        public String mReferenceId;
        /* access modifiers changed from: private */
        public String mReferenceType;
        /* access modifiers changed from: private */
        public String mReferenceValue;
        /* access modifiers changed from: private */
        public String mReliableMessage;
        /* access modifiers changed from: private */
        public ImsUri mRemoteUri;
        /* access modifiers changed from: private */
        public String mRequestMessageId;
        /* access modifiers changed from: private */
        public ImConstants.RevocationStatus mRevocationStatus = ImConstants.RevocationStatus.NONE;
        /* access modifiers changed from: private */
        public RoutingType mRoutingType;
        /* access modifiers changed from: private */
        public long mSentTimestamp;
        /* access modifiers changed from: private */
        public String mSimIMSI;
        /* access modifiers changed from: private */
        public ISlmServiceInterface mSlmService;
        /* access modifiers changed from: private */
        public ImConstants.Status mStatus;
        /* access modifiers changed from: private */
        public String mSuggestion;
        /* access modifiers changed from: private */
        public ThumbnailTool mThumbnailTool;
        /* access modifiers changed from: private */
        public ImConstants.Type mType;
        /* access modifiers changed from: private */
        public UriGenerator mUriGenerator;
        /* access modifiers changed from: private */
        public String mUserAlias;
        /* access modifiers changed from: private */
        public String mXmsMessage;

        /* access modifiers changed from: protected */
        public abstract T self();

        public T module(IModuleInterface module) {
            this.mModule = module;
            return self();
        }

        public T imsService(IImServiceInterface imsService) {
            this.mImsService = imsService;
            return self();
        }

        public T slmService(ISlmServiceInterface slmService) {
            this.mSlmService = slmService;
            return self();
        }

        public T uriGenerator(UriGenerator uriGenerator) {
            this.mUriGenerator = uriGenerator;
            return self();
        }

        public T config(ImConfig config) {
            this.mConfig = config;
            return self();
        }

        public T thumbnailTool(ThumbnailTool tool) {
            this.mThumbnailTool = tool;
            return self();
        }

        public T id(int id) {
            this.mId = id;
            return self();
        }

        public T chatId(String chatId) {
            this.mChatId = chatId;
            return self();
        }

        public T imdnId(String imdnId) {
            this.mImdnId = imdnId;
            return self();
        }

        public T imdnIdOriginalTo(String imdnOrigToHdr) {
            this.mImdnOriginalTo = imdnOrigToHdr;
            return self();
        }

        public T imdnRecordRouteList(List<ImImdnRecRoute> imdnRecRouteList) {
            if (imdnRecRouteList != null) {
                this.mImdnRecRouteList = new ArrayList(imdnRecRouteList);
            }
            return self();
        }

        public T contentType(String contentType) {
            this.mContentType = contentType;
            return self();
        }

        public T type(ImConstants.Type type) {
            this.mType = type;
            return self();
        }

        public T isSlmSvcMsg(boolean isSlm) {
            this.mIsSlmSvcMsg = isSlm;
            return self();
        }

        public T status(ImConstants.Status status) {
            this.mStatus = status;
            return self();
        }

        public T sentTimestamp(long sentTimestamp) {
            this.mSentTimestamp = sentTimestamp;
            return self();
        }

        public T insertedTimestamp(long insertedTimestamp) {
            this.mInsertedTimestamp = insertedTimestamp;
            return self();
        }

        public T deliveredTimestamp(long deliveredTimestamp) {
            this.mDeliveredTimestamp = deliveredTimestamp;
            return self();
        }

        public T displayedTimestamp(long displayedTimestamp) {
            this.mDisplayedTimestamp = displayedTimestamp;
            return self();
        }

        public T remoteUri(ImsUri remoteUri) {
            this.mRemoteUri = remoteUri;
            return self();
        }

        public T direction(ImDirection direction) {
            this.mDirection = direction;
            return self();
        }

        public T userAlias(String userAlias) {
            this.mUserAlias = userAlias;
            return self();
        }

        public T dispNotification(Set<NotificationStatus> dispNotification) {
            if (dispNotification != null) {
                this.mDispNotification.addAll(dispNotification);
            }
            return self();
        }

        public T notificationStatus(NotificationStatus notificationStatus) {
            this.mNotificationStatus = notificationStatus;
            return self();
        }

        public T desiredNotificationStatus(NotificationStatus desiredNotificationStatus) {
            this.mDesiredNotificationStatus = desiredNotificationStatus;
            return self();
        }

        public T notDisplayedCounter(int notDisplayedCounter) {
            this.mNotDisplayedCounter = notDisplayedCounter;
            return self();
        }

        public T requestMessageId(String requestMessageId) {
            this.mRequestMessageId = requestMessageId;
            return self();
        }

        public T body(String body) {
            this.mBody = body;
            return self();
        }

        public T suggestion(String suggestion) {
            this.mSuggestion = suggestion;
            return self();
        }

        public T isBroadcastMsg(boolean isBroadcastMsg) {
            this.mIsBroadcastMsg = isBroadcastMsg;
            return self();
        }

        public T isVM2TextMsg(boolean isVM2TextMsg) {
            this.mIsVM2TextMsg = isVM2TextMsg;
            return self();
        }

        public T isRoutingMsg(boolean isRoutingMsg) {
            this.mIsRoutingMsg = isRoutingMsg;
            return self();
        }

        public T routingType(RoutingType routingType) {
            this.mRoutingType = routingType;
            return self();
        }

        public T deviceName(String deviceName) {
            this.mDeviceName = deviceName;
            return self();
        }

        public T reliableMessage(String reliableMessage) {
            this.mReliableMessage = reliableMessage;
            return self();
        }

        public T extraFt(boolean extraFt) {
            this.mExtraFt = extraFt;
            return self();
        }

        public T xmsMessage(String xmsMessage) {
            this.mXmsMessage = xmsMessage;
            return self();
        }

        public T mnoStrategy(IMnoStrategy mnoStrategy) {
            this.mMnoStrategy = mnoStrategy;
            return self();
        }

        public T network(Network network) {
            this.mNetwork = network;
            return self();
        }

        public T extinfo(String extinfo) {
            this.mExtInfo = extinfo;
            return self();
        }

        public T contributionId(String contributionId) {
            this.mContributionId = contributionId;
            return self();
        }

        public T conversationId(String conversationId) {
            this.mConversationId = conversationId;
            return self();
        }

        public T deviceId(String deviceId) {
            this.mDeviceId = deviceId;
            return self();
        }

        public T simIMSI(String simIMSI) {
            this.mSimIMSI = simIMSI;
            return self();
        }

        public T flagMask(int flagMask) {
            this.mFlagMask = flagMask;
            return self();
        }

        public T revocationStatus(ImConstants.RevocationStatus revocationStatus) {
            this.mRevocationStatus = revocationStatus;
            return self();
        }

        public T maapTrafficType(String maapTrafficType) {
            this.mMaapTraficType = maapTrafficType;
            return self();
        }

        public T messagingTech(ImConstants.MessagingTech messagingTech) {
            this.mMessagingTech = messagingTech;
            return self();
        }

        public T referenceId(String referenceId) {
            this.mReferenceId = referenceId;
            return self();
        }

        public T referenceType(String referenceType) {
            this.mReferenceType = referenceType;
            return self();
        }

        public T referenceValue(String referenceValue) {
            this.mReferenceValue = referenceValue;
            return self();
        }

        public T rcsTrafficType(String rcsTrafficType) {
            this.mRcsTrafficType = rcsTrafficType;
            return self();
        }
    }
}
