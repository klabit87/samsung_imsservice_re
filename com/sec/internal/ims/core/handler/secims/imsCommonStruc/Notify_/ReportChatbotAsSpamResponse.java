package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReportChatbotAsSpamResponse extends Table {
    public static ReportChatbotAsSpamResponse getRootAsReportChatbotAsSpamResponse(ByteBuffer _bb) {
        return getRootAsReportChatbotAsSpamResponse(_bb, new ReportChatbotAsSpamResponse());
    }

    public static ReportChatbotAsSpamResponse getRootAsReportChatbotAsSpamResponse(ByteBuffer _bb, ReportChatbotAsSpamResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReportChatbotAsSpamResponse __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String requestId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer requestIdAsByteBuffer() {
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

    public static int createReportChatbotAsSpamResponse(FlatBufferBuilder builder, int uriOffset, int request_idOffset, int im_errorOffset) {
        builder.startObject(3);
        addImError(builder, im_errorOffset);
        addRequestId(builder, request_idOffset);
        addUri(builder, uriOffset);
        return endReportChatbotAsSpamResponse(builder);
    }

    public static void startReportChatbotAsSpamResponse(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addRequestId(FlatBufferBuilder builder, int requestIdOffset) {
        builder.addOffset(1, requestIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(2, imErrorOffset, 0);
    }

    public static int endReportChatbotAsSpamResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
