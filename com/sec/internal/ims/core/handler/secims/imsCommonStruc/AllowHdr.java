package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AllowHdr extends Table {
    public static AllowHdr getRootAsAllowHdr(ByteBuffer _bb) {
        return getRootAsAllowHdr(_bb, new AllowHdr());
    }

    public static AllowHdr getRootAsAllowHdr(ByteBuffer _bb, AllowHdr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public AllowHdr __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String text() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createAllowHdr(FlatBufferBuilder builder, int textOffset) {
        builder.startObject(1);
        addText(builder, textOffset);
        return endAllowHdr(builder);
    }

    public static void startAllowHdr(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(0, textOffset, 0);
    }

    public static int endAllowHdr(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
