package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class StartImSessionParams {
    public List<String> mAcceptTypes;
    public List<String> mAcceptWrappedTypes;
    public Message mCallback;
    public String mChatId;
    public ChatMode mChatMode;
    public String mContributionId;
    public String mConversationId;
    public Message mDedicatedBearerCallback;
    public String mInReplyToContributionId;
    public boolean mIsChatbotParticipant;
    public boolean mIsClosedGroupChat;
    public boolean mIsConf;
    public boolean mIsGeolocationPush;
    public boolean mIsInviteForBye;
    public boolean mIsRejoin;
    public String mOwnImsi;
    public String mPrevContributionId;
    public List<ImsUri> mReceivers;
    public String mSdpContentType;
    public SendMessageParams mSendMessageParams;
    public String mServiceId;
    public ServiceType mServiceType;
    public String mSubject;
    public Message mSynchronousCallback;
    public String mUserAlias;

    public enum ServiceType {
        NORMAL
    }

    public StartImSessionParams(String chatId, String subject, List<ImsUri> receivers, String contributionId, String prevContributionId, String userAlias, ServiceType serviceType, boolean isConf, String sdpContentType, Message callback, Message dedicatedBearerCb, Message synchronousCallback, SendMessageParams sendMessageParams, String conversationId, String inReplyToContributionId, boolean isRejoin, boolean isClosedGroupChat, boolean isInviteForBye, String serviceId, List<String> acceptTypes, List<String> acceptWrappedTypes, String ownImsi, boolean chatbotParticipant, ChatMode chatMode) {
        this.mChatId = chatId;
        this.mSubject = subject;
        this.mReceivers = new ArrayList(receivers);
        this.mContributionId = contributionId;
        this.mPrevContributionId = prevContributionId;
        this.mUserAlias = userAlias;
        this.mServiceType = serviceType;
        this.mIsConf = isConf;
        this.mSdpContentType = sdpContentType;
        this.mCallback = callback;
        this.mSynchronousCallback = synchronousCallback;
        this.mDedicatedBearerCallback = dedicatedBearerCb;
        this.mSendMessageParams = sendMessageParams;
        this.mConversationId = conversationId;
        this.mInReplyToContributionId = inReplyToContributionId;
        this.mIsRejoin = isRejoin;
        this.mIsClosedGroupChat = isClosedGroupChat;
        this.mIsInviteForBye = isInviteForBye;
        this.mServiceId = serviceId;
        this.mAcceptTypes = new ArrayList(acceptTypes);
        this.mAcceptWrappedTypes = new ArrayList(acceptWrappedTypes);
        this.mOwnImsi = ownImsi;
        this.mIsChatbotParticipant = chatbotParticipant;
        this.mChatMode = chatMode;
    }

    public String toString() {
        return "StartImSessionParams [mChatId=" + this.mChatId + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mReceivers=" + IMSLog.checker(this.mReceivers) + ", mContributionId=" + this.mContributionId + ", mPrevContributionId=" + this.mPrevContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mServiceType=" + this.mServiceType + ", mIsConf=" + this.mIsConf + ", mSdpContentType=" + this.mSdpContentType + ", mCallback=" + this.mCallback + ", mSendMessageParams=" + this.mSendMessageParams + ", mIsRejoin=" + this.mIsRejoin + ", mIsClosedGroupChat=" + this.mIsClosedGroupChat + ", mIsInviteForBye=" + this.mIsInviteForBye + ", mServiceId=" + this.mServiceId + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + ", mOwnImsi=" + IMSLog.checker(this.mOwnImsi) + ", mIsChatbotParticipant=" + this.mIsChatbotParticipant + ", mChatMode=" + this.mChatMode + "]";
    }
}
