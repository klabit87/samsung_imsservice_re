package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSetTextMode extends Table {
    public static RequestSetTextMode getRootAsRequestSetTextMode(ByteBuffer _bb) {
        return getRootAsRequestSetTextMode(_bb, new RequestSetTextMode());
    }

    public static RequestSetTextMode getRootAsRequestSetTextMode(ByteBuffer _bb, RequestSetTextMode obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSetTextMode __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long phoneId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long textMode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSetTextMode(FlatBufferBuilder builder, long phone_id, long text_mode) {
        builder.startObject(2);
        addTextMode(builder, text_mode);
        addPhoneId(builder, phone_id);
        return endRequestSetTextMode(builder);
    }

    public static void startRequestSetTextMode(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addTextMode(FlatBufferBuilder builder, long textMode) {
        builder.addInt(1, (int) textMode, 0);
    }

    public static int endRequestSetTextMode(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
