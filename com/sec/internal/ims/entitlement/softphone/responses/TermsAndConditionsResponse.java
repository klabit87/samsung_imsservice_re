package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class TermsAndConditionsResponse extends SoftphoneResponse {
    @SerializedName("tcResponse")
    public TCResponse mTCResponse;

    public static class TCResponse {
        @SerializedName("url")
        public String mUrl;

        public String toString() {
            return "TCResponse [mUrl = " + this.mUrl + "]";
        }
    }

    public String toString() {
        return "TermsAndConditionsResponse [mTCResponse = " + this.mTCResponse + "]";
    }
}
