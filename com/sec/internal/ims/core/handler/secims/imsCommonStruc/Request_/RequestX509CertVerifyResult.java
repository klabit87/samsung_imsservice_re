package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestX509CertVerifyResult extends Table {
    public static RequestX509CertVerifyResult getRootAsRequestX509CertVerifyResult(ByteBuffer _bb) {
        return getRootAsRequestX509CertVerifyResult(_bb, new RequestX509CertVerifyResult());
    }

    public static RequestX509CertVerifyResult getRootAsRequestX509CertVerifyResult(ByteBuffer _bb, RequestX509CertVerifyResult obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestX509CertVerifyResult __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public boolean result() {
        int o = __offset(4);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String reason() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestX509CertVerifyResult(FlatBufferBuilder builder, boolean result, int reasonOffset) {
        builder.startObject(2);
        addReason(builder, reasonOffset);
        addResult(builder, result);
        return endRequestX509CertVerifyResult(builder);
    }

    public static void startRequestX509CertVerifyResult(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addResult(FlatBufferBuilder builder, boolean result) {
        builder.addBoolean(0, result, false);
    }

    public static void addReason(FlatBufferBuilder builder, int reasonOffset) {
        builder.addOffset(1, reasonOffset, 0);
    }

    public static int endRequestX509CertVerifyResult(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
