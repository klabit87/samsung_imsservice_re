package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateRtpMsg extends Table {
    public static EcholocateRtpMsg getRootAsEcholocateRtpMsg(ByteBuffer _bb) {
        return getRootAsEcholocateRtpMsg(_bb, new EcholocateRtpMsg());
    }

    public static EcholocateRtpMsg getRootAsEcholocateRtpMsg(ByteBuffer _bb, EcholocateRtpMsg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EcholocateRtpMsg __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String dir() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dirAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String id() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String lossrate() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer lossrateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String delay() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer delayAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String jitter() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer jitterAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String measuredperiod() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer measuredperiodAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String nwstate() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nwstateAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createEcholocateRtpMsg(FlatBufferBuilder builder, int dirOffset, int idOffset, int lossrateOffset, int delayOffset, int jitterOffset, int measuredperiodOffset, int nwstateOffset) {
        builder.startObject(7);
        addNwstate(builder, nwstateOffset);
        addMeasuredperiod(builder, measuredperiodOffset);
        addJitter(builder, jitterOffset);
        addDelay(builder, delayOffset);
        addLossrate(builder, lossrateOffset);
        addId(builder, idOffset);
        addDir(builder, dirOffset);
        return endEcholocateRtpMsg(builder);
    }

    public static void startEcholocateRtpMsg(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addDir(FlatBufferBuilder builder, int dirOffset) {
        builder.addOffset(0, dirOffset, 0);
    }

    public static void addId(FlatBufferBuilder builder, int idOffset) {
        builder.addOffset(1, idOffset, 0);
    }

    public static void addLossrate(FlatBufferBuilder builder, int lossrateOffset) {
        builder.addOffset(2, lossrateOffset, 0);
    }

    public static void addDelay(FlatBufferBuilder builder, int delayOffset) {
        builder.addOffset(3, delayOffset, 0);
    }

    public static void addJitter(FlatBufferBuilder builder, int jitterOffset) {
        builder.addOffset(4, jitterOffset, 0);
    }

    public static void addMeasuredperiod(FlatBufferBuilder builder, int measuredperiodOffset) {
        builder.addOffset(5, measuredperiodOffset, 0);
    }

    public static void addNwstate(FlatBufferBuilder builder, int nwstateOffset) {
        builder.addOffset(6, nwstateOffset, 0);
    }

    public static int endEcholocateRtpMsg(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 14);
        builder.required(o, 16);
        return o;
    }
}
