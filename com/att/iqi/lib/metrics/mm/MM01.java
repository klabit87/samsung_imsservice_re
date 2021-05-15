package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM01 extends Metric {
    public static final Parcelable.Creator<MM01> CREATOR = new Parcelable.Creator<MM01>() {
        public MM01 createFromParcel(Parcel parcel) {
            return new MM01(parcel);
        }

        public MM01[] newArray(int i) {
            return new MM01[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM01");
    public static final byte IQ_SIP_REGISTER_DIR_NETWORK = 2;
    public static final byte IQ_SIP_REGISTER_DIR_UEAT = 1;
    public static final byte IQ_SIP_REGISTER_DIR_UNKNOWN = 0;
    public static final byte IQ_SIP_REG_TYPE_DEREG = 3;
    public static final byte IQ_SIP_REG_TYPE_INITIAL = 1;
    public static final byte IQ_SIP_REG_TYPE_REAUTH = 4;
    public static final byte IQ_SIP_REG_TYPE_REREG = 2;
    public static final byte IQ_SIP_REG_TYPE_UNKNOWN = 0;
    private byte c;
    private byte d;
    private String e;
    private String f;
    private String g;

    public MM01() {
        reset();
    }

    protected MM01(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readByte();
            this.d = parcel.readByte();
            this.e = parcel.readString();
            this.f = parcel.readString();
            this.g = parcel.readString();
        }
    }

    public String getCallId() {
        return this.e;
    }

    public byte getDirection() {
        return this.d;
    }

    public String getRequestUri() {
        return this.f;
    }

    public String getTo() {
        return this.g;
    }

    public byte getType() {
        return this.c;
    }

    public void reset() {
        this.c = 0;
        this.d = 0;
        this.e = "";
        this.f = "";
        this.g = "";
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        byteBuffer.put(this.d);
        stringOut(byteBuffer, this.e);
        stringOut(byteBuffer, this.f);
        stringOut(byteBuffer, this.g);
        return byteBuffer.position();
    }

    public MM01 setCallId(String str) {
        this.e = str;
        return this;
    }

    public MM01 setDirection(byte b) {
        this.d = b;
        return this;
    }

    public MM01 setRequestUri(String str) {
        this.f = str;
        return this;
    }

    public MM01 setTo(String str) {
        this.g = str;
        return this;
    }

    public MM01 setType(byte b) {
        this.c = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
        parcel.writeByte(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
    }
}
