package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RrcConnectionEvent extends Table {
    public static RrcConnectionEvent getRootAsRrcConnectionEvent(ByteBuffer _bb) {
        return getRootAsRrcConnectionEvent(_bb, new RrcConnectionEvent());
    }

    public static RrcConnectionEvent getRootAsRrcConnectionEvent(ByteBuffer _bb, RrcConnectionEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RrcConnectionEvent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int event() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRrcConnectionEvent(FlatBufferBuilder builder, int event) {
        builder.startObject(1);
        addEvent(builder, event);
        return endRrcConnectionEvent(builder);
    }

    public static void startRrcConnectionEvent(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addEvent(FlatBufferBuilder builder, int event) {
        builder.addInt(0, event, 0);
    }

    public static int endRrcConnectionEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
