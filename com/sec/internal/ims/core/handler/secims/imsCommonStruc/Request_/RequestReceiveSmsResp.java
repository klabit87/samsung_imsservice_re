package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReceiveSmsResp extends Table {
    public static RequestReceiveSmsResp getRootAsRequestReceiveSmsResp(ByteBuffer _bb) {
        return getRootAsRequestReceiveSmsResp(_bb, new RequestReceiveSmsResp());
    }

    public static RequestReceiveSmsResp getRootAsRequestReceiveSmsResp(ByteBuffer _bb, RequestReceiveSmsResp obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestReceiveSmsResp __assign(int _i, ByteBuffer _bb) {
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

    public long status() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestReceiveSmsResp(FlatBufferBuilder builder, long handle, int call_idOffset, long status) {
        builder.startObject(3);
        addStatus(builder, status);
        addCallId(builder, call_idOffset);
        addHandle(builder, handle);
        return endRequestReceiveSmsResp(builder);
    }

    public static void startRequestReceiveSmsResp(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(1, callIdOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, long status) {
        builder.addInt(2, (int) status, 0);
    }

    public static int endRequestReceiveSmsResp(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
