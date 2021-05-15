package com.att.iqi.lib.metrics.sp;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SPRX extends Metric {
    public static final Parcelable.Creator<SPRX> CREATOR = new Parcelable.Creator<SPRX>() {
        public SPRX createFromParcel(Parcel parcel) {
            return new SPRX(parcel);
        }

        public SPRX[] newArray(int i) {
            return new SPRX[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("SPRX");
    private int c;
    private int d;
    private String e;

    public SPRX() {
        reset();
    }

    protected SPRX(Parcel parcel) {
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

    public SPRX setCSeq(int i) {
        this.d = i;
        return this;
    }

    public SPRX setMessage(String str) {
        this.e = str;
        return this;
    }

    public SPRX setTransId(int i) {
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
