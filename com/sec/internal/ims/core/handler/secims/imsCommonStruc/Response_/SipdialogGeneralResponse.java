package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SipdialogGeneralResponse extends Table {
    public static SipdialogGeneralResponse getRootAsSipdialogGeneralResponse(ByteBuffer _bb) {
        return getRootAsSipdialogGeneralResponse(_bb, new SipdialogGeneralResponse());
    }

    public static SipdialogGeneralResponse getRootAsSipdialogGeneralResponse(ByteBuffer _bb, SipdialogGeneralResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SipdialogGeneralResponse __assign(int _i, ByteBuffer _bb) {
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

    public boolean success() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String sipmessage() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipmessageAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createSipdialogGeneralResponse(FlatBufferBuilder builder, long handle, boolean success, int sipmessageOffset) {
        builder.startObject(3);
        addSipmessage(builder, sipmessageOffset);
        addHandle(builder, handle);
        addSuccess(builder, success);
        return endSipdialogGeneralResponse(builder);
    }

    public static void startSipdialogGeneralResponse(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSuccess(FlatBufferBuilder builder, boolean success) {
        builder.addBoolean(1, success, false);
    }

    public static void addSipmessage(FlatBufferBuilder builder, int sipmessageOffset) {
        builder.addOffset(2, sipmessageOffset, 0);
    }

    public static int endSipdialogGeneralResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
