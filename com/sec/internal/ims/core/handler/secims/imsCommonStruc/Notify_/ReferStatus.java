package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReferStatus extends Table {
    public static ReferStatus getRootAsReferStatus(ByteBuffer _bb) {
        return getRootAsReferStatus(_bb, new ReferStatus());
    }

    public static ReferStatus getRootAsReferStatus(ByteBuffer _bb, ReferStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReferStatus __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long session() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long statusCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createReferStatus(FlatBufferBuilder builder, long session, long status_code) {
        builder.startObject(2);
        addStatusCode(builder, status_code);
        addSession(builder, session);
        return endReferStatus(builder);
    }

    public static void startReferStatus(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(1, (int) statusCode, 0);
    }

    public static int endReferStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
