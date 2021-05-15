package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegInfoChanged extends Table {
    public static RegInfoChanged getRootAsRegInfoChanged(ByteBuffer _bb) {
        return getRootAsRegInfoChanged(_bb, new RegInfoChanged());
    }

    public static RegInfoChanged getRootAsRegInfoChanged(ByteBuffer _bb, RegInfoChanged obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RegInfoChanged __assign(int _i, ByteBuffer _bb) {
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

    public Contact contacts(int j) {
        return contacts(new Contact(), j);
    }

    public Contact contacts(Contact obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createRegInfoChanged(FlatBufferBuilder builder, long handle, int contactsOffset) {
        builder.startObject(2);
        addContacts(builder, contactsOffset);
        addHandle(builder, handle);
        return endRegInfoChanged(builder);
    }

    public static void startRegInfoChanged(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addContacts(FlatBufferBuilder builder, int contactsOffset) {
        builder.addOffset(1, contactsOffset, 0);
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

    public static int endRegInfoChanged(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
