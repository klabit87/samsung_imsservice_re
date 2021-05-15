package com.sec.internal.ims.servicemodules.csh.event;

public class VshSessionEstablishedEvent {
    public VshResolution mResolution;
    public int mSessionId;

    public VshSessionEstablishedEvent(int sessionId, VshResolution resolution) {
        this.mSessionId = sessionId;
        this.mResolution = resolution;
    }

    public String toString() {
        return "VshSessionEstablishedEvent [mSessionId=" + this.mSessionId + ", mResolution=" + this.mResolution + "]";
    }
}
