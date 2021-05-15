package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresenceSubscribe extends Table {
    public static RequestPresenceSubscribe getRootAsRequestPresenceSubscribe(ByteBuffer _bb) {
        return getRootAsRequestPresenceSubscribe(_bb, new RequestPresenceSubscribe());
    }

    public static RequestPresenceSubscribe getRootAsRequestPresenceSubscribe(ByteBuffer _bb, RequestPresenceSubscribe obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPresenceSubscribe __assign(int _i, ByteBuffer _bb) {
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

    public String uri(int j) {
        int o = __offset(8);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int uriLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean isAnonymous() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isListSubscribe() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean gzipEnable() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long expires() {
        int o = __offset(16);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestPresenceSubscribe(FlatBufferBuilder builder, long handle, int subscription_idOffset, int uriOffset, boolean is_anonymous, boolean is_list_subscribe, boolean gzip_enable, long expires) {
        builder.startObject(7);
        addExpires(builder, expires);
        addUri(builder, uriOffset);
        addSubscriptionId(builder, subscription_idOffset);
        addHandle(builder, handle);
        addGzipEnable(builder, gzip_enable);
        addIsListSubscribe(builder, is_list_subscribe);
        addIsAnonymous(builder, is_anonymous);
        return endRequestPresenceSubscribe(builder);
    }

    public static void startRequestPresenceSubscribe(FlatBufferBuilder builder) {
        builder.startObject(7);
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

    public static int createUriVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startUriVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsAnonymous(FlatBufferBuilder builder, boolean isAnonymous) {
        builder.addBoolean(3, isAnonymous, false);
    }

    public static void addIsListSubscribe(FlatBufferBuilder builder, boolean isListSubscribe) {
        builder.addBoolean(4, isListSubscribe, false);
    }

    public static void addGzipEnable(FlatBufferBuilder builder, boolean gzipEnable) {
        builder.addBoolean(5, gzipEnable, false);
    }

    public static void addExpires(FlatBufferBuilder builder, long expires) {
        builder.addInt(6, (int) expires, 0);
    }

    public static int endRequestPresenceSubscribe(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
