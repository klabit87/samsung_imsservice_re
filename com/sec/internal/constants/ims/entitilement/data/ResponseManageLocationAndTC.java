package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseManageLocationAndTC extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageLocationAndTC> CREATOR = new Parcelable.Creator<ResponseManageLocationAndTC>() {
        public ResponseManageLocationAndTC createFromParcel(Parcel in) {
            return new ResponseManageLocationAndTC(in);
        }

        public ResponseManageLocationAndTC[] newArray(int size) {
            return new ResponseManageLocationAndTC[size];
        }
    };
    @SerializedName("address-id")
    public String addressId;
    @SerializedName("aid-expiration")
    public String aidExpiration;
    @SerializedName("location-status")
    public Boolean locationStatus;
    @SerializedName("server-data")
    public String serverData;
    @SerializedName("server-url")
    public String serverUrl;
    @SerializedName("service-status")
    public Integer serviceStatus;
    @SerializedName("tc-status")
    public Boolean tcStatus;

    protected ResponseManageLocationAndTC(Parcel in) {
        super(in);
        Boolean bool;
        Boolean bool2;
        byte locationStatusVal = in.readByte();
        boolean z = true;
        Integer num = null;
        if (locationStatusVal == 2) {
            bool = null;
        } else {
            bool = Boolean.valueOf(locationStatusVal != 0);
        }
        this.locationStatus = bool;
        byte tcStatusVal = in.readByte();
        if (tcStatusVal == 2) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(tcStatusVal == 0 ? false : z);
        }
        this.tcStatus = bool2;
        this.serviceStatus = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
        this.serverData = in.readString();
        this.serverUrl = in.readString();
        this.addressId = in.readString();
        this.aidExpiration = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        Boolean bool = this.locationStatus;
        if (bool == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool.booleanValue() ? (byte) 1 : 0);
        }
        Boolean bool2 = this.tcStatus;
        if (bool2 == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool2.booleanValue() ? (byte) 1 : 0);
        }
        if (this.serviceStatus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(this.serviceStatus.intValue());
        }
        dest.writeString(this.serverData);
        dest.writeString(this.serverUrl);
        dest.writeString(this.addressId);
        dest.writeString(this.aidExpiration);
    }
}
