package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartImSession extends Table {
    public static RequestStartImSession getRootAsRequestStartImSession(ByteBuffer _bb) {
        return getRootAsRequestStartImSession(_bb, new RequestStartImSession());
    }

    public static RequestStartImSession getRootAsRequestStartImSession(ByteBuffer _bb, RequestStartImSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartImSession __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long registrationHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImSessionParam session() {
        return session(new ImSessionParam());
    }

    public ImSessionParam session(ImSessionParam obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ImMessageParam messageParam() {
        return messageParam(new ImMessageParam());
    }

    public ImMessageParam messageParam(ImMessageParam obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestStartImSession(FlatBufferBuilder builder, long registration_handle, int sessionOffset, int message_paramOffset) {
        builder.startObject(3);
        addMessageParam(builder, message_paramOffset);
        addSession(builder, sessionOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestStartImSession(builder);
    }

    public static void startRequestStartImSession(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, int sessionOffset) {
        builder.addOffset(1, sessionOffset, 0);
    }

    public static void addMessageParam(FlatBufferBuilder builder, int messageParamOffset) {
        builder.addOffset(2, messageParamOffset, 0);
    }

    public static int endRequestStartImSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
