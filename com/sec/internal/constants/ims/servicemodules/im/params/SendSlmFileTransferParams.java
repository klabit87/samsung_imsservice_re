package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class SendSlmFileTransferParams {
    public Message mCallback;
    public String mConfUri;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public String mDeviceName;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public String mFileName;
    public String mFilePath;
    public long mFileSize;
    public String mImdnMsgId;
    public String mInReplyToContributionId;
    public boolean mIsBroadcastMsg;
    public int mMessageId = -1;
    public String mOwnImsi;
    public Set<ImsUri> mRecipients;
    public String mReliableMessage;
    public String mSdpContentType;
    public String mUserAlias;

    public SendSlmFileTransferParams(int messageId, Set<ImsUri> recipients, String confUri, String userAlias, String fileName, String filePath, long fileSize, String contentType, String sdpContentType, String contributionId, String conversationId, String inReplyToContributionId, String imdnMsgId, Set<NotificationStatus> dispNotifType, Message callback, boolean isBroadcastMsg, String deviceName, String reliableMessage, boolean extraFt, String ownImsi) {
        this.mMessageId = messageId;
        this.mRecipients = recipients;
        this.mConfUri = confUri;
        this.mUserAlias = userAlias;
        this.mFileName = fileName;
        this.mFilePath = filePath;
        this.mFileSize = fileSize;
        this.mContentType = contentType;
        this.mSdpContentType = sdpContentType;
        this.mContributionId = contributionId;
        this.mConversationId = conversationId;
        this.mInReplyToContributionId = inReplyToContributionId;
        this.mImdnMsgId = imdnMsgId;
        this.mDispositionNotification = dispNotifType;
        this.mCallback = callback;
        this.mIsBroadcastMsg = isBroadcastMsg;
        this.mDeviceName = deviceName;
        this.mReliableMessage = reliableMessage;
        this.mExtraFt = extraFt;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "SendSlmFileTransferParams [mMessageId=" + this.mMessageId + ", mRecipients=" + IMSLog.checker(this.mRecipients) + ", mConfUri=" + this.mConfUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mFileSize=" + this.mFileSize + ", mContentType=" + this.mContentType + ", mSdpContentType=" + this.mSdpContentType + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mImdnMsgId=" + this.mImdnMsgId + ", mDispositionNotification=" + this.mDispositionNotification + ", mCallback=" + this.mCallback + ", mIsBroadcastMsg=" + this.mIsBroadcastMsg + ", mDeviceName=" + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + "]";
    }
}
