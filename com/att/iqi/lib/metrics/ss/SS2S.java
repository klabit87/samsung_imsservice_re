package com.att.iqi.lib.metrics.ss;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class SS2S extends Metric {
    public static final Parcelable.Creator<SS2S> CREATOR = new Parcelable.Creator<SS2S>() {
        public SS2S createFromParcel(Parcel parcel) {
            return null;
        }

        public SS2S[] newArray(int i) {
            return new SS2S[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("SS2S");
    public static final byte SERVICE_SHOULD_NOT_RUN = 1;
    public static final byte SERVICE_SHOULD_RUN = 0;
    public static final int SHOULD_SERVICE_RUN = 0;
    private byte c = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IQISettings {
    }

    public SS2S() {
    }

    protected SS2S(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 2) {
            this.c = parcel.readByte();
        }
    }

    public byte getSetting(int i) {
        if (i == 0) {
            return (byte) (this.c & 1);
        }
        throw new IllegalArgumentException("Invalid setting ID " + i);
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.put(this.c);
        return byteBuffer.position();
    }

    public SS2S setSetting(int i, byte b) {
        if (i == 0) {
            if (b == 0 || b == 1) {
                byte b2 = (byte) (this.c & -2);
                this.c = b2;
                this.c = (byte) (b2 | b);
            } else {
                throw new IllegalArgumentException("Illegal value " + b + " for setting ID " + i);
            }
        }
        return this;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeByte(this.c);
    }
}
