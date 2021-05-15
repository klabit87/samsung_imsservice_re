package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IshIncomingSession extends Table {
    public static IshIncomingSession getRootAsIshIncomingSession(ByteBuffer _bb) {
        return getRootAsIshIncomingSession(_bb, new IshIncomingSession());
    }

    public static IshIncomingSession getRootAsIshIncomingSession(ByteBuffer _bb, IshIncomingSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public IshIncomingSession __assign(int _i, ByteBuffer _bb) {
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

    public String remoteUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String fileName() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer fileNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long size() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String contentType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public long userHandle() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createIshIncomingSession(FlatBufferBuilder builder, long session_id, int remote_uriOffset, int file_nameOffset, long size, int content_typeOffset, long user_handle) {
        builder.startObject(6);
        addUserHandle(builder, user_handle);
        addContentType(builder, content_typeOffset);
        addSize(builder, size);
        addFileName(builder, file_nameOffset);
        addRemoteUri(builder, remote_uriOffset);
        addSessionId(builder, session_id);
        return endIshIncomingSession(builder);
    }

    public static void startIshIncomingSession(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(1, remoteUriOffset, 0);
    }

    public static void addFileName(FlatBufferBuilder builder, int fileNameOffset) {
        builder.addOffset(2, fileNameOffset, 0);
    }

    public static void addSize(FlatBufferBuilder builder, long size) {
        builder.addInt(3, (int) size, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(4, contentTypeOffset, 0);
    }

    public static void addUserHandle(FlatBufferBuilder builder, long userHandle) {
        builder.addInt(5, (int) userHandle, 0);
    }

    public static int endIshIncomingSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
