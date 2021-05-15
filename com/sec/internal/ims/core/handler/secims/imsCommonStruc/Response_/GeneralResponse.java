package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class GeneralResponse extends Table {
    public static GeneralResponse getRootAsGeneralResponse(ByteBuffer _bb) {
        return getRootAsGeneralResponse(_bb, new GeneralResponse());
    }

    public static GeneralResponse getRootAsGeneralResponse(ByteBuffer _bb, GeneralResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public GeneralResponse __assign(int _i, ByteBuffer _bb) {
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

    public int result() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int reason() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long sipError() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String errorStr() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer errorStrAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createGeneralResponse(FlatBufferBuilder builder, long handle, int result, int reason, long sip_error, int error_strOffset) {
        builder.startObject(5);
        addErrorStr(builder, error_strOffset);
        addSipError(builder, sip_error);
        addReason(builder, reason);
        addResult(builder, result);
        addHandle(builder, handle);
        return endGeneralResponse(builder);
    }

    public static void startGeneralResponse(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addResult(FlatBufferBuilder builder, int result) {
        builder.addInt(1, result, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(2, reason, 0);
    }

    public static void addSipError(FlatBufferBuilder builder, long sipError) {
        builder.addInt(3, (int) sipError, 0);
    }

    public static void addErrorStr(FlatBufferBuilder builder, int errorStrOffset) {
        builder.addOffset(4, errorStrOffset, 0);
    }

    public static int endGeneralResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
