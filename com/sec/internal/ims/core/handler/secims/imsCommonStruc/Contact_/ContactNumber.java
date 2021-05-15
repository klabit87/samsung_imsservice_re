package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactNumber extends Table {
    public static ContactNumber getRootAsContactNumber(ByteBuffer _bb) {
        return getRootAsContactNumber(_bb, new ContactNumber());
    }

    public static ContactNumber getRootAsContactNumber(ByteBuffer _bb, ContactNumber obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactNumber __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String number() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer numberAsByteBuffer() {
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

    public static int createContactNumber(FlatBufferBuilder builder, int numberOffset, int typeOffset, int labelOffset) {
        builder.startObject(3);
        addLabel(builder, labelOffset);
        addType(builder, typeOffset);
        addNumber(builder, numberOffset);
        return endContactNumber(builder);
    }

    public static void startContactNumber(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addNumber(FlatBufferBuilder builder, int numberOffset) {
        builder.addOffset(0, numberOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int typeOffset) {
        builder.addOffset(1, typeOffset, 0);
    }

    public static void addLabel(FlatBufferBuilder builder, int labelOffset) {
        builder.addOffset(2, labelOffset, 0);
    }

    public static int endContactNumber(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
