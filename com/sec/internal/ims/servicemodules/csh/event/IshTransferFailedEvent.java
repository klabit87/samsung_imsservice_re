package com.sec.internal.ims.servicemodules.csh.event;

public class IshTransferFailedEvent {
    public CshErrorReason mReason;
    public int mSessionId;

    public IshTransferFailedEvent(int sessionId, CshErrorReason reason) {
        this.mSessionId = sessionId;
        this.mReason = reason;
    }
}
