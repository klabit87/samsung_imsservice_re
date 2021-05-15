package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMediaEvent extends Table {
    public static RequestSendMediaEvent getRootAsRequestSendMediaEvent(ByteBuffer _bb) {
        return getRootAsRequestSendMediaEvent(_bb, new RequestSendMediaEvent());
    }

    public static RequestSendMediaEvent getRootAsRequestSendMediaEvent(ByteBuffer _bb, RequestSendMediaEvent obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendMediaEvent __assign(int _i, ByteBuffer _bb) {
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

    public long target() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long eventType() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendMediaEvent(FlatBufferBuilder builder, long handle, long target, long event, long event_type) {
        builder.startObject(4);
        addEventType(builder, event_type);
        addEvent(builder, event);
        addTarget(builder, target);
        addHandle(builder, handle);
        return endRequestSendMediaEvent(builder);
    }

    public static void startRequestSendMediaEvent(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addTarget(FlatBufferBuilder builder, long target) {
        builder.addInt(1, (int) target, 0);
    }

    public static void addEvent(FlatBufferBuilder builder, long event) {
        builder.addInt(2, (int) event, 0);
    }

    public static void addEventType(FlatBufferBuilder builder, long eventType) {
        builder.addInt(3, (int) eventType, 0);
    }

    public static int endRequestSendMediaEvent(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
