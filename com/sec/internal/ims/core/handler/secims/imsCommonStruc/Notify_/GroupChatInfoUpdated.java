package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatInfoUpdated extends Table {
    public static GroupChatInfoUpdated getRootAsGroupChatInfoUpdated(ByteBuffer _bb) {
        return getRootAsGroupChatInfoUpdated(_bb, new GroupChatInfoUpdated());
    }

    public static GroupChatInfoUpdated getRootAsGroupChatInfoUpdated(ByteBuffer _bb, GroupChatInfoUpdated obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public GroupChatInfoUpdated __assign(int _i, ByteBuffer _bb) {
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

    public ImConfInfoUpdated info() {
        return info(new ImConfInfoUpdated());
    }

    public ImConfInfoUpdated info(ImConfInfoUpdated obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public long uaHandle() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createGroupChatInfoUpdated(FlatBufferBuilder builder, int uriOffset, int infoOffset, long ua_handle) {
        builder.startObject(3);
        addUaHandle(builder, ua_handle);
        addInfo(builder, infoOffset);
        addUri(builder, uriOffset);
        return endGroupChatInfoUpdated(builder);
    }

    public static void startGroupChatInfoUpdated(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addInfo(FlatBufferBuilder builder, int infoOffset) {
        builder.addOffset(1, infoOffset, 0);
    }

    public static void addUaHandle(FlatBufferBuilder builder, long uaHandle) {
        builder.addInt(2, (int) uaHandle, 0);
    }

    public static int endGroupChatInfoUpdated(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
