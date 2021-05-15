package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Notify extends Table {
    public static Notify getRootAsNotify(ByteBuffer _bb) {
        return getRootAsNotify(_bb, new Notify());
    }

    public static Notify getRootAsNotify(ByteBuffer _bb, Notify obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Notify __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int notifyid() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public byte notiType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table noti(Table obj) {
        int o = __offset(8);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createNotify(FlatBufferBuilder builder, int notifyid, byte noti_type, int notiOffset) {
        builder.startObject(3);
        addNoti(builder, notiOffset);
        addNotifyid(builder, notifyid);
        addNotiType(builder, noti_type);
        return endNotify(builder);
    }

    public static void startNotify(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addNotifyid(FlatBufferBuilder builder, int notifyid) {
        builder.addInt(0, notifyid, 0);
    }

    public static void addNotiType(FlatBufferBuilder builder, byte notiType) {
        builder.addByte(1, notiType, 0);
    }

    public static void addNoti(FlatBufferBuilder builder, int notiOffset) {
        builder.addOffset(2, notiOffset, 0);
    }

    public static int endNotify(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
