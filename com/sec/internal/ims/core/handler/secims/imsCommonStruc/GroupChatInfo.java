package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatInfo extends Table {
    public static GroupChatInfo getRootAsGroupChatInfo(ByteBuffer _bb) {
        return getRootAsGroupChatInfo(_bb, new GroupChatInfo());
    }

    public static GroupChatInfo getRootAsGroupChatInfo(ByteBuffer _bb, GroupChatInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public GroupChatInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String method() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer methodAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
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

    public String conversationId() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String subject() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createGroupChatInfo(FlatBufferBuilder builder, int methodOffset, int uriOffset, int conversationIdOffset, int subjectOffset) {
        builder.startObject(4);
        addSubject(builder, subjectOffset);
        addConversationId(builder, conversationIdOffset);
        addUri(builder, uriOffset);
        addMethod(builder, methodOffset);
        return endGroupChatInfo(builder);
    }

    public static void startGroupChatInfo(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addMethod(FlatBufferBuilder builder, int methodOffset) {
        builder.addOffset(0, methodOffset, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addConversationId(FlatBufferBuilder builder, int conversationIdOffset) {
        builder.addOffset(2, conversationIdOffset, 0);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(3, subjectOffset, 0);
    }

    public static int endGroupChatInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        return o;
    }
}
