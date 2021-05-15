package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RtpLossRateNoti extends Table {
    public static RtpLossRateNoti getRootAsRtpLossRateNoti(ByteBuffer _bb) {
        return getRootAsRtpLossRateNoti(_bb, new RtpLossRateNoti());
    }

    public static RtpLossRateNoti getRootAsRtpLossRateNoti(ByteBuffer _bb, RtpLossRateNoti obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RtpLossRateNoti __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long interval() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public float lossrate() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getFloat(this.bb_pos + o);
        }
        return 0.0f;
    }

    public float jitter() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getFloat(this.bb_pos + o);
        }
        return 0.0f;
    }

    public long notification() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRtpLossRateNoti(FlatBufferBuilder builder, long interval, float lossrate, float jitter, long notification) {
        builder.startObject(4);
        addNotification(builder, notification);
        addJitter(builder, jitter);
        addLossrate(builder, lossrate);
        addInterval(builder, interval);
        return endRtpLossRateNoti(builder);
    }

    public static void startRtpLossRateNoti(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addInterval(FlatBufferBuilder builder, long interval) {
        builder.addInt(0, (int) interval, 0);
    }

    public static void addLossrate(FlatBufferBuilder builder, float lossrate) {
        builder.addFloat(1, lossrate, 0.0d);
    }

    public static void addJitter(FlatBufferBuilder builder, float jitter) {
        builder.addFloat(2, jitter, 0.0d);
    }

    public static void addNotification(FlatBufferBuilder builder, long notification) {
        builder.addInt(3, (int) notification, 0);
    }

    public static int endRtpLossRateNoti(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
