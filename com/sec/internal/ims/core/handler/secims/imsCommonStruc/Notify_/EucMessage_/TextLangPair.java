package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextLangPair extends Table {
    public static TextLangPair getRootAsTextLangPair(ByteBuffer _bb) {
        return getRootAsTextLangPair(_bb, new TextLangPair());
    }

    public static TextLangPair getRootAsTextLangPair(ByteBuffer _bb, TextLangPair obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public TextLangPair __assign(int _i, ByteBuffer _bb) {
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

    public String lang() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer langAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createTextLangPair(FlatBufferBuilder builder, int textOffset, int langOffset) {
        builder.startObject(2);
        addLang(builder, langOffset);
        addText(builder, textOffset);
        return endTextLangPair(builder);
    }

    public static void startTextLangPair(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(0, textOffset, 0);
    }

    public static void addLang(FlatBufferBuilder builder, int langOffset) {
        builder.addOffset(1, langOffset, 0);
    }

    public static int endTextLangPair(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
