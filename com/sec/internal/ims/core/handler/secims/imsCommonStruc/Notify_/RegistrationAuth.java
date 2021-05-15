package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegistrationAuth extends Table {
    public static RegistrationAuth getRootAsRegistrationAuth(ByteBuffer _bb) {
        return getRootAsRegistrationAuth(_bb, new RegistrationAuth());
    }

    public static RegistrationAuth getRootAsRegistrationAuth(ByteBuffer _bb, RegistrationAuth obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RegistrationAuth __assign(int _i, ByteBuffer _bb) {
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

    public static int createRegistrationAuth(FlatBufferBuilder builder, long handle, int nonceOffset, long recv_mng) {
        builder.startObject(3);
        addRecvMng(builder, recv_mng);
        addNonce(builder, nonceOffset);
        addHandle(builder, handle);
        return endRegistrationAuth(builder);
    }

    public static void startRegistrationAuth(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addNonce(FlatBufferBuilder builder, int nonceOffset) {
        builder.addOffset(1, nonceOffset, 0);
    }

    public static void addRecvMng(FlatBufferBuilder builder, long recvMng) {
        builder.addInt(2, (int) recvMng, 0);
    }

    public static int endRegistrationAuth(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
