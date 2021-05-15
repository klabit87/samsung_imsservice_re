package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VshSessionEstablished extends Table {
    public static VshSessionEstablished getRootAsVshSessionEstablished(ByteBuffer _bb) {
        return getRootAsVshSessionEstablished(_bb, new VshSessionEstablished());
    }

    public static VshSessionEstablished getRootAsVshSessionEstablished(ByteBuffer _bb, VshSessionEstablished obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public VshSessionEstablished __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int resolution() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createVshSessionEstablished(FlatBufferBuilder builder, long session_id, int resolution) {
        builder.startObject(2);
        addResolution(builder, resolution);
        addSessionId(builder, session_id);
        return endVshSessionEstablished(builder);
    }

    public static void startVshSessionEstablished(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addResolution(FlatBufferBuilder builder, int resolution) {
        builder.addInt(1, resolution, 0);
    }

    public static int endVshSessionEstablished(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
