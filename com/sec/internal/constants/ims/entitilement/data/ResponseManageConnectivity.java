package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseManageConnectivity extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageConnectivity> CREATOR = new Parcelable.Creator<ResponseManageConnectivity>() {
        public ResponseManageConnectivity createFromParcel(Parcel in) {
            return new ResponseManageConnectivity(in);
        }

        public ResponseManageConnectivity[] newArray(int size) {
            return new ResponseManageConnectivity[size];
        }
    };
    public String certificate;
    @SerializedName("device-config")
    public String deviceConfig;
    @SerializedName("epdg-addresses")
    public ArrayList<String> epdgAddresses;
    @SerializedName("service-names")
    public ArrayList<ServiceName> serviceNames;
    @SerializedName("session-cookie")
    public String sessionCookie;

    public ResponseManageConnectivity(Parcel in) {
        super(in);
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        if (in.readByte() == 1) {
            this.certificate = in.readString();
        } else {
            this.certificate = null;
        }
        if (in.readByte() == 1) {
            ArrayList<String> arrayList = new ArrayList<>();
            this.epdgAddresses = arrayList;
            in.readList(arrayList, (ClassLoader) null);
        } else {
            this.epdgAddresses = null;
        }
        if (in.readByte() == 1) {
            ArrayList<ServiceName> arrayList2 = new ArrayList<>();
            this.serviceNames = arrayList2;
            in.readTypedList(arrayList2, ServiceName.CREATOR);
        } else {
            this.serviceNames = null;
        }
        if (in.readByte() == 1) {
            this.deviceConfig = in.readString();
        } else {
            this.deviceConfig = null;
        }
        if (in.readByte() == 1) {
            this.sessionCookie = in.readString();
        } else {
            this.sessionCookie = null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.certificate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(this.certificate);
        }
        if (this.epdgAddresses == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeList(this.epdgAddresses);
        }
        if (this.serviceNames == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeTypedList(this.serviceNames);
        }
        if (this.deviceConfig == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(this.deviceConfig);
        }
        if (this.sessionCookie == null) {
            dest.writeByte((byte) 0);
            return;
        }
        dest.writeByte((byte) 1);
        dest.writeString(this.sessionCookie);
    }
}
