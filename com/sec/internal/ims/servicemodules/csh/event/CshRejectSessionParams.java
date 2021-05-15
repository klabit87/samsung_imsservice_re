package com.sec.internal.ims.servicemodules.csh.event;

public class CshRejectSessionParams {
    public ICshSuccessCallback mCallback;
    public int mSessionId;

    public CshRejectSessionParams(int sessionId, ICshSuccessCallback callback) {
        this.mSessionId = sessionId;
        this.mCallback = callback;
    }
}
