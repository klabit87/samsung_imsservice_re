package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ExtraHeader extends Table {
    public static ExtraHeader getRootAsExtraHeader(ByteBuffer _bb) {
        return getRootAsExtraHeader(_bb, new ExtraHeader());
    }

    public static ExtraHeader getRootAsExtraHeader(ByteBuffer _bb, ExtraHeader obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ExtraHeader __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public Pair pair(int j) {
        return pair(new Pair(), j);
    }

    public Pair pair(Pair obj, int j) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int pairLength() {
        int o = __offset(4);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createExtraHeader(FlatBufferBuilder builder, int pairOffset) {
        builder.startObject(1);
        addPair(builder, pairOffset);
        return endExtraHeader(builder);
    }

    public static void startExtraHeader(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addPair(FlatBufferBuilder builder, int pairOffset) {
        builder.addOffset(0, pairOffset, 0);
    }

    public static int createPairVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startPairVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endExtraHeader(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
