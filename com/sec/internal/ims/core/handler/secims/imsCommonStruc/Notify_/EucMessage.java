package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EucMessage extends Table {
    public static EucMessage getRootAsEucMessage(ByteBuffer _bb) {
        return getRootAsEucMessage(_bb, new EucMessage());
    }

    public static EucMessage getRootAsEucMessage(ByteBuffer _bb, EucMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EucMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public byte messageType() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table message(Table obj) {
        int o = __offset(6);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createEucMessage(FlatBufferBuilder builder, byte message_type, int messageOffset) {
        builder.startObject(2);
        addMessage(builder, messageOffset);
        addMessageType(builder, message_type);
        return endEucMessage(builder);
    }

    public static void startEucMessage(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addMessageType(FlatBufferBuilder builder, byte messageType) {
        builder.addByte(0, messageType, 0);
    }

    public static void addMessage(FlatBufferBuilder builder, int messageOffset) {
        builder.addOffset(1, messageOffset, 0);
    }

    public static int endEucMessage(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
