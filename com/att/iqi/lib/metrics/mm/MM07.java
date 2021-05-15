package com.att.iqi.lib.metrics.mm;

import android.os.Parcel;
import android.os.Parcelable;
import com.att.iqi.lib.Metric;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MM07 extends Metric {
    public static final Parcelable.Creator<MM07> CREATOR = new Parcelable.Creator<MM07>() {
        public MM07 createFromParcel(Parcel parcel) {
            return new MM07(parcel);
        }

        public MM07[] newArray(int i) {
            return new MM07[i];
        }
    };
    public static final Metric.ID ID = new Metric.ID("MM07");
    public static final byte IQ_SDP_MEDIA_APPLICATION = 4;
    public static final byte IQ_SDP_MEDIA_AUDIO = 1;
    public static final byte IQ_SDP_MEDIA_CONTROL = 6;
    public static final byte IQ_SDP_MEDIA_DATA = 7;
    public static final byte IQ_SDP_MEDIA_MESSAGE = 5;
    public static final byte IQ_SDP_MEDIA_TEXT = 3;
    public static final byte IQ_SDP_MEDIA_UNKNOWN = 0;
    public static final byte IQ_SDP_MEDIA_VIDEO = 2;
    private HashMap<Short, RtpStats> c = new HashMap<>();

    private static class RtpStats {
        /* access modifiers changed from: private */
        public byte a;
        /* access modifiers changed from: private */
        public byte b;
        /* access modifiers changed from: private */
        public int c;
        /* access modifiers changed from: private */
        public int d;
        /* access modifiers changed from: private */
        public int e;
        /* access modifiers changed from: private */
        public int f;

        private RtpStats(byte b2, byte b3) {
            this.a = b2;
            this.b = b3;
            this.c = 0;
            this.d = 0;
            this.e = 0;
            this.f = 0;
        }

        /* access modifiers changed from: private */
        public void a(ByteBuffer byteBuffer) throws BufferOverflowException {
            byteBuffer.put(this.a);
            byteBuffer.put(this.b);
            byteBuffer.putInt(this.c);
            byteBuffer.putInt(this.d);
            byteBuffer.putInt(this.e);
            byteBuffer.putInt(this.f);
        }

        public void set(int i, int i2, int i3, int i4) {
            this.c = i;
            this.d = i2;
            this.e = i3;
            this.f = i4;
        }
    }

    public MM07() {
        reset();
    }

    protected MM07(Parcel parcel) {
        super(parcel);
        if (parcel.readInt() >= 1) {
            int readInt = parcel.readInt();
            for (int i = 0; i < readInt; i++) {
                RtpStats rtpStats = new RtpStats(parcel.readByte(), parcel.readByte());
                int unused = rtpStats.c = parcel.readInt();
                int unused2 = rtpStats.d = parcel.readInt();
                int unused3 = rtpStats.e = parcel.readInt();
                int unused4 = rtpStats.f = parcel.readInt();
                this.c.put(Short.valueOf((short) parcel.readInt()), rtpStats);
            }
        }
    }

    public int getDrop(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.e;
        }
        return 0;
    }

    public int getDuration(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.c;
        }
        return 0;
    }

    public byte getFormat(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.b;
        }
        return 0;
    }

    public int getLate(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.f;
        }
        return 0;
    }

    public byte getMediaType(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.a;
        }
        return 0;
    }

    public int getRcvd(short s) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats != null) {
            return rtpStats.d;
        }
        return 0;
    }

    public void reset() {
        this.c.clear();
    }

    /* access modifiers changed from: protected */
    public int serialize(ByteBuffer byteBuffer) throws BufferOverflowException {
        byteBuffer.putShort((short) this.c.size());
        for (Map.Entry next : this.c.entrySet()) {
            byteBuffer.putShort(((Short) next.getKey()).shortValue());
            ((RtpStats) next.getValue()).a(byteBuffer);
        }
        return byteBuffer.position();
    }

    public void setRtpStats(short s, byte b, byte b2, int i, int i2, int i3, int i4) {
        RtpStats rtpStats = this.c.get(Short.valueOf(s));
        if (rtpStats == null) {
            rtpStats = new RtpStats(b, b2);
            this.c.put(Short.valueOf(s), rtpStats);
        }
        rtpStats.set(i, i2, i3, i4);
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        int size = this.c.size();
        parcel.writeInt(size);
        if (size > 0) {
            for (Map.Entry next : this.c.entrySet()) {
                parcel.writeInt(((Short) next.getKey()).shortValue());
                RtpStats rtpStats = (RtpStats) next.getValue();
                parcel.writeByte(rtpStats.a);
                parcel.writeByte(rtpStats.b);
                parcel.writeInt(rtpStats.c);
                parcel.writeInt(rtpStats.d);
                parcel.writeInt(rtpStats.e);
                parcel.writeInt(rtpStats.f);
            }
        }
    }
}
