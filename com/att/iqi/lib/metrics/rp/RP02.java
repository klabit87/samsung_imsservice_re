package com.att.iqi.lib.metrics.rp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class RP02 extends Metric {
    public static final Parcelable.Creator<RP02> CREATOR = new Parcelable.Creator<RP02>() {
        public RP02 createFromParcel(Parcel parcel) {
            return new RP02(parcel);
        }

        public RP02[] newArray(int i) {
            return new RP02[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("RP02");
    public static final byte IQ_RTP_BADSSRC = 4;
    public static final byte IQ_RTP_DROP = 2;
    public static final byte IQ_RTP_LATE = 1;
    public static final byte IQ_RTP_OK = 0;
    public static final byte IQ_RTP_RESETSEQ = 3;
    private short c;
    private short d;
    private byte e;
    private byte f;
    private byte g;
    private short h;
    private int i;
    private int j;

    public RP02() {
        reset();
    }

    protected RP02(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = (short) parcel.readInt();
            this.d = (short) parcel.readInt();
            this.e = parcel.readByte();
            this.f = parcel.readByte();
            this.g = parcel.readByte();
            this.h = (short) parcel.readInt();
            this.i = parcel.readInt();
            this.j = parcel.readInt();
        }
    }

    public short getByteCount() {
        return this.c;
    }

    public short getDstPort() {
        return this.d;
    }

    public byte getFlags() {
        return this.f;
    }

    public byte getPayloadType() {
        return this.g;
    }

    public byte getPktStatus() {
        return this.e;
    }

    public short getSequenceNum() {
        return this.h;
    }

    public int getSourceId() {
        return this.j;
    }

    public int getTimestamp() {
        return this.i;
    }

    public void reset() {
        this.c = 0;
        this.d = 0;
        this.e = 0;
        this.f = 0;
        this.g = 0;
        this.h = 0;
        this.i = 0;
        this.j = 0;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putShort(this.c);
        byteBuffer.putShort(this.d);
        byteBuffer.put(this.e);
        byteBuffer.put(this.f);
        byteBuffer.put(this.g);
        byteBuffer.putShort(this.h);
        byteBuffer.putInt(this.i);
        byteBuffer.putInt(this.j);
        return byteBuffer.position();
    }

    public RP02 setByteCount(short s) {
        this.c = s;
        return this;
    }

    public RP02 setDstPort(short s) {
        this.d = s;
        return this;
    }

    public RP02 setFlags(byte b) {
        this.f = b;
        return this;
    }

    public RP02 setPayloadType(byte b) {
        this.g = b;
        return this;
    }

    public RP02 setPktStatus(byte b) {
        this.e = b;
        return this;
    }

    public RP02 setSequenceNum(short s) {
        this.h = s;
        return this;
    }

    public RP02 setSourceId(int i2) {
        this.j = i2;
        return this;
    }

    public RP02 setTimestamp(int i2) {
        this.i = i2;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i2) {
        super.writeToParcel(parcel, i2);
        parcel.writeInt(this.c);
        parcel.writeInt(this.d);
        parcel.writeByte(this.e);
        parcel.writeByte(this.f);
        parcel.writeByte(this.g);
        parcel.writeInt(this.h);
        parcel.writeInt(this.i);
        parcel.writeInt(this.j);
    }
}
