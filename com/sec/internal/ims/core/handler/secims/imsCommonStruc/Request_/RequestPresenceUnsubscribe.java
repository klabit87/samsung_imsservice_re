package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresenceUnsubscribe extends Table {
    public static RequestPresenceUnsubscribe getRootAsRequestPresenceUnsubscribe(ByteBuffer _bb) {
        return getRootAsRequestPresenceUnsubscribe(_bb, new RequestPresenceUnsubscribe());
    }

    public static RequestPresenceUnsubscribe getRootAsRequestPresenceUnsubscribe(ByteBuffer _bb, RequestPresenceUnsubscribe obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPresenceUnsubscribe __assign(int _i, ByteBuffer _bb) {
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

    public String subscriptionId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestPresenceUnsubscribe(FlatBufferBuilder builder, long handle, int subscription_idOffset) {
        builder.startObject(2);
        addSubscriptionId(builder, subscription_idOffset);
        addHandle(builder, handle);
        return endRequestPresenceUnsubscribe(builder);
    }

    public static void startRequestPresenceUnsubscribe(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, int subscriptionIdOffset) {
        builder.addOffset(1, subscriptionIdOffset, 0);
    }

    public static int endRequestPresenceUnsubscribe(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
