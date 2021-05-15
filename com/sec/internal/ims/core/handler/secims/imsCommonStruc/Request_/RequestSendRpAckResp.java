package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendRpAckResp extends Table {
    public static RequestSendRpAckResp getRootAsRequestSendRpAckResp(ByteBuffer _bb) {
        return getRootAsRequestSendRpAckResp(_bb, new RequestSendRpAckResp());
    }

    public static RequestSendRpAckResp getRootAsRequestSendRpAckResp(ByteBuffer _bb, RequestSendRpAckResp obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendRpAckResp __assign(int _i, ByteBuffer _bb) {
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

    public String callId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSendRpAckResp(FlatBufferBuilder builder, long handle, int call_idOffset) {
        builder.startObject(2);
        addCallId(builder, call_idOffset);
        addHandle(builder, handle);
        return endRequestSendRpAckResp(builder);
    }

    public static void startRequestSendRpAckResp(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(1, callIdOffset, 0);
    }

    public static int endRequestSendRpAckResp(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
