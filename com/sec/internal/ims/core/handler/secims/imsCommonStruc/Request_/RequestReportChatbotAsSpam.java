package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReportChatbotAsSpam extends Table {
    public static RequestReportChatbotAsSpam getRootAsRequestReportChatbotAsSpam(ByteBuffer _bb) {
        return getRootAsRequestReportChatbotAsSpam(_bb, new RequestReportChatbotAsSpam());
    }

    public static RequestReportChatbotAsSpam getRootAsRequestReportChatbotAsSpam(ByteBuffer _bb, RequestReportChatbotAsSpam obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestReportChatbotAsSpam __assign(int _i, ByteBuffer _bb) {
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

    public String chatbotUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer chatbotUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String requestId() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer requestIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String spamInfo() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer spamInfoAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestReportChatbotAsSpam(FlatBufferBuilder builder, long registration_handle, int chatbot_uriOffset, int request_idOffset, int spam_infoOffset) {
        builder.startObject(4);
        addSpamInfo(builder, spam_infoOffset);
        addRequestId(builder, request_idOffset);
        addChatbotUri(builder, chatbot_uriOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestReportChatbotAsSpam(builder);
    }

    public static void startRequestReportChatbotAsSpam(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addChatbotUri(FlatBufferBuilder builder, int chatbotUriOffset) {
        builder.addOffset(1, chatbotUriOffset, 0);
    }

    public static void addRequestId(FlatBufferBuilder builder, int requestIdOffset) {
        builder.addOffset(2, requestIdOffset, 0);
    }

    public static void addSpamInfo(FlatBufferBuilder builder, int spamInfoOffset) {
        builder.addOffset(3, spamInfoOffset, 0);
    }

    public static int endRequestReportChatbotAsSpam(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
