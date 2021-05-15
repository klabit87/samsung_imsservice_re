package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReceiveMessageNotification extends Table {
    public static ReceiveMessageNotification getRootAsReceiveMessageNotification(ByteBuffer _bb) {
        return getRootAsReceiveMessageNotification(_bb, new ReceiveMessageNotification());
    }

    public static ReceiveMessageNotification getRootAsReceiveMessageNotification(ByteBuffer _bb, ReceiveMessageNotification obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReceiveMessageNotification __assign(int _i, ByteBuffer _bb) {
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

    public String messageBody() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer messageBodyAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createReceiveMessageNotification(FlatBufferBuilder builder, long handle, int message_bodyOffset) {
        builder.startObject(2);
        addMessageBody(builder, message_bodyOffset);
        addHandle(builder, handle);
        return endReceiveMessageNotification(builder);
    }

    public static void startReceiveMessageNotification(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addMessageBody(FlatBufferBuilder builder, int messageBodyOffset) {
        builder.addOffset(1, messageBodyOffset, 0);
    }

    public static int endReceiveMessageNotification(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
