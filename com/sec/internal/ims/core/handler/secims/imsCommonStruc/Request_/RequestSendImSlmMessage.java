package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImSlmMessage extends Table {
    public static RequestSendImSlmMessage getRootAsRequestSendImSlmMessage(ByteBuffer _bb) {
        return getRootAsRequestSendImSlmMessage(_bb, new RequestSendImSlmMessage());
    }

    public static RequestSendImSlmMessage getRootAsRequestSendImSlmMessage(ByteBuffer _bb, RequestSendImSlmMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendImSlmMessage __assign(int _i, ByteBuffer _bb) {
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

    public ReportMessageHdr reportData() {
        return reportData(new ReportMessageHdr());
    }

    public ReportMessageHdr reportData(ReportMessageHdr obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestSendImSlmMessage(FlatBufferBuilder builder, long registration_handle, int session_dataOffset, int message_paramOffset, int participantOffset, int report_dataOffset) {
        builder.startObject(5);
        addReportData(builder, report_dataOffset);
        addParticipant(builder, participantOffset);
        addMessageParam(builder, message_paramOffset);
        addSessionData(builder, session_dataOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestSendImSlmMessage(builder);
    }

    public static void startRequestSendImSlmMessage(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addSessionData(FlatBufferBuilder builder, int sessionDataOffset) {
        builder.addOffset(1, sessionDataOffset, 0);
    }

    public static void addMessageParam(FlatBufferBuilder builder, int messageParamOffset) {
        builder.addOffset(2, messageParamOffset, 0);
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

    public static void addReportData(FlatBufferBuilder builder, int reportDataOffset) {
        builder.addOffset(4, reportDataOffset, 0);
    }

    public static int endRequestSendImSlmMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 12);
        return o;
    }
}
