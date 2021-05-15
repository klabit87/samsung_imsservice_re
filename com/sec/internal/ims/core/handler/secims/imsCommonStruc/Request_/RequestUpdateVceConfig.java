package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateVceConfig extends Table {
    public static RequestUpdateVceConfig getRootAsRequestUpdateVceConfig(ByteBuffer _bb) {
        return getRootAsRequestUpdateVceConfig(_bb, new RequestUpdateVceConfig());
    }

    public static RequestUpdateVceConfig getRootAsRequestUpdateVceConfig(ByteBuffer _bb, RequestUpdateVceConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateVceConfig __assign(int _i, ByteBuffer _bb) {
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

    public boolean vceConfig() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestUpdateVceConfig(FlatBufferBuilder builder, long handle, boolean vce_config) {
        builder.startObject(2);
        addHandle(builder, handle);
        addVceConfig(builder, vce_config);
        return endRequestUpdateVceConfig(builder);
    }

    public static void startRequestUpdateVceConfig(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addVceConfig(FlatBufferBuilder builder, boolean vceConfig) {
        builder.addBoolean(1, vceConfig, false);
    }

    public static int endRequestUpdateVceConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
