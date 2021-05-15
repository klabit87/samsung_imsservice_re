package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextDataEvent extends Table {
    public static TextDataEvent getRootAsTextDataEvent(ByteBuffer _bb) {
        return getRootAsTextDataEvent(_bb, new TextDataEvent());
    }

    public static TextDataEvent getRootAsTextDataEvent(ByteBuffer _bb, TextDataEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public TextDataEvent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String text() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long len() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createTextDataEvent(FlatBufferBuilder builder, int textOffset, long len) {
        builder.startObject(2);
        addLen(builder, len);
        addText(builder, textOffset);
        return endTextDataEvent(builder);
    }

    public static void startTextDataEvent(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(0, textOffset, 0);
    }

    public static void addLen(FlatBufferBuilder builder, long len) {
        builder.addInt(1, (int) len, 0);
    }

    public static int endTextDataEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
