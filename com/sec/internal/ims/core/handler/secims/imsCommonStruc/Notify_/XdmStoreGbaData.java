package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmStoreGbaData extends Table {
    public static XdmStoreGbaData getRootAsXdmStoreGbaData(ByteBuffer _bb) {
        return getRootAsXdmStoreGbaData(_bb, new XdmStoreGbaData());
    }

    public static XdmStoreGbaData getRootAsXdmStoreGbaData(ByteBuffer _bb, XdmStoreGbaData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XdmStoreGbaData __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rid() {
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

    public String btid() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String lifetime() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer lifetimeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String gbaType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer gbaTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String nafFqdn() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nafFqdnAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int hexProtocolId(int j) {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.get(__vector(o) + (j * 1)) & 255;
        }
        return 0;
    }

    public int hexProtocolIdLength() {
        int o = __offset(16);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer hexProtocolIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createXdmStoreGbaData(FlatBufferBuilder builder, long rid, int nonceOffset, int btidOffset, int lifetimeOffset, int gba_typeOffset, int naf_fqdnOffset, int hex_protocol_idOffset) {
        builder.startObject(7);
        addHexProtocolId(builder, hex_protocol_idOffset);
        addNafFqdn(builder, naf_fqdnOffset);
        addGbaType(builder, gba_typeOffset);
        addLifetime(builder, lifetimeOffset);
        addBtid(builder, btidOffset);
        addNonce(builder, nonceOffset);
        addRid(builder, rid);
        return endXdmStoreGbaData(builder);
    }

    public static void startXdmStoreGbaData(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addNonce(FlatBufferBuilder builder, int nonceOffset) {
        builder.addOffset(1, nonceOffset, 0);
    }

    public static void addBtid(FlatBufferBuilder builder, int btidOffset) {
        builder.addOffset(2, btidOffset, 0);
    }

    public static void addLifetime(FlatBufferBuilder builder, int lifetimeOffset) {
        builder.addOffset(3, lifetimeOffset, 0);
    }

    public static void addGbaType(FlatBufferBuilder builder, int gbaTypeOffset) {
        builder.addOffset(4, gbaTypeOffset, 0);
    }

    public static void addNafFqdn(FlatBufferBuilder builder, int nafFqdnOffset) {
        builder.addOffset(5, nafFqdnOffset, 0);
    }

    public static void addHexProtocolId(FlatBufferBuilder builder, int hexProtocolIdOffset) {
        builder.addOffset(6, hexProtocolIdOffset, 0);
    }

    public static int createHexProtocolIdVector(FlatBufferBuilder builder, byte[] data) {
        builder.startVector(1, data.length, 1);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addByte(data[i]);
        }
        return builder.endVector();
    }

    public static void startHexProtocolIdVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(1, numElems, 1);
    }

    public static int endXdmStoreGbaData(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 14);
        builder.required(o, 16);
        return o;
    }
}
