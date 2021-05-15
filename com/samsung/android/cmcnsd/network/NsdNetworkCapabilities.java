package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class NsdNetworkCapabilities implements Parcelable {
    public static final int CAPABILITY_FULL = 7;
    public static final int CAPABILITY_INCOMING_CALL = 1;
    public static final int CAPABILITY_LOG_SYNC = 2;
    public static final int CAPABILITY_MAX = 7;
    public static final int CAPABILITY_OUTGOING_CALL = 0;
    public static final Parcelable.Creator<NsdNetworkCapabilities> CREATOR = new Parcelable.Creator<NsdNetworkCapabilities>() {
        public NsdNetworkCapabilities createFromParcel(Parcel parcel) {
            return new NsdNetworkCapabilities(parcel);
        }

        public NsdNetworkCapabilities[] newArray(int i) {
            return new NsdNetworkCapabilities[i];
        }
    };
    public static final int TRANSPORT_WIFI_AP = 0;
    public static final int TRANSPORT_WIFI_DIRECT = 1;
    public int mCapabilities;
    public int mTransport;

    public int describeContents() {
        return 0;
    }

    public NsdNetworkCapabilities() {
        this.mTransport = 0;
        this.mCapabilities = 0;
    }

    public NsdNetworkCapabilities(Parcel parcel) {
        readFromParcel(parcel);
    }

    public int getTransport() {
        return this.mTransport;
    }

    public boolean hasTransport(int i) {
        return ((1 << i) & this.mTransport) != 0;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public boolean hasCapability(int i) {
        return ((1 << i) & this.mCapabilities) != 0;
    }

    public boolean hasCapabilities(NsdNetworkCapabilities nsdNetworkCapabilities) {
        if (!(nsdNetworkCapabilities == null || (this.mTransport & nsdNetworkCapabilities.getTransport()) == 0)) {
            if ((nsdNetworkCapabilities.getCapabilities() & this.mCapabilities) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean combine(NsdNetworkCapabilities nsdNetworkCapabilities) {
        if (nsdNetworkCapabilities == null) {
            return false;
        }
        int i = this.mCapabilities;
        int i2 = nsdNetworkCapabilities.mCapabilities;
        if ((i ^ i2) == 0) {
            return false;
        }
        this.mCapabilities = i2 | i;
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NsdNetworkCapabilities)) {
            return false;
        }
        NsdNetworkCapabilities nsdNetworkCapabilities = (NsdNetworkCapabilities) obj;
        if (this.mTransport == nsdNetworkCapabilities.mTransport && this.mCapabilities == nsdNetworkCapabilities.mCapabilities) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mTransport), Integer.valueOf(this.mCapabilities)});
    }

    public String toString() {
        return "[transport=" + this.mTransport + " capabilities=" + this.mCapabilities + "]";
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mTransport);
        parcel.writeInt(this.mCapabilities);
    }

    private void readFromParcel(Parcel parcel) {
        this.mTransport = parcel.readInt();
        this.mCapabilities = parcel.readInt();
    }

    public static class Builder {
        public final NsdNetworkCapabilities mNsdNetworkCapabilities = new NsdNetworkCapabilities();

        public Builder addTransport(int i) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mTransport = (1 << i) | nsdNetworkCapabilities.mTransport;
            return this;
        }

        public Builder addCapability(int i) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mCapabilities = (1 << i) | nsdNetworkCapabilities.mCapabilities;
            return this;
        }

        public Builder setCapabilities(int i) {
            int unused = this.mNsdNetworkCapabilities.mCapabilities = i;
            return this;
        }

        public Builder combineCapabilities(int i) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mCapabilities = i | nsdNetworkCapabilities.mCapabilities;
            return this;
        }

        public NsdNetworkCapabilities build() {
            return this.mNsdNetworkCapabilities;
        }
    }
}
