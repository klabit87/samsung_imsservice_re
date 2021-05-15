package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReceiveSmsNotification extends Table {
    public static ReceiveSmsNotification getRootAsReceiveSmsNotification(ByteBuffer _bb) {
        return getRootAsReceiveSmsNotification(_bb, new ReceiveSmsNotification());
    }

    public static ReceiveSmsNotification getRootAsReceiveSmsNotification(ByteBuffer _bb, ReceiveSmsNotification obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReceiveSmsNotification __assign(int _i, ByteBuffer _bb) {
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

    public String content() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentAsByteBuffer() {
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

    public long len() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String scUri() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer scUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public static int createReceiveSmsNotification(FlatBufferBuilder builder, long handle, int call_idOffset, int contentOffset, int content_typeOffset, int content_sub_typeOffset, long len, int sc_uriOffset) {
        builder.startObject(7);
        addScUri(builder, sc_uriOffset);
        addLen(builder, len);
        addContentSubType(builder, content_sub_typeOffset);
        addContentType(builder, content_typeOffset);
        addContent(builder, contentOffset);
        addCallId(builder, call_idOffset);
        addHandle(builder, handle);
        return endReceiveSmsNotification(builder);
    }

    public static void startReceiveSmsNotification(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(1, callIdOffset, 0);
    }

    public static void addContent(FlatBufferBuilder builder, int contentOffset) {
        builder.addOffset(2, contentOffset, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(3, contentTypeOffset, 0);
    }

    public static void addContentSubType(FlatBufferBuilder builder, int contentSubTypeOffset) {
        builder.addOffset(4, contentSubTypeOffset, 0);
    }

    public static void addLen(FlatBufferBuilder builder, long len) {
        builder.addInt(5, (int) len, 0);
    }

    public static void addScUri(FlatBufferBuilder builder, int scUriOffset) {
        builder.addOffset(6, scUriOffset, 0);
    }

    public static int endReceiveSmsNotification(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 16);
        return o;
    }
}
