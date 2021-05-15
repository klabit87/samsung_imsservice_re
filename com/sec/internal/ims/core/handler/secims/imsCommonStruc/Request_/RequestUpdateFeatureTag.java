package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateFeatureTag extends Table {
    public static RequestUpdateFeatureTag getRootAsRequestUpdateFeatureTag(ByteBuffer _bb) {
        return getRootAsRequestUpdateFeatureTag(_bb, new RequestUpdateFeatureTag());
    }

    public static RequestUpdateFeatureTag getRootAsRequestUpdateFeatureTag(ByteBuffer _bb, RequestUpdateFeatureTag obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateFeatureTag __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int featureTagList(int j) {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int featureTagListLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer featureTagListAsByteBuffer() {
        return __vector_as_bytebuffer(6, 4);
    }

    public static int createRequestUpdateFeatureTag(FlatBufferBuilder builder, long handle, int feature_tag_listOffset) {
        builder.startObject(2);
        addFeatureTagList(builder, feature_tag_listOffset);
        addHandle(builder, handle);
        return endRequestUpdateFeatureTag(builder);
    }

    public static void startRequestUpdateFeatureTag(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addFeatureTagList(FlatBufferBuilder builder, int featureTagListOffset) {
        builder.addOffset(1, featureTagListOffset, 0);
    }

    public static int createFeatureTagListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startFeatureTagListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endRequestUpdateFeatureTag(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
