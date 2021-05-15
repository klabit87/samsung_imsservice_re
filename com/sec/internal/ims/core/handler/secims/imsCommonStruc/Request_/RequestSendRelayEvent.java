package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendRelayEvent extends Table {
    public static RequestSendRelayEvent getRootAsRequestSendRelayEvent(ByteBuffer _bb) {
        return getRootAsRequestSendRelayEvent(_bb, new RequestSendRelayEvent());
    }

    public static RequestSendRelayEvent getRootAsRequestSendRelayEvent(ByteBuffer _bb, RequestSendRelayEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendRelayEvent __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long streamId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendRelayEvent(FlatBufferBuilder builder, long stream_id, long event) {
        builder.startObject(2);
        addEvent(builder, event);
        addStreamId(builder, stream_id);
        return endRequestSendRelayEvent(builder);
    }

    public static void startRequestSendRelayEvent(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addStreamId(FlatBufferBuilder builder, long streamId) {
        builder.addInt(0, (int) streamId, 0);
    }

    public static void addEvent(FlatBufferBuilder builder, long event) {
        builder.addInt(1, (int) event, 0);
    }

    public static int endRequestSendRelayEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
