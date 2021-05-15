package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactName extends Table {
    public static ContactName getRootAsContactName(ByteBuffer _bb) {
        return getRootAsContactName(_bb, new ContactName());
    }

    public static ContactName getRootAsContactName(ByteBuffer _bb, ContactName obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactName __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String title() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer titleAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String givenName() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer givenNameAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String middleName() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer middleNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String familyName() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer familyNameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String generationId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer generationIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String displayName() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createContactName(FlatBufferBuilder builder, int titleOffset, int given_nameOffset, int middle_nameOffset, int family_nameOffset, int generation_idOffset, int display_nameOffset) {
        builder.startObject(6);
        addDisplayName(builder, display_nameOffset);
        addGenerationId(builder, generation_idOffset);
        addFamilyName(builder, family_nameOffset);
        addMiddleName(builder, middle_nameOffset);
        addGivenName(builder, given_nameOffset);
        addTitle(builder, titleOffset);
        return endContactName(builder);
    }

    public static void startContactName(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addTitle(FlatBufferBuilder builder, int titleOffset) {
        builder.addOffset(0, titleOffset, 0);
    }

    public static void addGivenName(FlatBufferBuilder builder, int givenNameOffset) {
        builder.addOffset(1, givenNameOffset, 0);
    }

    public static void addMiddleName(FlatBufferBuilder builder, int middleNameOffset) {
        builder.addOffset(2, middleNameOffset, 0);
    }

    public static void addFamilyName(FlatBufferBuilder builder, int familyNameOffset) {
        builder.addOffset(3, familyNameOffset, 0);
    }

    public static void addGenerationId(FlatBufferBuilder builder, int generationIdOffset) {
        builder.addOffset(4, generationIdOffset, 0);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(5, displayNameOffset, 0);
    }

    public static int endContactName(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
