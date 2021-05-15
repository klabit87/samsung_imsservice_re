package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Participant extends Table {
    public static Participant getRootAsParticipant(ByteBuffer _bb) {
        return getRootAsParticipant(_bb, new Participant());
    }

    public static Participant getRootAsParticipant(ByteBuffer _bb, Participant obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Participant __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public int copyControl() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createParticipant(FlatBufferBuilder builder, int uriOffset, int copy_control) {
        builder.startObject(2);
        addCopyControl(builder, copy_control);
        addUri(builder, uriOffset);
        return endParticipant(builder);
    }

    public static void startParticipant(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addCopyControl(FlatBufferBuilder builder, int copyControl) {
        builder.addInt(1, copyControl, 0);
    }

    public static int endParticipant(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
