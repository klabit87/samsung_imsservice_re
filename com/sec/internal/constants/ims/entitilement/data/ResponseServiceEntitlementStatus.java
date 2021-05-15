package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class ResponseServiceEntitlementStatus extends NSDSResponse {
    public static final Parcelable.Creator<ResponseServiceEntitlementStatus> CREATOR = new Parcelable.Creator<ResponseServiceEntitlementStatus>() {
        public ResponseServiceEntitlementStatus createFromParcel(Parcel in) {
            return new ResponseServiceEntitlementStatus(in);
        }

        public ResponseServiceEntitlementStatus[] newArray(int size) {
            return new ResponseServiceEntitlementStatus[size];
        }
    };
    @SerializedName("enable-notifications")
    public Boolean enableNotifications;
    @SerializedName("poll-interval")
    public Integer pollInterval;
    @SerializedName("service-entitlement")
    public ArrayList<ServiceEntitlement> serviceEntitlementList;

    protected ResponseServiceEntitlementStatus(Parcel in) {
        super(in);
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        if (in.readByte() == 1) {
            ArrayList<ServiceEntitlement> arrayList = new ArrayList<>();
            this.serviceEntitlementList = arrayList;
            in.readTypedList(arrayList, ServiceEntitlement.CREATOR);
            return;
        }
        this.serviceEntitlementList = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.serviceEntitlementList == null) {
            dest.writeByte((byte) 0);
            return;
        }
        dest.writeByte((byte) 1);
        dest.writeTypedList(this.serviceEntitlementList);
    }
}
