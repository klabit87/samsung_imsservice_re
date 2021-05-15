package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_.ImParticipantInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConferenceInfoUpdated extends Table {
    public static ImConferenceInfoUpdated getRootAsImConferenceInfoUpdated(ByteBuffer _bb) {
        return getRootAsImConferenceInfoUpdated(_bb, new ImConferenceInfoUpdated());
    }

    public static ImConferenceInfoUpdated getRootAsImConferenceInfoUpdated(ByteBuffer _bb, ImConferenceInfoUpdated obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImConferenceInfoUpdated __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String subject() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long maxUserCount() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImParticipantInfo participants(int j) {
        return participants(new ImParticipantInfo(), j);
    }

    public ImParticipantInfo participants(ImParticipantInfo obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int participantsLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createImConferenceInfoUpdated(FlatBufferBuilder builder, int session_idOffset, int subjectOffset, long max_user_count, int participantsOffset) {
        builder.startObject(4);
        addParticipants(builder, participantsOffset);
        addMaxUserCount(builder, max_user_count);
        addSubject(builder, subjectOffset);
        addSessionId(builder, session_idOffset);
        return endImConferenceInfoUpdated(builder);
    }

    public static void startImConferenceInfoUpdated(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSessionId(FlatBufferBuilder builder, int sessionIdOffset) {
        builder.addOffset(0, sessionIdOffset, 0);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(1, subjectOffset, 0);
    }

    public static void addMaxUserCount(FlatBufferBuilder builder, long maxUserCount) {
        builder.addInt(2, (int) maxUserCount, 0);
    }

    public static void addParticipants(FlatBufferBuilder builder, int participantsOffset) {
        builder.addOffset(3, participantsOffset, 0);
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

    public static int endImConferenceInfoUpdated(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
