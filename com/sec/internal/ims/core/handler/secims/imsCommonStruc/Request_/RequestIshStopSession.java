package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestIshStopSession extends Table {
    public static RequestIshStopSession getRootAsRequestIshStopSession(ByteBuffer _bb) {
        return getRootAsRequestIshStopSession(_bb, new RequestIshStopSession());
    }

    public static RequestIshStopSession getRootAsRequestIshStopSession(ByteBuffer _bb, RequestIshStopSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestIshStopSession __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int reason() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestIshStopSession(FlatBufferBuilder builder, long session_id, int reason) {
        builder.startObject(2);
        addReason(builder, reason);
        addSessionId(builder, session_id);
        return endRequestIshStopSession(builder);
    }

    public static void startRequestIshStopSession(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(1, reason, 0);
    }

    public static int endRequestIshStopSession(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
