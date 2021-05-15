package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMsg extends Table {
    public static RequestSendMsg getRootAsRequestSendMsg(ByteBuffer _bb) {
        return getRootAsRequestSendMsg(_bb, new RequestSendMsg());
    }

    public static RequestSendMsg getRootAsRequestSendMsg(ByteBuffer _bb, RequestSendMsg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendMsg __assign(int _i, ByteBuffer _bb) {
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

    public String smsc() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer smscAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String localUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer localUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String contentType() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contentSubType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentSubTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public int contentLen() {
        int o = __offset(14);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String contentBody() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentBodyAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String inReplyTo() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer inReplyToAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createRequestSendMsg(FlatBufferBuilder builder, long handle, int smscOffset, int local_uriOffset, int content_typeOffset, int content_sub_typeOffset, int content_len, int content_bodyOffset, int in_reply_toOffset) {
        builder.startObject(8);
        addInReplyTo(builder, in_reply_toOffset);
        addContentBody(builder, content_bodyOffset);
        addContentLen(builder, content_len);
        addContentSubType(builder, content_sub_typeOffset);
        addContentType(builder, content_typeOffset);
        addLocalUri(builder, local_uriOffset);
        addSmsc(builder, smscOffset);
        addHandle(builder, handle);
        return endRequestSendMsg(builder);
    }

    public static void startRequestSendMsg(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSmsc(FlatBufferBuilder builder, int smscOffset) {
        builder.addOffset(1, smscOffset, 0);
    }

    public static void addLocalUri(FlatBufferBuilder builder, int localUriOffset) {
        builder.addOffset(2, localUriOffset, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(3, contentTypeOffset, 0);
    }

    public static void addContentSubType(FlatBufferBuilder builder, int contentSubTypeOffset) {
        builder.addOffset(4, contentSubTypeOffset, 0);
    }

    public static void addContentLen(FlatBufferBuilder builder, int contentLen) {
        builder.addInt(5, contentLen, 0);
    }

    public static void addContentBody(FlatBufferBuilder builder, int contentBodyOffset) {
        builder.addOffset(6, contentBodyOffset, 0);
    }

    public static void addInReplyTo(FlatBufferBuilder builder, int inReplyToOffset) {
        builder.addOffset(7, inReplyToOffset, 0);
    }

    public static int endRequestSendMsg(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 16);
        return o;
    }
}
