package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestProgressIncomingCall extends Table {
    public static RequestProgressIncomingCall getRootAsRequestProgressIncomingCall(ByteBuffer _bb) {
        return getRootAsRequestProgressIncomingCall(_bb, new RequestProgressIncomingCall());
    }

    public static RequestProgressIncomingCall getRootAsRequestProgressIncomingCall(ByteBuffer _bb, RequestProgressIncomingCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestProgressIncomingCall __assign(int _i, ByteBuffer _bb) {
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

    public ExtraHeader extraHeader() {
        return extraHeader(new ExtraHeader());
    }

    public ExtraHeader extraHeader(ExtraHeader obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestProgressIncomingCall(FlatBufferBuilder builder, long session, int extra_headerOffset) {
        builder.startObject(2);
        addExtraHeader(builder, extra_headerOffset);
        addSession(builder, session);
        return endRequestProgressIncomingCall(builder);
    }

    public static void startRequestProgressIncomingCall(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addExtraHeader(FlatBufferBuilder builder, int extraHeaderOffset) {
        builder.addOffset(1, extraHeaderOffset, 0);
    }

    public static int endRequestProgressIncomingCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
