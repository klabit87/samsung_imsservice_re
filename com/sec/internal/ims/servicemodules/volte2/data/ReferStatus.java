package com.sec.internal.ims.servicemodules.volte2.data;

public class ReferStatus {
    public int mRespCode;
    public int mSessionId;

    public ReferStatus(int sessionId, int respCode) {
        this.mSessionId = sessionId;
        this.mRespCode = respCode;
    }
}
