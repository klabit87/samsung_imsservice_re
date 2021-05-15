package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImsBuffer extends Table {
    public static ImsBuffer getRootAsImsBuffer(ByteBuffer _bb) {
        return getRootAsImsBuffer(_bb, new ImsBuffer());
    }

    public static ImsBuffer getRootAsImsBuffer(ByteBuffer _bb, ImsBuffer obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImsBuffer __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long trid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public byte msgType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table msg(Table obj) {
        int o = __offset(8);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createImsBuffer(FlatBufferBuilder builder, long trid, byte msg_type, int msgOffset) {
        builder.startObject(3);
        addMsg(builder, msgOffset);
        addTrid(builder, trid);
        addMsgType(builder, msg_type);
        return endImsBuffer(builder);
    }

    public static void startImsBuffer(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addTrid(FlatBufferBuilder builder, long trid) {
        builder.addInt(0, (int) trid, 0);
    }

    public static void addMsgType(FlatBufferBuilder builder, byte msgType) {
        builder.addByte(1, msgType, 0);
    }

    public static void addMsg(FlatBufferBuilder builder, int msgOffset) {
        builder.addOffset(2, msgOffset, 0);
    }

    public static int endImsBuffer(FlatBufferBuilder builder) {
        return builder.endObject();
    }

    public static void finishImsBufferBuffer(FlatBufferBuilder builder, int offset) {
        builder.finish(offset);
    }
}
