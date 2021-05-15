package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRejectImSession extends Table {
    public static RequestRejectImSession getRootAsRequestRejectImSession(ByteBuffer _bb) {
        return getRootAsRequestRejectImSession(_bb, new RequestRejectImSession());
    }

    public static RequestRejectImSession getRootAsRequestRejectImSession(ByteBuffer _bb, RequestRejectImSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestRejectImSession __assign(int _i, ByteBuffer _bb) {
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

    public long sipCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestRejectImSession(FlatBufferBuilder builder, long session_handle, long sip_code, int warning_hdrOffset) {
        builder.startObject(3);
        addWarningHdr(builder, warning_hdrOffset);
        addSipCode(builder, sip_code);
        addSessionHandle(builder, session_handle);
        return endRequestRejectImSession(builder);
    }

    public static void startRequestRejectImSession(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addSipCode(FlatBufferBuilder builder, long sipCode) {
        builder.addInt(1, (int) sipCode, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder builder, int warningHdrOffset) {
        builder.addOffset(2, warningHdrOffset, 0);
    }

    public static int endRequestRejectImSession(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
