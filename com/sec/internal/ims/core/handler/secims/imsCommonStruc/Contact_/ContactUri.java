package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactUri extends Table {
    public static ContactUri getRootAsContactUri(ByteBuffer _bb) {
        return getRootAsContactUri(_bb, new ContactUri());
    }

    public static ContactUri getRootAsContactUri(ByteBuffer _bb, ContactUri obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactUri __assign(int _i, ByteBuffer _bb) {
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

    public String type() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String label() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer labelAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createContactUri(FlatBufferBuilder builder, int uriOffset, int typeOffset, int labelOffset) {
        builder.startObject(3);
        addLabel(builder, labelOffset);
        addType(builder, typeOffset);
        addUri(builder, uriOffset);
        return endContactUri(builder);
    }

    public static void startContactUri(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int typeOffset) {
        builder.addOffset(1, typeOffset, 0);
    }

    public static void addLabel(FlatBufferBuilder builder, int labelOffset) {
        builder.addOffset(2, labelOffset, 0);
    }

    public static int endContactUri(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
