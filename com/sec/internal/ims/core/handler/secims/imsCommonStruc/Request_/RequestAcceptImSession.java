package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptImSession extends Table {
    public static RequestAcceptImSession getRootAsRequestAcceptImSession(ByteBuffer _bb) {
        return getRootAsRequestAcceptImSession(_bb, new RequestAcceptImSession());
    }

    public static RequestAcceptImSession getRootAsRequestAcceptImSession(ByteBuffer _bb, RequestAcceptImSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestAcceptImSession __assign(int _i, ByteBuffer _bb) {
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

    public String userAlias() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestAcceptImSession(FlatBufferBuilder builder, long session_id, int user_aliasOffset) {
        builder.startObject(2);
        addUserAlias(builder, user_aliasOffset);
        addSessionId(builder, session_id);
        return endRequestAcceptImSession(builder);
    }

    public static void startRequestAcceptImSession(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(1, userAliasOffset, 0);
    }

    public static int endRequestAcceptImSession(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
