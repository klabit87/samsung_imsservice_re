package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DedicatedBearerEvent extends Table {
    public static DedicatedBearerEvent getRootAsDedicatedBearerEvent(ByteBuffer _bb) {
        return getRootAsDedicatedBearerEvent(_bb, new DedicatedBearerEvent());
    }

    public static DedicatedBearerEvent getRootAsDedicatedBearerEvent(ByteBuffer _bb, DedicatedBearerEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DedicatedBearerEvent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
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

    public long qci() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int bearerState() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createDedicatedBearerEvent(FlatBufferBuilder builder, long handle, long session, long qci, int bearer_state) {
        builder.startObject(4);
        addBearerState(builder, bearer_state);
        addQci(builder, qci);
        addSession(builder, session);
        addHandle(builder, handle);
        return endDedicatedBearerEvent(builder);
    }

    public static void startDedicatedBearerEvent(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addQci(FlatBufferBuilder builder, long qci) {
        builder.addInt(2, (int) qci, 0);
    }

    public static void addBearerState(FlatBufferBuilder builder, int bearerState) {
        builder.addInt(3, bearerState, 0);
    }

    public static int endDedicatedBearerEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
