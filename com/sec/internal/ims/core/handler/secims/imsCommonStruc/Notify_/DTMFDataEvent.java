package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DTMFDataEvent extends Table {
    public static DTMFDataEvent getRootAsDTMFDataEvent(ByteBuffer _bb) {
        return getRootAsDTMFDataEvent(_bb, new DTMFDataEvent());
    }

    public static DTMFDataEvent getRootAsDTMFDataEvent(ByteBuffer _bb, DTMFDataEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DTMFDataEvent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long event() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long volume() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long duration() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long endbit() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createDTMFDataEvent(FlatBufferBuilder builder, long event, long volume, long duration, long endbit) {
        builder.startObject(4);
        addEndbit(builder, endbit);
        addDuration(builder, duration);
        addVolume(builder, volume);
        addEvent(builder, event);
        return endDTMFDataEvent(builder);
    }

    public static void startDTMFDataEvent(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addEvent(FlatBufferBuilder builder, long event) {
        builder.addInt(0, (int) event, 0);
    }

    public static void addVolume(FlatBufferBuilder builder, long volume) {
        builder.addInt(1, (int) volume, 0);
    }

    public static void addDuration(FlatBufferBuilder builder, long duration) {
        builder.addInt(2, (int) duration, 0);
    }

    public static void addEndbit(FlatBufferBuilder builder, long endbit) {
        builder.addInt(3, (int) endbit, 0);
    }

    public static int endDTMFDataEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
