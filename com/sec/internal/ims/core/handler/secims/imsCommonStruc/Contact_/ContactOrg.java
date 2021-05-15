package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactOrg extends Table {
    public static ContactOrg getRootAsContactOrg(ByteBuffer _bb) {
        return getRootAsContactOrg(_bb, new ContactOrg());
    }

    public static ContactOrg getRootAsContactOrg(ByteBuffer _bb, ContactOrg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactOrg __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String displayName() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String entity() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer entityAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String unit() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer unitAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createContactOrg(FlatBufferBuilder builder, int display_nameOffset, int entityOffset, int unitOffset) {
        builder.startObject(3);
        addUnit(builder, unitOffset);
        addEntity(builder, entityOffset);
        addDisplayName(builder, display_nameOffset);
        return endContactOrg(builder);
    }

    public static void startContactOrg(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(0, displayNameOffset, 0);
    }

    public static void addEntity(FlatBufferBuilder builder, int entityOffset) {
        builder.addOffset(1, entityOffset, 0);
    }

    public static void addUnit(FlatBufferBuilder builder, int unitOffset) {
        builder.addOffset(2, unitOffset, 0);
    }

    public static int endContactOrg(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
