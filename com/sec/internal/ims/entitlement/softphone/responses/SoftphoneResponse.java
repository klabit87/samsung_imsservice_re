package com.sec.internal.ims.entitlement.softphone.responses;

public class SoftphoneResponse {
    public String mReason;
    public int mStatusCode;
    public boolean mSuccess;

    public String toString() {
        return "SoftphoneResponse [mSuccess = " + this.mSuccess + ", mReason = " + this.mReason + ", mStatusCode = " + this.mStatusCode + "]";
    }
}
