package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendCmcInfo extends Table {
    public static RequestSendCmcInfo getRootAsRequestSendCmcInfo(ByteBuffer _bb) {
        return getRootAsRequestSendCmcInfo(_bb, new RequestSendCmcInfo());
    }

    public static RequestSendCmcInfo getRootAsRequestSendCmcInfo(ByteBuffer _bb, RequestSendCmcInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendCmcInfo __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
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

    public static int createRequestSendCmcInfo(FlatBufferBuilder builder, long handle, long session, int additional_contentsOffset) {
        builder.startObject(3);
        addAdditionalContents(builder, additional_contentsOffset);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestSendCmcInfo(builder);
    }

    public static void startRequestSendCmcInfo(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder builder, int additionalContentsOffset) {
        builder.addOffset(2, additionalContentsOffset, 0);
    }

    public static int endRequestSendCmcInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
