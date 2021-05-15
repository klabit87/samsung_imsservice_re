package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUserDisconnectionInfo extends Table {
    public static ImConfUserDisconnectionInfo getRootAsImConfUserDisconnectionInfo(ByteBuffer _bb) {
        return getRootAsImConfUserDisconnectionInfo(_bb, new ImConfUserDisconnectionInfo());
    }

    public static ImConfUserDisconnectionInfo getRootAsImConfUserDisconnectionInfo(ByteBuffer _bb, ImConfUserDisconnectionInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImConfUserDisconnectionInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String when() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer whenAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
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

    public String by() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer byAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createImConfUserDisconnectionInfo(FlatBufferBuilder builder, int whenOffset, int reasonOffset, int byOffset) {
        builder.startObject(3);
        addBy(builder, byOffset);
        addReason(builder, reasonOffset);
        addWhen(builder, whenOffset);
        return endImConfUserDisconnectionInfo(builder);
    }

    public static void startImConfUserDisconnectionInfo(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addWhen(FlatBufferBuilder builder, int whenOffset) {
        builder.addOffset(0, whenOffset, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reasonOffset) {
        builder.addOffset(1, reasonOffset, 0);
    }

    public static void addBy(FlatBufferBuilder builder, int byOffset) {
        builder.addOffset(2, byOffset, 0);
    }

    public static int endImConfUserDisconnectionInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
