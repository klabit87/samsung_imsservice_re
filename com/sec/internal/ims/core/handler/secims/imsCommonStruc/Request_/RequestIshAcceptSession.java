package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestIshAcceptSession extends Table {
    public static RequestIshAcceptSession getRootAsRequestIshAcceptSession(ByteBuffer _bb) {
        return getRootAsRequestIshAcceptSession(_bb, new RequestIshAcceptSession());
    }

    public static RequestIshAcceptSession getRootAsRequestIshAcceptSession(ByteBuffer _bb, RequestIshAcceptSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestIshAcceptSession __assign(int _i, ByteBuffer _bb) {
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

    public String filePath() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer filePathAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestIshAcceptSession(FlatBufferBuilder builder, long session_id, int file_pathOffset) {
        builder.startObject(2);
        addFilePath(builder, file_pathOffset);
        addSessionId(builder, session_id);
        return endRequestIshAcceptSession(builder);
    }

    public static void startRequestIshAcceptSession(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addFilePath(FlatBufferBuilder builder, int filePathOffset) {
        builder.addOffset(1, filePathOffset, 0);
    }

    public static int endRequestIshAcceptSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
