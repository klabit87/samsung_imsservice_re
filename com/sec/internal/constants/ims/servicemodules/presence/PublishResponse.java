package com.sec.internal.constants.ims.servicemodules.presence;

public class PublishResponse extends PresenceResponse {
    private String mEtag;
    private long mExpiresTimer;
    private boolean mIsRefresh;
    private long mRetryAfter;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PublishResponse(boolean success, int error, String reason, int retryTime, String eTag, long expiresTimer, boolean isRefresh, long retryAfter, int phoneId) {
        super(success, error, reason, retryTime, phoneId);
        this.mEtag = eTag;
        this.mExpiresTimer = expiresTimer;
        this.mIsRefresh = isRefresh;
        this.mRetryAfter = retryAfter;
    }

    public String getEtag() {
        return this.mEtag;
    }

    public long getExpiresTimer() {
        return this.mExpiresTimer;
    }

    public boolean isRefresh() {
        return this.mIsRefresh;
    }

    public long getRetryAfter() {
        return this.mRetryAfter;
    }
}
