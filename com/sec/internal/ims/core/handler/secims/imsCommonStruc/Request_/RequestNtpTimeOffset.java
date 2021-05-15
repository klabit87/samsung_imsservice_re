package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestNtpTimeOffset extends Table {
    public static RequestNtpTimeOffset getRootAsRequestNtpTimeOffset(ByteBuffer _bb) {
        return getRootAsRequestNtpTimeOffset(_bb, new RequestNtpTimeOffset());
    }

    public static RequestNtpTimeOffset getRootAsRequestNtpTimeOffset(ByteBuffer _bb, RequestNtpTimeOffset obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestNtpTimeOffset __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long offset() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestNtpTimeOffset(FlatBufferBuilder builder, long offset) {
        builder.startObject(1);
        addOffset(builder, offset);
        return endRequestNtpTimeOffset(builder);
    }

    public static void startRequestNtpTimeOffset(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addOffset(FlatBufferBuilder builder, long offset) {
        builder.addLong(0, offset, 0);
    }

    public static int endRequestNtpTimeOffset(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
