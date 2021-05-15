package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestTransferCall extends Table {
    public static RequestTransferCall getRootAsRequestTransferCall(ByteBuffer _bb) {
        return getRootAsRequestTransferCall(_bb, new RequestTransferCall());
    }

    public static RequestTransferCall getRootAsRequestTransferCall(ByteBuffer _bb, RequestTransferCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestTransferCall __assign(int _i, ByteBuffer _bb) {
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

    public String targetUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer targetUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long replacingSession() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestTransferCall(FlatBufferBuilder builder, long handle, long session, int target_uriOffset, long replacing_session) {
        builder.startObject(4);
        addReplacingSession(builder, replacing_session);
        addTargetUri(builder, target_uriOffset);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestTransferCall(builder);
    }

    public static void startRequestTransferCall(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addTargetUri(FlatBufferBuilder builder, int targetUriOffset) {
        builder.addOffset(2, targetUriOffset, 0);
    }

    public static void addReplacingSession(FlatBufferBuilder builder, long replacingSession) {
        builder.addInt(3, (int) replacingSession, 0);
    }

    public static int endRequestTransferCall(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
