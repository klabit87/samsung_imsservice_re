package com.att.iqi.lib.metrics.hw;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class HW0E extends Metric {
    public static final Parcelable.Creator<HW0E> CREATOR = new Parcelable.Creator<HW0E>() {
        public HW0E createFromParcel(Parcel parcel) {
            return new HW0E(parcel);
        }

        public HW0E[] newArray(int i) {
            return new HW0E[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("HW0E");
    private byte c;

    public HW0E() {
    }

    protected HW0E(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readByte();
        }
    }

    public byte getEvent() {
        return this.c;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        return byteBuffer.position();
    }

    public HW0E setEvent(byte b) {
        this.c = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
    }
}
