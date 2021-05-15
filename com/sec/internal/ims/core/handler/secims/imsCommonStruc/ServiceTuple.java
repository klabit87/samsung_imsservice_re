package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple_.Status;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ServiceTuple extends Table {
    public static ServiceTuple getRootAsServiceTuple(ByteBuffer _bb) {
        return getRootAsServiceTuple(_bb, new ServiceTuple());
    }

    public static ServiceTuple getRootAsServiceTuple(ByteBuffer _bb, ServiceTuple obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ServiceTuple __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String serviceId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer serviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String version() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer versionAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String description() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer descriptionAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public Status status() {
        return status(new Status());
    }

    public Status status(Status obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public Element mediaCapabilities(int j) {
        return mediaCapabilities(new Element(), j);
    }

    public Element mediaCapabilities(Element obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int mediaCapabilitiesLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element contacts(int j) {
        return contacts(new Element(), j);
    }

    public Element contacts(Element obj, int j) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int contactsLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element extensions(int j) {
        return extensions(new Element(), j);
    }

    public Element extensions(Element obj, int j) {
        int o = __offset(16);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int extensionsLength() {
        int o = __offset(16);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element notes(int j) {
        return notes(new Element(), j);
    }

    public Element notes(Element obj, int j) {
        int o = __offset(18);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int o = __offset(18);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String timestamp() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String tupleId() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer tupleIdAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public static int createServiceTuple(FlatBufferBuilder builder, int service_idOffset, int versionOffset, int descriptionOffset, int statusOffset, int media_capabilitiesOffset, int contactsOffset, int extensionsOffset, int notesOffset, int timestampOffset, int tuple_idOffset) {
        builder.startObject(10);
        addTupleId(builder, tuple_idOffset);
        addTimestamp(builder, timestampOffset);
        addNotes(builder, notesOffset);
        addExtensions(builder, extensionsOffset);
        addContacts(builder, contactsOffset);
        addMediaCapabilities(builder, media_capabilitiesOffset);
        addStatus(builder, statusOffset);
        addDescription(builder, descriptionOffset);
        addVersion(builder, versionOffset);
        addServiceId(builder, service_idOffset);
        return endServiceTuple(builder);
    }

    public static void startServiceTuple(FlatBufferBuilder builder) {
        builder.startObject(10);
    }

    public static void addServiceId(FlatBufferBuilder builder, int serviceIdOffset) {
        builder.addOffset(0, serviceIdOffset, 0);
    }

    public static void addVersion(FlatBufferBuilder builder, int versionOffset) {
        builder.addOffset(1, versionOffset, 0);
    }

    public static void addDescription(FlatBufferBuilder builder, int descriptionOffset) {
        builder.addOffset(2, descriptionOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(3, statusOffset, 0);
    }

    public static void addMediaCapabilities(FlatBufferBuilder builder, int mediaCapabilitiesOffset) {
        builder.addOffset(4, mediaCapabilitiesOffset, 0);
    }

    public static int createMediaCapabilitiesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startMediaCapabilitiesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContacts(FlatBufferBuilder builder, int contactsOffset) {
        builder.addOffset(5, contactsOffset, 0);
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

    public static void addExtensions(FlatBufferBuilder builder, int extensionsOffset) {
        builder.addOffset(6, extensionsOffset, 0);
    }

    public static int createExtensionsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startExtensionsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addNotes(FlatBufferBuilder builder, int notesOffset) {
        builder.addOffset(7, notesOffset, 0);
    }

    public static int createNotesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startNotesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addTimestamp(FlatBufferBuilder builder, int timestampOffset) {
        builder.addOffset(8, timestampOffset, 0);
    }

    public static void addTupleId(FlatBufferBuilder builder, int tupleIdOffset) {
        builder.addOffset(9, tupleIdOffset, 0);
    }

    public static int endServiceTuple(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        return o;
    }
}
