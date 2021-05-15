package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AlarmWakeUp extends Table {
    public static AlarmWakeUp getRootAsAlarmWakeUp(ByteBuffer _bb) {
        return getRootAsAlarmWakeUp(_bb, new AlarmWakeUp());
    }

    public static AlarmWakeUp getRootAsAlarmWakeUp(ByteBuffer _bb, AlarmWakeUp obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public AlarmWakeUp __assign(int _i, ByteBuffer _bb) {
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

    public long delay() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createAlarmWakeUp(FlatBufferBuilder builder, long id, long delay) {
        builder.startObject(2);
        addDelay(builder, delay);
        addId(builder, id);
        return endAlarmWakeUp(builder);
    }

    public static void startAlarmWakeUp(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addId(FlatBufferBuilder builder, long id) {
        builder.addInt(0, (int) id, 0);
    }

    public static void addDelay(FlatBufferBuilder builder, long delay) {
        builder.addInt(1, (int) delay, 0);
    }

    public static int endAlarmWakeUp(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
