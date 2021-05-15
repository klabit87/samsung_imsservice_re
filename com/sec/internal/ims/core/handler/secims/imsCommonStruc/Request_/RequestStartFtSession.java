package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartFtSession extends Table {
    public static RequestStartFtSession getRootAsRequestStartFtSession(ByteBuffer _bb) {
        return getRootAsRequestStartFtSession(_bb, new RequestStartFtSession());
    }

    public static RequestStartFtSession getRootAsRequestStartFtSession(ByteBuffer _bb, RequestStartFtSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartFtSession __assign(int _i, ByteBuffer _bb) {
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

    public FtPayloadParam payload() {
        return payload(new FtPayloadParam());
    }

    public FtPayloadParam payload(FtPayloadParam obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ReportMessageHdr reportData() {
        return reportData(new ReportMessageHdr());
    }

    public ReportMessageHdr reportData(ReportMessageHdr obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestStartFtSession(FlatBufferBuilder builder, long registration_handle, int session_dataOffset, int payloadOffset, int report_dataOffset) {
        builder.startObject(4);
        addReportData(builder, report_dataOffset);
        addPayload(builder, payloadOffset);
        addSessionData(builder, session_dataOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestStartFtSession(builder);
    }

    public static void startRequestStartFtSession(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addSessionData(FlatBufferBuilder builder, int sessionDataOffset) {
        builder.addOffset(1, sessionDataOffset, 0);
    }

    public static void addPayload(FlatBufferBuilder builder, int payloadOffset) {
        builder.addOffset(2, payloadOffset, 0);
    }

    public static void addReportData(FlatBufferBuilder builder, int reportDataOffset) {
        builder.addOffset(3, reportDataOffset, 0);
    }

    public static int endRequestStartFtSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        return o;
    }
}
