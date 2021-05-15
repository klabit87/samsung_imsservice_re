package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptCall extends Table {
    public static RequestAcceptCall getRootAsRequestAcceptCall(ByteBuffer _bb) {
        return getRootAsRequestAcceptCall(_bb, new RequestAcceptCall());
    }

    public static RequestAcceptCall getRootAsRequestAcceptCall(ByteBuffer _bb, RequestAcceptCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestAcceptCall __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long session() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int callType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String cmcCallTime() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcCallTimeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestAcceptCall(FlatBufferBuilder builder, long session, int call_type, int cmc_call_timeOffset) {
        builder.startObject(3);
        addCmcCallTime(builder, cmc_call_timeOffset);
        addCallType(builder, call_type);
        addSession(builder, session);
        return endRequestAcceptCall(builder);
    }

    public static void startRequestAcceptCall(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(1, callType, 0);
    }

    public static void addCmcCallTime(FlatBufferBuilder builder, int cmcCallTimeOffset) {
        builder.addOffset(2, cmcCallTimeOffset, 0);
    }

    public static int endRequestAcceptCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
