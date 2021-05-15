package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymizeResponseReceived extends Table {
    public static RequestChatbotAnonymizeResponseReceived getRootAsRequestChatbotAnonymizeResponseReceived(ByteBuffer _bb) {
        return getRootAsRequestChatbotAnonymizeResponseReceived(_bb, new RequestChatbotAnonymizeResponseReceived());
    }

    public static RequestChatbotAnonymizeResponseReceived getRootAsRequestChatbotAnonymizeResponseReceived(ByteBuffer _bb, RequestChatbotAnonymizeResponseReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestChatbotAnonymizeResponseReceived __assign(int _i, ByteBuffer _bb) {
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

    public String result() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer resultAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder builder, int uriOffset, int resultOffset) {
        builder.startObject(2);
        addResult(builder, resultOffset);
        addUri(builder, uriOffset);
        return endRequestChatbotAnonymizeResponseReceived(builder);
    }

    public static void startRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addResult(FlatBufferBuilder builder, int resultOffset) {
        builder.addOffset(1, resultOffset, 0);
    }

    public static int endRequestChatbotAnonymizeResponseReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
