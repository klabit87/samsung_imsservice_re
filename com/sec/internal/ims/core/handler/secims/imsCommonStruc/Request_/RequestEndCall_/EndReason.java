package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestEndCall_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EndReason extends Table {
    public static EndReason getRootAsEndReason(ByteBuffer _bb) {
        return getRootAsEndReason(_bb, new EndReason());
    }

    public static EndReason getRootAsEndReason(ByteBuffer _bb, EndReason obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EndReason __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String protocol() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer protocolAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long cause() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String text() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String extension(int j) {
        int o = __offset(10);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int extensionLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean isLocalRelease() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createEndReason(FlatBufferBuilder builder, int protocolOffset, long cause, int textOffset, int extensionOffset, boolean is_local_release) {
        builder.startObject(5);
        addExtension(builder, extensionOffset);
        addText(builder, textOffset);
        addCause(builder, cause);
        addProtocol(builder, protocolOffset);
        addIsLocalRelease(builder, is_local_release);
        return endEndReason(builder);
    }

    public static void startEndReason(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addProtocol(FlatBufferBuilder builder, int protocolOffset) {
        builder.addOffset(0, protocolOffset, 0);
    }

    public static void addCause(FlatBufferBuilder builder, long cause) {
        builder.addInt(1, (int) cause, 0);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(2, textOffset, 0);
    }

    public static void addExtension(FlatBufferBuilder builder, int extensionOffset) {
        builder.addOffset(3, extensionOffset, 0);
    }

    public static int createExtensionVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startExtensionVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsLocalRelease(FlatBufferBuilder builder, boolean isLocalRelease) {
        builder.addBoolean(4, isLocalRelease, false);
    }

    public static int endEndReason(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
