package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImError extends Table {
    public static ImError getRootAsImError(ByteBuffer _bb) {
        return getRootAsImError(_bb, new ImError());
    }

    public static ImError getRootAsImError(ByteBuffer _bb, ImError obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImError __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int errorType() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int errorCode() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createImError(FlatBufferBuilder builder, int error_type, int error_code) {
        builder.startObject(2);
        addErrorCode(builder, error_code);
        addErrorType(builder, error_type);
        return endImError(builder);
    }

    public static void startImError(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addErrorType(FlatBufferBuilder builder, int errorType) {
        builder.addInt(0, errorType, 0);
    }

    public static void addErrorCode(FlatBufferBuilder builder, int errorCode) {
        builder.addInt(1, errorCode, 0);
    }

    public static int endImError(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
