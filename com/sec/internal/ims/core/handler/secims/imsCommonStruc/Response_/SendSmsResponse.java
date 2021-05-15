package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendSmsResponse extends Table {
    public static SendSmsResponse getRootAsSendSmsResponse(ByteBuffer _bb) {
        return getRootAsSendSmsResponse(_bb, new SendSmsResponse());
    }

    public static SendSmsResponse getRootAsSendSmsResponse(ByteBuffer _bb, SendSmsResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SendSmsResponse __assign(int _i, ByteBuffer _bb) {
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

    public long statusCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String callId() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String errStr() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer errStrAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long retryAfter() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String content() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String contentType() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String contentSubType() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createSendSmsResponse(FlatBufferBuilder builder, long handle, long status_code, int call_idOffset, int err_strOffset, long retry_after, int contentOffset, int content_typeOffset, int content_sub_typeOffset) {
        builder.startObject(8);
        addContentSubType(builder, content_sub_typeOffset);
        addContentType(builder, content_typeOffset);
        addContent(builder, contentOffset);
        addRetryAfter(builder, retry_after);
        addErrStr(builder, err_strOffset);
        addCallId(builder, call_idOffset);
        addStatusCode(builder, status_code);
        addHandle(builder, handle);
        return endSendSmsResponse(builder);
    }

    public static void startSendSmsResponse(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(1, (int) statusCode, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(2, callIdOffset, 0);
    }

    public static void addErrStr(FlatBufferBuilder builder, int errStrOffset) {
        builder.addOffset(3, errStrOffset, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, long retryAfter) {
        builder.addInt(4, (int) retryAfter, 0);
    }

    public static void addContent(FlatBufferBuilder builder, int contentOffset) {
        builder.addOffset(5, contentOffset, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(6, contentTypeOffset, 0);
    }

    public static void addContentSubType(FlatBufferBuilder builder, int contentSubTypeOffset) {
        builder.addOffset(7, contentSubTypeOffset, 0);
    }

    public static int endSendSmsResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 14);
        builder.required(o, 16);
        builder.required(o, 18);
        return o;
    }
}
