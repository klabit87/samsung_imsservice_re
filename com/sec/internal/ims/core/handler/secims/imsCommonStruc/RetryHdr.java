package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RetryHdr extends Table {
    public static RetryHdr getRootAsRetryHdr(ByteBuffer _bb) {
        return getRootAsRetryHdr(_bb, new RetryHdr());
    }

    public static RetryHdr getRootAsRetryHdr(ByteBuffer _bb, RetryHdr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RetryHdr __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int retryTimer() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String contactValue() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contactValueAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRetryHdr(FlatBufferBuilder builder, int retry_timer, int contact_valueOffset) {
        builder.startObject(2);
        addContactValue(builder, contact_valueOffset);
        addRetryTimer(builder, retry_timer);
        return endRetryHdr(builder);
    }

    public static void startRetryHdr(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addRetryTimer(FlatBufferBuilder builder, int retryTimer) {
        builder.addInt(0, retryTimer, 0);
    }

    public static void addContactValue(FlatBufferBuilder builder, int contactValueOffset) {
        builder.addOffset(1, contactValueOffset, 0);
    }

    public static int endRetryHdr(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
