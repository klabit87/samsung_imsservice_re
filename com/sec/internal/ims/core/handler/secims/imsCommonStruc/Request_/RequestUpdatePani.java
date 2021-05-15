package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdatePani extends Table {
    public static RequestUpdatePani getRootAsRequestUpdatePani(ByteBuffer _bb) {
        return getRootAsRequestUpdatePani(_bb, new RequestUpdatePani());
    }

    public static RequestUpdatePani getRootAsRequestUpdatePani(ByteBuffer _bb, RequestUpdatePani obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdatePani __assign(int _i, ByteBuffer _bb) {
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

    public String pani() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer paniAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String lastPani() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer lastPaniAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestUpdatePani(FlatBufferBuilder builder, long handle, int paniOffset, int last_paniOffset) {
        builder.startObject(3);
        addLastPani(builder, last_paniOffset);
        addPani(builder, paniOffset);
        addHandle(builder, handle);
        return endRequestUpdatePani(builder);
    }

    public static void startRequestUpdatePani(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addPani(FlatBufferBuilder builder, int paniOffset) {
        builder.addOffset(1, paniOffset, 0);
    }

    public static void addLastPani(FlatBufferBuilder builder, int lastPaniOffset) {
        builder.addOffset(2, lastPaniOffset, 0);
    }

    public static int endRequestUpdatePani(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
