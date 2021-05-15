package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOpenSipDialog extends Table {
    public static RequestOpenSipDialog getRootAsRequestOpenSipDialog(ByteBuffer _bb) {
        return getRootAsRequestOpenSipDialog(_bb, new RequestOpenSipDialog());
    }

    public static RequestOpenSipDialog getRootAsRequestOpenSipDialog(ByteBuffer _bb, RequestOpenSipDialog obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestOpenSipDialog __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public boolean isRequired() {
        int o = __offset(4);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestOpenSipDialog(FlatBufferBuilder builder, boolean is_required) {
        builder.startObject(1);
        addIsRequired(builder, is_required);
        return endRequestOpenSipDialog(builder);
    }

    public static void startRequestOpenSipDialog(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addIsRequired(FlatBufferBuilder builder, boolean isRequired) {
        builder.addBoolean(0, isRequired, false);
    }

    public static int endRequestOpenSipDialog(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
