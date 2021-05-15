package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Icon extends Table {
    public static Icon getRootAsIcon(ByteBuffer _bb) {
        return getRootAsIcon(_bb, new Icon());
    }

    public static Icon getRootAsIcon(ByteBuffer _bb, Icon obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Icon __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int icontype() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String participant() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer participantAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String timestamp() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String iconLocation() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer iconLocationAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createIcon(FlatBufferBuilder builder, int icontype, int participantOffset, int timestampOffset, int icon_locationOffset) {
        builder.startObject(4);
        addIconLocation(builder, icon_locationOffset);
        addTimestamp(builder, timestampOffset);
        addParticipant(builder, participantOffset);
        addIcontype(builder, icontype);
        return endIcon(builder);
    }

    public static void startIcon(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addIcontype(FlatBufferBuilder builder, int icontype) {
        builder.addInt(0, icontype, 0);
    }

    public static void addParticipant(FlatBufferBuilder builder, int participantOffset) {
        builder.addOffset(1, participantOffset, 0);
    }

    public static void addTimestamp(FlatBufferBuilder builder, int timestampOffset) {
        builder.addOffset(2, timestampOffset, 0);
    }

    public static void addIconLocation(FlatBufferBuilder builder, int iconLocationOffset) {
        builder.addOffset(3, iconLocationOffset, 0);
    }

    public static int endIcon(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
