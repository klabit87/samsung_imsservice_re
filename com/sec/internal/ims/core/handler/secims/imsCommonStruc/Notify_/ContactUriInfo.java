package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactUriInfo extends Table {
    public static ContactUriInfo getRootAsContactUriInfo(ByteBuffer _bb) {
        return getRootAsContactUriInfo(_bb, new ContactUriInfo());
    }

    public static ContactUriInfo getRootAsContactUriInfo(ByteBuffer _bb, ContactUriInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactUriInfo __assign(int _i, ByteBuffer _bb) {
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

    public String uriList(int j) {
        int o = __offset(6);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int uriListLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long isRegi() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String uriType() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createContactUriInfo(FlatBufferBuilder builder, long handle, int uri_listOffset, long is_regi, int uri_typeOffset) {
        builder.startObject(4);
        addUriType(builder, uri_typeOffset);
        addIsRegi(builder, is_regi);
        addUriList(builder, uri_listOffset);
        addHandle(builder, handle);
        return endContactUriInfo(builder);
    }

    public static void startContactUriInfo(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUriList(FlatBufferBuilder builder, int uriListOffset) {
        builder.addOffset(1, uriListOffset, 0);
    }

    public static int createUriListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startUriListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsRegi(FlatBufferBuilder builder, long isRegi) {
        builder.addInt(2, (int) isRegi, 0);
    }

    public static void addUriType(FlatBufferBuilder builder, int uriTypeOffset) {
        builder.addOffset(3, uriTypeOffset, 0);
    }

    public static int endContactUriInfo(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
