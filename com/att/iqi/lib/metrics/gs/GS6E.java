package com.att.iqi.lib.metrics.gs;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class GS6E extends Metric {
    public static final Parcelable.Creator<GS6E> CREATOR = new Parcelable.Creator<GS6E>() {
        public GS6E createFromParcel(Parcel parcel) {
            return new GS6E(parcel);
        }

        public GS6E[] newArray(int i) {
            return new GS6E[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("GS6E");
    private byte c;

    public GS6E() {
    }

    public GS6E(byte b) {
        this.c = b;
    }

    protected GS6E(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readByte();
        }
    }

    public byte getRadioMode() {
        return this.c;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        return byteBuffer.position();
    }

    public GS6E setRadioMode(byte b) {
        this.c = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
    }
}
