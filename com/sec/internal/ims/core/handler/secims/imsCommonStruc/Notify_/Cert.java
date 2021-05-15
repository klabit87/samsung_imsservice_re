package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Cert extends Table {
    public static Cert getRootAsCert(ByteBuffer _bb) {
        return getRootAsCert(_bb, new Cert());
    }

    public static Cert getRootAsCert(ByteBuffer _bb, Cert obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Cert __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int certData(int j) {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.get(__vector(o) + (j * 1)) & 255;
        }
        return 0;
    }

    public int certDataLength() {
        int o = __offset(4);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer certDataAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createCert(FlatBufferBuilder builder, int cert_dataOffset) {
        builder.startObject(1);
        addCertData(builder, cert_dataOffset);
        return endCert(builder);
    }

    public static void startCert(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addCertData(FlatBufferBuilder builder, int certDataOffset) {
        builder.addOffset(0, certDataOffset, 0);
    }

    public static int createCertDataVector(FlatBufferBuilder builder, byte[] data) {
        builder.startVector(1, data.length, 1);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addByte(data[i]);
        }
        return builder.endVector();
    }

    public static void startCertDataVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(1, numElems, 1);
    }

    public static int endCert(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
