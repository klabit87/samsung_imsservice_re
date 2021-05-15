package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class Request3gppAuthentication extends NSDSRequest {
    @SerializedName("aka-challenge-rsp")
    public String akaChallengeRsp;
    @SerializedName("aka-token")
    public String akaToken;
    @SerializedName("device-name")
    public String deviceName;
    @SerializedName("device-type")
    public int deviceType;
    @SerializedName("imsi-eap")
    public String imsiEap;
    @SerializedName("os-type")
    public int osType;
}
