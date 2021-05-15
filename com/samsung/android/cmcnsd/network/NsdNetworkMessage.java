package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;

public class NsdNetworkMessage implements Parcelable {
    public static final Parcelable.Creator<NsdNetworkMessage> CREATOR = new Parcelable.Creator<NsdNetworkMessage>() {
        public NsdNetworkMessage createFromParcel(Parcel parcel) {
            return new NsdNetworkMessage(parcel);
        }

        public NsdNetworkMessage[] newArray(int i) {
            return new NsdNetworkMessage[i];
        }
    };
    public int mEvent;

    public int describeContents() {
        return 0;
    }

    public NsdNetworkMessage() {
    }

    public NsdNetworkMessage(Parcel parcel) {
        readFromParcel(parcel);
    }

    public int getEvent() {
        return this.mEvent;
    }

    public String toString() {
        return "[event=" + this.mEvent + "]";
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mEvent);
    }

    private void readFromParcel(Parcel parcel) {
        this.mEvent = parcel.readInt();
    }

    public static class Builder {
        public final NsdNetworkMessage mNsdNetworkMessage = new NsdNetworkMessage();

        public Builder setEvent(int i) {
            NsdNetworkMessage nsdNetworkMessage = this.mNsdNetworkMessage;
            int unused = nsdNetworkMessage.mEvent = i | nsdNetworkMessage.mEvent;
            return this;
        }

        public NsdNetworkMessage build() {
            return this.mNsdNetworkMessage;
        }
    }
}
