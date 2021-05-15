package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

public class NSDSCommonParameters {
    private String mAkaToken;
    private String mChallengeResponse;
    private String mDeviceId;
    private String mImsiEap;

    public NSDSCommonParameters(String challengeResponse, String akaToken, String imsiEap, String deviceId) {
        this.mChallengeResponse = challengeResponse;
        this.mAkaToken = akaToken;
        this.mImsiEap = imsiEap;
        this.mDeviceId = deviceId;
    }

    public String getChallengeResponse() {
        return this.mChallengeResponse;
    }

    public String getAkaToken() {
        return this.mAkaToken;
    }

    public String getImsiEap() {
        return this.mImsiEap;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }
}
