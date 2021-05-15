package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class WarningHdr extends Table {
    public static WarningHdr getRootAsWarningHdr(ByteBuffer _bb) {
        return getRootAsWarningHdr(_bb, new WarningHdr());
    }

    public static WarningHdr getRootAsWarningHdr(ByteBuffer _bb, WarningHdr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public WarningHdr __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int code() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return -1;
    }

    public String host() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer hostAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String text() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createWarningHdr(FlatBufferBuilder builder, int code, int hostOffset, int textOffset) {
        builder.startObject(3);
        addText(builder, textOffset);
        addHost(builder, hostOffset);
        addCode(builder, code);
        return endWarningHdr(builder);
    }

    public static void startWarningHdr(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addCode(FlatBufferBuilder builder, int code) {
        builder.addInt(0, code, -1);
    }

    public static void addHost(FlatBufferBuilder builder, int hostOffset) {
        builder.addOffset(1, hostOffset, 0);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(2, textOffset, 0);
    }

    public static int endWarningHdr(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
