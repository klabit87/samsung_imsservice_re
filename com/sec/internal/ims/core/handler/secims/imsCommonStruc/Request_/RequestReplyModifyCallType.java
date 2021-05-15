package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReplyModifyCallType extends Table {
    public static RequestReplyModifyCallType getRootAsRequestReplyModifyCallType(ByteBuffer _bb) {
        return getRootAsRequestReplyModifyCallType(_bb, new RequestReplyModifyCallType());
    }

    public static RequestReplyModifyCallType getRootAsRequestReplyModifyCallType(ByteBuffer _bb, RequestReplyModifyCallType obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestReplyModifyCallType __assign(int _i, ByteBuffer _bb) {
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

    public int reqType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int curType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int repType() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String cmcCallTime() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcCallTimeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestReplyModifyCallType(FlatBufferBuilder builder, long session, int req_type, int cur_type, int rep_type, int cmc_call_timeOffset) {
        builder.startObject(5);
        addCmcCallTime(builder, cmc_call_timeOffset);
        addRepType(builder, rep_type);
        addCurType(builder, cur_type);
        addReqType(builder, req_type);
        addSession(builder, session);
        return endRequestReplyModifyCallType(builder);
    }

    public static void startRequestReplyModifyCallType(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addReqType(FlatBufferBuilder builder, int reqType) {
        builder.addInt(1, reqType, 0);
    }

    public static void addCurType(FlatBufferBuilder builder, int curType) {
        builder.addInt(2, curType, 0);
    }

    public static void addRepType(FlatBufferBuilder builder, int repType) {
        builder.addInt(3, repType, 0);
    }

    public static void addCmcCallTime(FlatBufferBuilder builder, int cmcCallTimeOffset) {
        builder.addOffset(4, cmcCallTimeOffset, 0);
    }

    public static int endRequestReplyModifyCallType(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
