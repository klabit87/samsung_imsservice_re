package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AdditionalContents extends Table {
    public static AdditionalContents getRootAsAdditionalContents(ByteBuffer _bb) {
        return getRootAsAdditionalContents(_bb, new AdditionalContents());
    }

    public static AdditionalContents getRootAsAdditionalContents(ByteBuffer _bb, AdditionalContents obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public AdditionalContents __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String mimeType() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer mimeTypeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String contents() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentsAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int rawContents(int j) {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.get(__vector(o) + (j * 1)) & 255;
        }
        return 0;
    }

    public int rawContentsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer rawContentsAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createAdditionalContents(FlatBufferBuilder builder, int mime_typeOffset, int contentsOffset, int raw_contentsOffset) {
        builder.startObject(3);
        addRawContents(builder, raw_contentsOffset);
        addContents(builder, contentsOffset);
        addMimeType(builder, mime_typeOffset);
        return endAdditionalContents(builder);
    }

    public static void startAdditionalContents(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addMimeType(FlatBufferBuilder builder, int mimeTypeOffset) {
        builder.addOffset(0, mimeTypeOffset, 0);
    }

    public static void addContents(FlatBufferBuilder builder, int contentsOffset) {
        builder.addOffset(1, contentsOffset, 0);
    }

    public static void addRawContents(FlatBufferBuilder builder, int rawContentsOffset) {
        builder.addOffset(2, rawContentsOffset, 0);
    }

    public static int createRawContentsVector(FlatBufferBuilder builder, byte[] data) {
        builder.startVector(1, data.length, 1);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addByte(data[i]);
        }
        return builder.endVector();
    }

    public static void startRawContentsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(1, numElems, 1);
    }

    public static int endAdditionalContents(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
