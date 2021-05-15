package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.XdmGeneralResponse_.Pair;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmGeneralResponse extends Table {
    public static XdmGeneralResponse getRootAsXdmGeneralResponse(ByteBuffer _bb) {
        return getRootAsXdmGeneralResponse(_bb, new XdmGeneralResponse());
    }

    public static XdmGeneralResponse getRootAsXdmGeneralResponse(ByteBuffer _bb, XdmGeneralResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XdmGeneralResponse __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean success() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String reason() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int statusCode() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String etag() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String retryAfter() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer retryAfterAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public Pair result(int j) {
        return result(new Pair(), j);
    }

    public Pair result(Pair obj, int j) {
        int o = __offset(16);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int resultLength() {
        int o = __offset(16);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createXdmGeneralResponse(FlatBufferBuilder builder, long rid, boolean success, int reasonOffset, int status_code, int etagOffset, int retry_afterOffset, int resultOffset) {
        builder.startObject(7);
        addResult(builder, resultOffset);
        addRetryAfter(builder, retry_afterOffset);
        addEtag(builder, etagOffset);
        addStatusCode(builder, status_code);
        addReason(builder, reasonOffset);
        addRid(builder, rid);
        addSuccess(builder, success);
        return endXdmGeneralResponse(builder);
    }

    public static void startXdmGeneralResponse(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addSuccess(FlatBufferBuilder builder, boolean success) {
        builder.addBoolean(1, success, false);
    }

    public static void addReason(FlatBufferBuilder builder, int reasonOffset) {
        builder.addOffset(2, reasonOffset, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, int statusCode) {
        builder.addInt(3, statusCode, 0);
    }

    public static void addEtag(FlatBufferBuilder builder, int etagOffset) {
        builder.addOffset(4, etagOffset, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, int retryAfterOffset) {
        builder.addOffset(5, retryAfterOffset, 0);
    }

    public static void addResult(FlatBufferBuilder builder, int resultOffset) {
        builder.addOffset(6, resultOffset, 0);
    }

    public static int createResultVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startResultVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endXdmGeneralResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
