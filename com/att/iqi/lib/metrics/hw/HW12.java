package com.att.iqi.lib.metrics.hw;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class HW12 extends Metric {
    public static final Parcelable.Creator<HW12> CREATOR = new Parcelable.Creator<HW12>() {
        public HW12 createFromParcel(Parcel parcel) {
            return new HW12(parcel);
        }

        public HW12[] newArray(int i) {
            return new HW12[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("HW12");
    private byte c;
    private byte d;

    public HW12() {
    }

    protected HW12(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readByte();
            this.d = parcel.readByte();
        }
    }

    public short getCause() {
        return (short) this.c;
    }

    public byte getProcessor() {
        return this.d;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        byteBuffer.put(this.d);
        return byteBuffer.position();
    }

    public HW12 setCause(byte b) {
        this.c = b;
        return this;
    }

    public HW12 setProcessor(byte b) {
        this.d = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
        parcel.writeByte(this.d);
    }
}
