package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class UpdateRouteTable extends Table {
    public static UpdateRouteTable getRootAsUpdateRouteTable(ByteBuffer _bb) {
        return getRootAsUpdateRouteTable(_bb, new UpdateRouteTable());
    }

    public static UpdateRouteTable getRootAsUpdateRouteTable(ByteBuffer _bb, UpdateRouteTable obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public UpdateRouteTable __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int operation() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long handle() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String address() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer addressAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createUpdateRouteTable(FlatBufferBuilder builder, int operation, long handle, int addressOffset) {
        builder.startObject(3);
        addAddress(builder, addressOffset);
        addHandle(builder, handle);
        addOperation(builder, operation);
        return endUpdateRouteTable(builder);
    }

    public static void startUpdateRouteTable(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addOperation(FlatBufferBuilder builder, int operation) {
        builder.addInt(0, operation, 0);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(1, (int) handle, 0);
    }

    public static void addAddress(FlatBufferBuilder builder, int addressOffset) {
        builder.addOffset(2, addressOffset, 0);
    }

    public static int endUpdateRouteTable(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
