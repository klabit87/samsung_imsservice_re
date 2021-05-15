package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegistrationImpu extends Table {
    public static RegistrationImpu getRootAsRegistrationImpu(ByteBuffer _bb) {
        return getRootAsRegistrationImpu(_bb, new RegistrationImpu());
    }

    public static RegistrationImpu getRootAsRegistrationImpu(ByteBuffer _bb, RegistrationImpu obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RegistrationImpu __assign(int _i, ByteBuffer _bb) {
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

    public String impu() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRegistrationImpu(FlatBufferBuilder builder, long handle, int impuOffset) {
        builder.startObject(2);
        addImpu(builder, impuOffset);
        addHandle(builder, handle);
        return endRegistrationImpu(builder);
    }

    public static void startRegistrationImpu(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(1, impuOffset, 0);
    }

    public static int endRegistrationImpu(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
