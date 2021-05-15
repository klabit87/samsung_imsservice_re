package com.sec.internal.ims.servicemodules.csh.event;

public class IshSessionEstablishedEvent {
    public int mSessionId;

    public IshSessionEstablishedEvent(int sessionId) {
        this.mSessionId = sessionId;
    }
}
