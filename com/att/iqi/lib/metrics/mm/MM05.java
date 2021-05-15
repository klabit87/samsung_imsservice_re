package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM05 extends Metric {
    public static final Parcelable.Creator<MM05> CREATOR = new Parcelable.Creator<MM05>() {
        public MM05 createFromParcel(Parcel parcel) {
            return new MM05(parcel);
        }

        public MM05[] newArray(int i) {
            return new MM05[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM05");
    public static final byte IQ_SIP_CALL_STATE_ANSWERED = 8;
    public static final byte IQ_SIP_CALL_STATE_CONNECTED = 9;
    public static final byte IQ_SIP_CALL_STATE_DISCONNECTING = 11;
    public static final byte IQ_SIP_CALL_STATE_HELD = 10;
    public static final byte IQ_SIP_CALL_STATE_IDLE = 1;
    public static final byte IQ_SIP_CALL_STATE_INVITE = 2;
    public static final byte IQ_SIP_CALL_STATE_NEGOTIATING = 5;
    public static final byte IQ_SIP_CALL_STATE_PROGRESS = 4;
    public static final byte IQ_SIP_CALL_STATE_RINGING = 7;
    public static final byte IQ_SIP_CALL_STATE_TRYING = 3;
    public static final byte IQ_SIP_CALL_STATE_UNKNOWN = 0;
    public static final byte IQ_SIP_CALL_STATE_UPDATED = 6;
    private byte c;
    private String d;

    public MM05() {
        reset();
    }

    protected MM05(Parcel parcel) {
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

    public MM05 setCallId(String str) {
        this.d = str;
        return this;
    }

    public MM05 setCallState(byte b) {
        this.c = b;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
        parcel.writeString(this.d);
    }
}
