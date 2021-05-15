package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendSip extends Table {
    public static RequestSendSip getRootAsRequestSendSip(ByteBuffer _bb) {
        return getRootAsRequestSendSip(_bb, new RequestSendSip());
    }

    public static RequestSendSip getRootAsRequestSendSip(ByteBuffer _bb, RequestSendSip obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendSip __assign(int _i, ByteBuffer _bb) {
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

    public String sipMessage() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipMessageAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSendSip(FlatBufferBuilder builder, long handle, int sip_messageOffset) {
        builder.startObject(2);
        addSipMessage(builder, sip_messageOffset);
        addHandle(builder, handle);
        return endRequestSendSip(builder);
    }

    public static void startRequestSendSip(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSipMessage(FlatBufferBuilder builder, int sipMessageOffset) {
        builder.addOffset(1, sipMessageOffset, 0);
    }

    public static int endRequestSendSip(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
