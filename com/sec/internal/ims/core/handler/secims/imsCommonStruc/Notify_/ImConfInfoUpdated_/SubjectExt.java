package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SubjectExt extends Table {
    public static SubjectExt getRootAsSubjectExt(ByteBuffer _bb) {
        return getRootAsSubjectExt(_bb, new SubjectExt());
    }

    public static SubjectExt getRootAsSubjectExt(ByteBuffer _bb, SubjectExt obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SubjectExt __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String subject() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
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

    public static int createSubjectExt(FlatBufferBuilder builder, int subjectOffset, int participantOffset, int timestampOffset) {
        builder.startObject(3);
        addTimestamp(builder, timestampOffset);
        addParticipant(builder, participantOffset);
        addSubject(builder, subjectOffset);
        return endSubjectExt(builder);
    }

    public static void startSubjectExt(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(0, subjectOffset, 0);
    }

    public static void addParticipant(FlatBufferBuilder builder, int participantOffset) {
        builder.addOffset(1, participantOffset, 0);
    }

    public static void addTimestamp(FlatBufferBuilder builder, int timestampOffset) {
        builder.addOffset(2, timestampOffset, 0);
    }

    public static int endSubjectExt(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
