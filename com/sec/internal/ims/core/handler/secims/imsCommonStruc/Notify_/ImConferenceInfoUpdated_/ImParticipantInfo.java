package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImParticipantInfo extends Table {
    public static ImParticipantInfo getRootAsImParticipantInfo(ByteBuffer _bb) {
        return getRootAsImParticipantInfo(_bb, new ImParticipantInfo());
    }

    public static ImParticipantInfo getRootAsImParticipantInfo(ByteBuffer _bb, ImParticipantInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImParticipantInfo __assign(int _i, ByteBuffer _bb) {
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

    public boolean isOwn() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int status() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int reason() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createImParticipantInfo(FlatBufferBuilder builder, int uriOffset, boolean is_own, int status, int reason) {
        builder.startObject(4);
        addReason(builder, reason);
        addStatus(builder, status);
        addUri(builder, uriOffset);
        addIsOwn(builder, is_own);
        return endImParticipantInfo(builder);
    }

    public static void startImParticipantInfo(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addIsOwn(FlatBufferBuilder builder, boolean isOwn) {
        builder.addBoolean(1, isOwn, false);
    }

    public static void addStatus(FlatBufferBuilder builder, int status) {
        builder.addInt(2, status, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(3, reason, 0);
    }

    public static int endImParticipantInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
