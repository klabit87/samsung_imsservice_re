package com.sec.internal.ims.servicemodules.im.data.info;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import java.util.HashSet;
import java.util.Set;

public class ImSessionInfo {
    public String mContributionId;
    public String mConversationId;
    public ImDirection mDirection;
    public String mInReplyToContributionId;
    public boolean mIsTryToLeave;
    public ImError mLastProvisionalResponse;
    public Object mPrevExtendRawHandle;
    public Object mRawHandle;
    public Set<Integer> mReceivedMessageIds;
    public String mSdpContentType;
    public SessionType mSessionType;
    public ImsUri mSessionUri;
    public StartingReason mStartingReason;
    public ImSessionState mState;

    public enum ImSessionState {
        INITIAL,
        PENDING_INVITE,
        STARTING,
        STARTED,
        ACCEPTING,
        ESTABLISHED,
        CLOSING
    }

    public enum SessionType {
        NORMAL,
        SNF_SESSION,
        SNF_NOTIFICATION_SESSION
    }

    public enum StartingReason {
        NORMAL,
        RESTARTING,
        AUTOMATIC_REJOINING,
        RESTARTING_WITH_NEW_ID,
        EXTENDING_1_1_TO_GROUP
    }

    public ImSessionInfo(Object rawHandle, ImSessionState state, ImDirection direction, ImsUri sessionUri, String contributionId, String conversationId, String inReplyToContributionId, String sdpContentType) {
        this.mState = ImSessionState.INITIAL;
        this.mStartingReason = StartingReason.NORMAL;
        this.mSessionType = SessionType.NORMAL;
        this.mReceivedMessageIds = new HashSet();
        this.mRawHandle = rawHandle;
        this.mState = state;
        this.mDirection = direction;
        this.mSessionUri = sessionUri;
        this.mContributionId = contributionId;
        this.mConversationId = conversationId;
        this.mInReplyToContributionId = inReplyToContributionId;
        this.mSdpContentType = sdpContentType;
    }

    public ImSessionInfo(ImSessionState state, ImDirection direction, ImsUri sessionUri, String contributionId, String conversationId, String inReplyToContributionId, String sdpContentType) {
        this((Object) null, state, direction, sessionUri, contributionId, conversationId, inReplyToContributionId, sdpContentType);
    }

    public boolean isSnFSession() {
        return this.mSessionType == SessionType.SNF_SESSION || this.mSessionType == SessionType.SNF_NOTIFICATION_SESSION;
    }

    public String toString() {
        return "ImSessionInfo [mRawHandle=" + this.mRawHandle + ", mState=" + this.mState + ", mDirection=" + this.mDirection + ", mSessionUri=" + this.mSessionUri + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mPrevExtendRawHandle=" + this.mPrevExtendRawHandle + ", mSdpContentType=" + this.mSdpContentType + ", mStartingReason=" + this.mStartingReason + ", mSessionType=" + this.mSessionType + ", mIsTryToLeave=" + this.mIsTryToLeave + "]";
    }
}
