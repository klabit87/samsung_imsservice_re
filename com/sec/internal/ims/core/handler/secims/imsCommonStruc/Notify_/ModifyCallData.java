package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ModifyCallData extends Table {
    public static ModifyCallData getRootAsModifyCallData(ByteBuffer _bb) {
        return getRootAsModifyCallData(_bb, new ModifyCallData());
    }

    public static ModifyCallData getRootAsModifyCallData(ByteBuffer _bb, ModifyCallData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ModifyCallData __assign(int _i, ByteBuffer _bb) {
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

    public long oldType() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long newType() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSdToSdPull() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createModifyCallData(FlatBufferBuilder builder, long session, long old_type, long new_type, boolean is_sd_to_sd_pull) {
        builder.startObject(4);
        addNewType(builder, new_type);
        addOldType(builder, old_type);
        addSession(builder, session);
        addIsSdToSdPull(builder, is_sd_to_sd_pull);
        return endModifyCallData(builder);
    }

    public static void startModifyCallData(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addOldType(FlatBufferBuilder builder, long oldType) {
        builder.addInt(1, (int) oldType, 0);
    }

    public static void addNewType(FlatBufferBuilder builder, long newType) {
        builder.addInt(2, (int) newType, 0);
    }

    public static void addIsSdToSdPull(FlatBufferBuilder builder, boolean isSdToSdPull) {
        builder.addBoolean(3, isSdToSdPull, false);
    }

    public static int endModifyCallData(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
