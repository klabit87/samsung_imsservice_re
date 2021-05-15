package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateSimInfo extends Table {
    public static RequestUpdateSimInfo getRootAsRequestUpdateSimInfo(ByteBuffer _bb) {
        return getRootAsRequestUpdateSimInfo(_bb, new RequestUpdateSimInfo());
    }

    public static RequestUpdateSimInfo getRootAsRequestUpdateSimInfo(ByteBuffer _bb, RequestUpdateSimInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateSimInfo __assign(int _i, ByteBuffer _bb) {
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

    public long simInfo() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateSimInfo(FlatBufferBuilder builder, long phone_id, long sim_info) {
        builder.startObject(2);
        addSimInfo(builder, sim_info);
        addPhoneId(builder, phone_id);
        return endRequestUpdateSimInfo(builder);
    }

    public static void startRequestUpdateSimInfo(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addSimInfo(FlatBufferBuilder builder, long simInfo) {
        builder.addInt(1, (int) simInfo, 0);
    }

    public static int endRequestUpdateSimInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
