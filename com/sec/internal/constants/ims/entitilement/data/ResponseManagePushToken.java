package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ResponseManagePushToken extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManagePushToken> CREATOR = new Parcelable.Creator<ResponseManagePushToken>() {
        public ResponseManagePushToken createFromParcel(Parcel in) {
            return new ResponseManagePushToken(in);
        }

        public ResponseManagePushToken[] newArray(int size) {
            return new ResponseManagePushToken[size];
        }
    };

    public ResponseManagePushToken(Parcel in) {
        super(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
