package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SSGetGbaKey extends Table {
    public static SSGetGbaKey getRootAsSSGetGbaKey(ByteBuffer _bb) {
        return getRootAsSSGetGbaKey(_bb, new SSGetGbaKey());
    }

    public static SSGetGbaKey getRootAsSSGetGbaKey(ByteBuffer _bb, SSGetGbaKey obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SSGetGbaKey __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String gbatype() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer gbatypeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String ck() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer ckAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String ik() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer ikAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String nonce() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nonceAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String lifetime() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer lifetimeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String btid() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSSGetGbaKey(FlatBufferBuilder builder, int gbatypeOffset, int ckOffset, int ikOffset, int nonceOffset, int lifetimeOffset, int btidOffset) {
        builder.startObject(6);
        addBtid(builder, btidOffset);
        addLifetime(builder, lifetimeOffset);
        addNonce(builder, nonceOffset);
        addIk(builder, ikOffset);
        addCk(builder, ckOffset);
        addGbatype(builder, gbatypeOffset);
        return endSSGetGbaKey(builder);
    }

    public static void startSSGetGbaKey(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addGbatype(FlatBufferBuilder builder, int gbatypeOffset) {
        builder.addOffset(0, gbatypeOffset, 0);
    }

    public static void addCk(FlatBufferBuilder builder, int ckOffset) {
        builder.addOffset(1, ckOffset, 0);
    }

    public static void addIk(FlatBufferBuilder builder, int ikOffset) {
        builder.addOffset(2, ikOffset, 0);
    }

    public static void addNonce(FlatBufferBuilder builder, int nonceOffset) {
        builder.addOffset(3, nonceOffset, 0);
    }

    public static void addLifetime(FlatBufferBuilder builder, int lifetimeOffset) {
        builder.addOffset(4, lifetimeOffset, 0);
    }

    public static void addBtid(FlatBufferBuilder builder, int btidOffset) {
        builder.addOffset(5, btidOffset, 0);
    }

    public static int endSSGetGbaKey(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 14);
        return o;
    }
}
