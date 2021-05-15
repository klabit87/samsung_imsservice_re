package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateRat extends Table {
    public static RequestUpdateRat getRootAsRequestUpdateRat(ByteBuffer _bb) {
        return getRootAsRequestUpdateRat(_bb, new RequestUpdateRat());
    }

    public static RequestUpdateRat getRootAsRequestUpdateRat(ByteBuffer _bb, RequestUpdateRat obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateRat __assign(int _i, ByteBuffer _bb) {
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

    public long rat() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateRat(FlatBufferBuilder builder, long handle, long rat) {
        builder.startObject(2);
        addRat(builder, rat);
        addHandle(builder, handle);
        return endRequestUpdateRat(builder);
    }

    public static void startRequestUpdateRat(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addRat(FlatBufferBuilder builder, long rat) {
        builder.addInt(1, (int) rat, 0);
    }

    public static int endRequestUpdateRat(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
