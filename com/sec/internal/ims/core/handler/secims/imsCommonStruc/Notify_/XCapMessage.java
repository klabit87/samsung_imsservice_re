package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XCapMessage extends Table {
    public static XCapMessage getRootAsXCapMessage(ByteBuffer _bb) {
        return getRootAsXCapMessage(_bb, new XCapMessage());
    }

    public static XCapMessage getRootAsXCapMessage(ByteBuffer _bb, XCapMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XCapMessage __assign(int _i, ByteBuffer _bb) {
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

    public int direction() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String xcapMessage() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer xcapMessageAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createXCapMessage(FlatBufferBuilder builder, long handle, int direction, int xcap_messageOffset) {
        builder.startObject(3);
        addXcapMessage(builder, xcap_messageOffset);
        addDirection(builder, direction);
        addHandle(builder, handle);
        return endXCapMessage(builder);
    }

    public static void startXCapMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addDirection(FlatBufferBuilder builder, int direction) {
        builder.addInt(1, direction, 0);
    }

    public static void addXcapMessage(FlatBufferBuilder builder, int xcapMessageOffset) {
        builder.addOffset(2, xcapMessageOffset, 0);
    }

    public static int endXCapMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
