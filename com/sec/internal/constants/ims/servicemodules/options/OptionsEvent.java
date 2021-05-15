package com.sec.internal.constants.ims.servicemodules.options;

import com.sec.ims.util.ImsUri;
import java.util.Set;

public class OptionsEvent {
    private String mExtFeature;
    private long mFeatures;
    private boolean mIsResponse;
    private boolean mIsTokenUsed;
    private Set<ImsUri> mPAssertedIdSet;
    private int mPhoneId;
    private OptionsFailureReason mReason;
    private int mSessionId;
    private boolean mSuccess;
    private String mTxId;
    private ImsUri mUri;
    private int mlastSeen = -1;

    public enum OptionsFailureReason {
        USER_NOT_AVAILABLE,
        DOES_NOT_EXIST_ANYWHERE,
        USER_NOT_REGISTERED,
        USER_NOT_REACHABLE,
        FORBIDDEN_403,
        REQUEST_TIMED_OUT,
        AUTOMATA_PRESENT,
        INVALID_DATA,
        USER_AVAILABLE_OFFLINE,
        ERROR
    }

    public OptionsEvent(boolean success, ImsUri uri, long features, int phoneId, boolean isResponse, int sessionId, String txId, Set<ImsUri> pAssertedIds, String extFeature) {
        this.mSuccess = success;
        this.mUri = uri;
        this.mFeatures = features;
        this.mPhoneId = phoneId;
        this.mIsResponse = isResponse;
        this.mSessionId = sessionId;
        this.mTxId = txId;
        this.mExtFeature = extFeature;
        this.mPAssertedIdSet = pAssertedIds;
        this.mIsTokenUsed = false;
    }

    public ImsUri getUri() {
        return this.mUri;
    }

    public boolean isSuccess() {
        return this.mSuccess;
    }

    public boolean isResponse() {
        return this.mIsResponse;
    }

    public long getFeatures() {
        return this.mFeatures;
    }

    public boolean getIsTokenUsed() {
        return this.mIsTokenUsed;
    }

    public void setFeatures(long features) {
        this.mFeatures = features;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void setReason(OptionsFailureReason reason) {
        this.mReason = reason;
    }

    public void setIsTokenUsed(boolean isTokenUsed) {
        this.mIsTokenUsed = isTokenUsed;
    }

    public OptionsFailureReason getReason() {
        return this.mReason;
    }

    public String getTxId() {
        return this.mTxId;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public int getLastSeen() {
        return this.mlastSeen;
    }

    public void setLastSeen(int mlastSeen2) {
        this.mlastSeen = mlastSeen2;
    }

    public String getExtFeature() {
        return this.mExtFeature;
    }

    public Set<ImsUri> getPAssertedIdSet() {
        return this.mPAssertedIdSet;
    }

    public String toString() {
        return "OptionsEvent [mUri=" + this.mUri + ", mSuccess=" + this.mSuccess + ", mFeatures=" + this.mFeatures + ", mPhoneId=" + this.mPhoneId + ", mIsResponse=" + this.mIsResponse + ", mReason=" + this.mReason + ", mSessionId=" + this.mSessionId + ", mPAssertedIdSet=" + this.mPAssertedIdSet + ", mExtFeature=" + this.mExtFeature + "]";
    }
}
