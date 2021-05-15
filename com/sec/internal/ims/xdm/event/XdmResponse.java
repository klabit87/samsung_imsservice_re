package com.sec.internal.ims.xdm.event;

public final class XdmResponse {
    public final String mEtag;
    public final String mReason;
    public final String mRetryAfter;
    public final int mStatusCode;
    public final boolean mSuccess;
    public final String mUuid;

    public XdmResponse(boolean success, int statusCode, String reason, String etag, String retryAfter, String uuid) {
        this.mSuccess = success;
        this.mStatusCode = statusCode;
        this.mReason = reason;
        this.mEtag = etag;
        this.mUuid = uuid;
        this.mRetryAfter = retryAfter;
    }

    public XdmResponse(boolean success, int statusCode, String reason) {
        this(success, statusCode, reason, (String) null, (String) null, (String) null);
    }

    public String toString() {
        return "XdmResponse [mSuccess =" + this.mSuccess + ", mStatusCode =" + this.mStatusCode + ", mReason=" + this.mReason + ", mEtag=" + this.mEtag + ", mUuid=" + this.mUuid + ", mRetryAfter=" + this.mRetryAfter + "]";
    }
}
