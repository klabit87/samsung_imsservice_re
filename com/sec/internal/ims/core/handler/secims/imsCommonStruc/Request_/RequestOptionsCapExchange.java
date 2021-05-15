package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOptionsCapExchange extends Table {
    public static RequestOptionsCapExchange getRootAsRequestOptionsCapExchange(ByteBuffer _bb) {
        return getRootAsRequestOptionsCapExchange(_bb, new RequestOptionsCapExchange());
    }

    public static RequestOptionsCapExchange getRootAsRequestOptionsCapExchange(ByteBuffer _bb, RequestOptionsCapExchange obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestOptionsCapExchange __assign(int _i, ByteBuffer _bb) {
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

    public int myFeatures(int j) {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int myFeaturesLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer myFeaturesAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public String extFeature() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer extFeatureAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestOptionsCapExchange(FlatBufferBuilder builder, long handle, int uriOffset, int my_featuresOffset, int extFeatureOffset) {
        builder.startObject(4);
        addExtFeature(builder, extFeatureOffset);
        addMyFeatures(builder, my_featuresOffset);
        addUri(builder, uriOffset);
        addHandle(builder, handle);
        return endRequestOptionsCapExchange(builder);
    }

    public static void startRequestOptionsCapExchange(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addMyFeatures(FlatBufferBuilder builder, int myFeaturesOffset) {
        builder.addOffset(2, myFeaturesOffset, 0);
    }

    public static int createMyFeaturesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startMyFeaturesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addExtFeature(FlatBufferBuilder builder, int extFeatureOffset) {
        builder.addOffset(3, extFeatureOffset, 0);
    }

    public static int endRequestOptionsCapExchange(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
