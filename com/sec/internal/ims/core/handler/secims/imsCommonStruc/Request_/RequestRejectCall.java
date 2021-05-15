package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRejectCall extends Table {
    public static RequestRejectCall getRootAsRequestRejectCall(ByteBuffer _bb) {
        return getRootAsRequestRejectCall(_bb, new RequestRejectCall());
    }

    public static RequestRejectCall getRootAsRequestRejectCall(ByteBuffer _bb, RequestRejectCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestRejectCall __assign(int _i, ByteBuffer _bb) {
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

    public long statusCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestRejectCall(FlatBufferBuilder builder, long session, long status_code, int reason_phraseOffset) {
        builder.startObject(3);
        addReasonPhrase(builder, reason_phraseOffset);
        addStatusCode(builder, status_code);
        addSession(builder, session);
        return endRequestRejectCall(builder);
    }

    public static void startRequestRejectCall(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(1, (int) statusCode, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder builder, int reasonPhraseOffset) {
        builder.addOffset(2, reasonPhraseOffset, 0);
    }

    public static int endRequestRejectCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
