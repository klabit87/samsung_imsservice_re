package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PersonTuple extends Table {
    public static PersonTuple getRootAsPersonTuple(ByteBuffer _bb) {
        return getRootAsPersonTuple(_bb, new PersonTuple());
    }

    public static PersonTuple getRootAsPersonTuple(ByteBuffer _bb, PersonTuple obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public PersonTuple __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String statusIcon() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer statusIconAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public Element extensions(int j) {
        return extensions(new Element(), j);
    }

    public Element extensions(Element obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int extensionsLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element notes(int j) {
        return notes(new Element(), j);
    }

    public Element notes(Element obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String timestamp() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createPersonTuple(FlatBufferBuilder builder, int status_iconOffset, int extensionsOffset, int notesOffset, int timestampOffset) {
        builder.startObject(4);
        addTimestamp(builder, timestampOffset);
        addNotes(builder, notesOffset);
        addExtensions(builder, extensionsOffset);
        addStatusIcon(builder, status_iconOffset);
        return endPersonTuple(builder);
    }

    public static void startPersonTuple(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addStatusIcon(FlatBufferBuilder builder, int statusIconOffset) {
        builder.addOffset(0, statusIconOffset, 0);
    }

    public static void addExtensions(FlatBufferBuilder builder, int extensionsOffset) {
        builder.addOffset(1, extensionsOffset, 0);
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
        builder.addOffset(2, notesOffset, 0);
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
        builder.addOffset(3, timestampOffset, 0);
    }

    public static int endPersonTuple(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
