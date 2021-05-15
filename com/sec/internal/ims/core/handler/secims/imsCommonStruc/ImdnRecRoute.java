package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImdnRecRoute extends Table {
    public static ImdnRecRoute getRootAsImdnRecRoute(ByteBuffer _bb) {
        return getRootAsImdnRecRoute(_bb, new ImdnRecRoute());
    }

    public static ImdnRecRoute getRootAsImdnRecRoute(ByteBuffer _bb, ImdnRecRoute obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImdnRecRoute __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String name() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createImdnRecRoute(FlatBufferBuilder builder, int uriOffset, int nameOffset) {
        builder.startObject(2);
        addName(builder, nameOffset);
        addUri(builder, uriOffset);
        return endImdnRecRoute(builder);
    }

    public static void startImdnRecRoute(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(1, nameOffset, 0);
    }

    public static int endImdnRecRoute(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
