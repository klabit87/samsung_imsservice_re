package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class SIPConnectivity {
    public String apn;
    @SerializedName("pcscf-address")
    public String pcscfAddress;
    @SerializedName("sip-uri")
    public String sipUri;
}
