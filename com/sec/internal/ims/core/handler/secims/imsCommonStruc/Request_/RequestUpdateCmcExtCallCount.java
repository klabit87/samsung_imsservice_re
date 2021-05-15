package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateCmcExtCallCount extends Table {
    public static RequestUpdateCmcExtCallCount getRootAsRequestUpdateCmcExtCallCount(ByteBuffer _bb) {
        return getRootAsRequestUpdateCmcExtCallCount(_bb, new RequestUpdateCmcExtCallCount());
    }

    public static RequestUpdateCmcExtCallCount getRootAsRequestUpdateCmcExtCallCount(ByteBuffer _bb, RequestUpdateCmcExtCallCount obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateCmcExtCallCount __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long phoneId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long callCount() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateCmcExtCallCount(FlatBufferBuilder builder, long phone_id, long call_count) {
        builder.startObject(2);
        addCallCount(builder, call_count);
        addPhoneId(builder, phone_id);
        return endRequestUpdateCmcExtCallCount(builder);
    }

    public static void startRequestUpdateCmcExtCallCount(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addCallCount(FlatBufferBuilder builder, long callCount) {
        builder.addInt(1, (int) callCount, 0);
    }

    public static int endRequestUpdateCmcExtCallCount(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
