package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMessageRevokeRequest extends Table {
    public static RequestSendMessageRevokeRequest getRootAsRequestSendMessageRevokeRequest(ByteBuffer _bb) {
        return getRootAsRequestSendMessageRevokeRequest(_bb, new RequestSendMessageRevokeRequest());
    }

    public static RequestSendMessageRevokeRequest getRootAsRequestSendMessageRevokeRequest(ByteBuffer _bb, RequestSendMessageRevokeRequest obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendMessageRevokeRequest __assign(int _i, ByteBuffer _bb) {
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

    public String imdnMessageId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long registrationHandle() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String conversationId() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contributionId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public int service() {
        int o = __offset(14);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestSendMessageRevokeRequest(FlatBufferBuilder builder, int uriOffset, int imdn_message_idOffset, long registration_handle, int conversation_idOffset, int contribution_idOffset, int service) {
        builder.startObject(6);
        addService(builder, service);
        addContributionId(builder, contribution_idOffset);
        addConversationId(builder, conversation_idOffset);
        addRegistrationHandle(builder, registration_handle);
        addImdnMessageId(builder, imdn_message_idOffset);
        addUri(builder, uriOffset);
        return endRequestSendMessageRevokeRequest(builder);
    }

    public static void startRequestSendMessageRevokeRequest(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(1, imdnMessageIdOffset, 0);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(2, (int) registrationHandle, 0);
    }

    public static void addConversationId(FlatBufferBuilder builder, int conversationIdOffset) {
        builder.addOffset(3, conversationIdOffset, 0);
    }

    public static void addContributionId(FlatBufferBuilder builder, int contributionIdOffset) {
        builder.addOffset(4, contributionIdOffset, 0);
    }

    public static void addService(FlatBufferBuilder builder, int service) {
        builder.addInt(5, service, 0);
    }

    public static int endRequestSendMessageRevokeRequest(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
