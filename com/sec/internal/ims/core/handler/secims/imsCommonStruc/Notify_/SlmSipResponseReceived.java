package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SlmSipResponseReceived extends Table {
    public static SlmSipResponseReceived getRootAsSlmSipResponseReceived(ByteBuffer _bb) {
        return getRootAsSlmSipResponseReceived(_bb, new SlmSipResponseReceived());
    }

    public static SlmSipResponseReceived getRootAsSlmSipResponseReceived(ByteBuffer _bb, SlmSipResponseReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SlmSipResponseReceived __assign(int _i, ByteBuffer _bb) {
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

    public String imdnMessageId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
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

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String passertedId() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer passertedIdAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSlmSipResponseReceived(FlatBufferBuilder builder, long session_handle, int imdn_message_idOffset, int im_errorOffset, int warning_hdrOffset, int reason_hdrOffset, int passerted_idOffset) {
        builder.startObject(6);
        addPassertedId(builder, passerted_idOffset);
        addReasonHdr(builder, reason_hdrOffset);
        addWarningHdr(builder, warning_hdrOffset);
        addImError(builder, im_errorOffset);
        addImdnMessageId(builder, imdn_message_idOffset);
        addSessionHandle(builder, session_handle);
        return endSlmSipResponseReceived(builder);
    }

    public static void startSlmSipResponseReceived(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(1, imdnMessageIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(2, imErrorOffset, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder builder, int warningHdrOffset) {
        builder.addOffset(3, warningHdrOffset, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder builder, int reasonHdrOffset) {
        builder.addOffset(4, reasonHdrOffset, 0);
    }

    public static void addPassertedId(FlatBufferBuilder builder, int passertedIdOffset) {
        builder.addOffset(5, passertedIdOffset, 0);
    }

    public static int endSlmSipResponseReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
