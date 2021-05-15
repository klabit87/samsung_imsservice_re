package com.att.iqi.lib.metrics.rp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class RP01 extends Metric {
    public static final Parcelable.Creator<RP01> CREATOR = new Parcelable.Creator<RP01>() {
        public RP01 createFromParcel(Parcel parcel) {
            return new RP01(parcel);
        }

        public RP01[] newArray(int i) {
            return new RP01[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("RP01");
    private short c;
    private short d;
    private byte e;
    private byte f;
    private short g;
    private int h;
    private int i;

    public RP01() {
        reset();
    }

    protected RP01(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = (short) parcel.readInt();
            this.d = (short) parcel.readInt();
            this.e = parcel.readByte();
            this.f = parcel.readByte();
            this.g = (short) parcel.readInt();
            this.h = parcel.readInt();
            this.i = parcel.readInt();
        }
    }

    public short getByteCount() {
        return this.c;
    }

    public short getDstPort() {
        return this.d;
    }

    public byte getFlags() {
        return this.e;
    }

    public byte getPayloadType() {
        return this.f;
    }

    public short getSequenceNum() {
        return this.g;
    }

    public int getSourceId() {
        return this.i;
    }

    public int getTimestamp() {
        return this.h;
    }

    public void reset() {
        this.c = 0;
        this.d = 0;
        this.e = 0;
        this.f = 0;
        this.g = 0;
        this.h = 0;
        this.i = 0;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putShort(this.c);
        byteBuffer.putShort(this.d);
        byteBuffer.put(this.e);
        byteBuffer.put(this.f);
        byteBuffer.putShort(this.g);
        byteBuffer.putInt(this.h);
        byteBuffer.putInt(this.i);
        return byteBuffer.position();
    }

    public RP01 setByteCount(short s) {
        this.c = s;
        return this;
    }

    public RP01 setDstPort(short s) {
        this.d = s;
        return this;
    }

    public RP01 setFlags(byte b) {
        this.e = b;
        return this;
    }

    public RP01 setPayloadType(byte b) {
        this.f = b;
        return this;
    }

    public RP01 setSequenceNum(short s) {
        this.g = s;
        return this;
    }

    public RP01 setSourceId(int i2) {
        this.i = i2;
        return this;
    }

    public RP01 setTimestamp(int i2) {
        this.h = i2;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i2) {
        super.writeToParcel(parcel, i2);
        parcel.writeInt(this.c);
        parcel.writeInt(this.d);
        parcel.writeByte(this.e);
        parcel.writeByte(this.f);
        parcel.writeInt(this.g);
        parcel.writeInt(this.h);
        parcel.writeInt(this.i);
    }
}
