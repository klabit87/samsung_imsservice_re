package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class ReleaseImsNetworkIdentifiersRequest {
    @SerializedName("subscriberIdentitiessubscriberCredentials")
    public SubscriberCredentials mSubscriberCredentials;

    public static class SubscriberCredentials {
        @SerializedName("privateUserId")
        public String mPrivateUserId;
        @SerializedName("publicUserId")
        public String mPublicUserId;

        public SubscriberCredentials(String impi, String impu) {
            this.mPrivateUserId = impi;
            this.mPublicUserId = impu;
        }

        public String toString() {
            return "SubscriberCredentials [mPrivateUserId = " + this.mPrivateUserId + ", mPublicUserId = " + this.mPublicUserId + "]";
        }
    }

    public ReleaseImsNetworkIdentifiersRequest(String impi, String impu) {
        this.mSubscriberCredentials = new SubscriberCredentials(impi, impu);
    }

    public String toString() {
        return "ReleaseImsNetworkIdentifiersRequest [mSubscriberCredentials = " + this.mSubscriberCredentials + "]";
    }
}
