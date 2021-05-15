package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImMessage extends Table {
    public static RequestSendImMessage getRootAsRequestSendImMessage(ByteBuffer _bb) {
        return getRootAsRequestSendImMessage(_bb, new RequestSendImMessage());
    }

    public static RequestSendImMessage getRootAsRequestSendImMessage(ByteBuffer _bb, RequestSendImMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendImMessage __assign(int _i, ByteBuffer _bb) {
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

    public static int createRequestSendImMessage(FlatBufferBuilder builder, int session_dataOffset, int message_paramOffset) {
        builder.startObject(2);
        addMessageParam(builder, message_paramOffset);
        addSessionData(builder, session_dataOffset);
        return endRequestSendImMessage(builder);
    }

    public static void startRequestSendImMessage(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionData(FlatBufferBuilder builder, int sessionDataOffset) {
        builder.addOffset(0, sessionDataOffset, 0);
    }

    public static void addMessageParam(FlatBufferBuilder builder, int messageParamOffset) {
        builder.addOffset(1, messageParamOffset, 0);
    }

    public static int endRequestSendImMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
