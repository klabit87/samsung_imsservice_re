package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DumpMessage extends Table {
    public static DumpMessage getRootAsDumpMessage(ByteBuffer _bb) {
        return getRootAsDumpMessage(_bb, new DumpMessage());
    }

    public static DumpMessage getRootAsDumpMessage(ByteBuffer _bb, DumpMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DumpMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String tag() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer tagAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String value() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer valueAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean secure() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createDumpMessage(FlatBufferBuilder builder, int tagOffset, int valueOffset, boolean secure) {
        builder.startObject(3);
        addValue(builder, valueOffset);
        addTag(builder, tagOffset);
        addSecure(builder, secure);
        return endDumpMessage(builder);
    }

    public static void startDumpMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addTag(FlatBufferBuilder builder, int tagOffset) {
        builder.addOffset(0, tagOffset, 0);
    }

    public static void addValue(FlatBufferBuilder builder, int valueOffset) {
        builder.addOffset(1, valueOffset, 0);
    }

    public static void addSecure(FlatBufferBuilder builder, boolean secure) {
        builder.addBoolean(2, secure, false);
    }

    public static int endDumpMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
