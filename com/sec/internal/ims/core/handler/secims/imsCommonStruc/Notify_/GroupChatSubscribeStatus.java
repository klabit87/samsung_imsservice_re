package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatSubscribeStatus extends Table {
    public static GroupChatSubscribeStatus getRootAsGroupChatSubscribeStatus(ByteBuffer _bb) {
        return getRootAsGroupChatSubscribeStatus(_bb, new GroupChatSubscribeStatus());
    }

    public static GroupChatSubscribeStatus getRootAsGroupChatSubscribeStatus(ByteBuffer _bb, GroupChatSubscribeStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public GroupChatSubscribeStatus __assign(int _i, ByteBuffer _bb) {
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

    public boolean isSuccess() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long sipErrorCode() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String sipErrorPhrase() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipErrorPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String subscriptionId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createGroupChatSubscribeStatus(FlatBufferBuilder builder, long handle, boolean is_success, long sip_error_code, int sip_error_phraseOffset, int subscription_idOffset) {
        builder.startObject(5);
        addSubscriptionId(builder, subscription_idOffset);
        addSipErrorPhrase(builder, sip_error_phraseOffset);
        addSipErrorCode(builder, sip_error_code);
        addHandle(builder, handle);
        addIsSuccess(builder, is_success);
        return endGroupChatSubscribeStatus(builder);
    }

    public static void startGroupChatSubscribeStatus(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addIsSuccess(FlatBufferBuilder builder, boolean isSuccess) {
        builder.addBoolean(1, isSuccess, false);
    }

    public static void addSipErrorCode(FlatBufferBuilder builder, long sipErrorCode) {
        builder.addInt(2, (int) sipErrorCode, 0);
    }

    public static void addSipErrorPhrase(FlatBufferBuilder builder, int sipErrorPhraseOffset) {
        builder.addOffset(3, sipErrorPhraseOffset, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, int subscriptionIdOffset) {
        builder.addOffset(4, subscriptionIdOffset, 0);
    }

    public static int endGroupChatSubscribeStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        builder.required(o, 12);
        return o;
    }
}
