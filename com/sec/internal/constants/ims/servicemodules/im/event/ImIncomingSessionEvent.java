package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class ImIncomingSessionEvent {
    public List<String> mAcceptTypes;
    public List<String> mAcceptWrappedTypes;
    public String mContributionId;
    public String mConversationId;
    public String mDeviceId;
    public boolean mFromBlocked;
    public String mInReplyToContributionId;
    public ImsUri mInitiator;
    public String mInitiatorAlias;
    public boolean mIsChatbotRole;
    public boolean mIsClosedGroupChat;
    public boolean mIsDeferred;
    public boolean mIsForStoredNoti;
    public boolean mIsMsgFallbackSupported;
    public boolean mIsMsgRevokeSupported;
    public boolean mIsParticipantNtfy;
    public boolean mIsSendOnly;
    public boolean mIsTokenUsed;
    public String mOwnImsi;
    public String mPrevContributionId;
    public Object mRawHandle;
    public ImIncomingMessageEvent mReceivedMessage;
    public List<ImsUri> mRecipients;
    public String mRemoteMsrpAddress;
    public String mSdpContentType;
    public String mServiceId;
    public ImServiceType mServiceType;
    public ImSessionType mSessionType;
    public ImsUri mSessionUri;
    public String mSubject;

    public enum ImServiceType {
        NORMAL
    }

    public enum ImSessionType {
        SINGLE,
        CONFERENCE
    }

    public String toString() {
        return "ImIncomingSessionEvent [mRawHandle=" + this.mRawHandle + ", mContributionId=" + this.mContributionId + ", mPrevContributionId=" + this.mPrevContributionId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mConversationId=" + this.mConversationId + ", mSdpContentType=" + this.mSdpContentType + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + ", mRecipients=" + IMSLog.checker(this.mRecipients) + ", mInitiator=" + IMSLog.checker(this.mInitiator) + ", mWelcomeNote=" + IMSLog.checker(this.mSubject) + ", mIsDeferred=" + this.mIsDeferred + ", mIsForStoredNoti=" + this.mIsForStoredNoti + ", mIsTokenUsed=" + this.mIsTokenUsed + ", mSessionUri=" + this.mSessionUri + ", mServiceType=" + this.mServiceType + ", mIsParticipantNtfy=" + this.mIsParticipantNtfy + ", mSessionType=" + this.mSessionType + ", mIsClosedGroupChat=" + this.mIsClosedGroupChat + ", mReceivedMessage=" + this.mReceivedMessage + ", mRemoteMsrpAddress=" + this.mRemoteMsrpAddress + ", mInitiatorAlias=" + IMSLog.checker(this.mInitiatorAlias) + ", mServiceId=" + this.mServiceId + ", mDeviceId=" + this.mDeviceId + ", mIsMsgRevokeSupported=" + this.mIsMsgRevokeSupported + ", mFromBlocked=" + this.mFromBlocked + ", mOwnImsi= " + IMSLog.checker(this.mOwnImsi) + ", mIsSendOnly= " + this.mIsSendOnly + ", mIsChatbotRole= " + this.mIsChatbotRole + "]";
    }
}
