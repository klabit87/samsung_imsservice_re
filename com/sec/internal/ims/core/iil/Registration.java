package com.sec.internal.ims.core.iil;

public class Registration {
    public static final int CDPN_REGISTERED = 32;
    public static final int RCS_REGISTERED = 4;
    public static final int SMS_OVER_IMS_REGISTERED = 2;
    public static final int VOLTE_REGISTERED = 1;
    public static final int VT_REGISTERED = 8;
    private int mDereiReasonCode;
    private int mEcmpMode;
    private int mEpdgMode;
    private int mError;
    private String mErrorMsg;
    private int mFeatureMask;
    private String mFeatureTags;
    private String mImpu;
    private int mLimitedMode;
    private int mPdnType;
    private int mRat;

    public Registration(int feature, int pdnType, int ecmp, int limited, int epdgMode, int rat) {
        this.mFeatureMask = 0;
        this.mPdnType = -1;
        this.mEcmpMode = 0;
        this.mLimitedMode = 0;
        this.mEpdgMode = 0;
        this.mRat = 0;
        this.mError = 0;
        this.mDereiReasonCode = 0;
        this.mErrorMsg = null;
        this.mFeatureTags = "";
        this.mImpu = null;
        this.mFeatureMask = feature;
        this.mPdnType = pdnType;
        this.mEcmpMode = ecmp;
        this.mLimitedMode = limited;
        this.mEpdgMode = epdgMode;
        this.mRat = rat;
    }

    public Registration() {
        this.mFeatureMask = 0;
        this.mPdnType = -1;
        this.mEcmpMode = 0;
        this.mLimitedMode = 0;
        this.mEpdgMode = 0;
        this.mRat = 0;
        this.mError = 0;
        this.mDereiReasonCode = 0;
        this.mErrorMsg = null;
        this.mFeatureTags = "";
        this.mImpu = null;
        this.mFeatureMask = 0;
        this.mPdnType = 0;
        this.mEcmpMode = 0;
        this.mLimitedMode = 0;
        this.mEpdgMode = 0;
        this.mRat = 0;
        this.mError = 0;
        this.mDereiReasonCode = 0;
    }

    public void setSipError(int error) {
        this.mError = error;
    }

    public int getSipError() {
        return this.mError;
    }

    public void setDeregiReasonCode(int reasonCode) {
        this.mDereiReasonCode = reasonCode;
    }

    public int getDeregiReasonCode() {
        return this.mDereiReasonCode;
    }

    public void setErrorMessage(String msg) {
        this.mErrorMsg = msg;
    }

    public String getErrorMessage() {
        return this.mErrorMsg;
    }

    public void setFeatureMask(int featureMask) {
        this.mFeatureMask = featureMask;
    }

    public int getFeatureMask() {
        return this.mFeatureMask;
    }

    public void setNetworkType(int pdnType) {
        this.mPdnType = pdnType;
    }

    public int getNetworkType() {
        return this.mPdnType;
    }

    public void setEcmpMode(int ecmpMode) {
        this.mEcmpMode = ecmpMode;
    }

    public int getEcmpMode() {
        return this.mEcmpMode;
    }

    public void setLimitedMode(int limitedMode) {
        this.mLimitedMode = limitedMode;
    }

    public int getLimitedMode() {
        return this.mLimitedMode;
    }

    public void setEpdgMode(int epdgMode) {
        this.mEpdgMode = epdgMode;
    }

    public int getEpdgMode() {
        return this.mEpdgMode;
    }

    public void setFeatureTags(String featureTags) {
        this.mFeatureTags = featureTags;
    }

    public String getFeatureTags() {
        return this.mFeatureTags;
    }

    public void setImpu(String impu) {
        this.mImpu = impu;
    }

    public String getImpu() {
        return this.mImpu;
    }

    public void setRegiRat(int rat) {
        this.mRat = rat;
    }

    public int getRegiRat() {
        return this.mRat;
    }
}
