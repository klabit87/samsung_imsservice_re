package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ScreenConfig extends Table {
    public static ScreenConfig getRootAsScreenConfig(ByteBuffer _bb) {
        return getRootAsScreenConfig(_bb, new ScreenConfig());
    }

    public static ScreenConfig getRootAsScreenConfig(ByteBuffer _bb, ScreenConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ScreenConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long on() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createScreenConfig(FlatBufferBuilder builder, long on) {
        builder.startObject(1);
        addOn(builder, on);
        return endScreenConfig(builder);
    }

    public static void startScreenConfig(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addOn(FlatBufferBuilder builder, long on) {
        builder.addInt(0, (int) on, 0);
    }

    public static int endScreenConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
