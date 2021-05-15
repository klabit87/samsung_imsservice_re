package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM02 extends Metric {
    public static final Parcelable.Creator<MM02> CREATOR = new Parcelable.Creator<MM02>() {
        public MM02 createFromParcel(Parcel parcel) {
            return new MM02(parcel);
        }

        public MM02[] newArray(int i) {
            return new MM02[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM02");
    public static final byte IQ_SIP_REG_STATE_AUTH_CHALLENGE = 3;
    public static final byte IQ_SIP_REG_STATE_AUTH_REGISTER = 4;
    public static final byte IQ_SIP_REG_STATE_NOTIFIED = 6;
    public static final byte IQ_SIP_REG_STATE_NOT_REGISTERED = 1;
    public static final byte IQ_SIP_REG_STATE_REGISTERED = 5;
    public static final byte IQ_SIP_REG_STATE_REGISTERING = 2;
    public static final byte IQ_SIP_REG_STATE_TIMEOUT = 7;
    public static final byte IQ_SIP_REG_STATE_UNKNOWN = 0;
    private byte c;
    private String d;

    public MM02() {
        reset();
    }

    protected MM02(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readByte();
            this.d = parcel.readString();
        }
    }

    public String getCallId() {
        return this.d;
    }

    public byte getRegState() {
        return this.c;
    }

    public void reset() {
        this.c = 0;
        this.d = "";
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        stringOut(byteBuffer, this.d);
        return byteBuffer.position();
    }

    public MM02 setCallId(String str) {
        this.d = str;
        return this;
    }

    public MM02 setRegState(byte b) {
        this.c = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
        parcel.writeString(this.d);
    }
}
