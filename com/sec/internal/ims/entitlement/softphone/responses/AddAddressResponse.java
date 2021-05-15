package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddAddressResponse extends SoftphoneResponse {
    @SerializedName("locationResponse")
    public LocationResponse mLocationResponse;

    public static class LocationResponse {
        @SerializedName("locations")
        public List<String> mLocations;

        public String toString() {
            return "LocationResponse [mLocations = " + this.mLocations + "]";
        }
    }

    public String toString() {
        return "AddAddressResponse [mLocationResponse = " + this.mLocationResponse + "]";
    }
}
