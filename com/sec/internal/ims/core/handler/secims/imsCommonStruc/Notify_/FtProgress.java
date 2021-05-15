package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtProgress extends Table {
    public static FtProgress getRootAsFtProgress(ByteBuffer _bb) {
        return getRootAsFtProgress(_bb, new FtProgress());
    }

    public static FtProgress getRootAsFtProgress(ByteBuffer _bb, FtProgress obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public FtProgress __assign(int _i, ByteBuffer _bb) {
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

    public long total() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long transferred() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long state() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr obj) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ReasonHdr reasonHdr() {
        return reasonHdr(new ReasonHdr());
    }

    public ReasonHdr reasonHdr(ReasonHdr obj) {
        int o = __offset(16);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createFtProgress(FlatBufferBuilder builder, long session_handle, long total, long transferred, long state, int im_errorOffset, int warning_hdrOffset, int reason_hdrOffset) {
        builder.startObject(7);
        addReasonHdr(builder, reason_hdrOffset);
        addWarningHdr(builder, warning_hdrOffset);
        addImError(builder, im_errorOffset);
        addState(builder, state);
        addTransferred(builder, transferred);
        addTotal(builder, total);
        addSessionHandle(builder, session_handle);
        return endFtProgress(builder);
    }

    public static void startFtProgress(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addTotal(FlatBufferBuilder builder, long total) {
        builder.addInt(1, (int) total, 0);
    }

    public static void addTransferred(FlatBufferBuilder builder, long transferred) {
        builder.addInt(2, (int) transferred, 0);
    }

    public static void addState(FlatBufferBuilder builder, long state) {
        builder.addInt(3, (int) state, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(4, imErrorOffset, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder builder, int warningHdrOffset) {
        builder.addOffset(5, warningHdrOffset, 0);
    }

    public static void addReasonHdr(FlatBufferBuilder builder, int reasonHdrOffset) {
        builder.addOffset(6, reasonHdrOffset, 0);
    }

    public static int endFtProgress(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 12);
        return o;
    }
}
