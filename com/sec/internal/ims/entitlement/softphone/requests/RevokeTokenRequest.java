package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class RevokeTokenRequest {
    @SerializedName("client_id")
    public String mClientId;
    @SerializedName("client_secret")
    public String mClientSecret;
    @SerializedName("token")
    public String mToken;
    @SerializedName("token_type_hint")
    public String mTokenType;

    public RevokeTokenRequest(String clientId, String clientSecret, String token, String tokenType) {
        this.mClientId = clientId;
        this.mClientSecret = clientSecret;
        this.mToken = token;
        this.mTokenType = tokenType;
    }

    public String toString() {
        return "RevokeTokenRequest [mClientId = " + this.mClientId + ", mClientSecret = " + this.mClientSecret + ", mToken = " + this.mToken + ", mTokenType = " + this.mTokenType + "]";
    }
}
