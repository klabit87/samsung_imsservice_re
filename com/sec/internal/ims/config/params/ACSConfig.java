package com.sec.internal.ims.config.params;

public class ACSConfig {
    private boolean mAcsCompleted;
    private int mAcsLastError;
    private int mAcsVersion;
    private boolean mIsTriggeredByNrcr;
    private boolean mNeedForceAcs;
    private boolean mRcsBlocked;
    private boolean mRcsDormantMode;

    public boolean isRcsDormantMode() {
        return this.mRcsDormantMode;
    }

    public void setRcsDormantMode(boolean set) {
        this.mRcsDormantMode = set;
    }

    public boolean isRcsDisabled() {
        return this.mRcsBlocked;
    }

    public void disableRcsByAcs(boolean set) {
        this.mRcsBlocked = set;
    }

    public int getAcsLastError() {
        return this.mAcsLastError;
    }

    public void setAcsLastError(int lastErrorCode) {
        this.mAcsLastError = lastErrorCode;
    }

    public int getAcsVersion() {
        return this.mAcsVersion;
    }

    public void setAcsVersion(int version) {
        this.mAcsVersion = version;
    }

    public boolean isAcsCompleted() {
        return this.mAcsCompleted;
    }

    public boolean needForceAcs() {
        return this.mNeedForceAcs;
    }

    public void setAcsCompleteStatus(boolean set) {
        this.mAcsCompleted = set;
    }

    public void setForceAcs(boolean set) {
        this.mNeedForceAcs = set;
    }

    public void setIsTriggeredByNrcr(boolean set) {
        this.mIsTriggeredByNrcr = set;
    }

    public boolean isTriggeredByNrcr() {
        return this.mIsTriggeredByNrcr;
    }

    public void resetAcsSettings() {
        this.mAcsCompleted = false;
        this.mNeedForceAcs = true;
    }

    public void clear() {
        this.mAcsVersion = 0;
        this.mAcsLastError = 0;
        this.mAcsCompleted = false;
        this.mRcsDormantMode = false;
        this.mNeedForceAcs = true;
        this.mRcsBlocked = false;
    }
}
