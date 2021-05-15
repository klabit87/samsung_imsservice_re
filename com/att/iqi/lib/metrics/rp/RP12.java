package com.att.iqi.lib.metrics.rp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class RP12 extends Metric {
    public static final Parcelable.Creator<RP12> CREATOR = new Parcelable.Creator<RP12>() {
        public RP12 createFromParcel(Parcel parcel) {
            return new RP12(parcel);
        }

        public RP12[] newArray(int i) {
            return new RP12[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("RP12");
    private int c;
    private int d;
    private int e;
    private int f;
    private int g;
    private short h;
    private short i;
    private short j;
    private short k;
    private short l;
    private byte m;
    private byte n;
    private byte[] o;

    public RP12() {
        reset();
    }

    protected RP12(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readInt();
            this.d = parcel.readInt();
            this.e = parcel.readInt();
            this.f = parcel.readInt();
            this.g = parcel.readInt();
            this.h = (short) parcel.readInt();
            this.i = (short) parcel.readInt();
            this.j = (short) parcel.readInt();
            this.k = (short) parcel.readInt();
            this.l = (short) parcel.readInt();
            this.m = parcel.readByte();
            this.n = parcel.readByte();
            int readInt = parcel.readInt();
            if (readInt > 0) {
                byte[] bArr = new byte[readInt];
                this.o = bArr;
                parcel.readByteArray(bArr);
            }
        }
    }

    public int getByteCount() {
        return this.g;
    }

    public short getCumAvgPktSize() {
        return this.l;
    }

    public short getDstPort() {
        return this.h;
    }

    public int getDuration() {
        return this.d;
    }

    public byte[] getIpSrcAddr() {
        return this.o;
    }

    public byte getIpVersion() {
        return this.n;
    }

    public short getMaxDelta() {
        return this.k;
    }

    public short getMaxJitter() {
        return this.j;
    }

    public short getMeanJitter() {
        return this.i;
    }

    public byte getMediaType() {
        return this.m;
    }

    public int getPktCount() {
        return this.e;
    }

    public int getPktLoss() {
        return this.f;
    }

    public int getSsrc() {
        return this.c;
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
        this.k = 0;
        this.l = 0;
        this.m = 0;
        this.n = 0;
        this.o = null;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putInt(this.c);
        byteBuffer.putInt(this.d);
        byteBuffer.putInt(this.e);
        byteBuffer.putInt(this.f);
        byteBuffer.putInt(this.g);
        byteBuffer.putShort(this.h);
        byteBuffer.putShort(this.i);
        byteBuffer.putShort(this.j);
        byteBuffer.putShort(this.k);
        byteBuffer.putShort(this.l);
        byteBuffer.put(this.m);
        byteBuffer.put(this.n);
        byte[] bArr = this.o;
        if (bArr != null) {
            byteBuffer.put(bArr);
        }
        return byteBuffer.position();
    }

    public RP12 setByteCount(int i2) {
        this.g = i2;
        return this;
    }

    public RP12 setCumAvgPktSize(short s) {
        this.l = s;
        return this;
    }

    public RP12 setDstPort(short s) {
        this.h = s;
        return this;
    }

    public RP12 setDuration(int i2) {
        this.d = i2;
        return this;
    }

    public RP12 setIpSrcAddr(byte[] bArr) {
        this.o = bArr;
        return this;
    }

    public RP12 setIpVersion(byte b) {
        this.n = b;
        return this;
    }

    public RP12 setMaxDelta(short s) {
        this.k = s;
        return this;
    }

    public RP12 setMaxJitter(short s) {
        this.j = s;
        return this;
    }

    public RP12 setMeanJitter(short s) {
        this.i = s;
        return this;
    }

    public RP12 setMediaType(byte b) {
        this.m = b;
        return this;
    }

    public RP12 setPktCount(int i2) {
        this.e = i2;
        return this;
    }

    public RP12 setPktLoss(int i2) {
        this.f = i2;
        return this;
    }

    public RP12 setSsrc(int i2) {
        this.c = i2;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i2) {
        super.writeToParcel(parcel, i2);
        parcel.writeInt(this.c);
        parcel.writeInt(this.d);
        parcel.writeInt(this.e);
        parcel.writeInt(this.f);
        parcel.writeInt(this.g);
        parcel.writeInt(this.h);
        parcel.writeInt(this.i);
        parcel.writeInt(this.j);
        parcel.writeInt(this.k);
        parcel.writeInt(this.l);
        parcel.writeByte(this.m);
        parcel.writeByte(this.n);
        byte[] bArr = this.o;
        int length = bArr != null ? bArr.length : 0;
        if (length > 0) {
            parcel.writeInt(length);
            parcel.writeByteArray(this.o);
            return;
        }
        parcel.writeInt(0);
    }
}
