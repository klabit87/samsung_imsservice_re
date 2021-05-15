package com.sec.internal.ims.servicemodules.csh.event;

public class IshTransferCompleteEvent {
    public int mSessionId;

    public IshTransferCompleteEvent(int sessionId) {
        this.mSessionId = sessionId;
    }
}
