package com.att.iqi.lib.metrics.rp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class RP11 extends Metric {
    public static final Parcelable.Creator<RP11> CREATOR = new Parcelable.Creator<RP11>() {
        public RP11 createFromParcel(Parcel parcel) {
            return new RP11(parcel);
        }

        public RP11[] newArray(int i) {
            return new RP11[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("RP11");
    private int c;
    private int d;
    private int e;
    private int f;
    private short g;
    private short h;
    private byte i;
    private byte j;
    private byte[] k;

    public RP11() {
        reset();
    }

    protected RP11(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readInt();
            this.d = parcel.readInt();
            this.e = parcel.readInt();
            this.f = parcel.readInt();
            this.g = (short) parcel.readInt();
            this.h = (short) parcel.readInt();
            this.i = parcel.readByte();
            this.j = parcel.readByte();
            int readInt = parcel.readInt();
            if (readInt > 0) {
                byte[] bArr = new byte[readInt];
                this.k = bArr;
                parcel.readByteArray(bArr);
            }
        }
    }

    public int getByteCount() {
        return this.f;
    }

    public short getDstPort() {
        return this.g;
    }

    public int getDuration() {
        return this.d;
    }

    public byte[] getIpDstAddr() {
        return this.k;
    }

    public byte getIpVersion() {
        return this.j;
    }

    public short getMeanJitter() {
        return this.h;
    }

    public byte getMediaType() {
        return this.i;
    }

    public int getPktCount() {
        return this.e;
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
        this.k = null;
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putInt(this.c);
        byteBuffer.putInt(this.d);
        byteBuffer.putInt(this.e);
        byteBuffer.putInt(this.f);
        byteBuffer.putShort(this.g);
        byteBuffer.putShort(this.h);
        byteBuffer.put(this.i);
        byteBuffer.put(this.j);
        byte[] bArr = this.k;
        if (bArr != null) {
            byteBuffer.put(bArr);
        }
        return byteBuffer.position();
    }

    public RP11 setByteCount(int i2) {
        this.f = i2;
        return this;
    }

    public RP11 setDstPort(short s) {
        this.g = s;
        return this;
    }

    public RP11 setDuration(int i2) {
        this.d = i2;
        return this;
    }

    public RP11 setIpDstAddr(byte[] bArr) {
        this.k = bArr;
        return this;
    }

    public RP11 setIpVersion(byte b) {
        this.j = b;
        return this;
    }

    public RP11 setMeanJitter(short s) {
        this.h = s;
        return this;
    }

    public RP11 setMediaType(byte b) {
        this.i = b;
        return this;
    }

    public RP11 setPktCount(int i2) {
        this.e = i2;
        return this;
    }

    public RP11 setSsrc(int i2) {
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
        parcel.writeByte(this.i);
        parcel.writeByte(this.j);
        byte[] bArr = this.k;
        int length = bArr != null ? bArr.length : 0;
        if (length > 0) {
            parcel.writeInt(length);
            parcel.writeByteArray(this.k);
            return;
        }
        parcel.writeInt(0);
    }
}
