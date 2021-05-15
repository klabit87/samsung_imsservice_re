package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class RequestManagePushToken extends NSDSRequest {
    @SerializedName("client-id")
    public String clientId;
    public String msisdn;
    public int operation;
    @SerializedName("push-token")
    public String pushToken;
    @SerializedName("service-name")
    public String serviceName;
}
