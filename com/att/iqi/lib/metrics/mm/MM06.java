package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM06 extends Metric {
    public static final Parcelable.Creator<MM06> CREATOR = new Parcelable.Creator<MM06>() {
        public MM06 createFromParcel(Parcel parcel) {
            return new MM06(parcel);
        }

        public MM06[] newArray(int i) {
            return new MM06[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM06");
    public static final byte IQ_SIP_ORIGINATED = 0;
    public static final byte IQ_SIP_TERMINATED = 1;
    private short c;
    private String d;

    public MM06() {
        reset();
    }

    protected MM06(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = (short) parcel.readInt();
            this.d = parcel.readString();
        }
    }

    public String getCallId() {
        return this.d;
    }

    public short getResponseCode() {
        return (short) (this.c & Short.MAX_VALUE);
    }

    public byte getTerminationDirection() {
        return (byte) (this.c >> 15);
    }

    public void reset() {
        this.c = 0;
        this.d = "";
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putShort(this.c);
        stringOut(byteBuffer, this.d);
        return byteBuffer.position();
    }

    public MM06 setCallId(String str) {
        this.d = str;
        return this;
    }

    public MM06 setResponseCode(short s) {
        this.c = (short) ((s & Short.MAX_VALUE) | (this.c & Short.MIN_VALUE));
        return this;
    }

    public MM06 setTerminationDirection(byte b) {
        this.c = (short) ((((short) b) << 15) | (this.c & Short.MAX_VALUE));
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.c);
        parcel.writeString(this.d);
    }
}
