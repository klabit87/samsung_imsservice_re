package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SlmLMMInvited extends Table {
    public static SlmLMMInvited getRootAsSlmLMMInvited(ByteBuffer _bb) {
        return getRootAsSlmLMMInvited(_bb, new SlmLMMInvited());
    }

    public static SlmLMMInvited getRootAsSlmLMMInvited(ByteBuffer _bb, SlmLMMInvited obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SlmLMMInvited __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String userAlias() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String sender() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long userHandle() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createSlmLMMInvited(FlatBufferBuilder builder, long session_handle, int user_aliasOffset, int senderOffset, long user_handle) {
        builder.startObject(4);
        addUserHandle(builder, user_handle);
        addSender(builder, senderOffset);
        addUserAlias(builder, user_aliasOffset);
        addSessionHandle(builder, session_handle);
        return endSlmLMMInvited(builder);
    }

    public static void startSlmLMMInvited(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(1, userAliasOffset, 0);
    }

    public static void addSender(FlatBufferBuilder builder, int senderOffset) {
        builder.addOffset(2, senderOffset, 0);
    }

    public static void addUserHandle(FlatBufferBuilder builder, long userHandle) {
        builder.addInt(3, (int) userHandle, 0);
    }

    public static int endSlmLMMInvited(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
