package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallSendCmcInfo extends Table {
    public static CallSendCmcInfo getRootAsCallSendCmcInfo(ByteBuffer _bb) {
        return getRootAsCallSendCmcInfo(_bb, new CallSendCmcInfo());
    }

    public static CallSendCmcInfo getRootAsCallSendCmcInfo(ByteBuffer _bb, CallSendCmcInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CallSendCmcInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long sessionId() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createCallSendCmcInfo(FlatBufferBuilder builder, long handle, long session_id, int additional_contentsOffset) {
        builder.startObject(3);
        addAdditionalContents(builder, additional_contentsOffset);
        addSessionId(builder, session_id);
        addHandle(builder, handle);
        return endCallSendCmcInfo(builder);
    }

    public static void startCallSendCmcInfo(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(1, (int) sessionId, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder builder, int additionalContentsOffset) {
        builder.addOffset(2, additionalContentsOffset, 0);
    }

    public static int endCallSendCmcInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
