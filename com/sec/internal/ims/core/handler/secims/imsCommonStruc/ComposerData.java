package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ComposerData extends Table {
    public static ComposerData getRootAsComposerData(ByteBuffer _bb) {
        return getRootAsComposerData(_bb, new ComposerData());
    }

    public static ComposerData getRootAsComposerData(ByteBuffer _bb, ComposerData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ComposerData __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public boolean importance() {
        int o = __offset(4);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String subject() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String image() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imageAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String latitude() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer latitudeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String longitude() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer longitudeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String radius() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer radiusAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createComposerData(FlatBufferBuilder builder, boolean importance, int subjectOffset, int imageOffset, int latitudeOffset, int longitudeOffset, int radiusOffset) {
        builder.startObject(6);
        addRadius(builder, radiusOffset);
        addLongitude(builder, longitudeOffset);
        addLatitude(builder, latitudeOffset);
        addImage(builder, imageOffset);
        addSubject(builder, subjectOffset);
        addImportance(builder, importance);
        return endComposerData(builder);
    }

    public static void startComposerData(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addImportance(FlatBufferBuilder builder, boolean importance) {
        builder.addBoolean(0, importance, false);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(1, subjectOffset, 0);
    }

    public static void addImage(FlatBufferBuilder builder, int imageOffset) {
        builder.addOffset(2, imageOffset, 0);
    }

    public static void addLatitude(FlatBufferBuilder builder, int latitudeOffset) {
        builder.addOffset(3, latitudeOffset, 0);
    }

    public static void addLongitude(FlatBufferBuilder builder, int longitudeOffset) {
        builder.addOffset(4, longitudeOffset, 0);
    }

    public static void addRadius(FlatBufferBuilder builder, int radiusOffset) {
        builder.addOffset(5, radiusOffset, 0);
    }

    public static int endComposerData(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
