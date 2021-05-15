package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NewRemoteCapabilities extends Table {
    public static NewRemoteCapabilities getRootAsNewRemoteCapabilities(ByteBuffer _bb) {
        return getRootAsNewRemoteCapabilities(_bb, new NewRemoteCapabilities());
    }

    public static NewRemoteCapabilities getRootAsNewRemoteCapabilities(ByteBuffer _bb, NewRemoteCapabilities obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public NewRemoteCapabilities __assign(int _i, ByteBuffer _bb) {
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

    public String uri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String capabilties(int j) {
        int o = __offset(8);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int capabiltiesLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createNewRemoteCapabilities(FlatBufferBuilder builder, long handle, int uriOffset, int capabiltiesOffset) {
        builder.startObject(3);
        addCapabilties(builder, capabiltiesOffset);
        addUri(builder, uriOffset);
        addHandle(builder, handle);
        return endNewRemoteCapabilities(builder);
    }

    public static void startNewRemoteCapabilities(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addCapabilties(FlatBufferBuilder builder, int capabiltiesOffset) {
        builder.addOffset(2, capabiltiesOffset, 0);
    }

    public static int createCapabiltiesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startCapabiltiesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endNewRemoteCapabilities(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
