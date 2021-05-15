package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendSlmResponse extends Table {
    public static SendSlmResponse getRootAsSendSlmResponse(ByteBuffer _bb) {
        return getRootAsSendSlmResponse(_bb, new SendSlmResponse());
    }

    public static SendSlmResponse getRootAsSendSlmResponse(ByteBuffer _bb, SendSlmResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SendSlmResponse __assign(int _i, ByteBuffer _bb) {
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

    public long slmMode() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long sessionHandle() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createSendSlmResponse(FlatBufferBuilder builder, int imdn_message_idOffset, int im_errorOffset, long slm_mode, long session_handle) {
        builder.startObject(4);
        addSessionHandle(builder, session_handle);
        addSlmMode(builder, slm_mode);
        addImError(builder, im_errorOffset);
        addImdnMessageId(builder, imdn_message_idOffset);
        return endSendSlmResponse(builder);
    }

    public static void startSendSlmResponse(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(0, imdnMessageIdOffset, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static void addSlmMode(FlatBufferBuilder builder, long slmMode) {
        builder.addInt(2, (int) slmMode, 0);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(3, (int) sessionHandle, 0);
    }

    public static int endSendSlmResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
