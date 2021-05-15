package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateTimeInPlani extends Table {
    public static RequestUpdateTimeInPlani getRootAsRequestUpdateTimeInPlani(ByteBuffer _bb) {
        return getRootAsRequestUpdateTimeInPlani(_bb, new RequestUpdateTimeInPlani());
    }

    public static RequestUpdateTimeInPlani getRootAsRequestUpdateTimeInPlani(ByteBuffer _bb, RequestUpdateTimeInPlani obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateTimeInPlani __assign(int _i, ByteBuffer _bb) {
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

    public long time() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestUpdateTimeInPlani(FlatBufferBuilder builder, long handle, long time) {
        builder.startObject(2);
        addTime(builder, time);
        addHandle(builder, handle);
        return endRequestUpdateTimeInPlani(builder);
    }

    public static void startRequestUpdateTimeInPlani(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addTime(FlatBufferBuilder builder, long time) {
        builder.addLong(1, time, 0);
    }

    public static int endRequestUpdateTimeInPlani(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
