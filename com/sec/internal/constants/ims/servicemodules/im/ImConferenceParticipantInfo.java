package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ImConferenceParticipantInfo {
    public ImError mDisconnectionCause;
    public ImConferenceDisconnectionReason mDisconnectionReason;
    public Date mDisconnectionTime;
    public String mDispName;
    public boolean mIsChairman;
    public boolean mIsOwn;
    public Date mJoiningTime;
    public ImConferenceParticipantStatus mParticipantStatus;
    public ImsUri mUri;
    public ImConferenceUserElemState mUserElemState = ImConferenceUserElemState.FULL;

    public enum ImConferenceDisconnectionReason {
        DEPARTED,
        BOOTED,
        FAILED,
        BUSY
    }

    public enum ImConferenceParticipantStatus {
        CONNECTED,
        DISCONNECTED,
        ON_HOLD,
        ALERTING,
        MUTED_VIA_FOCUS,
        PENDING,
        DIALING_IN,
        DIALING_OUT,
        DISCONNECTING,
        INVALID
    }

    public enum ImConferenceUserElemState {
        FULL,
        DELETED,
        PARTIAL
    }

    public String toString() {
        return "ImConferenceParticipantInfo [mUri=" + IMSLog.checker(this.mUri) + ", mIsOwn=" + this.mIsOwn + ", mUserElemState=" + this.mUserElemState + ", mParticipantStatus=" + this.mParticipantStatus + ", mDisconnectionReason=" + this.mDisconnectionReason + ", mJoiningTime=" + this.mJoiningTime + ", mDisconnectionTime=" + this.mDisconnectionTime + ", mIsChairman=" + this.mIsChairman + ", mDispName=" + IMSLog.checker(this.mDispName) + ", mDisconnectionCause=" + this.mDisconnectionCause + "]";
    }
}
