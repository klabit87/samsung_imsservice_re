package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CancelAlarm extends Table {
    public static CancelAlarm getRootAsCancelAlarm(ByteBuffer _bb) {
        return getRootAsCancelAlarm(_bb, new CancelAlarm());
    }

    public static CancelAlarm getRootAsCancelAlarm(ByteBuffer _bb, CancelAlarm obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CancelAlarm __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long id() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createCancelAlarm(FlatBufferBuilder builder, long id) {
        builder.startObject(1);
        addId(builder, id);
        return endCancelAlarm(builder);
    }

    public static void startCancelAlarm(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addId(FlatBufferBuilder builder, long id) {
        builder.addInt(0, (int) id, 0);
    }

    public static int endCancelAlarm(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
