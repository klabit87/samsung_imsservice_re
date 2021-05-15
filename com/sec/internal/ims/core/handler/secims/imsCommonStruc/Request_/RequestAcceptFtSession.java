package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptFtSession extends Table {
    public static RequestAcceptFtSession getRootAsRequestAcceptFtSession(ByteBuffer _bb) {
        return getRootAsRequestAcceptFtSession(_bb, new RequestAcceptFtSession());
    }

    public static RequestAcceptFtSession getRootAsRequestAcceptFtSession(ByteBuffer _bb, RequestAcceptFtSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestAcceptFtSession __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long start() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public long end() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public String filePath() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer filePathAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String userAlias() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestAcceptFtSession(FlatBufferBuilder builder, long session_handle, long start, long end, int file_pathOffset, int user_aliasOffset) {
        builder.startObject(5);
        addEnd(builder, end);
        addStart(builder, start);
        addUserAlias(builder, user_aliasOffset);
        addFilePath(builder, file_pathOffset);
        addSessionHandle(builder, session_handle);
        return endRequestAcceptFtSession(builder);
    }

    public static void startRequestAcceptFtSession(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addStart(FlatBufferBuilder builder, long start) {
        builder.addLong(1, start, 0);
    }

    public static void addEnd(FlatBufferBuilder builder, long end) {
        builder.addLong(2, end, 0);
    }

    public static void addFilePath(FlatBufferBuilder builder, int filePathOffset) {
        builder.addOffset(3, filePathOffset, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(4, userAliasOffset, 0);
    }

    public static int endRequestAcceptFtSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        builder.required(o, 12);
        return o;
    }
}
