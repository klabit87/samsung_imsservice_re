package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseRegisteredMSISDN extends NSDSResponse {
    public static final Parcelable.Creator<ResponseRegisteredMSISDN> CREATOR = new Parcelable.Creator<ResponseRegisteredMSISDN>() {
        public ResponseRegisteredMSISDN createFromParcel(Parcel in) {
            return new ResponseRegisteredMSISDN(in);
        }

        public ResponseRegisteredMSISDN[] newArray(int size) {
            return new ResponseRegisteredMSISDN[size];
        }
    };
    @SerializedName("registered-msisdns")
    public ArrayList<RegisteredMSISDN> registeredMSISDNs;

    protected ResponseRegisteredMSISDN(Parcel in) {
        super(in);
        if (in.readByte() == 1) {
            ArrayList<RegisteredMSISDN> arrayList = new ArrayList<>();
            this.registeredMSISDNs = arrayList;
            in.readList(arrayList, RegisteredMSISDN.class.getClassLoader());
            return;
        }
        this.registeredMSISDNs = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.registeredMSISDNs == null) {
            dest.writeByte((byte) 0);
            return;
        }
        dest.writeByte((byte) 1);
        dest.writeList(this.registeredMSISDNs);
    }
}
