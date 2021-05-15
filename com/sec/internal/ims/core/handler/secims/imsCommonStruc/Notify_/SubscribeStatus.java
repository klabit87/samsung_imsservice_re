package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubscribeStatus extends Table {
    public static SubscribeStatus getRootAsSubscribeStatus(ByteBuffer _bb) {
        return getRootAsSubscribeStatus(_bb, new SubscribeStatus());
    }

    public static SubscribeStatus getRootAsSubscribeStatus(ByteBuffer _bb, SubscribeStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SubscribeStatus __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long respCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String respReason() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer respReasonAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSubscribeStatus(FlatBufferBuilder builder, long handle, long resp_code, int resp_reasonOffset) {
        builder.startObject(3);
        addRespReason(builder, resp_reasonOffset);
        addRespCode(builder, resp_code);
        addHandle(builder, handle);
        return endSubscribeStatus(builder);
    }

    public static void startSubscribeStatus(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addRespCode(FlatBufferBuilder builder, long respCode) {
        builder.addInt(1, (int) respCode, 0);
    }

    public static void addRespReason(FlatBufferBuilder builder, int respReasonOffset) {
        builder.addOffset(2, respReasonOffset, 0);
    }

    public static int endSubscribeStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
