package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendInfo extends Table {
    public static RequestSendInfo getRootAsRequestSendInfo(ByteBuffer _bb) {
        return getRootAsRequestSendInfo(_bb, new RequestSendInfo());
    }

    public static RequestSendInfo getRootAsRequestSendInfo(ByteBuffer _bb, RequestSendInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendInfo __assign(int _i, ByteBuffer _bb) {
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

    public int callType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long ussdType() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestSendInfo(FlatBufferBuilder builder, long handle, long session, int call_type, long ussd_type, int additional_contentsOffset) {
        builder.startObject(5);
        addAdditionalContents(builder, additional_contentsOffset);
        addUssdType(builder, ussd_type);
        addCallType(builder, call_type);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestSendInfo(builder);
    }

    public static void startRequestSendInfo(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(2, callType, 0);
    }

    public static void addUssdType(FlatBufferBuilder builder, long ussdType) {
        builder.addInt(3, (int) ussdType, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder builder, int additionalContentsOffset) {
        builder.addOffset(4, additionalContentsOffset, 0);
    }

    public static int endRequestSendInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
