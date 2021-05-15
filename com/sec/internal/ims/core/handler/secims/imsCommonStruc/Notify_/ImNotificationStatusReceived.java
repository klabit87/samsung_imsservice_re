package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImNotificationStatusReceived extends Table {
    public static ImNotificationStatusReceived getRootAsImNotificationStatusReceived(ByteBuffer _bb) {
        return getRootAsImNotificationStatusReceived(_bb, new ImNotificationStatusReceived());
    }

    public static ImNotificationStatusReceived getRootAsImNotificationStatusReceived(ByteBuffer _bb, ImNotificationStatusReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImNotificationStatusReceived __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String uri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String cpimDateTime() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cpimDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public ImNotificationParam status() {
        return status(new ImNotificationParam());
    }

    public ImNotificationParam status(ImNotificationParam obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createImNotificationStatusReceived(FlatBufferBuilder builder, long session_id, int uriOffset, int cpim_date_timeOffset, int statusOffset) {
        builder.startObject(4);
        addStatus(builder, statusOffset);
        addCpimDateTime(builder, cpim_date_timeOffset);
        addUri(builder, uriOffset);
        addSessionId(builder, session_id);
        return endImNotificationStatusReceived(builder);
    }

    public static void startImNotificationStatusReceived(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addCpimDateTime(FlatBufferBuilder builder, int cpimDateTimeOffset) {
        builder.addOffset(2, cpimDateTimeOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(3, statusOffset, 0);
    }

    public static int endImNotificationStatusReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        return o;
    }
}
