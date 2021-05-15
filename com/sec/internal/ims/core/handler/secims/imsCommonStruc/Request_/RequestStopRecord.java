package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStopRecord extends Table {
    public static RequestStopRecord getRootAsRequestStopRecord(ByteBuffer _bb) {
        return getRootAsRequestStopRecord(_bb, new RequestStopRecord());
    }

    public static RequestStopRecord getRootAsRequestStopRecord(ByteBuffer _bb, RequestStopRecord obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStopRecord __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestStopRecord(FlatBufferBuilder builder, long handle, long session) {
        builder.startObject(2);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestStopRecord(builder);
    }

    public static void startRequestStopRecord(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static int endRequestStopRecord(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
