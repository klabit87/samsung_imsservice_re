package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartLocalRingBackTone extends Table {
    public static RequestStartLocalRingBackTone getRootAsRequestStartLocalRingBackTone(ByteBuffer _bb) {
        return getRootAsRequestStartLocalRingBackTone(_bb, new RequestStartLocalRingBackTone());
    }

    public static RequestStartLocalRingBackTone getRootAsRequestStartLocalRingBackTone(ByteBuffer _bb, RequestStartLocalRingBackTone obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartLocalRingBackTone __assign(int _i, ByteBuffer _bb) {
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

    public long streamType() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long volume() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long toneType() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestStartLocalRingBackTone(FlatBufferBuilder builder, long handle, long stream_type, long volume, long tone_type) {
        builder.startObject(4);
        addToneType(builder, tone_type);
        addVolume(builder, volume);
        addStreamType(builder, stream_type);
        addHandle(builder, handle);
        return endRequestStartLocalRingBackTone(builder);
    }

    public static void startRequestStartLocalRingBackTone(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addStreamType(FlatBufferBuilder builder, long streamType) {
        builder.addInt(1, (int) streamType, 0);
    }

    public static void addVolume(FlatBufferBuilder builder, long volume) {
        builder.addInt(2, (int) volume, 0);
    }

    public static void addToneType(FlatBufferBuilder builder, long toneType) {
        builder.addInt(3, (int) toneType, 0);
    }

    public static int endRequestStartLocalRingBackTone(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
