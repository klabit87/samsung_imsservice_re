package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateCommonConfig extends Table {
    public static RequestUpdateCommonConfig getRootAsRequestUpdateCommonConfig(ByteBuffer _bb) {
        return getRootAsRequestUpdateCommonConfig(_bb, new RequestUpdateCommonConfig());
    }

    public static RequestUpdateCommonConfig getRootAsRequestUpdateCommonConfig(ByteBuffer _bb, RequestUpdateCommonConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateCommonConfig __assign(int _i, ByteBuffer _bb) {
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

    public byte configType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table config(Table obj) {
        int o = __offset(8);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createRequestUpdateCommonConfig(FlatBufferBuilder builder, long phone_id, byte config_type, int configOffset) {
        builder.startObject(3);
        addConfig(builder, configOffset);
        addPhoneId(builder, phone_id);
        addConfigType(builder, config_type);
        return endRequestUpdateCommonConfig(builder);
    }

    public static void startRequestUpdateCommonConfig(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addConfigType(FlatBufferBuilder builder, byte configType) {
        builder.addByte(1, configType, 0);
    }

    public static void addConfig(FlatBufferBuilder builder, int configOffset) {
        builder.addOffset(2, configOffset, 0);
    }

    public static int endRequestUpdateCommonConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
