package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactAddress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactName;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactNumber;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactOrg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_.ContactUri;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Contact extends Table {
    public static Contact getRootAsContact(ByteBuffer _bb) {
        return getRootAsContact(_bb, new Contact());
    }

    public static Contact getRootAsContact(ByteBuffer _bb, Contact obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Contact __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String contactId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contactIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public ContactName contactNames(int j) {
        return contactNames(new ContactName(), j);
    }

    public ContactName contactNames(ContactName obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactNamesLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ContactAddress contactAddresses(int j) {
        return contactAddresses(new ContactAddress(), j);
    }

    public ContactAddress contactAddresses(ContactAddress obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactAddressesLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ContactNumber contactNumbers(int j) {
        return contactNumbers(new ContactNumber(), j);
    }

    public ContactNumber contactNumbers(ContactNumber obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactNumbersLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ContactUri contactUris(int j) {
        return contactUris(new ContactUri(), j);
    }

    public ContactUri contactUris(ContactUri obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactUrisLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ContactOrg contactOrgs(int j) {
        return contactOrgs(new ContactOrg(), j);
    }

    public ContactOrg contactOrgs(ContactOrg obj, int j) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactOrgsLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createContact(FlatBufferBuilder builder, int contact_idOffset, int contact_namesOffset, int contact_addressesOffset, int contact_numbersOffset, int contact_urisOffset, int contact_orgsOffset) {
        builder.startObject(6);
        addContactOrgs(builder, contact_orgsOffset);
        addContactUris(builder, contact_urisOffset);
        addContactNumbers(builder, contact_numbersOffset);
        addContactAddresses(builder, contact_addressesOffset);
        addContactNames(builder, contact_namesOffset);
        addContactId(builder, contact_idOffset);
        return endContact(builder);
    }

    public static void startContact(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addContactId(FlatBufferBuilder builder, int contactIdOffset) {
        builder.addOffset(0, contactIdOffset, 0);
    }

    public static void addContactNames(FlatBufferBuilder builder, int contactNamesOffset) {
        builder.addOffset(1, contactNamesOffset, 0);
    }

    public static int createContactNamesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactNamesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContactAddresses(FlatBufferBuilder builder, int contactAddressesOffset) {
        builder.addOffset(2, contactAddressesOffset, 0);
    }

    public static int createContactAddressesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactAddressesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContactNumbers(FlatBufferBuilder builder, int contactNumbersOffset) {
        builder.addOffset(3, contactNumbersOffset, 0);
    }

    public static int createContactNumbersVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactNumbersVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContactUris(FlatBufferBuilder builder, int contactUrisOffset) {
        builder.addOffset(4, contactUrisOffset, 0);
    }

    public static int createContactUrisVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactUrisVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContactOrgs(FlatBufferBuilder builder, int contactOrgsOffset) {
        builder.addOffset(5, contactOrgsOffset, 0);
    }

    public static int createContactOrgsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startContactOrgsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endContact(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
