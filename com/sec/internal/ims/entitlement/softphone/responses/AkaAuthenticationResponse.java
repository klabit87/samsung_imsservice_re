package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class AkaAuthenticationResponse {
    @SerializedName("challengeResponse")
    public ChallengeResponse mChallengeResponse;

    public static class ChallengeResponse {
        @SerializedName("authenticationResponse")
        public String mAuthenticationResponse;
        @SerializedName("cipherKey")
        public String mCipherKey;
        @SerializedName("integrityKey")
        public String mIntegrityKey;

        public String toString() {
            return "ChallengeResponse [mAuthenticationResponse = " + this.mAuthenticationResponse + ", mCipherKey = " + this.mCipherKey + ", mIntegrityKey = " + this.mIntegrityKey + "]";
        }
    }

    public String toString() {
        return "AkaAuthenticationResponse [mChallengeResponse = " + this.mChallengeResponse + "]";
    }
}
