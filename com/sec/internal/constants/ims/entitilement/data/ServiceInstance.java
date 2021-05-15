package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ServiceInstance implements Parcelable {
    public static final transient Parcelable.Creator<ServiceInstance> CREATOR = new Parcelable.Creator<ServiceInstance>() {
        public ServiceInstance createFromParcel(Parcel in) {
            return new ServiceInstance(in);
        }

        public ServiceInstance[] newArray(int size) {
            return new ServiceInstance[size];
        }
    };
    @SerializedName("config-parameters")
    public String configParameters;
    @SerializedName("end-time")
    public String endTime;
    @SerializedName("expiration-time")
    public Integer expirationTime;
    @SerializedName("friendly-name")
    public String friendlyName;
    @SerializedName("is-owner")
    public Boolean isOwner;
    public String msisdn;
    @SerializedName("provisioning-parameters")
    public ProvisioningParameters provisioningParameters;
    @SerializedName("service-instance-id")
    public String serviceInstanceId;
    @SerializedName("service-name")
    public String serviceName;

    public ServiceInstance() {
    }

    protected ServiceInstance(Parcel in) {
        Boolean bool;
        this.serviceName = in.readString();
        this.serviceInstanceId = in.readString();
        byte isOwnerVal = in.readByte();
        Integer num = null;
        if (isOwnerVal == 2) {
            bool = null;
        } else {
            bool = Boolean.valueOf(isOwnerVal != 0);
        }
        this.isOwner = bool;
        this.endTime = in.readString();
        this.expirationTime = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
        this.msisdn = in.readString();
        this.friendlyName = in.readString();
        this.provisioningParameters = (ProvisioningParameters) in.readValue(ProvisioningParameters.class.getClassLoader());
        this.configParameters = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceName);
        dest.writeString(this.serviceInstanceId);
        Boolean bool = this.isOwner;
        if (bool == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte(bool.booleanValue() ? (byte) 1 : 0);
        }
        dest.writeString(this.endTime);
        if (this.expirationTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(this.expirationTime.intValue());
        }
        dest.writeString(this.msisdn);
        dest.writeString(this.friendlyName);
        dest.writeValue(this.provisioningParameters);
        dest.writeString(this.configParameters);
    }
}
