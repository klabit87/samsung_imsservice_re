package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestEndCall_.EndReason;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestEndCall extends Table {
    public static RequestEndCall getRootAsRequestEndCall(ByteBuffer _bb) {
        return getRootAsRequestEndCall(_bb, new RequestEndCall());
    }

    public static RequestEndCall getRootAsRequestEndCall(ByteBuffer _bb, RequestEndCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestEndCall __assign(int _i, ByteBuffer _bb) {
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

    public EndReason endReason() {
        return endReason(new EndReason());
    }

    public EndReason endReason(EndReason obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestEndCall(FlatBufferBuilder builder, long session, int end_reasonOffset) {
        builder.startObject(2);
        addEndReason(builder, end_reasonOffset);
        addSession(builder, session);
        return endRequestEndCall(builder);
    }

    public static void startRequestEndCall(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addEndReason(FlatBufferBuilder builder, int endReasonOffset) {
        builder.addOffset(1, endReasonOffset, 0);
    }

    public static int endRequestEndCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
