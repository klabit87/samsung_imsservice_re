package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ServiceName implements Parcelable {
    public static final Parcelable.Creator<ServiceName> CREATOR = new Parcelable.Creator<ServiceName>() {
        public ServiceName createFromParcel(Parcel in) {
            return new ServiceName(in);
        }

        public ServiceName[] newArray(int size) {
            return new ServiceName[size];
        }
    };
    @SerializedName("appstore-url")
    public String appstoreUrl;
    @SerializedName("client-id")
    public String clientId;
    @SerializedName("package-name")
    public String packageName;
    @SerializedName("service-name")
    public String serviceName;

    protected ServiceName(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.serviceName = in.readString();
        this.clientId = in.readString();
        this.packageName = in.readString();
        this.appstoreUrl = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceName);
        dest.writeString(this.clientId);
        dest.writeString(this.packageName);
        dest.writeString(this.appstoreUrl);
    }

    public String toString() {
        return "serviceName:" + this.serviceName + " clientId:" + this.clientId + " packageName:" + this.packageName + " appstoreUrl:" + this.appstoreUrl;
    }
}
