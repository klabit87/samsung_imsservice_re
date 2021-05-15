package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestCloseImSession extends Table {
    public static RequestCloseImSession getRootAsRequestCloseImSession(ByteBuffer _bb) {
        return getRootAsRequestCloseImSession(_bb, new RequestCloseImSession());
    }

    public static RequestCloseImSession getRootAsRequestCloseImSession(ByteBuffer _bb, RequestCloseImSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestCloseImSession __assign(int _i, ByteBuffer _bb) {
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

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestCloseImSession(FlatBufferBuilder builder, long session_id, int reason_hdrOffset) {
        builder.startObject(2);
        addReasonHdr(builder, reason_hdrOffset);
        addSessionId(builder, session_id);
        return endRequestCloseImSession(builder);
    }

    public static void startRequestCloseImSession(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder builder, int reasonHdrOffset) {
        builder.addOffset(1, reasonHdrOffset, 0);
    }

    public static int endRequestCloseImSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
