package com.sec.internal.ims.servicemodules.csh.event;

public class VshSessionTerminatedEvent {
    public CshErrorReason mReason;
    public int mSessionId;

    public VshSessionTerminatedEvent(int sessionId, CshErrorReason reason) {
        this.mSessionId = sessionId;
        this.mReason = reason;
    }

    public String toString() {
        return "VshSessionTerminatedEvent [mSessionId=" + this.mSessionId + ", mReason=" + this.mReason + "]";
    }
}
