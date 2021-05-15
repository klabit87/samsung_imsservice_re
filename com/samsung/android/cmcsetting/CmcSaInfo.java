package com.samsung.android.cmcsetting;

public class CmcSaInfo {
    private String mSaAccessToken = "";
    private String mSaUserId = "";

    public void setSaUserId(String str) {
        this.mSaUserId = str;
    }

    public String getSaUserId() {
        return this.mSaUserId;
    }

    public void setSaAccessToken(String str) {
        this.mSaAccessToken = str;
    }

    public String getSaAccessToken() {
        return this.mSaAccessToken;
    }

    public String toString() {
        return (("{" + "saUserId:" + this.mSaUserId) + ",saAccessToken:" + this.mSaAccessToken) + "}";
    }
}
