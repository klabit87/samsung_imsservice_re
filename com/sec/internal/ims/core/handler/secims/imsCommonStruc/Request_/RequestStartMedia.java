package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartMedia extends Table {
    public static RequestStartMedia getRootAsRequestStartMedia(ByteBuffer _bb) {
        return getRootAsRequestStartMedia(_bb, new RequestStartMedia());
    }

    public static RequestStartMedia getRootAsRequestStartMedia(ByteBuffer _bb, RequestStartMedia obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartMedia __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestStartMedia(FlatBufferBuilder builder, long session_id) {
        builder.startObject(1);
        addSessionId(builder, session_id);
        return endRequestStartMedia(builder);
    }

    public static void startRequestStartMedia(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static int endRequestStartMedia(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
