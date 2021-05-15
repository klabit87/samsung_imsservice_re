package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SessionClosed extends Table {
    public static SessionClosed getRootAsSessionClosed(ByteBuffer _bb) {
        return getRootAsSessionClosed(_bb, new SessionClosed());
    }

    public static SessionClosed getRootAsSessionClosed(ByteBuffer _bb, SessionClosed obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SessionClosed __assign(int _i, ByteBuffer _bb) {
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

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String referredBy() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer referredByAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createSessionClosed(FlatBufferBuilder builder, long session_handle, int im_errorOffset, int reason_hdrOffset, int referred_byOffset) {
        builder.startObject(4);
        addReferredBy(builder, referred_byOffset);
        addReasonHdr(builder, reason_hdrOffset);
        addImError(builder, im_errorOffset);
        addSessionHandle(builder, session_handle);
        return endSessionClosed(builder);
    }

    public static void startSessionClosed(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder builder, int reasonHdrOffset) {
        builder.addOffset(2, reasonHdrOffset, 0);
    }

    public static void addReferredBy(FlatBufferBuilder builder, int referredByOffset) {
        builder.addOffset(3, referredByOffset, 0);
    }

    public static int endSessionClosed(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
