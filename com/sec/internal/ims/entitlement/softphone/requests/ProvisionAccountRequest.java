package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ProvisionAccountRequest {
    @SerializedName("provisionSPRequest")
    public ProvisionSPRequest mProvisionSPRequest;

    public static class ProvisionSPRequest {
        @SerializedName("tcAccept")
        public String mTcAccept;

        public ProvisionSPRequest(String accept) {
            this.mTcAccept = accept;
        }

        public String toString() {
            return "ProvisionSPRequest [mTcAccept = " + this.mTcAccept + "]";
        }
    }

    public ProvisionAccountRequest(String accept) {
        this.mProvisionSPRequest = new ProvisionSPRequest(accept);
    }

    public String toString() {
        return "ProvisionAccountRequest [mProvisionSPRequest = " + this.mProvisionSPRequest + "]";
    }
}
