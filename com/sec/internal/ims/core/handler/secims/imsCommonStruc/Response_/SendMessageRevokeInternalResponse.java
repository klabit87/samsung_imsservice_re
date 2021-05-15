package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendMessageRevokeInternalResponse extends Table {
    public static SendMessageRevokeInternalResponse getRootAsSendMessageRevokeInternalResponse(ByteBuffer _bb) {
        return getRootAsSendMessageRevokeInternalResponse(_bb, new SendMessageRevokeInternalResponse());
    }

    public static SendMessageRevokeInternalResponse getRootAsSendMessageRevokeInternalResponse(ByteBuffer _bb, SendMessageRevokeInternalResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SendMessageRevokeInternalResponse __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String imdnMessageId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createSendMessageRevokeInternalResponse(FlatBufferBuilder builder, int imdn_message_idOffset, int im_errorOffset) {
        builder.startObject(2);
        addImError(builder, im_errorOffset);
        addImdnMessageId(builder, imdn_message_idOffset);
        return endSendMessageRevokeInternalResponse(builder);
    }

    public static void startSendMessageRevokeInternalResponse(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(0, imdnMessageIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static int endSendMessageRevokeInternalResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
