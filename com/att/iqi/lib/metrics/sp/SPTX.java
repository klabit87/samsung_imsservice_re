package com.att.iqi.lib.metrics.sp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SPTX extends Metric {
    public static final Parcelable.Creator<SPTX> CREATOR = new Parcelable.Creator<SPTX>() {
        public SPTX createFromParcel(Parcel parcel) {
            return new SPTX(parcel);
        }

        public SPTX[] newArray(int i) {
            return new SPTX[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("SPTX");
    private int c;
    private int d;
    private String e;

    public SPTX() {
        reset();
    }

    protected SPTX(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            this.c = parcel.readInt();
            this.d = parcel.readInt();
            this.e = parcel.readString();
        }
    }

    public int getCSeq() {
        return this.d;
    }

    public String getMessage() {
        return this.e;
    }

    public int getTransId() {
        return this.c;
    }

    public void reset() {
        this.c = 0;
        this.d = 0;
        this.e = "";
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putInt(this.c);
        byteBuffer.putInt(this.d);
        String str = this.e;
        int length = str == null ? 0 : str.length();
        byteBuffer.putInt(length);
        if (length > 0) {
            byteBuffer.put(this.e.getBytes(StandardCharsets.US_ASCII));
        }
        return byteBuffer.position();
    }

    public SPTX setCSeq(int i) {
        this.d = i;
        return this;
    }

    public SPTX setMessage(String str) {
        this.e = str;
        return this;
    }

    public SPTX setTransId(int i) {
        this.c = i;
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.c);
        parcel.writeInt(this.d);
        parcel.writeString(this.e);
    }
}
