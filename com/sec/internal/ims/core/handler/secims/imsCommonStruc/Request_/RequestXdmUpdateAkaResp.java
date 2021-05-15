package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateAkaResp extends Table {
    public static RequestXdmUpdateAkaResp getRootAsRequestXdmUpdateAkaResp(ByteBuffer _bb) {
        return getRootAsRequestXdmUpdateAkaResp(_bb, new RequestXdmUpdateAkaResp());
    }

    public static RequestXdmUpdateAkaResp getRootAsRequestXdmUpdateAkaResp(ByteBuffer _bb, RequestXdmUpdateAkaResp obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmUpdateAkaResp __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int akaResp(int j) {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(__vector(o) + (j * 1)) & 255;
        }
        return 0;
    }

    public int akaRespLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
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

    public static int createRequestXdmUpdateAkaResp(FlatBufferBuilder builder, long sid, int aka_respOffset, long recv_mng) {
        builder.startObject(3);
        addRecvMng(builder, recv_mng);
        addAkaResp(builder, aka_respOffset);
        addSid(builder, sid);
        return endRequestXdmUpdateAkaResp(builder);
    }

    public static void startRequestXdmUpdateAkaResp(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSid(FlatBufferBuilder builder, long sid) {
        builder.addInt(0, (int) sid, 0);
    }

    public static void addAkaResp(FlatBufferBuilder builder, int akaRespOffset) {
        builder.addOffset(1, akaRespOffset, 0);
    }

    public static int createAkaRespVector(FlatBufferBuilder builder, byte[] data) {
        builder.startVector(1, data.length, 1);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addByte(data[i]);
        }
        return builder.endVector();
    }

    public static void startAkaRespVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(1, numElems, 1);
    }

    public static void addRecvMng(FlatBufferBuilder builder, long recvMng) {
        builder.addInt(2, (int) recvMng, 0);
    }

    public static int endRequestXdmUpdateAkaResp(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
