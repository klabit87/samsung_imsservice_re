package com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Status extends Table {
    public static Status getRootAsStatus(ByteBuffer _bb) {
        return getRootAsStatus(_bb, new Status());
    }

    public static Status getRootAsStatus(ByteBuffer _bb, Status obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Status __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String basic() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer basicAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createStatus(FlatBufferBuilder builder, int basicOffset) {
        builder.startObject(1);
        addBasic(builder, basicOffset);
        return endStatus(builder);
    }

    public static void startStatus(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addBasic(FlatBufferBuilder builder, int basicOffset) {
        builder.addOffset(0, basicOffset, 0);
    }

    public static int endStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
