package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class X509CertVerifyRequest extends Table {
    public static X509CertVerifyRequest getRootAsX509CertVerifyRequest(ByteBuffer _bb) {
        return getRootAsX509CertVerifyRequest(_bb, new X509CertVerifyRequest());
    }

    public static X509CertVerifyRequest getRootAsX509CertVerifyRequest(ByteBuffer _bb, X509CertVerifyRequest obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public X509CertVerifyRequest __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public Cert cert(int j) {
        return cert(new Cert(), j);
    }

    public Cert cert(Cert obj, int j) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int certLength() {
        int o = __offset(4);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String keyExchangeAlgo() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer keyExchangeAlgoAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createX509CertVerifyRequest(FlatBufferBuilder builder, int certOffset, int key_exchange_algoOffset) {
        builder.startObject(2);
        addKeyExchangeAlgo(builder, key_exchange_algoOffset);
        addCert(builder, certOffset);
        return endX509CertVerifyRequest(builder);
    }

    public static void startX509CertVerifyRequest(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addCert(FlatBufferBuilder builder, int certOffset) {
        builder.addOffset(0, certOffset, 0);
    }

    public static int createCertVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startCertVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addKeyExchangeAlgo(FlatBufferBuilder builder, int keyExchangeAlgoOffset) {
        builder.addOffset(1, keyExchangeAlgoOffset, 0);
    }

    public static int endX509CertVerifyRequest(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
