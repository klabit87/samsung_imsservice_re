package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestCapabilityExchange extends Table {
    public static RequestCapabilityExchange getRootAsRequestCapabilityExchange(ByteBuffer _bb) {
        return getRootAsRequestCapabilityExchange(_bb, new RequestCapabilityExchange());
    }

    public static RequestCapabilityExchange getRootAsRequestCapabilityExchange(ByteBuffer _bb, RequestCapabilityExchange obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestCapabilityExchange __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestCapabilityExchange(FlatBufferBuilder builder, long handle, int uriOffset) {
        builder.startObject(2);
        addUri(builder, uriOffset);
        addHandle(builder, handle);
        return endRequestCapabilityExchange(builder);
    }

    public static void startRequestCapabilityExchange(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static int endRequestCapabilityExchange(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
