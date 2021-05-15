package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;

public class NsdNetworkCapabilities implements Parcelable {
    public static final int CAPABILITY_FULL = 7;
    public static final int CAPABILITY_INCOMING_CALL = 1;
    public static final int CAPABILITY_LOG_SYNC = 2;
    public static final int CAPABILITY_MAX = 7;
    public static final int CAPABILITY_OUTGOING_CALL = 0;
    public static final Parcelable.Creator<NsdNetworkCapabilities> CREATOR = new Parcelable.Creator<NsdNetworkCapabilities>() {
        public NsdNetworkCapabilities createFromParcel(Parcel in) {
            return new NsdNetworkCapabilities(in);
        }

        public NsdNetworkCapabilities[] newArray(int size) {
            return new NsdNetworkCapabilities[size];
        }
    };
    public static final int TRANSPORT_WIFI_AP = 0;
    public static final int TRANSPORT_WIFI_DIRECT = 1;
    /* access modifiers changed from: private */
    public int mCapabilities;
    /* access modifiers changed from: private */
    public int mTransport;

    private NsdNetworkCapabilities() {
        this.mTransport = 0;
        this.mCapabilities = 0;
    }

    private NsdNetworkCapabilities(Parcel in) {
        readFromParcel(in);
    }

    public int getTransport() {
        return this.mTransport;
    }

    public boolean hasTransport(int transport) {
        return (this.mTransport & (1 << transport)) != 0;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public boolean hasCapability(int capability) {
        return (this.mCapabilities & (1 << capability)) != 0;
    }

    public boolean hasCapabilities(NsdNetworkCapabilities capabilities) {
        return ((this.mTransport & capabilities.getTransport()) == 0 || (this.mCapabilities & capabilities.getCapabilities()) == 0) ? false : true;
    }

    public boolean combine(NsdNetworkCapabilities capabilities) {
        int i = this.mCapabilities;
        int i2 = capabilities.mCapabilities;
        if ((i ^ i2) == 0) {
            return false;
        }
        this.mCapabilities = i | i2;
        return true;
    }

    public String toString() {
        return "[transport=" + this.mTransport + " capabilities=" + this.mCapabilities + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTransport);
        dest.writeInt(this.mCapabilities);
    }

    private void readFromParcel(Parcel in) {
        this.mTransport = in.readInt();
        this.mCapabilities = in.readInt();
    }

    public static class Builder {
        private final NsdNetworkCapabilities mNsdNetworkCapabilities = new NsdNetworkCapabilities();

        public Builder addTransport(int transport) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mTransport = nsdNetworkCapabilities.mTransport | (1 << transport);
            return this;
        }

        public Builder addCapability(int capability) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mCapabilities = nsdNetworkCapabilities.mCapabilities | (1 << capability);
            return this;
        }

        public Builder setCapabilities(int capabilities) {
            int unused = this.mNsdNetworkCapabilities.mCapabilities = capabilities;
            return this;
        }

        public Builder combineCapabilities(int capabilities) {
            NsdNetworkCapabilities nsdNetworkCapabilities = this.mNsdNetworkCapabilities;
            int unused = nsdNetworkCapabilities.mCapabilities = nsdNetworkCapabilities.mCapabilities | capabilities;
            return this;
        }

        public NsdNetworkCapabilities build() {
            return this.mNsdNetworkCapabilities;
        }
    }
}
