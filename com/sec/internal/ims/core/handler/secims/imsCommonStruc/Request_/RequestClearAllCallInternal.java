package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestClearAllCallInternal extends Table {
    public static RequestClearAllCallInternal getRootAsRequestClearAllCallInternal(ByteBuffer _bb) {
        return getRootAsRequestClearAllCallInternal(_bb, new RequestClearAllCallInternal());
    }

    public static RequestClearAllCallInternal getRootAsRequestClearAllCallInternal(ByteBuffer _bb, RequestClearAllCallInternal obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestClearAllCallInternal __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long cmcType() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestClearAllCallInternal(FlatBufferBuilder builder, long cmc_type) {
        builder.startObject(1);
        addCmcType(builder, cmc_type);
        return endRequestClearAllCallInternal(builder);
    }

    public static void startRequestClearAllCallInternal(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addCmcType(FlatBufferBuilder builder, long cmcType) {
        builder.addInt(0, (int) cmcType, 0);
    }

    public static int endRequestClearAllCallInternal(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
