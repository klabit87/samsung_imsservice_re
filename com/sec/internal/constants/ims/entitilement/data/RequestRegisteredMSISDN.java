package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class RequestRegisteredMSISDN extends NSDSRequest {
    @SerializedName("is-available")
    public Boolean isAvailable;
    public int operation;
    @SerializedName("service-name")
    public String serviceName;
}
