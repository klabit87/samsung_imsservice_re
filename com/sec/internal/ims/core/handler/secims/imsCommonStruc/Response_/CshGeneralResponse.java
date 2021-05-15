package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CshGeneralResponse extends Table {
    public static CshGeneralResponse getRootAsCshGeneralResponse(ByteBuffer _bb) {
        return getRootAsCshGeneralResponse(_bb, new CshGeneralResponse());
    }

    public static CshGeneralResponse getRootAsCshGeneralResponse(ByteBuffer _bb, CshGeneralResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CshGeneralResponse __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int error() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createCshGeneralResponse(FlatBufferBuilder builder, long session_id, int error) {
        builder.startObject(2);
        addError(builder, error);
        addSessionId(builder, session_id);
        return endCshGeneralResponse(builder);
    }

    public static void startCshGeneralResponse(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addError(FlatBufferBuilder builder, int error) {
        builder.addInt(1, error, 0);
    }

    public static int endCshGeneralResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
