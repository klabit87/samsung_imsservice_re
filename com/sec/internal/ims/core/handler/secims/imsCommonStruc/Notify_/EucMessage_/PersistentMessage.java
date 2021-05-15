package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PersistentMessage extends Table {
    public static PersistentMessage getRootAsPersistentMessage(ByteBuffer _bb) {
        return getRootAsPersistentMessage(_bb, new PersistentMessage());
    }

    public static PersistentMessage getRootAsPersistentMessage(ByteBuffer _bb, PersistentMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public PersistentMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public RequestMessage request() {
        return request(new RequestMessage());
    }

    public RequestMessage request(RequestMessage obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createPersistentMessage(FlatBufferBuilder builder, int requestOffset) {
        builder.startObject(1);
        addRequest(builder, requestOffset);
        return endPersistentMessage(builder);
    }

    public static void startPersistentMessage(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addRequest(FlatBufferBuilder builder, int requestOffset) {
        builder.addOffset(0, requestOffset, 0);
    }

    public static int endPersistentMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
