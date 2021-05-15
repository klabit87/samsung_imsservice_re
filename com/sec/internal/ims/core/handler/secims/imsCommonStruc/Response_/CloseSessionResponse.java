package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CloseSessionResponse extends Table {
    public static CloseSessionResponse getRootAsCloseSessionResponse(ByteBuffer _bb) {
        return getRootAsCloseSessionResponse(_bb, new CloseSessionResponse());
    }

    public static CloseSessionResponse getRootAsCloseSessionResponse(ByteBuffer _bb, CloseSessionResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CloseSessionResponse __assign(int _i, ByteBuffer _bb) {
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

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createCloseSessionResponse(FlatBufferBuilder builder, long session_handle, int im_errorOffset) {
        builder.startObject(2);
        addImError(builder, im_errorOffset);
        addSessionHandle(builder, session_handle);
        return endCloseSessionResponse(builder);
    }

    public static void startCloseSessionResponse(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static int endCloseSessionResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
