package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class DeviceInstance implements Parcelable {
    @SerializedName("device-id")
    public String deviceId;
    @SerializedName("device-name")
    public String deviceName;
    @SerializedName("device-type")
    public int deviceType;
    @SerializedName("service-instances")
    public ArrayList<ServiceInstance> serviceInstances;

    protected DeviceInstance(Parcel in) {
        this.deviceId = in.readString();
        this.deviceName = in.readString();
        this.deviceType = in.readInt();
        if (in.readByte() == 1) {
            ArrayList<ServiceInstance> arrayList = new ArrayList<>();
            this.serviceInstances = arrayList;
            in.readList(arrayList, ServiceInstance.class.getClassLoader());
            return;
        }
        this.serviceInstances = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeInt(this.deviceType);
        if (this.serviceInstances == null) {
            dest.writeByte((byte) 0);
            return;
        }
        dest.writeByte((byte) 1);
        dest.writeList(this.serviceInstances);
    }
}
