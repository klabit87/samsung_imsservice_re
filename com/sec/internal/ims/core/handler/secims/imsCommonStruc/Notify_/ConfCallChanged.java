package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_.Participant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ConfCallChanged extends Table {
    public static ConfCallChanged getRootAsConfCallChanged(ByteBuffer _bb) {
        return getRootAsConfCallChanged(_bb, new ConfCallChanged());
    }

    public static ConfCallChanged getRootAsConfCallChanged(ByteBuffer _bb, ConfCallChanged obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ConfCallChanged __assign(int _i, ByteBuffer _bb) {
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

    public int event() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public Participant participants(int j) {
        return participants(new Participant(), j);
    }

    public Participant participants(Participant obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int participantsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createConfCallChanged(FlatBufferBuilder builder, long session, int event, int participantsOffset) {
        builder.startObject(3);
        addParticipants(builder, participantsOffset);
        addEvent(builder, event);
        addSession(builder, session);
        return endConfCallChanged(builder);
    }

    public static void startConfCallChanged(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addEvent(FlatBufferBuilder builder, int event) {
        builder.addInt(1, event, 0);
    }

    public static void addParticipants(FlatBufferBuilder builder, int participantsOffset) {
        builder.addOffset(2, participantsOffset, 0);
    }

    public static int createParticipantsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startParticipantsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endConfCallChanged(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
