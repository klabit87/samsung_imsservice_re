package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class AccessTokenResponse extends SoftphoneResponse {
    @SerializedName("access_token")
    public String mAccessToken;
    @SerializedName("expires_in")
    public String mExpiresIn;
    @SerializedName("refresh_token")
    public String mRefreshToken;
    @SerializedName("token_type")
    public String mTokenType;

    public String toString() {
        return "AccessTokenResponse [mAccessToken = " + this.mAccessToken + ", mTokenType = " + this.mTokenType + ", mExpiresIn = " + this.mExpiresIn + ", mRefreshToken = " + this.mRefreshToken + "]";
    }
}
