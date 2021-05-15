package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class ProvisioningParameters {
    @SerializedName("sip-connectivity")
    public SIPConnectivity sipConnectivity;
    @SerializedName("sip-credential")
    public SIPCredential sipCredential;
}
