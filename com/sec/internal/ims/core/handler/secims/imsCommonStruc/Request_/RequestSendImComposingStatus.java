package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImComposingStatus extends Table {
    public static RequestSendImComposingStatus getRootAsRequestSendImComposingStatus(ByteBuffer _bb) {
        return getRootAsRequestSendImComposingStatus(_bb, new RequestSendImComposingStatus());
    }

    public static RequestSendImComposingStatus getRootAsRequestSendImComposingStatus(ByteBuffer _bb, RequestSendImComposingStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendImComposingStatus __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImComposingStatus status() {
        return status(new ImComposingStatus());
    }

    public ImComposingStatus status(ImComposingStatus obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String userAlias() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestSendImComposingStatus(FlatBufferBuilder builder, long session_id, int statusOffset, int user_aliasOffset) {
        builder.startObject(3);
        addUserAlias(builder, user_aliasOffset);
        addStatus(builder, statusOffset);
        addSessionId(builder, session_id);
        return endRequestSendImComposingStatus(builder);
    }

    public static void startRequestSendImComposingStatus(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(1, statusOffset, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(2, userAliasOffset, 0);
    }

    public static int endRequestSendImComposingStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
