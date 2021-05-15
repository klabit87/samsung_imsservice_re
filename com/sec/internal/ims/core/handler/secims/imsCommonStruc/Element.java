package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element_.Attribute;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Element extends Table {
    public static Element getRootAsElement(ByteBuffer _bb) {
        return getRootAsElement(_bb, new Element());
    }

    public static Element getRootAsElement(ByteBuffer _bb, Element obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Element __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String name() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public Attribute attributes(int j) {
        return attributes(new Attribute(), j);
    }

    public Attribute attributes(Attribute obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int attributesLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element elements(int j) {
        return elements(new Element(), j);
    }

    public Element elements(Element obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int elementsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String value() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String nameSpace() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameSpaceAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createElement(FlatBufferBuilder builder, int nameOffset, int attributesOffset, int elementsOffset, int valueOffset, int name_spaceOffset) {
        builder.startObject(5);
        addNameSpace(builder, name_spaceOffset);
        addValue(builder, valueOffset);
        addElements(builder, elementsOffset);
        addAttributes(builder, attributesOffset);
        addName(builder, nameOffset);
        return endElement(builder);
    }

    public static void startElement(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addAttributes(FlatBufferBuilder builder, int attributesOffset) {
        builder.addOffset(1, attributesOffset, 0);
    }

    public static int createAttributesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startAttributesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addElements(FlatBufferBuilder builder, int elementsOffset) {
        builder.addOffset(2, elementsOffset, 0);
    }

    public static int createElementsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startElementsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addValue(FlatBufferBuilder builder, int valueOffset) {
        builder.addOffset(3, valueOffset, 0);
    }

    public static void addNameSpace(FlatBufferBuilder builder, int nameSpaceOffset) {
        builder.addOffset(4, nameSpaceOffset, 0);
    }

    public static int endElement(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
