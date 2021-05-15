package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateAkaResp extends Table {
    public static RequestUpdateAkaResp getRootAsRequestUpdateAkaResp(ByteBuffer _bb) {
        return getRootAsRequestUpdateAkaResp(_bb, new RequestUpdateAkaResp());
    }

    public static RequestUpdateAkaResp getRootAsRequestUpdateAkaResp(ByteBuffer _bb, RequestUpdateAkaResp obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateAkaResp __assign(int _i, ByteBuffer _bb) {
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

    public String akaResp() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer akaRespAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long recvMng() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateAkaResp(FlatBufferBuilder builder, long handle, int aka_respOffset, long recv_mng) {
        builder.startObject(3);
        addRecvMng(builder, recv_mng);
        addAkaResp(builder, aka_respOffset);
        addHandle(builder, handle);
        return endRequestUpdateAkaResp(builder);
    }

    public static void startRequestUpdateAkaResp(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addAkaResp(FlatBufferBuilder builder, int akaRespOffset) {
        builder.addOffset(1, akaRespOffset, 0);
    }

    public static void addRecvMng(FlatBufferBuilder builder, long recvMng) {
        builder.addInt(2, (int) recvMng, 0);
    }

    public static int endRequestUpdateAkaResp(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
