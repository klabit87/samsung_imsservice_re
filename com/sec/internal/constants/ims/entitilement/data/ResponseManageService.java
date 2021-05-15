package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ResponseManageService extends NSDSResponse {
    public static final Parcelable.Creator<ResponseManageService> CREATOR = new Parcelable.Creator<ResponseManageService>() {
        public ResponseManageService createFromParcel(Parcel in) {
            return new ResponseManageService(in);
        }

        public ResponseManageService[] newArray(int size) {
            return new ResponseManageService[size];
        }
    };
    @SerializedName("instance-token")
    public InstanceToken instanceToken;
    @SerializedName("service-instance")
    public ServiceInstance serviceInstance;

    protected ResponseManageService(Parcel in) {
        super(in);
        this.serviceInstance = (ServiceInstance) in.readValue(ServiceInstance.class.getClassLoader());
        this.instanceToken = (InstanceToken) in.readValue(InstanceToken.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(this.serviceInstance);
        dest.writeValue(this.instanceToken);
    }
}
