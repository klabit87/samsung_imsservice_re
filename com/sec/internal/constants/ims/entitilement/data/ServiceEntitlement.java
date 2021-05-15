package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ServiceEntitlement implements Parcelable {
    public static final Parcelable.Creator<ServiceEntitlement> CREATOR = new Parcelable.Creator<ServiceEntitlement>() {
        public ServiceEntitlement createFromParcel(Parcel in) {
            return new ServiceEntitlement(in);
        }

        public ServiceEntitlement[] newArray(int size) {
            return new ServiceEntitlement[size];
        }
    };
    @SerializedName("client-id")
    public String clientId;
    @SerializedName("display-name")
    public String displayName;
    @SerializedName("entitlement-status")
    public int entitlementStatus;
    @SerializedName("management-websheet")
    public Boolean managementWebsheet;
    @SerializedName("on-demand-prov")
    public Boolean onDemandProv;
    @SerializedName("service-name")
    public String serviceName;
    @SerializedName("visible")
    public Boolean visible;
    @SerializedName("websheet-pre-activation")
    public Boolean websheetPreActivation;

    protected ServiceEntitlement(Parcel in) {
        Boolean bool;
        Boolean bool2;
        Boolean bool3;
        this.serviceName = in.readString();
        this.entitlementStatus = in.readInt();
        byte onDemandProvVal = in.readByte();
        boolean z = true;
        Boolean bool4 = null;
        if (onDemandProvVal == 2) {
            bool = null;
        } else {
            bool = Boolean.valueOf(onDemandProvVal != 0);
        }
        this.onDemandProv = bool;
        this.clientId = in.readString();
        this.displayName = in.readString();
        byte visibleVal = in.readByte();
        if (visibleVal == 2) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(visibleVal != 0);
        }
        this.visible = bool2;
        byte websheetPreActivationVal = in.readByte();
        if (websheetPreActivationVal == 2) {
            bool3 = null;
        } else {
            bool3 = Boolean.valueOf(websheetPreActivationVal != 0);
        }
        this.websheetPreActivation = bool3;
        byte managementWebsheetVal = in.readByte();
        if (managementWebsheetVal != 2) {
            bool4 = Boolean.valueOf(managementWebsheetVal == 0 ? false : z);
        }
        this.managementWebsheet = bool4;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceName);
        dest.writeInt(this.entitlementStatus);
        Boolean bool = this.onDemandProv;
        if (bool == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool.booleanValue() ? (byte) 1 : 0);
        }
        dest.writeString(this.clientId);
        dest.writeString(this.displayName);
        Boolean bool2 = this.visible;
        if (bool2 == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool2.booleanValue() ? (byte) 1 : 0);
        }
        Boolean bool3 = this.websheetPreActivation;
        if (bool3 == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool3.booleanValue() ? (byte) 1 : 0);
        }
        Boolean bool4 = this.managementWebsheet;
        if (bool4 == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool4.booleanValue() ? (byte) 1 : 0);
        }
    }
}
