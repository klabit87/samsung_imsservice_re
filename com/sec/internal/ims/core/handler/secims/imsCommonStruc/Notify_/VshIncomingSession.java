package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VshIncomingSession extends Table {
    public static VshIncomingSession getRootAsVshIncomingSession(ByteBuffer _bb) {
        return getRootAsVshIncomingSession(_bb, new VshIncomingSession());
    }

    public static VshIncomingSession getRootAsVshIncomingSession(ByteBuffer _bb, VshIncomingSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public VshIncomingSession __assign(int _i, ByteBuffer _bb) {
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

    public int type() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String fileName() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer fileNameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long userHandle() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createVshIncomingSession(FlatBufferBuilder builder, long session_id, int remote_uriOffset, int type, int file_nameOffset, long user_handle) {
        builder.startObject(5);
        addUserHandle(builder, user_handle);
        addFileName(builder, file_nameOffset);
        addType(builder, type);
        addRemoteUri(builder, remote_uriOffset);
        addSessionId(builder, session_id);
        return endVshIncomingSession(builder);
    }

    public static void startVshIncomingSession(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(1, remoteUriOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int type) {
        builder.addInt(2, type, 0);
    }

    public static void addFileName(FlatBufferBuilder builder, int fileNameOffset) {
        builder.addOffset(3, fileNameOffset, 0);
    }

    public static void addUserHandle(FlatBufferBuilder builder, long userHandle) {
        builder.addInt(4, (int) userHandle, 0);
    }

    public static int endVshIncomingSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
