package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class InstanceToken implements Parcelable {
    public static final Parcelable.Creator<InstanceToken> CREATOR = new Parcelable.Creator<InstanceToken>() {
        public InstanceToken createFromParcel(Parcel in) {
            return new InstanceToken(in);
        }

        public InstanceToken[] newArray(int size) {
            return new InstanceToken[size];
        }
    };
    @SerializedName("expiration-time")
    public String expirationTime;
    @SerializedName("service-instance-token")
    public String serviceInstanceToken;

    protected InstanceToken(Parcel in) {
        this.serviceInstanceToken = in.readString();
        this.expirationTime = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceInstanceToken);
        dest.writeString(this.expirationTime);
    }
}
