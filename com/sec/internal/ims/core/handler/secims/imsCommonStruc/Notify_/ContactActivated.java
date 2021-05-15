package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactActivated extends Table {
    public static ContactActivated getRootAsContactActivated(ByteBuffer _bb) {
        return getRootAsContactActivated(_bb, new ContactActivated());
    }

    public static ContactActivated getRootAsContactActivated(ByteBuffer _bb, ContactActivated obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactActivated __assign(int _i, ByteBuffer _bb) {
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

    public static int createContactActivated(FlatBufferBuilder builder, long handle) {
        builder.startObject(1);
        addHandle(builder, handle);
        return endContactActivated(builder);
    }

    public static void startContactActivated(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static int endContactActivated(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
