package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImExtension;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtIncomingSession extends Table {
    public static FtIncomingSession getRootAsFtIncomingSession(ByteBuffer _bb) {
        return getRootAsFtIncomingSession(_bb, new FtIncomingSession());
    }

    public static FtIncomingSession getRootAsFtIncomingSession(ByteBuffer _bb, FtIncomingSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public FtIncomingSession __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public BaseSessionData session() {
        return session(new BaseSessionData());
    }

    public BaseSessionData session(BaseSessionData obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public FtPayloadParam payload() {
        return payload(new FtPayloadParam());
    }

    public FtPayloadParam payload(FtPayloadParam obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public long userHandle() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImExtension extension() {
        return extension(new ImExtension());
    }

    public ImExtension extension(ImExtension obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public boolean isLmm() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createFtIncomingSession(FlatBufferBuilder builder, int sessionOffset, int payloadOffset, long user_handle, int extensionOffset, boolean is_lmm) {
        builder.startObject(5);
        addExtension(builder, extensionOffset);
        addUserHandle(builder, user_handle);
        addPayload(builder, payloadOffset);
        addSession(builder, sessionOffset);
        addIsLmm(builder, is_lmm);
        return endFtIncomingSession(builder);
    }

    public static void startFtIncomingSession(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addSession(FlatBufferBuilder builder, int sessionOffset) {
        builder.addOffset(0, sessionOffset, 0);
    }

    public static void addPayload(FlatBufferBuilder builder, int payloadOffset) {
        builder.addOffset(1, payloadOffset, 0);
    }

    public static void addUserHandle(FlatBufferBuilder builder, long userHandle) {
        builder.addInt(2, (int) userHandle, 0);
    }

    public static void addExtension(FlatBufferBuilder builder, int extensionOffset) {
        builder.addOffset(3, extensionOffset, 0);
    }

    public static void addIsLmm(FlatBufferBuilder builder, boolean isLmm) {
        builder.addBoolean(4, isLmm, false);
    }

    public static int endFtIncomingSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
