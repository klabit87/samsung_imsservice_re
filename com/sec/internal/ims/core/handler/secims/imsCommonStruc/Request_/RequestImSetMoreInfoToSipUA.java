package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestImSetMoreInfoToSipUA extends Table {
    public static RequestImSetMoreInfoToSipUA getRootAsRequestImSetMoreInfoToSipUA(ByteBuffer _bb) {
        return getRootAsRequestImSetMoreInfoToSipUA(_bb, new RequestImSetMoreInfoToSipUA());
    }

    public static RequestImSetMoreInfoToSipUA getRootAsRequestImSetMoreInfoToSipUA(ByteBuffer _bb, RequestImSetMoreInfoToSipUA obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestImSetMoreInfoToSipUA __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String value() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createRequestImSetMoreInfoToSipUA(FlatBufferBuilder builder, int valueOffset) {
        builder.startObject(1);
        addValue(builder, valueOffset);
        return endRequestImSetMoreInfoToSipUA(builder);
    }

    public static void startRequestImSetMoreInfoToSipUA(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addValue(FlatBufferBuilder builder, int valueOffset) {
        builder.addOffset(0, valueOffset, 0);
    }

    public static int endRequestImSetMoreInfoToSipUA(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
