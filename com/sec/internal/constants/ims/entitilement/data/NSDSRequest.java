package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class NSDSRequest {
    @SerializedName("device-id")
    public String deviceId;
    @SerializedName("message-id")
    public int messageId;
    public String method;
}
