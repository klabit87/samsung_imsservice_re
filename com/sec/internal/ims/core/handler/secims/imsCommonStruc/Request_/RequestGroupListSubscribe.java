package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestGroupListSubscribe extends Table {
    public static RequestGroupListSubscribe getRootAsRequestGroupListSubscribe(ByteBuffer _bb) {
        return getRootAsRequestGroupListSubscribe(_bb, new RequestGroupListSubscribe());
    }

    public static RequestGroupListSubscribe getRootAsRequestGroupListSubscribe(ByteBuffer _bb, RequestGroupListSubscribe obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestGroupListSubscribe __assign(int _i, ByteBuffer _bb) {
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

    public long version() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isIncrease() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestGroupListSubscribe(FlatBufferBuilder builder, long handle, int subscription_idOffset, long version, boolean isIncrease) {
        builder.startObject(4);
        addVersion(builder, version);
        addSubscriptionId(builder, subscription_idOffset);
        addHandle(builder, handle);
        addIsIncrease(builder, isIncrease);
        return endRequestGroupListSubscribe(builder);
    }

    public static void startRequestGroupListSubscribe(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, int subscriptionIdOffset) {
        builder.addOffset(1, subscriptionIdOffset, 0);
    }

    public static void addVersion(FlatBufferBuilder builder, long version) {
        builder.addInt(2, (int) version, 0);
    }

    public static void addIsIncrease(FlatBufferBuilder builder, boolean isIncrease) {
        builder.addBoolean(3, isIncrease, false);
    }

    public static int endRequestGroupListSubscribe(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
