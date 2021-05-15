package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class UpdateParticipantsResponse extends Table {
    public static UpdateParticipantsResponse getRootAsUpdateParticipantsResponse(ByteBuffer _bb) {
        return getRootAsUpdateParticipantsResponse(_bb, new UpdateParticipantsResponse());
    }

    public static UpdateParticipantsResponse getRootAsUpdateParticipantsResponse(ByteBuffer _bb, UpdateParticipantsResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public UpdateParticipantsResponse __assign(int _i, ByteBuffer _bb) {
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

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String reqKey() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reqKeyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createUpdateParticipantsResponse(FlatBufferBuilder builder, long session_handle, int im_errorOffset, int req_keyOffset, int warning_hdrOffset) {
        builder.startObject(4);
        addWarningHdr(builder, warning_hdrOffset);
        addReqKey(builder, req_keyOffset);
        addImError(builder, im_errorOffset);
        addSessionHandle(builder, session_handle);
        return endUpdateParticipantsResponse(builder);
    }

    public static void startUpdateParticipantsResponse(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static void addReqKey(FlatBufferBuilder builder, int reqKeyOffset) {
        builder.addOffset(2, reqKeyOffset, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder builder, int warningHdrOffset) {
        builder.addOffset(3, warningHdrOffset, 0);
    }

    public static int endUpdateParticipantsResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
