package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImComposingStatus extends Table {
    public static ImComposingStatus getRootAsImComposingStatus(ByteBuffer _bb) {
        return getRootAsImComposingStatus(_bb, new ImComposingStatus());
    }

    public static ImComposingStatus getRootAsImComposingStatus(ByteBuffer _bb, ImComposingStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImComposingStatus __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String contentType() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long interval() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isActive() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createImComposingStatus(FlatBufferBuilder builder, int content_typeOffset, long interval, boolean is_active) {
        builder.startObject(3);
        addInterval(builder, interval);
        addContentType(builder, content_typeOffset);
        addIsActive(builder, is_active);
        return endImComposingStatus(builder);
    }

    public static void startImComposingStatus(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(0, contentTypeOffset, 0);
    }

    public static void addInterval(FlatBufferBuilder builder, long interval) {
        builder.addInt(1, (int) interval, 0);
    }

    public static void addIsActive(FlatBufferBuilder builder, boolean isActive) {
        builder.addBoolean(2, isActive, false);
    }

    public static int endImComposingStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
