package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRejectModifyCallType extends Table {
    public static RequestRejectModifyCallType getRootAsRequestRejectModifyCallType(ByteBuffer _bb) {
        return getRootAsRequestRejectModifyCallType(_bb, new RequestRejectModifyCallType());
    }

    public static RequestRejectModifyCallType getRootAsRequestRejectModifyCallType(ByteBuffer _bb, RequestRejectModifyCallType obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestRejectModifyCallType __assign(int _i, ByteBuffer _bb) {
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

    public int reason() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestRejectModifyCallType(FlatBufferBuilder builder, long session, int reason) {
        builder.startObject(2);
        addReason(builder, reason);
        addSession(builder, session);
        return endRequestRejectModifyCallType(builder);
    }

    public static void startRequestRejectModifyCallType(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(1, reason, 0);
    }

    public static int endRequestRejectModifyCallType(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
