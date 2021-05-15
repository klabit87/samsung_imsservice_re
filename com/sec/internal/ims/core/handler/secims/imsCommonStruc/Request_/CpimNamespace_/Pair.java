package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.CpimNamespace_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Pair extends Table {
    public static Pair getRootAsPair(ByteBuffer _bb) {
        return getRootAsPair(_bb, new Pair());
    }

    public static Pair getRootAsPair(ByteBuffer _bb, Pair obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Pair __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String key() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer keyAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String value() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createPair(FlatBufferBuilder builder, int keyOffset, int valueOffset) {
        builder.startObject(2);
        addValue(builder, valueOffset);
        addKey(builder, keyOffset);
        return endPair(builder);
    }

    public static void startPair(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addKey(FlatBufferBuilder builder, int keyOffset) {
        builder.addOffset(0, keyOffset, 0);
    }

    public static void addValue(FlatBufferBuilder builder, int valueOffset) {
        builder.addOffset(1, valueOffset, 0);
    }

    public static int endPair(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
