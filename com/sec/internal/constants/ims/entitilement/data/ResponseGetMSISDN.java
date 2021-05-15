package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseGetMSISDN extends NSDSResponse {
    public static final Parcelable.Creator<ResponseGetMSISDN> CREATOR = new Parcelable.Creator<ResponseGetMSISDN>() {
        public ResponseGetMSISDN createFromParcel(Parcel in) {
            return new ResponseGetMSISDN(in);
        }

        public ResponseGetMSISDN[] newArray(int size) {
            return new ResponseGetMSISDN[size];
        }
    };
    public String msisdn;
    @SerializedName("service-fingerprint")
    public String serviceFingerprint;

    protected ResponseGetMSISDN(Parcel in) {
        super(in);
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.msisdn = in.readString();
        this.serviceFingerprint = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.msisdn);
        dest.writeString(this.serviceFingerprint);
    }
}
