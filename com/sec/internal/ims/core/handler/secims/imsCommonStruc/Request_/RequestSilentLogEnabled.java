package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSilentLogEnabled extends Table {
    public static RequestSilentLogEnabled getRootAsRequestSilentLogEnabled(ByteBuffer _bb) {
        return getRootAsRequestSilentLogEnabled(_bb, new RequestSilentLogEnabled());
    }

    public static RequestSilentLogEnabled getRootAsRequestSilentLogEnabled(ByteBuffer _bb, RequestSilentLogEnabled obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSilentLogEnabled __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public boolean onoff() {
        int o = __offset(4);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestSilentLogEnabled(FlatBufferBuilder builder, boolean onoff) {
        builder.startObject(1);
        addOnoff(builder, onoff);
        return endRequestSilentLogEnabled(builder);
    }

    public static void startRequestSilentLogEnabled(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addOnoff(FlatBufferBuilder builder, boolean onoff) {
        builder.addBoolean(0, onoff, false);
    }

    public static int endRequestSilentLogEnabled(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
