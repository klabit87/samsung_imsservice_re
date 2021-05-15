package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AckMessage extends Table {
    public static AckMessage getRootAsAckMessage(ByteBuffer _bb) {
        return getRootAsAckMessage(_bb, new AckMessage());
    }

    public static AckMessage getRootAsAckMessage(ByteBuffer _bb, AckMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public AckMessage __assign(int _i, ByteBuffer _bb) {
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

    public EucContent content() {
        return content(new EucContent());
    }

    public EucContent content(EucContent obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public int status() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createAckMessage(FlatBufferBuilder builder, int baseOffset, int contentOffset, int status) {
        builder.startObject(3);
        addStatus(builder, status);
        addContent(builder, contentOffset);
        addBase(builder, baseOffset);
        return endAckMessage(builder);
    }

    public static void startAckMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder builder, int baseOffset) {
        builder.addOffset(0, baseOffset, 0);
    }

    public static void addContent(FlatBufferBuilder builder, int contentOffset) {
        builder.addOffset(1, contentOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int status) {
        builder.addInt(2, status, 0);
    }

    public static int endAckMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
