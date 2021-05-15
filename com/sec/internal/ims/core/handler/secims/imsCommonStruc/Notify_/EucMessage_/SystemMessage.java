package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SystemMessage extends Table {
    public static SystemMessage getRootAsSystemMessage(ByteBuffer _bb) {
        return getRootAsSystemMessage(_bb, new SystemMessage());
    }

    public static SystemMessage getRootAsSystemMessage(ByteBuffer _bb, SystemMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SystemMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public BaseMessage base() {
        return base(new BaseMessage());
    }

    public BaseMessage base(BaseMessage obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String type() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String data() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dataAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSystemMessage(FlatBufferBuilder builder, int baseOffset, int typeOffset, int dataOffset) {
        builder.startObject(3);
        addData(builder, dataOffset);
        addType(builder, typeOffset);
        addBase(builder, baseOffset);
        return endSystemMessage(builder);
    }

    public static void startSystemMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder builder, int baseOffset) {
        builder.addOffset(0, baseOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int typeOffset) {
        builder.addOffset(1, typeOffset, 0);
    }

    public static void addData(FlatBufferBuilder builder, int dataOffset) {
        builder.addOffset(2, dataOffset, 0);
    }

    public static int endSystemMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
