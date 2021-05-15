package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XqContent extends Table {
    public static XqContent getRootAsXqContent(ByteBuffer _bb) {
        return getRootAsXqContent(_bb, new XqContent());
    }

    public static XqContent getRootAsXqContent(ByteBuffer _bb, XqContent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XqContent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int type() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long intVal() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String strVal() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer strValAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createXqContent(FlatBufferBuilder builder, int type, long intVal, int strValOffset) {
        builder.startObject(3);
        addStrVal(builder, strValOffset);
        addIntVal(builder, intVal);
        addType(builder, type);
        return endXqContent(builder);
    }

    public static void startXqContent(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addType(FlatBufferBuilder builder, int type) {
        builder.addInt(0, type, 0);
    }

    public static void addIntVal(FlatBufferBuilder builder, long intVal) {
        builder.addInt(1, (int) intVal, 0);
    }

    public static void addStrVal(FlatBufferBuilder builder, int strValOffset) {
        builder.addOffset(2, strValOffset, 0);
    }

    public static int endXqContent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
