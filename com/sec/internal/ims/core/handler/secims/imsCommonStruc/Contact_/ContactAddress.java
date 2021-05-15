package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactAddress extends Table {
    public static ContactAddress getRootAsContactAddress(ByteBuffer _bb) {
        return getRootAsContactAddress(_bb, new ContactAddress());
    }

    public static ContactAddress getRootAsContactAddress(ByteBuffer _bb, ContactAddress obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactAddress __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String type() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String label() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer labelAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String addrStr() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer addrStrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String country() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer countryAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String region() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer regionAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String locality() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer localityAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String street() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer streetAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String postCode() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer postCodeAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createContactAddress(FlatBufferBuilder builder, int typeOffset, int labelOffset, int addr_strOffset, int countryOffset, int regionOffset, int localityOffset, int streetOffset, int post_codeOffset) {
        builder.startObject(8);
        addPostCode(builder, post_codeOffset);
        addStreet(builder, streetOffset);
        addLocality(builder, localityOffset);
        addRegion(builder, regionOffset);
        addCountry(builder, countryOffset);
        addAddrStr(builder, addr_strOffset);
        addLabel(builder, labelOffset);
        addType(builder, typeOffset);
        return endContactAddress(builder);
    }

    public static void startContactAddress(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addType(FlatBufferBuilder builder, int typeOffset) {
        builder.addOffset(0, typeOffset, 0);
    }

    public static void addLabel(FlatBufferBuilder builder, int labelOffset) {
        builder.addOffset(1, labelOffset, 0);
    }

    public static void addAddrStr(FlatBufferBuilder builder, int addrStrOffset) {
        builder.addOffset(2, addrStrOffset, 0);
    }

    public static void addCountry(FlatBufferBuilder builder, int countryOffset) {
        builder.addOffset(3, countryOffset, 0);
    }

    public static void addRegion(FlatBufferBuilder builder, int regionOffset) {
        builder.addOffset(4, regionOffset, 0);
    }

    public static void addLocality(FlatBufferBuilder builder, int localityOffset) {
        builder.addOffset(5, localityOffset, 0);
    }

    public static void addStreet(FlatBufferBuilder builder, int streetOffset) {
        builder.addOffset(6, streetOffset, 0);
    }

    public static void addPostCode(FlatBufferBuilder builder, int postCodeOffset) {
        builder.addOffset(7, postCodeOffset, 0);
    }

    public static int endContactAddress(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
