package com.sec.internal.ims.servicemodules.csh.event;

public class CshCancelSessionParams {
    public ICshSuccessCallback mCallback;
    public int mSessionId;

    public CshCancelSessionParams(int sessionId, ICshSuccessCallback callback) {
        this.mSessionId = sessionId;
        this.mCallback = callback;
    }
}
