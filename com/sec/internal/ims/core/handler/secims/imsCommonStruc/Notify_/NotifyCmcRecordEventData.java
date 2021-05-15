package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NotifyCmcRecordEventData extends Table {
    public static NotifyCmcRecordEventData getRootAsNotifyCmcRecordEventData(ByteBuffer _bb) {
        return getRootAsNotifyCmcRecordEventData(_bb, new NotifyCmcRecordEventData());
    }

    public static NotifyCmcRecordEventData getRootAsNotifyCmcRecordEventData(ByteBuffer _bb, NotifyCmcRecordEventData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public NotifyCmcRecordEventData __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long arg1() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long arg2() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createNotifyCmcRecordEventData(FlatBufferBuilder builder, long phone_id, long session, long event, long arg1, long arg2) {
        builder.startObject(5);
        addArg2(builder, arg2);
        addArg1(builder, arg1);
        addEvent(builder, event);
        addSession(builder, session);
        addPhoneId(builder, phone_id);
        return endNotifyCmcRecordEventData(builder);
    }

    public static void startNotifyCmcRecordEventData(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addEvent(FlatBufferBuilder builder, long event) {
        builder.addInt(2, (int) event, 0);
    }

    public static void addArg1(FlatBufferBuilder builder, long arg1) {
        builder.addInt(3, (int) arg1, 0);
    }

    public static void addArg2(FlatBufferBuilder builder, long arg2) {
        builder.addInt(4, (int) arg2, 0);
    }

    public static int endNotifyCmcRecordEventData(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
