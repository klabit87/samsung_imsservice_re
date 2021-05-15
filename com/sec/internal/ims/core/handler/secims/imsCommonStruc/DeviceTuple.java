package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DeviceTuple extends Table {
    public static DeviceTuple getRootAsDeviceTuple(ByteBuffer _bb) {
        return getRootAsDeviceTuple(_bb, new DeviceTuple());
    }

    public static DeviceTuple getRootAsDeviceTuple(ByteBuffer _bb, DeviceTuple obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DeviceTuple __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String deviceId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public Element deviceCapabilities(int j) {
        return deviceCapabilities(new Element(), j);
    }

    public Element deviceCapabilities(Element obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int deviceCapabilitiesLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element descriptions(int j) {
        return descriptions(new Element(), j);
    }

    public Element descriptions(Element obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int descriptionsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element extensions(int j) {
        return extensions(new Element(), j);
    }

    public Element extensions(Element obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int extensionsLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element notes(int j) {
        return notes(new Element(), j);
    }

    public Element notes(Element obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String timestamp() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createDeviceTuple(FlatBufferBuilder builder, int device_idOffset, int device_capabilitiesOffset, int descriptionsOffset, int extensionsOffset, int notesOffset, int timestampOffset) {
        builder.startObject(6);
        addTimestamp(builder, timestampOffset);
        addNotes(builder, notesOffset);
        addExtensions(builder, extensionsOffset);
        addDescriptions(builder, descriptionsOffset);
        addDeviceCapabilities(builder, device_capabilitiesOffset);
        addDeviceId(builder, device_idOffset);
        return endDeviceTuple(builder);
    }

    public static void startDeviceTuple(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addDeviceId(FlatBufferBuilder builder, int deviceIdOffset) {
        builder.addOffset(0, deviceIdOffset, 0);
    }

    public static void addDeviceCapabilities(FlatBufferBuilder builder, int deviceCapabilitiesOffset) {
        builder.addOffset(1, deviceCapabilitiesOffset, 0);
    }

    public static int createDeviceCapabilitiesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startDeviceCapabilitiesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addDescriptions(FlatBufferBuilder builder, int descriptionsOffset) {
        builder.addOffset(2, descriptionsOffset, 0);
    }

    public static int createDescriptionsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startDescriptionsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addExtensions(FlatBufferBuilder builder, int extensionsOffset) {
        builder.addOffset(3, extensionsOffset, 0);
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
        builder.addOffset(4, notesOffset, 0);
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
        builder.addOffset(5, timestampOffset, 0);
    }

    public static int endDeviceTuple(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
