package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRtpStatsToStack extends Table {
    public static RequestRtpStatsToStack getRootAsRequestRtpStatsToStack(ByteBuffer _bb) {
        return getRootAsRequestRtpStatsToStack(_bb, new RequestRtpStatsToStack());
    }

    public static RequestRtpStatsToStack getRootAsRequestRtpStatsToStack(ByteBuffer _bb, RequestRtpStatsToStack obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestRtpStatsToStack __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long channelid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long lossrate() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long delay() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long jitter() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long measuredperiod() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long direction() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestRtpStatsToStack(FlatBufferBuilder builder, long channelid, long lossrate, long delay, long jitter, long measuredperiod, long direction) {
        builder.startObject(6);
        addDirection(builder, direction);
        addMeasuredperiod(builder, measuredperiod);
        addJitter(builder, jitter);
        addDelay(builder, delay);
        addLossrate(builder, lossrate);
        addChannelid(builder, channelid);
        return endRequestRtpStatsToStack(builder);
    }

    public static void startRequestRtpStatsToStack(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addChannelid(FlatBufferBuilder builder, long channelid) {
        builder.addInt(0, (int) channelid, 0);
    }

    public static void addLossrate(FlatBufferBuilder builder, long lossrate) {
        builder.addInt(1, (int) lossrate, 0);
    }

    public static void addDelay(FlatBufferBuilder builder, long delay) {
        builder.addInt(2, (int) delay, 0);
    }

    public static void addJitter(FlatBufferBuilder builder, long jitter) {
        builder.addInt(3, (int) jitter, 0);
    }

    public static void addMeasuredperiod(FlatBufferBuilder builder, long measuredperiod) {
        builder.addInt(4, (int) measuredperiod, 0);
    }

    public static void addDirection(FlatBufferBuilder builder, long direction) {
        builder.addInt(5, (int) direction, 0);
    }

    public static int endRequestRtpStatsToStack(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
