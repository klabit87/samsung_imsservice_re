package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SendFtSessionParams {
    public final Message mCallback;
    public ImsUri mConfUri;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public String mDeviceName;
    public ImDirection mDirection;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public String mFileFingerPrint;
    public String mFileName;
    public String mFilePath;
    public long mFileSize;
    public String mFileTransferID;
    public String mImdnId;
    public Date mImdnTime;
    public String mInReplyToContributionId;
    public boolean mIsPublicAccountMsg;
    public boolean mIsResuming;
    public int mMessageId = -1;
    public String mOwnImsi;
    public List<ImsUri> mRecipients;
    public String mReliableMessage;
    public SendReportMsgParams mReportMsgParams;
    public final Message mSessionHandleCallback;
    public String mThumbPath;
    public int mTimeDuration;
    public long mTransferredBytes;
    public String mUserAlias;

    public SendFtSessionParams(int messageId, String contributionId, String conversationId, String inReplyToContributionId, Message callback, Message sessionHandleCallback, List<ImsUri> recipients, ImsUri confUri, String userAlias, String fileName, String filePath, long fileSize, String contentType, ImDirection direction, boolean isResuming, long transferred, Set<NotificationStatus> dispositionNotification, String imdnId, Date imdnTime, String fileTransferID, String thumbnailPath, int timeDuration, String deviceName, String reliableMessage, boolean extraFt, boolean isPublicAccountMsg, String fileFingerPrint, String ownImsi) {
        this.mMessageId = messageId;
        this.mConversationId = conversationId;
        this.mInReplyToContributionId = inReplyToContributionId;
        this.mCallback = callback;
        this.mSessionHandleCallback = sessionHandleCallback;
        this.mRecipients = new ArrayList(recipients);
        this.mConfUri = confUri;
        this.mUserAlias = userAlias;
        this.mFilePath = filePath;
        this.mFileName = fileName;
        this.mFileSize = fileSize;
        this.mContentType = contentType;
        this.mContributionId = contributionId;
        this.mDirection = direction;
        this.mIsResuming = isResuming;
        this.mTransferredBytes = transferred;
        this.mDispositionNotification = dispositionNotification;
        this.mImdnId = imdnId;
        this.mImdnTime = imdnTime;
        this.mFileTransferID = fileTransferID;
        this.mThumbPath = thumbnailPath;
        this.mTimeDuration = timeDuration;
        this.mDeviceName = deviceName;
        this.mReliableMessage = reliableMessage;
        this.mExtraFt = extraFt;
        this.mIsPublicAccountMsg = isPublicAccountMsg;
        this.mFileFingerPrint = fileFingerPrint;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "SendFtSessionParams [mMessageId=" + this.mMessageId + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mRecipients=" + IMSLog.checker(this.mRecipients) + ", mConfUri=" + this.mConfUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mFileSize=" + this.mFileSize + ", mContentType=" + this.mContentType + ", mContributionId=" + this.mContributionId + ", mDirection=" + this.mDirection + ", mIsResuming=" + this.mIsResuming + ", mTransferredBytes=" + this.mTransferredBytes + ", mDispositionNotification=" + this.mDispositionNotification + ", mImdnId=" + this.mImdnId + ", mImdnTime=" + this.mImdnTime + ", mFileTransferID = " + this.mFileTransferID + ", mCallback=" + this.mCallback + ", mSessionHandleCallback=" + this.mSessionHandleCallback + ", mThumbPath=" + this.mThumbPath + ", mTimeDuration = " + this.mTimeDuration + ", mDeviceName = " + this.mDeviceName + ", mReliableMessage = " + this.mReliableMessage + ", mExtraFt = " + this.mExtraFt + ", mIsPublicAccountMsg = " + this.mIsPublicAccountMsg + ", mFileFingerPrint = " + this.mFileFingerPrint + "]";
    }
}
