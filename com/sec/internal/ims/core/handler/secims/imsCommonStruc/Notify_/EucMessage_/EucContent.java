package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EucContent extends Table {
    public static EucContent getRootAsEucContent(ByteBuffer _bb) {
        return getRootAsEucContent(_bb, new EucContent());
    }

    public static EucContent getRootAsEucContent(ByteBuffer _bb, EucContent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EucContent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public TextLangPair texts(int j) {
        return texts(new TextLangPair(), j);
    }

    public TextLangPair texts(TextLangPair obj, int j) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int textsLength() {
        int o = __offset(4);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public TextLangPair subjects(int j) {
        return subjects(new TextLangPair(), j);
    }

    public TextLangPair subjects(TextLangPair obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int subjectsLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createEucContent(FlatBufferBuilder builder, int textsOffset, int subjectsOffset) {
        builder.startObject(2);
        addSubjects(builder, subjectsOffset);
        addTexts(builder, textsOffset);
        return endEucContent(builder);
    }

    public static void startEucContent(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addTexts(FlatBufferBuilder builder, int textsOffset) {
        builder.addOffset(0, textsOffset, 0);
    }

    public static int createTextsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startTextsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addSubjects(FlatBufferBuilder builder, int subjectsOffset) {
        builder.addOffset(1, subjectsOffset, 0);
    }

    public static int createSubjectsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startSubjectsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endEucContent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
