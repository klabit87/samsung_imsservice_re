package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImMessageReceived extends Table {
    public static ImMessageReceived getRootAsImMessageReceived(ByteBuffer _bb) {
        return getRootAsImMessageReceived(_bb, new ImMessageReceived());
    }

    public static ImMessageReceived getRootAsImMessageReceived(ByteBuffer _bb, ImMessageReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImMessageReceived __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public BaseSessionData sessionData() {
        return sessionData(new BaseSessionData());
    }

    public BaseSessionData sessionData(BaseSessionData obj) {
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

    public static int createImMessageReceived(FlatBufferBuilder builder, int session_dataOffset, int message_paramOffset) {
        builder.startObject(2);
        addMessageParam(builder, message_paramOffset);
        addSessionData(builder, session_dataOffset);
        return endImMessageReceived(builder);
    }

    public static void startImMessageReceived(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionData(FlatBufferBuilder builder, int sessionDataOffset) {
        builder.addOffset(0, sessionDataOffset, 0);
    }

    public static void addMessageParam(FlatBufferBuilder builder, int messageParamOffset) {
        builder.addOffset(1, messageParamOffset, 0);
    }

    public static int endImMessageReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
