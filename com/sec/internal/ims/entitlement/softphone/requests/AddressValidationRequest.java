package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;

public class AddressValidationRequest {
    @SerializedName("e911Context")
    public E911Context mE911Context;

    public static class E911Context {
        @SerializedName("address")
        public Address mAddress;
        @SerializedName("isAddressConfirmed")
        public String mConfirmed;

        public E911Context(Address address, String confirmed) {
            this.mAddress = address;
            this.mConfirmed = confirmed;
        }

        public String toString() {
            return "E911Context [mAddress = " + this.mAddress + ", mConfirmed = " + this.mConfirmed + "]";
        }
    }

    public static class Address {
        @SerializedName("addressAdditional")
        public String addressAdditional;
        @SerializedName("city")
        public String city;
        @SerializedName("houseNumExt")
        public String houseNumExt;
        @SerializedName("houseNumber")
        public String houseNumber;
        @SerializedName("name")
        public String name;
        @SerializedName("state")
        public String state;
        @SerializedName("street")
        public String street;
        @SerializedName("streetDir")
        public String streetDir;
        @SerializedName("streetDirSuffix")
        public String streetDirSuffix;
        @SerializedName("streetNameSuffix")
        public String streetNameSuffix;
        @SerializedName("zip")
        public String zip;

        public Address(String name2, String houseNumber2, String houseNumExt2, String streetDir2, String street2, String StreetNameSuffix, String streetDirSuffix2, String city2, String state2, String zip2, String addressAdditional2) {
            this.name = name2;
            this.houseNumber = houseNumber2;
            this.houseNumExt = houseNumExt2;
            this.streetDir = streetDir2;
            this.street = street2;
            this.streetNameSuffix = StreetNameSuffix;
            this.streetDirSuffix = streetDirSuffix2;
            this.city = city2;
            this.state = state2;
            this.zip = zip2;
            this.addressAdditional = addressAdditional2;
        }

        public String toString() {
            return "Address [name = " + this.name + ", houseNumber = " + this.houseNumber + ", houseNumExt = " + this.houseNumExt + ", streetDir = " + this.streetDir + ", street = " + this.street + ", streetNameSuffix = " + this.streetNameSuffix + ", streetDirSuffix = " + this.streetDirSuffix + ", city = " + this.city + ", state = " + this.state + ", zip = " + this.zip + ", addressAdditional = " + this.addressAdditional + "]";
        }
    }

    public AddressValidationRequest(Address address, String confirmed) {
        this.mE911Context = new E911Context(address, confirmed);
    }

    public String toString() {
        return "AddressValidationRequest [mE911Context = " + this.mE911Context + "]";
    }
}
