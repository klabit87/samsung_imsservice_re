package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XdmReqGbaData extends Table {
    public static XdmReqGbaData getRootAsXdmReqGbaData(ByteBuffer _bb) {
        return getRootAsXdmReqGbaData(_bb, new XdmReqGbaData());
    }

    public static XdmReqGbaData getRootAsXdmReqGbaData(ByteBuffer _bb, XdmReqGbaData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XdmReqGbaData __assign(int _i, ByteBuffer _bb) {
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

    public String nafFqdn() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nafFqdnAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int hexProtocolId(int j) {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.get(__vector(o) + (j * 1)) & 255;
        }
        return 0;
    }

    public int hexProtocolIdLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer hexProtocolIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createXdmReqGbaData(FlatBufferBuilder builder, long rid, int naf_fqdnOffset, int hex_protocol_idOffset) {
        builder.startObject(3);
        addHexProtocolId(builder, hex_protocol_idOffset);
        addNafFqdn(builder, naf_fqdnOffset);
        addRid(builder, rid);
        return endXdmReqGbaData(builder);
    }

    public static void startXdmReqGbaData(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addNafFqdn(FlatBufferBuilder builder, int nafFqdnOffset) {
        builder.addOffset(1, nafFqdnOffset, 0);
    }

    public static void addHexProtocolId(FlatBufferBuilder builder, int hexProtocolIdOffset) {
        builder.addOffset(2, hexProtocolIdOffset, 0);
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

    public static int endXdmReqGbaData(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
