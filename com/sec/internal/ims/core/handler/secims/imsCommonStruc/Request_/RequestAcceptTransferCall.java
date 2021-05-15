package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptTransferCall extends Table {
    public static RequestAcceptTransferCall getRootAsRequestAcceptTransferCall(ByteBuffer _bb) {
        return getRootAsRequestAcceptTransferCall(_bb, new RequestAcceptTransferCall());
    }

    public static RequestAcceptTransferCall getRootAsRequestAcceptTransferCall(ByteBuffer _bb, RequestAcceptTransferCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestAcceptTransferCall __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean accept() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long statusCode() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestAcceptTransferCall(FlatBufferBuilder builder, long handle, long session, boolean accept, long status_code, int reason_phraseOffset) {
        builder.startObject(5);
        addReasonPhrase(builder, reason_phraseOffset);
        addStatusCode(builder, status_code);
        addSession(builder, session);
        addHandle(builder, handle);
        addAccept(builder, accept);
        return endRequestAcceptTransferCall(builder);
    }

    public static void startRequestAcceptTransferCall(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addAccept(FlatBufferBuilder builder, boolean accept) {
        builder.addBoolean(2, accept, false);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(3, (int) statusCode, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder builder, int reasonPhraseOffset) {
        builder.addOffset(4, reasonPhraseOffset, 0);
    }

    public static int endRequestAcceptTransferCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
