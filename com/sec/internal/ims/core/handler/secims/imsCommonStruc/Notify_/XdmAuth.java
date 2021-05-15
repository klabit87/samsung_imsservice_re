package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmAuth extends Table {
    public static XdmAuth getRootAsXdmAuth(ByteBuffer _bb) {
        return getRootAsXdmAuth(_bb, new XdmAuth());
    }

    public static XdmAuth getRootAsXdmAuth(ByteBuffer _bb, XdmAuth obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XdmAuth __assign(int _i, ByteBuffer _bb) {
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

    public String nonce() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nonceAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long recvMng() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createXdmAuth(FlatBufferBuilder builder, long session_id, int nonceOffset, long recv_mng) {
        builder.startObject(3);
        addRecvMng(builder, recv_mng);
        addNonce(builder, nonceOffset);
        addSessionId(builder, session_id);
        return endXdmAuth(builder);
    }

    public static void startXdmAuth(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addNonce(FlatBufferBuilder builder, int nonceOffset) {
        builder.addOffset(1, nonceOffset, 0);
    }

    public static void addRecvMng(FlatBufferBuilder builder, long recvMng) {
        builder.addInt(2, (int) recvMng, 0);
    }

    public static int endXdmAuth(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
