package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class CallWaitingResponse extends SoftphoneResponse {
    @SerializedName("@active")
    public String mActive;
}
