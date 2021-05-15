package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateXqEnable extends Table {
    public static RequestUpdateXqEnable getRootAsRequestUpdateXqEnable(ByteBuffer _bb) {
        return getRootAsRequestUpdateXqEnable(_bb, new RequestUpdateXqEnable());
    }

    public static RequestUpdateXqEnable getRootAsRequestUpdateXqEnable(ByteBuffer _bb, RequestUpdateXqEnable obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateXqEnable __assign(int _i, ByteBuffer _bb) {
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

    public boolean enable() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestUpdateXqEnable(FlatBufferBuilder builder, long phone_id, boolean enable) {
        builder.startObject(2);
        addPhoneId(builder, phone_id);
        addEnable(builder, enable);
        return endRequestUpdateXqEnable(builder);
    }

    public static void startRequestUpdateXqEnable(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addEnable(FlatBufferBuilder builder, boolean enable) {
        builder.addBoolean(1, enable, false);
    }

    public static int endRequestUpdateXqEnable(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
