package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSetOwnCapabilities extends Table {
    public static RequestSetOwnCapabilities getRootAsRequestSetOwnCapabilities(ByteBuffer _bb) {
        return getRootAsRequestSetOwnCapabilities(_bb, new RequestSetOwnCapabilities());
    }

    public static RequestSetOwnCapabilities getRootAsRequestSetOwnCapabilities(ByteBuffer _bb, RequestSetOwnCapabilities obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSetOwnCapabilities __assign(int _i, ByteBuffer _bb) {
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

    public String serviceid(int j) {
        int o = __offset(6);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int serviceidLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createRequestSetOwnCapabilities(FlatBufferBuilder builder, long handle, int serviceidOffset) {
        builder.startObject(2);
        addServiceid(builder, serviceidOffset);
        addHandle(builder, handle);
        return endRequestSetOwnCapabilities(builder);
    }

    public static void startRequestSetOwnCapabilities(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addServiceid(FlatBufferBuilder builder, int serviceidOffset) {
        builder.addOffset(1, serviceidOffset, 0);
    }

    public static int createServiceidVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startServiceidVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endRequestSetOwnCapabilities(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
