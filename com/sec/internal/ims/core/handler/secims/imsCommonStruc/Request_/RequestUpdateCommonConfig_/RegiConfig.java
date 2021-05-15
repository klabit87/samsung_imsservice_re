package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegiConfig extends Table {
    public static RegiConfig getRootAsRegiConfig(ByteBuffer _bb) {
        return getRootAsRegiConfig(_bb, new RegiConfig());
    }

    public static RegiConfig getRootAsRegiConfig(ByteBuffer _bb, RegiConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RegiConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String imei() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imeiAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String supported() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer supportedAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String privacy() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer privacyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRegiConfig(FlatBufferBuilder builder, int imeiOffset, int supportedOffset, int privacyOffset) {
        builder.startObject(3);
        addPrivacy(builder, privacyOffset);
        addSupported(builder, supportedOffset);
        addImei(builder, imeiOffset);
        return endRegiConfig(builder);
    }

    public static void startRegiConfig(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addImei(FlatBufferBuilder builder, int imeiOffset) {
        builder.addOffset(0, imeiOffset, 0);
    }

    public static void addSupported(FlatBufferBuilder builder, int supportedOffset) {
        builder.addOffset(1, supportedOffset, 0);
    }

    public static void addPrivacy(FlatBufferBuilder builder, int privacyOffset) {
        builder.addOffset(2, privacyOffset, 0);
    }

    public static int endRegiConfig(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
