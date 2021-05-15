package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUploadContacts extends Table {
    public static RequestXdmUploadContacts getRootAsRequestXdmUploadContacts(ByteBuffer _bb) {
        return getRootAsRequestXdmUploadContacts(_bb, new RequestXdmUploadContacts());
    }

    public static RequestXdmUploadContacts getRootAsRequestXdmUploadContacts(ByteBuffer _bb, RequestXdmUploadContacts obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmUploadContacts __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String impu() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public Contact contacts(int j) {
        return contacts(new Contact(), j);
    }

    public Contact contacts(Contact obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String uuid() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uuidAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String etag() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public long mtc() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestXdmUploadContacts(FlatBufferBuilder builder, long rid, int impuOffset, int contactsOffset, int uuidOffset, int etagOffset, long mtc) {
        builder.startObject(6);
        addMtc(builder, mtc);
        addEtag(builder, etagOffset);
        addUuid(builder, uuidOffset);
        addContacts(builder, contactsOffset);
        addImpu(builder, impuOffset);
        addRid(builder, rid);
        return endRequestXdmUploadContacts(builder);
    }

    public static void startRequestXdmUploadContacts(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(1, impuOffset, 0);
    }

    public static void addContacts(FlatBufferBuilder builder, int contactsOffset) {
        builder.addOffset(2, contactsOffset, 0);
    }

    public static int createContactsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addUuid(FlatBufferBuilder builder, int uuidOffset) {
        builder.addOffset(3, uuidOffset, 0);
    }

    public static void addEtag(FlatBufferBuilder builder, int etagOffset) {
        builder.addOffset(4, etagOffset, 0);
    }

    public static void addMtc(FlatBufferBuilder builder, long mtc) {
        builder.addInt(5, (int) mtc, 0);
    }

    public static int endRequestXdmUploadContacts(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
