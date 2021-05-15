package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ModifyVideoData extends Table {
    public static ModifyVideoData getRootAsModifyVideoData(ByteBuffer _bb) {
        return getRootAsModifyVideoData(_bb, new ModifyVideoData());
    }

    public static ModifyVideoData getRootAsModifyVideoData(ByteBuffer _bb, ModifyVideoData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ModifyVideoData __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long session() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long direction() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isHeldCall() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createModifyVideoData(FlatBufferBuilder builder, long session, long direction, boolean is_held_call) {
        builder.startObject(3);
        addDirection(builder, direction);
        addSession(builder, session);
        addIsHeldCall(builder, is_held_call);
        return endModifyVideoData(builder);
    }

    public static void startModifyVideoData(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addDirection(FlatBufferBuilder builder, long direction) {
        builder.addInt(1, (int) direction, 0);
    }

    public static void addIsHeldCall(FlatBufferBuilder builder, boolean isHeldCall) {
        builder.addBoolean(2, isHeldCall, false);
    }

    public static int endModifyVideoData(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
