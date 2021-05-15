package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.GroupChatInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GroupChatListUpdated extends Table {
    public static GroupChatListUpdated getRootAsGroupChatListUpdated(ByteBuffer _bb) {
        return getRootAsGroupChatListUpdated(_bb, new GroupChatListUpdated());
    }

    public static GroupChatListUpdated getRootAsGroupChatListUpdated(ByteBuffer _bb, GroupChatListUpdated obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public GroupChatListUpdated __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long version() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean increaseMode() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public GroupChatInfo groupChats(int j) {
        return groupChats(new GroupChatInfo(), j);
    }

    public GroupChatInfo groupChats(GroupChatInfo obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int groupChatsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long uaHandle() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createGroupChatListUpdated(FlatBufferBuilder builder, long version, boolean increaseMode, int groupChatsOffset, long ua_handle) {
        builder.startObject(4);
        addUaHandle(builder, ua_handle);
        addGroupChats(builder, groupChatsOffset);
        addVersion(builder, version);
        addIncreaseMode(builder, increaseMode);
        return endGroupChatListUpdated(builder);
    }

    public static void startGroupChatListUpdated(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addVersion(FlatBufferBuilder builder, long version) {
        builder.addInt(0, (int) version, 0);
    }

    public static void addIncreaseMode(FlatBufferBuilder builder, boolean increaseMode) {
        builder.addBoolean(1, increaseMode, false);
    }

    public static void addGroupChats(FlatBufferBuilder builder, int groupChatsOffset) {
        builder.addOffset(2, groupChatsOffset, 0);
    }

    public static int createGroupChatsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startGroupChatsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addUaHandle(FlatBufferBuilder builder, long uaHandle) {
        builder.addInt(3, (int) uaHandle, 0);
    }

    public static int endGroupChatListUpdated(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
