package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImSessionInvited extends Table {
    public static ImSessionInvited getRootAsImSessionInvited(ByteBuffer _bb) {
        return getRootAsImSessionInvited(_bb, new ImSessionInvited());
    }

    public static ImSessionInvited getRootAsImSessionInvited(ByteBuffer _bb, ImSessionInvited obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImSessionInvited __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public ImSessionParam session() {
        return session(new ImSessionParam());
    }

    public ImSessionParam session(ImSessionParam obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ImMessageParam messageParam() {
        return messageParam(new ImMessageParam());
    }

    public ImMessageParam messageParam(ImMessageParam obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String remoteMsrpAddr() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteMsrpAddrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean isDeferred() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isForStoredNoti() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long userHandle() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createImSessionInvited(FlatBufferBuilder builder, int sessionOffset, int message_paramOffset, int remote_msrp_addrOffset, boolean is_deferred, boolean is_for_stored_noti, long user_handle) {
        builder.startObject(6);
        addUserHandle(builder, user_handle);
        addRemoteMsrpAddr(builder, remote_msrp_addrOffset);
        addMessageParam(builder, message_paramOffset);
        addSession(builder, sessionOffset);
        addIsForStoredNoti(builder, is_for_stored_noti);
        addIsDeferred(builder, is_deferred);
        return endImSessionInvited(builder);
    }

    public static void startImSessionInvited(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addSession(FlatBufferBuilder builder, int sessionOffset) {
        builder.addOffset(0, sessionOffset, 0);
    }

    public static void addMessageParam(FlatBufferBuilder builder, int messageParamOffset) {
        builder.addOffset(1, messageParamOffset, 0);
    }

    public static void addRemoteMsrpAddr(FlatBufferBuilder builder, int remoteMsrpAddrOffset) {
        builder.addOffset(2, remoteMsrpAddrOffset, 0);
    }

    public static void addIsDeferred(FlatBufferBuilder builder, boolean isDeferred) {
        builder.addBoolean(3, isDeferred, false);
    }

    public static void addIsForStoredNoti(FlatBufferBuilder builder, boolean isForStoredNoti) {
        builder.addBoolean(4, isForStoredNoti, false);
    }

    public static void addUserHandle(FlatBufferBuilder builder, long userHandle) {
        builder.addInt(5, (int) userHandle, 0);
    }

    public static int endImSessionInvited(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
