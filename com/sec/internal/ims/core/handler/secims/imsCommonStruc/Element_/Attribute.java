package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Attribute extends Table {
    public static Attribute getRootAsAttribute(ByteBuffer _bb) {
        return getRootAsAttribute(_bb, new Attribute());
    }

    public static Attribute getRootAsAttribute(ByteBuffer _bb, Attribute obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Attribute __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String name() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
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

    public String nameSpace() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameSpaceAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createAttribute(FlatBufferBuilder builder, int nameOffset, int valueOffset, int name_spaceOffset) {
        builder.startObject(3);
        addNameSpace(builder, name_spaceOffset);
        addValue(builder, valueOffset);
        addName(builder, nameOffset);
        return endAttribute(builder);
    }

    public static void startAttribute(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addValue(FlatBufferBuilder builder, int valueOffset) {
        builder.addOffset(1, valueOffset, 0);
    }

    public static void addNameSpace(FlatBufferBuilder builder, int nameSpaceOffset) {
        builder.addOffset(2, nameSpaceOffset, 0);
    }

    public static int endAttribute(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
