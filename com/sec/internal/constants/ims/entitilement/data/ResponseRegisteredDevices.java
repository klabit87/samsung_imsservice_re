package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseRegisteredDevices extends NSDSResponse {
    public static final Parcelable.Creator<ResponseRegisteredDevices> CREATOR = new Parcelable.Creator<ResponseRegisteredDevices>() {
        public ResponseRegisteredDevices createFromParcel(Parcel in) {
            return new ResponseRegisteredDevices(in);
        }

        public ResponseRegisteredDevices[] newArray(int size) {
            return new ResponseRegisteredDevices[size];
        }
    };
    @SerializedName("device-info")
    public ArrayList<DeviceInstance> deviceInstance;

    protected ResponseRegisteredDevices(Parcel in) {
        super(in);
        if (in.readByte() == 1) {
            ArrayList<DeviceInstance> arrayList = new ArrayList<>();
            this.deviceInstance = arrayList;
            in.readList(arrayList, DeviceInstance.class.getClassLoader());
            return;
        }
        this.deviceInstance = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.deviceInstance == null) {
            dest.writeByte((byte) 0);
            return;
        }
        dest.writeByte((byte) 1);
        dest.writeList(this.deviceInstance);
    }
}
