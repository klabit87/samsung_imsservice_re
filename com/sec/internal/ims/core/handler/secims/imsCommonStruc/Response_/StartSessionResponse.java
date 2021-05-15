package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StartSessionResponse extends Table {
    public static StartSessionResponse getRootAsStartSessionResponse(ByteBuffer _bb) {
        return getRootAsStartSessionResponse(_bb, new StartSessionResponse());
    }

    public static StartSessionResponse getRootAsStartSessionResponse(ByteBuffer _bb, StartSessionResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public StartSessionResponse __assign(int _i, ByteBuffer _bb) {
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

    public String fwSessionId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer fwSessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createStartSessionResponse(FlatBufferBuilder builder, long session_handle, int fw_session_idOffset, int im_errorOffset) {
        builder.startObject(3);
        addImError(builder, im_errorOffset);
        addFwSessionId(builder, fw_session_idOffset);
        addSessionHandle(builder, session_handle);
        return endStartSessionResponse(builder);
    }

    public static void startStartSessionResponse(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addFwSessionId(FlatBufferBuilder builder, int fwSessionIdOffset) {
        builder.addOffset(1, fwSessionIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(2, imErrorOffset, 0);
    }

    public static int endStartSessionResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
