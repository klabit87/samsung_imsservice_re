package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MessageRevokeResponseReceived extends Table {
    public static MessageRevokeResponseReceived getRootAsMessageRevokeResponseReceived(ByteBuffer _bb) {
        return getRootAsMessageRevokeResponseReceived(_bb, new MessageRevokeResponseReceived());
    }

    public static MessageRevokeResponseReceived getRootAsMessageRevokeResponseReceived(ByteBuffer _bb, MessageRevokeResponseReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public MessageRevokeResponseReceived __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String imdnMessageId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean result() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createMessageRevokeResponseReceived(FlatBufferBuilder builder, int uriOffset, int imdn_message_idOffset, boolean result) {
        builder.startObject(3);
        addImdnMessageId(builder, imdn_message_idOffset);
        addUri(builder, uriOffset);
        addResult(builder, result);
        return endMessageRevokeResponseReceived(builder);
    }

    public static void startMessageRevokeResponseReceived(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(1, imdnMessageIdOffset, 0);
    }

    public static void addResult(FlatBufferBuilder builder, boolean result) {
        builder.addBoolean(2, result, false);
    }

    public static int endMessageRevokeResponseReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
