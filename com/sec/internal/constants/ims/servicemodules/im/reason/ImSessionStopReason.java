package com.sec.internal.constants.ims.servicemodules.im.reason;

public enum ImSessionStopReason {
    VOLUNTARILY(200, "Call completed"),
    INVOLUNTARILY(503, "Service Unavailable"),
    DEDICATED_BEARER_UNAVAILABLE_TIMEOUT,
    GC_FORCE_CLOSE(200, "Call completed"),
    NO_RESPONSE(503, "Service Unavailable"),
    CLOSE_1_TO_1_SESSION(200, "Call completed");
    
    private final int mCauseCode;
    private final String mReasonText;

    private ImSessionStopReason(int causeCode, String reasonText) {
        this.mCauseCode = causeCode;
        this.mReasonText = reasonText;
    }

    public int getCauseCode() {
        return this.mCauseCode;
    }

    public String getReasonText() {
        return this.mReasonText;
    }
}
