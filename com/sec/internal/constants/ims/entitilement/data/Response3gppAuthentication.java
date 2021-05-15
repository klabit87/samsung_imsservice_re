package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Response3gppAuthentication extends NSDSResponse {
    public static final Parcelable.Creator<Response3gppAuthentication> CREATOR = new Parcelable.Creator<Response3gppAuthentication>() {
        public Response3gppAuthentication createFromParcel(Parcel in) {
            return new Response3gppAuthentication(in);
        }

        public Response3gppAuthentication[] newArray(int size) {
            return new Response3gppAuthentication[size];
        }
    };
    @SerializedName("aka-challenge")
    public String akaChallenge;
    @SerializedName("aka-token")
    public String akaToken;

    public Response3gppAuthentication(Parcel in) {
        super(in);
        this.akaChallenge = in.readString();
        this.akaToken = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.akaChallenge);
        dest.writeString(this.akaToken);
    }
}
