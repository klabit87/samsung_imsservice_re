package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class AddAddressRequest {
    @SerializedName("locationRequest")
    public LocationRequest mLocationRequest;

    public static class LocationRequest {
        @SerializedName("location")
        public String mLocation;

        public LocationRequest(String location) {
            this.mLocation = location;
        }

        public String toString() {
            return "LocationRequest [mLocation = " + this.mLocation + "]";
        }
    }

    public AddAddressRequest(String location) {
        this.mLocationRequest = new LocationRequest(location);
    }

    public String toString() {
        return "AddAddressRequest [mLocationRequest = " + this.mLocationRequest + "]";
    }
}
