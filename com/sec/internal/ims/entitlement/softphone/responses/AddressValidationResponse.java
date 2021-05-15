package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class AddressValidationResponse extends SoftphoneResponse {
    public int mAddressId;
    public boolean mConfirmed;
    @SerializedName("e911Locations")
    public E911Locations mE911Locations;
    public int mTransactionId;

    public static class E911Locations {
        @SerializedName("addressIdentifier")
        public String mAddressIdentifier;
        @SerializedName("expirationDate")
        public String mExpirationDate;
        @SerializedName("status")
        public String mStatus;

        public String toString() {
            return "E911Locations [mAddressIdentifier = " + this.mAddressIdentifier + ", mStatus = " + this.mStatus + ", mExpirationDate = " + this.mExpirationDate + "]";
        }
    }

    public String toString() {
        return "AddressValidationResponse [mE911Locations = " + this.mE911Locations + ", mTransactionId = " + this.mTransactionId + ", mAddressId = " + this.mAddressId + ", mConfirmed = " + this.mConfirmed + "]";
    }
}
