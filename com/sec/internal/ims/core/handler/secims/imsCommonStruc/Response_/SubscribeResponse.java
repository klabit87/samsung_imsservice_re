package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubscribeResponse extends Table {
    public static SubscribeResponse getRootAsSubscribeResponse(ByteBuffer _bb) {
        return getRootAsSubscribeResponse(_bb, new SubscribeResponse());
    }

    public static SubscribeResponse getRootAsSubscribeResponse(ByteBuffer _bb, SubscribeResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SubscribeResponse __assign(int _i, ByteBuffer _bb) {
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

    public long subscriptionId() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long expiry() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int state() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int reason() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createSubscribeResponse(FlatBufferBuilder builder, long handle, long subscription_id, long expiry, int state, int reason) {
        builder.startObject(5);
        addReason(builder, reason);
        addState(builder, state);
        addExpiry(builder, expiry);
        addSubscriptionId(builder, subscription_id);
        addHandle(builder, handle);
        return endSubscribeResponse(builder);
    }

    public static void startSubscribeResponse(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, long subscriptionId) {
        builder.addInt(1, (int) subscriptionId, 0);
    }

    public static void addExpiry(FlatBufferBuilder builder, long expiry) {
        builder.addInt(2, (int) expiry, 0);
    }

    public static void addState(FlatBufferBuilder builder, int state) {
        builder.addInt(3, state, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(4, reason, 0);
    }

    public static int endSubscribeResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
