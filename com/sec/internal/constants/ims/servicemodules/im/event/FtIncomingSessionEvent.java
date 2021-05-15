package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class FtIncomingSessionEvent {
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public ImCpimNamespaces mCpimNamespaces;
    public String mDeviceId;
    public String mDeviceName;
    public Set<NotificationStatus> mDisposition;
    public int mEnd;
    public boolean mExtraFt;
    public String mFileName;
    public String mFilePath;
    public long mFileSize;
    public String mFileTransferId;
    public String mImdnId;
    public Date mImdnTime;
    public String mInReplyToConversationId;
    public boolean mIsConference;
    public boolean mIsLMM;
    public boolean mIsPublicAccountMsg;
    public boolean mIsRoutingMsg;
    public boolean mIsSlmSvcMsg;
    public String mOriginalToHdr;
    public String mOwnImsi;
    public ImsUri mPAssertedId;
    public List<ImsUri> mParticipants;
    public boolean mPush;
    public Object mRawHandle;
    public List<ImImdnRecRoute> mRecRouteList;
    public ImsUri mReceiver;
    public String mReliableMessage;
    public ImsUri mRequestUri;
    public RoutingType mRoutingType;
    public String mSdpContentType;
    public ImsUri mSenderUri;
    public int mStart;
    public String mThumbPath;
    public int mTimeDuration;
    public String mUserAlias;

    public String toString() {
        return "FtIncomingSessionEvent [mContributionId=" + this.mContributionId + ", mSenderUri=" + IMSLog.checker(this.mSenderUri) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mContentType=" + this.mContentType + ", mFileSize=" + this.mFileSize + ", mSdpContentType=" + this.mSdpContentType + ", mRawHandle=" + this.mRawHandle + ", mConversationId=" + this.mConversationId + ", mInReplyToConversationId=" + this.mInReplyToConversationId + ", mFileTransferId=" + this.mFileTransferId + ", mStart=" + this.mStart + ", mEnd=" + this.mEnd + ", mPush=" + this.mPush + ", mThumbPath=" + this.mThumbPath + ", mImdnId=" + this.mImdnId + ", mImdnTime=" + this.mImdnTime + ", mDisposition=" + this.mDisposition + ", mDeviceId=" + this.mDeviceId + ", mOriginalToHdr=" + this.mOriginalToHdr + ", mRecRouteList=" + this.mRecRouteList + ", mParticipants=" + IMSLog.checker(this.mParticipants) + ", mTimeDuration=" + this.mTimeDuration + ", mIsPublicAccountMsg=" + this.mIsPublicAccountMsg + ", mIsConference=" + this.mIsConference + ", mDeviceName=" + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + ", mCpimNamespaces=" + this.mCpimNamespaces + ", mIsRoutingMsg=" + this.mIsRoutingMsg + ", mRequestUri= " + IMSLog.checker(this.mRequestUri) + ", mPAssertedId= " + IMSLog.checker(this.mPAssertedId) + ", mReceiver= " + IMSLog.checker(this.mReceiver) + ", mRoutingType= " + this.mRoutingType + ", mIsLMM= " + this.mIsLMM + "]";
    }
}
