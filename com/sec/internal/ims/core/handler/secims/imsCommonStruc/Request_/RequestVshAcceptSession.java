package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestVshAcceptSession extends Table {
    public static RequestVshAcceptSession getRootAsRequestVshAcceptSession(ByteBuffer _bb) {
        return getRootAsRequestVshAcceptSession(_bb, new RequestVshAcceptSession());
    }

    public static RequestVshAcceptSession getRootAsRequestVshAcceptSession(ByteBuffer _bb, RequestVshAcceptSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestVshAcceptSession __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestVshAcceptSession(FlatBufferBuilder builder, long session_id) {
        builder.startObject(1);
        addSessionId(builder, session_id);
        return endRequestVshAcceptSession(builder);
    }

    public static void startRequestVshAcceptSession(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static int endRequestVshAcceptSession(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
