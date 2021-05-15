package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOptionsSendCmcCheckMsg extends Table {
    public static RequestOptionsSendCmcCheckMsg getRootAsRequestOptionsSendCmcCheckMsg(ByteBuffer _bb) {
        return getRootAsRequestOptionsSendCmcCheckMsg(_bb, new RequestOptionsSendCmcCheckMsg());
    }

    public static RequestOptionsSendCmcCheckMsg getRootAsRequestOptionsSendCmcCheckMsg(ByteBuffer _bb, RequestOptionsSendCmcCheckMsg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestOptionsSendCmcCheckMsg __assign(int _i, ByteBuffer _bb) {
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

    public String uri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestOptionsSendCmcCheckMsg(FlatBufferBuilder builder, long handle, int uriOffset) {
        builder.startObject(2);
        addUri(builder, uriOffset);
        addHandle(builder, handle);
        return endRequestOptionsSendCmcCheckMsg(builder);
    }

    public static void startRequestOptionsSendCmcCheckMsg(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static int endRequestOptionsSendCmcCheckMsg(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
