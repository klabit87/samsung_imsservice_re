package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymizeResponse extends Table {
    public static RequestChatbotAnonymizeResponse getRootAsRequestChatbotAnonymizeResponse(ByteBuffer _bb) {
        return getRootAsRequestChatbotAnonymizeResponse(_bb, new RequestChatbotAnonymizeResponse());
    }

    public static RequestChatbotAnonymizeResponse getRootAsRequestChatbotAnonymizeResponse(ByteBuffer _bb, RequestChatbotAnonymizeResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestChatbotAnonymizeResponse __assign(int _i, ByteBuffer _bb) {
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

    public String commandId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer commandIdAsByteBuffer() {
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

    public int retryAfter() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestChatbotAnonymizeResponse(FlatBufferBuilder builder, int uriOffset, int command_idOffset, int im_errorOffset, int retryAfter) {
        builder.startObject(4);
        addRetryAfter(builder, retryAfter);
        addImError(builder, im_errorOffset);
        addCommandId(builder, command_idOffset);
        addUri(builder, uriOffset);
        return endRequestChatbotAnonymizeResponse(builder);
    }

    public static void startRequestChatbotAnonymizeResponse(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addCommandId(FlatBufferBuilder builder, int commandIdOffset) {
        builder.addOffset(1, commandIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(2, imErrorOffset, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, int retryAfter) {
        builder.addInt(3, retryAfter, 0);
    }

    public static int endRequestChatbotAnonymizeResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
