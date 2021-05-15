package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.CpimNamespace_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CpimNamespace extends Table {
    public static CpimNamespace getRootAsCpimNamespace(ByteBuffer _bb) {
        return getRootAsCpimNamespace(_bb, new CpimNamespace());
    }

    public static CpimNamespace getRootAsCpimNamespace(ByteBuffer _bb, CpimNamespace obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CpimNamespace __assign(int _i, ByteBuffer _bb) {
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

    public Pair headers(int j) {
        return headers(new Pair(), j);
    }

    public Pair headers(Pair obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int headersLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createCpimNamespace(FlatBufferBuilder builder, int nameOffset, int uriOffset, int headersOffset) {
        builder.startObject(3);
        addHeaders(builder, headersOffset);
        addUri(builder, uriOffset);
        addName(builder, nameOffset);
        return endCpimNamespace(builder);
    }

    public static void startCpimNamespace(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addHeaders(FlatBufferBuilder builder, int headersOffset) {
        builder.addOffset(2, headersOffset, 0);
    }

    public static int createHeadersVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startHeadersVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endCpimNamespace(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
