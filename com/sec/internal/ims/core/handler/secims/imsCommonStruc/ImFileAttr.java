package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImFileAttr extends Table {
    public static ImFileAttr getRootAsImFileAttr(ByteBuffer _bb) {
        return getRootAsImFileAttr(_bb, new ImFileAttr());
    }

    public static ImFileAttr getRootAsImFileAttr(ByteBuffer _bb, ImFileAttr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImFileAttr __assign(int _i, ByteBuffer _bb) {
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

    public String path() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pathAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String contentType() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long size() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public long start() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public long end() {
        int o = __offset(14);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public long timeDuration() {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public static int createImFileAttr(FlatBufferBuilder builder, int nameOffset, int pathOffset, int content_typeOffset, long size, long start, long end, long time_duration) {
        builder.startObject(7);
        addTimeDuration(builder, time_duration);
        addEnd(builder, end);
        addStart(builder, start);
        addSize(builder, size);
        addContentType(builder, content_typeOffset);
        addPath(builder, pathOffset);
        addName(builder, nameOffset);
        return endImFileAttr(builder);
    }

    public static void startImFileAttr(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addPath(FlatBufferBuilder builder, int pathOffset) {
        builder.addOffset(1, pathOffset, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(2, contentTypeOffset, 0);
    }

    public static void addSize(FlatBufferBuilder builder, long size) {
        builder.addLong(3, size, 0);
    }

    public static void addStart(FlatBufferBuilder builder, long start) {
        builder.addLong(4, start, 0);
    }

    public static void addEnd(FlatBufferBuilder builder, long end) {
        builder.addLong(5, end, 0);
    }

    public static void addTimeDuration(FlatBufferBuilder builder, long timeDuration) {
        builder.addLong(6, timeDuration, 0);
    }

    public static int endImFileAttr(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
