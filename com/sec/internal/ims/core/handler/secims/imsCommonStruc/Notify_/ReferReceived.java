package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReferReceived extends Table {
    public static ReferReceived getRootAsReferReceived(ByteBuffer _bb) {
        return getRootAsReferReceived(_bb, new ReferReceived());
    }

    public static ReferReceived getRootAsReferReceived(ByteBuffer _bb, ReferReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReferReceived __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String referorUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer referorUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String targetUri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer targetUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createReferReceived(FlatBufferBuilder builder, long handle, long session, int referor_uriOffset, int target_uriOffset) {
        builder.startObject(4);
        addTargetUri(builder, target_uriOffset);
        addReferorUri(builder, referor_uriOffset);
        addSession(builder, session);
        addHandle(builder, handle);
        return endReferReceived(builder);
    }

    public static void startReferReceived(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addReferorUri(FlatBufferBuilder builder, int referorUriOffset) {
        builder.addOffset(2, referorUriOffset, 0);
    }

    public static void addTargetUri(FlatBufferBuilder builder, int targetUriOffset) {
        builder.addOffset(3, targetUriOffset, 0);
    }

    public static int endReferReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        builder.required(o, 10);
        return o;
    }
}
