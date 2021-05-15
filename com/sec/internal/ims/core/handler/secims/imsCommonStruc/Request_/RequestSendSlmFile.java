package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendSlmFile extends Table {
    public static RequestSendSlmFile getRootAsRequestSendSlmFile(ByteBuffer _bb) {
        return getRootAsRequestSendSlmFile(_bb, new RequestSendSlmFile());
    }

    public static RequestSendSlmFile getRootAsRequestSendSlmFile(ByteBuffer _bb, RequestSendSlmFile obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendSlmFile __assign(int _i, ByteBuffer _bb) {
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

    public BaseSessionData sessionData() {
        return sessionData(new BaseSessionData());
    }

    public BaseSessionData sessionData(BaseSessionData obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public FtPayloadParam payloadParam() {
        return payloadParam(new FtPayloadParam());
    }

    public FtPayloadParam payloadParam(FtPayloadParam obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public Participant participant(int j) {
        return participant(new Participant(), j);
    }

    public Participant participant(Participant obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int participantLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createRequestSendSlmFile(FlatBufferBuilder builder, long registration_handle, int session_dataOffset, int payload_paramOffset, int participantOffset) {
        builder.startObject(4);
        addParticipant(builder, participantOffset);
        addPayloadParam(builder, payload_paramOffset);
        addSessionData(builder, session_dataOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestSendSlmFile(builder);
    }

    public static void startRequestSendSlmFile(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addSessionData(FlatBufferBuilder builder, int sessionDataOffset) {
        builder.addOffset(1, sessionDataOffset, 0);
    }

    public static void addPayloadParam(FlatBufferBuilder builder, int payloadParamOffset) {
        builder.addOffset(2, payloadParamOffset, 0);
    }

    public static void addParticipant(FlatBufferBuilder builder, int participantOffset) {
        builder.addOffset(3, participantOffset, 0);
    }

    public static int createParticipantVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startParticipantVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endRequestSendSlmFile(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
