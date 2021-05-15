package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_;

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

    public int status() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String aor() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer aorAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long participantid() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long sessionId() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createParticipant(FlatBufferBuilder builder, int uriOffset, int status, int aorOffset, long participantid, long session_id) {
        builder.startObject(5);
        addSessionId(builder, session_id);
        addParticipantid(builder, participantid);
        addAor(builder, aorOffset);
        addStatus(builder, status);
        addUri(builder, uriOffset);
        return endParticipant(builder);
    }

    public static void startParticipant(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int status) {
        builder.addInt(1, status, 0);
    }

    public static void addAor(FlatBufferBuilder builder, int aorOffset) {
        builder.addOffset(2, aorOffset, 0);
    }

    public static void addParticipantid(FlatBufferBuilder builder, long participantid) {
        builder.addInt(3, (int) participantid, 0);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(4, (int) sessionId, 0);
    }

    public static int endParticipant(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
