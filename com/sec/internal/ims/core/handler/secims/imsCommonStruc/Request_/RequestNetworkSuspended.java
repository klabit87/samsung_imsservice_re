package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestNetworkSuspended extends Table {
    public static RequestNetworkSuspended getRootAsRequestNetworkSuspended(ByteBuffer _bb) {
        return getRootAsRequestNetworkSuspended(_bb, new RequestNetworkSuspended());
    }

    public static RequestNetworkSuspended getRootAsRequestNetworkSuspended(ByteBuffer _bb, RequestNetworkSuspended obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestNetworkSuspended __assign(int _i, ByteBuffer _bb) {
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

    public boolean state() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestNetworkSuspended(FlatBufferBuilder builder, long handle, boolean state) {
        builder.startObject(2);
        addHandle(builder, handle);
        addState(builder, state);
        return endRequestNetworkSuspended(builder);
    }

    public static void startRequestNetworkSuspended(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addState(FlatBufferBuilder builder, boolean state) {
        builder.addBoolean(1, state, false);
    }

    public static int endRequestNetworkSuspended(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
