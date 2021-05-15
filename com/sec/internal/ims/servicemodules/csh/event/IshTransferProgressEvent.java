package com.sec.internal.ims.servicemodules.csh.event;

public class IshTransferProgressEvent {
    public long mProgress;
    public int mSessionId;

    public IshTransferProgressEvent(int sessionId, long progress) {
        this.mSessionId = sessionId;
        this.mProgress = progress;
    }
}
