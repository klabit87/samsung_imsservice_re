package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateConfCall extends Table {
    public static RequestUpdateConfCall getRootAsRequestUpdateConfCall(ByteBuffer _bb) {
        return getRootAsRequestUpdateConfCall(_bb, new RequestUpdateConfCall());
    }

    public static RequestUpdateConfCall getRootAsRequestUpdateConfCall(ByteBuffer _bb, RequestUpdateConfCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateConfCall __assign(int _i, ByteBuffer _bb) {
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

    public long cmd() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long participantId() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String participant() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer participantAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestUpdateConfCall(FlatBufferBuilder builder, long session, long cmd, long participant_id, int participantOffset) {
        builder.startObject(4);
        addParticipant(builder, participantOffset);
        addParticipantId(builder, participant_id);
        addCmd(builder, cmd);
        addSession(builder, session);
        return endRequestUpdateConfCall(builder);
    }

    public static void startRequestUpdateConfCall(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addCmd(FlatBufferBuilder builder, long cmd) {
        builder.addInt(1, (int) cmd, 0);
    }

    public static void addParticipantId(FlatBufferBuilder builder, long participantId) {
        builder.addInt(2, (int) participantId, 0);
    }

    public static void addParticipant(FlatBufferBuilder builder, int participantOffset) {
        builder.addOffset(3, participantOffset, 0);
    }

    public static int endRequestUpdateConfCall(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        return o;
    }
}
