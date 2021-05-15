package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPublishDialog extends Table {
    public static RequestPublishDialog getRootAsRequestPublishDialog(ByteBuffer _bb) {
        return getRootAsRequestPublishDialog(_bb, new RequestPublishDialog());
    }

    public static RequestPublishDialog getRootAsRequestPublishDialog(ByteBuffer _bb, RequestPublishDialog obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPublishDialog __assign(int _i, ByteBuffer _bb) {
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

    public String origUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String dispName() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dispNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String xmlBody() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer xmlBodyAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public int expireTime() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestPublishDialog(FlatBufferBuilder builder, long handle, int orig_uriOffset, int disp_nameOffset, int xml_bodyOffset, int expire_time) {
        builder.startObject(5);
        addExpireTime(builder, expire_time);
        addXmlBody(builder, xml_bodyOffset);
        addDispName(builder, disp_nameOffset);
        addOrigUri(builder, orig_uriOffset);
        addHandle(builder, handle);
        return endRequestPublishDialog(builder);
    }

    public static void startRequestPublishDialog(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addOrigUri(FlatBufferBuilder builder, int origUriOffset) {
        builder.addOffset(1, origUriOffset, 0);
    }

    public static void addDispName(FlatBufferBuilder builder, int dispNameOffset) {
        builder.addOffset(2, dispNameOffset, 0);
    }

    public static void addXmlBody(FlatBufferBuilder builder, int xmlBodyOffset) {
        builder.addOffset(3, xmlBodyOffset, 0);
    }

    public static void addExpireTime(FlatBufferBuilder builder, int expireTime) {
        builder.addInt(4, expireTime, 0);
    }

    public static int endRequestPublishDialog(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        return o;
    }
}
