package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;

public class NsdNetworkMessage implements Parcelable {
    public static final Parcelable.Creator<NsdNetworkMessage> CREATOR = new Parcelable.Creator<NsdNetworkMessage>() {
        public NsdNetworkMessage createFromParcel(Parcel in) {
            return new NsdNetworkMessage(in);
        }

        public NsdNetworkMessage[] newArray(int size) {
            return new NsdNetworkMessage[size];
        }
    };
    /* access modifiers changed from: private */
    public int mEvent;

    private NsdNetworkMessage() {
    }

    private NsdNetworkMessage(Parcel in) {
        readFromParcel(in);
    }

    public int getEvent() {
        return this.mEvent;
    }

    public String toString() {
        return "[event=" + this.mEvent + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEvent);
    }

    private void readFromParcel(Parcel in) {
        this.mEvent = in.readInt();
    }

    public static class Builder {
        private final NsdNetworkMessage mNsdNetworkMessage = new NsdNetworkMessage();

        public Builder setEvent(int event) {
            NsdNetworkMessage nsdNetworkMessage = this.mNsdNetworkMessage;
            int unused = nsdNetworkMessage.mEvent = nsdNetworkMessage.mEvent | event;
            return this;
        }

        public NsdNetworkMessage build() {
            return this.mNsdNetworkMessage;
        }
    }
}
