package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PresenceServiceStatus extends Table {
    public static PresenceServiceStatus getRootAsPresenceServiceStatus(ByteBuffer _bb) {
        return getRootAsPresenceServiceStatus(_bb, new PresenceServiceStatus());
    }

    public static PresenceServiceStatus getRootAsPresenceServiceStatus(ByteBuffer _bb, PresenceServiceStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public PresenceServiceStatus __assign(int _i, ByteBuffer _bb) {
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

    public String status() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String mediaCapabilities(int j) {
        int o = __offset(10);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int mediaCapabilitiesLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createPresenceServiceStatus(FlatBufferBuilder builder, int serviceIdOffset, int versionOffset, int statusOffset, int media_capabilitiesOffset) {
        builder.startObject(4);
        addMediaCapabilities(builder, media_capabilitiesOffset);
        addStatus(builder, statusOffset);
        addVersion(builder, versionOffset);
        addServiceId(builder, serviceIdOffset);
        return endPresenceServiceStatus(builder);
    }

    public static void startPresenceServiceStatus(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addServiceId(FlatBufferBuilder builder, int serviceIdOffset) {
        builder.addOffset(0, serviceIdOffset, 0);
    }

    public static void addVersion(FlatBufferBuilder builder, int versionOffset) {
        builder.addOffset(1, versionOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(2, statusOffset, 0);
    }

    public static void addMediaCapabilities(FlatBufferBuilder builder, int mediaCapabilitiesOffset) {
        builder.addOffset(3, mediaCapabilitiesOffset, 0);
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

    public static int endPresenceServiceStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
