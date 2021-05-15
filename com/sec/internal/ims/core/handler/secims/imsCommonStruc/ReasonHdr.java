package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReasonHdr extends Table {
    public static ReasonHdr getRootAsReasonHdr(ByteBuffer _bb) {
        return getRootAsReasonHdr(_bb, new ReasonHdr());
    }

    public static ReasonHdr getRootAsReasonHdr(ByteBuffer _bb, ReasonHdr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReasonHdr __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long code() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String text() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createReasonHdr(FlatBufferBuilder builder, long code, int textOffset) {
        builder.startObject(2);
        addText(builder, textOffset);
        addCode(builder, code);
        return endReasonHdr(builder);
    }

    public static void startReasonHdr(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addCode(FlatBufferBuilder builder, long code) {
        builder.addInt(0, (int) code, 0);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(1, textOffset, 0);
    }

    public static int endReasonHdr(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
