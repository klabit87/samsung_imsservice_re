package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SmsRpAckNotification extends Table {
    public static SmsRpAckNotification getRootAsSmsRpAckNotification(ByteBuffer _bb) {
        return getRootAsSmsRpAckNotification(_bb, new SmsRpAckNotification());
    }

    public static SmsRpAckNotification getRootAsSmsRpAckNotification(ByteBuffer _bb, SmsRpAckNotification obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SmsRpAckNotification __assign(int _i, ByteBuffer _bb) {
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

    public String callId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String ackCode() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer ackCodeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long ackLen() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String contentType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String contentSubType() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createSmsRpAckNotification(FlatBufferBuilder builder, long handle, int call_idOffset, int ack_codeOffset, long ack_len, int content_typeOffset, int content_sub_typeOffset) {
        builder.startObject(6);
        addContentSubType(builder, content_sub_typeOffset);
        addContentType(builder, content_typeOffset);
        addAckLen(builder, ack_len);
        addAckCode(builder, ack_codeOffset);
        addCallId(builder, call_idOffset);
        addHandle(builder, handle);
        return endSmsRpAckNotification(builder);
    }

    public static void startSmsRpAckNotification(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(1, callIdOffset, 0);
    }

    public static void addAckCode(FlatBufferBuilder builder, int ackCodeOffset) {
        builder.addOffset(2, ackCodeOffset, 0);
    }

    public static void addAckLen(FlatBufferBuilder builder, long ackLen) {
        builder.addInt(3, (int) ackLen, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(4, contentTypeOffset, 0);
    }

    public static void addContentSubType(FlatBufferBuilder builder, int contentSubTypeOffset) {
        builder.addOffset(5, contentSubTypeOffset, 0);
    }

    public static int endSmsRpAckNotification(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 12);
        builder.required(o, 14);
        return o;
    }
}
