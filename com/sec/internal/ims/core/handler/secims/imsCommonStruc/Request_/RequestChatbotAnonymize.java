package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestChatbotAnonymize extends Table {
    public static RequestChatbotAnonymize getRootAsRequestChatbotAnonymize(ByteBuffer _bb) {
        return getRootAsRequestChatbotAnonymize(_bb, new RequestChatbotAnonymize());
    }

    public static RequestChatbotAnonymize getRootAsRequestChatbotAnonymize(ByteBuffer _bb, RequestChatbotAnonymize obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestChatbotAnonymize __assign(int _i, ByteBuffer _bb) {
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

    public String anonymizeInfo() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer anonymizeInfoAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String commandId() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer commandIdAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createRequestChatbotAnonymize(FlatBufferBuilder builder, long registration_handle, int chatbot_uriOffset, int anonymize_infoOffset, int command_idOffset) {
        builder.startObject(4);
        addCommandId(builder, command_idOffset);
        addAnonymizeInfo(builder, anonymize_infoOffset);
        addChatbotUri(builder, chatbot_uriOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestChatbotAnonymize(builder);
    }

    public static void startRequestChatbotAnonymize(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addChatbotUri(FlatBufferBuilder builder, int chatbotUriOffset) {
        builder.addOffset(1, chatbotUriOffset, 0);
    }

    public static void addAnonymizeInfo(FlatBufferBuilder builder, int anonymizeInfoOffset) {
        builder.addOffset(2, anonymizeInfoOffset, 0);
    }

    public static void addCommandId(FlatBufferBuilder builder, int commandIdOffset) {
        builder.addOffset(3, commandIdOffset, 0);
    }

    public static int endRequestChatbotAnonymize(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
