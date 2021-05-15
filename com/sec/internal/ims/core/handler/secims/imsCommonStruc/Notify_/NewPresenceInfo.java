package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ContactInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NewPresenceInfo extends Table {
    public static NewPresenceInfo getRootAsNewPresenceInfo(ByteBuffer _bb) {
        return getRootAsNewPresenceInfo(_bb, new NewPresenceInfo());
    }

    public static NewPresenceInfo getRootAsNewPresenceInfo(ByteBuffer _bb, NewPresenceInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public NewPresenceInfo __assign(int _i, ByteBuffer _bb) {
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

    public String subscriptionId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subscriptionIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public ContactInfo contactInfo(int j) {
        return contactInfo(new ContactInfo(), j);
    }

    public ContactInfo contactInfo(ContactInfo obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactInfoLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createNewPresenceInfo(FlatBufferBuilder builder, long handle, int subscription_idOffset, int contact_infoOffset) {
        builder.startObject(3);
        addContactInfo(builder, contact_infoOffset);
        addSubscriptionId(builder, subscription_idOffset);
        addHandle(builder, handle);
        return endNewPresenceInfo(builder);
    }

    public static void startNewPresenceInfo(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSubscriptionId(FlatBufferBuilder builder, int subscriptionIdOffset) {
        builder.addOffset(1, subscriptionIdOffset, 0);
    }

    public static void addContactInfo(FlatBufferBuilder builder, int contactInfoOffset) {
        builder.addOffset(2, contactInfoOffset, 0);
    }

    public static int createContactInfoVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactInfoVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endNewPresenceInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
