package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM03 extends Metric {
    public static final Parcelable.Creator<MM03> CREATOR = new Parcelable.Creator<MM03>() {
        public MM03 createFromParcel(Parcel parcel) {
            return new MM03(parcel);
        }

        public MM03[] newArray(int i) {
            return new MM03[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM03");
    private short c;
    private String d;

    public MM03() {
        reset();
    }

    protected MM03(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = (short) parcel.readInt();
            this.d = parcel.readString();
        }
    }

    public String getCallId() {
        return this.d;
    }

    public short getRegState() {
        return this.c;
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

    public MM03 setCallId(String str) {
        this.d = str;
        return this;
    }

    public MM03 setRegState(short s) {
        this.c = s;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.c);
        parcel.writeString(this.d);
    }
}
