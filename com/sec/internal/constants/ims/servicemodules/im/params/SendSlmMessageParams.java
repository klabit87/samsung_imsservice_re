package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Set;

public class SendSlmMessageParams {
    public String mBody;
    public Message mCallback;
    public String mChatId;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public String mDeviceName;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public String mImdnMessageId;
    public Date mImdnTime;
    public String mInReplyToContributionId;
    public boolean mIsBroadcastMsg;
    public boolean mIsChatbotParticipant;
    public boolean mIsPublicAccountMsg;
    public String mMaapTrafficType;
    public int mMessageId = -1;
    public String mOwnImsi;
    public Set<ImsUri> mReceivers;
    public String mReliableMessage;
    public SendReportMsgParams mReportMsgParams;
    public String mUserAlias;

    public SendSlmMessageParams(int messageId, String chatId, String body, String contentType, String userAlias, String imdnMessageId, Date imdnTime, Set<NotificationStatus> dispositionNotification, String contributionId, String conversationId, String inReplyToContributionId, Set<ImsUri> receivers, Message callback, boolean isPublicAccountMsg, boolean isBroadcastMsg, String deviceName, String reliableMessage, boolean extraFt, String ownImsi, boolean isChatbotMessage, String maapTrafficType) {
        this.mMessageId = messageId;
        this.mChatId = chatId;
        this.mBody = body;
        this.mContentType = contentType;
        this.mUserAlias = userAlias;
        this.mImdnMessageId = imdnMessageId;
        this.mImdnTime = imdnTime;
        this.mCallback = callback;
        this.mDispositionNotification = dispositionNotification;
        this.mContributionId = contributionId;
        this.mConversationId = conversationId;
        this.mInReplyToContributionId = inReplyToContributionId;
        this.mReceivers = receivers;
        this.mIsPublicAccountMsg = isPublicAccountMsg;
        this.mIsBroadcastMsg = isBroadcastMsg;
        this.mDeviceName = deviceName;
        this.mReliableMessage = reliableMessage;
        this.mExtraFt = extraFt;
        this.mOwnImsi = ownImsi;
        this.mIsChatbotParticipant = isChatbotMessage;
        this.mMaapTrafficType = maapTrafficType;
    }

    public String toString() {
        return "SendMessageParams [mMessageId=" + this.mMessageId + ", mChatId=" + this.mChatId + ", mBody=" + IMSLog.checker(this.mBody) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mCallback=" + this.mCallback + ", mIsPublicAccountMsg = " + this.mIsPublicAccountMsg + ", mIsBroadcastMsg = " + this.mIsBroadcastMsg + ", mDeviceName = " + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + ", mOwnImsi=" + IMSLog.checker(this.mOwnImsi) + ", mIsChatbotParticipant=" + this.mIsChatbotParticipant + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mInReplyToContributionId = " + this.mInReplyToContributionId + ", mReceivers=" + IMSLog.checker(this.mReceivers) + "]";
    }
}
