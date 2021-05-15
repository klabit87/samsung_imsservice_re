package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ExchangeForAccessTokenRequest {
    @SerializedName("client_id")
    public String mClientId;
    @SerializedName("client_secret")
    public String mClientSecret;
    @SerializedName("grant_type")
    public String mGrantType;
    @SerializedName("password")
    public String mPassword;
    @SerializedName("scope")
    public String mScope;
    @SerializedName("username")
    public String mUsername;

    public ExchangeForAccessTokenRequest(String clientId, String clientSecret, String username, String grantType, String password, String scope) {
        this.mClientId = clientId;
        this.mClientSecret = clientSecret;
        this.mUsername = username;
        this.mGrantType = grantType;
        this.mPassword = password;
        this.mScope = scope;
    }

    public String toString() {
        return "ExchangeForAccessTokenRequest [mClientId = " + this.mClientId + ", mClientSecret = " + this.mClientSecret + ", mUsername = " + this.mUsername + ", mGrantType = " + this.mGrantType + ", mPassword = " + this.mPassword + ", mScope = " + this.mScope + "]";
    }
}
