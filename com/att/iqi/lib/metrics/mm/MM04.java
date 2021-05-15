package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class MM04 extends Metric {
    public static final Parcelable.Creator<MM04> CREATOR = new Parcelable.Creator<MM04>() {
        public MM04 createFromParcel(Parcel parcel) {
            return new MM04(parcel);
        }

        public MM04[] newArray(int i) {
            return new MM04[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM04");
    private String c;
    private String d;
    private String e;
    private String f;

    public MM04() {
        reset();
    }

    protected MM04(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readString();
            this.d = parcel.readString();
            this.e = parcel.readString();
            this.f = parcel.readString();
        }
    }

    public String getCallId() {
        return this.d;
    }

    public String getDialedString() {
        return this.c;
    }

    public String getOriginatingUri() {
        return this.e;
    }

    public String getTerminatingUri() {
        return this.f;
    }

    public void reset() {
        this.c = "";
        this.d = "";
        this.e = "";
        this.f = "";
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        stringOut(byteBuffer, this.c);
        stringOut(byteBuffer, this.d);
        stringOut(byteBuffer, this.e);
        stringOut(byteBuffer, this.f);
        return byteBuffer.position();
    }

    public MM04 setCallId(String str) {
        this.d = str;
        return this;
    }

    public MM04 setDialedString(String str) {
        this.c = str;
        return this;
    }

    public MM04 setOriginatingUri(String str) {
        this.e = str;
        return this;
    }

    public MM04 setTerminatingUri(String str) {
        this.f = str;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
    }
}
