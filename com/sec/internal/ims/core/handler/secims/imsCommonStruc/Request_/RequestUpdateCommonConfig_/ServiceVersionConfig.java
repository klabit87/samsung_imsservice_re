package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ServiceVersionConfig extends Table {
    public static ServiceVersionConfig getRootAsServiceVersionConfig(ByteBuffer _bb) {
        return getRootAsServiceVersionConfig(_bb, new ServiceVersionConfig());
    }

    public static ServiceVersionConfig getRootAsServiceVersionConfig(ByteBuffer _bb, ServiceVersionConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ServiceVersionConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public ExtraHeader extraHeaders() {
        return extraHeaders(new ExtraHeader());
    }

    public ExtraHeader extraHeaders(ExtraHeader obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createServiceVersionConfig(FlatBufferBuilder builder, int extra_headersOffset) {
        builder.startObject(1);
        addExtraHeaders(builder, extra_headersOffset);
        return endServiceVersionConfig(builder);
    }

    public static void startServiceVersionConfig(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addExtraHeaders(FlatBufferBuilder builder, int extraHeadersOffset) {
        builder.addOffset(0, extraHeadersOffset, 0);
    }

    public static int endServiceVersionConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
