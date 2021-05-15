package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresenceUnpublish extends Table {
    public static RequestPresenceUnpublish getRootAsRequestPresenceUnpublish(ByteBuffer _bb) {
        return getRootAsRequestPresenceUnpublish(_bb, new RequestPresenceUnpublish());
    }

    public static RequestPresenceUnpublish getRootAsRequestPresenceUnpublish(ByteBuffer _bb, RequestPresenceUnpublish obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPresenceUnpublish __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestPresenceUnpublish(FlatBufferBuilder builder, long handle) {
        builder.startObject(1);
        addHandle(builder, handle);
        return endRequestPresenceUnpublish(builder);
    }

    public static void startRequestPresenceUnpublish(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static int endRequestPresenceUnpublish(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
