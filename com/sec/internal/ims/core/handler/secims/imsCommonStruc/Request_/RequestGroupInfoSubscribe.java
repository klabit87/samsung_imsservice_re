package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestGroupInfoSubscribe extends Table {
    public static RequestGroupInfoSubscribe getRootAsRequestGroupInfoSubscribe(ByteBuffer _bb) {
        return getRootAsRequestGroupInfoSubscribe(_bb, new RequestGroupInfoSubscribe());
    }

    public static RequestGroupInfoSubscribe getRootAsRequestGroupInfoSubscribe(ByteBuffer _bb, RequestGroupInfoSubscribe obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestGroupInfoSubscribe __assign(int _i, ByteBuffer _bb) {
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

    public String uri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestGroupInfoSubscribe(FlatBufferBuilder builder, long handle, int subscription_idOffset, int uriOffset) {
        builder.startObject(3);
        addUri(builder, uriOffset);
        addSubscriptionId(builder, subscription_idOffset);
        addHandle(builder, handle);
        return endRequestGroupInfoSubscribe(builder);
    }

    public static void startRequestGroupInfoSubscribe(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, int subscriptionIdOffset) {
        builder.addOffset(1, subscriptionIdOffset, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(2, uriOffset, 0);
    }

    public static int endRequestGroupInfoSubscribe(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
