package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateAudioInterface extends Table {
    public static RequestUpdateAudioInterface getRootAsRequestUpdateAudioInterface(ByteBuffer _bb) {
        return getRootAsRequestUpdateAudioInterface(_bb, new RequestUpdateAudioInterface());
    }

    public static RequestUpdateAudioInterface getRootAsRequestUpdateAudioInterface(ByteBuffer _bb, RequestUpdateAudioInterface obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateAudioInterface __assign(int _i, ByteBuffer _bb) {
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

    public String mode() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer modeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestUpdateAudioInterface(FlatBufferBuilder builder, long handle, int modeOffset) {
        builder.startObject(2);
        addMode(builder, modeOffset);
        addHandle(builder, handle);
        return endRequestUpdateAudioInterface(builder);
    }

    public static void startRequestUpdateAudioInterface(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addMode(FlatBufferBuilder builder, int modeOffset) {
        builder.addOffset(1, modeOffset, 0);
    }

    public static int endRequestUpdateAudioInterface(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
