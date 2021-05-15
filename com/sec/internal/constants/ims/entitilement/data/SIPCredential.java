package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class SIPCredential {
    public String impu;
    @SerializedName("sip-password")
    public String sipPassword;
    @SerializedName("sip-username")
    public String sipUsername;
}
