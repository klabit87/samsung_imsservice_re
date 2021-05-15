package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestVshStopSession extends Table {
    public static RequestVshStopSession getRootAsRequestVshStopSession(ByteBuffer _bb) {
        return getRootAsRequestVshStopSession(_bb, new RequestVshStopSession());
    }

    public static RequestVshStopSession getRootAsRequestVshStopSession(ByteBuffer _bb, RequestVshStopSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestVshStopSession __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestVshStopSession(FlatBufferBuilder builder, long session_id) {
        builder.startObject(1);
        addSessionId(builder, session_id);
        return endRequestVshStopSession(builder);
    }

    public static void startRequestVshStopSession(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static int endRequestVshStopSession(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
