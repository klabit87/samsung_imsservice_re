package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Response extends Table {
    public static Response getRootAsResponse(ByteBuffer _bb) {
        return getRootAsResponse(_bb, new Response());
    }

    public static Response getRootAsResponse(ByteBuffer _bb, Response obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Response __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int resid() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public byte respType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table resp(Table obj) {
        int o = __offset(8);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createResponse(FlatBufferBuilder builder, int resid, byte resp_type, int respOffset) {
        builder.startObject(3);
        addResp(builder, respOffset);
        addResid(builder, resid);
        addRespType(builder, resp_type);
        return endResponse(builder);
    }

    public static void startResponse(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addResid(FlatBufferBuilder builder, int resid) {
        builder.addInt(0, resid, 0);
    }

    public static void addRespType(FlatBufferBuilder builder, byte respType) {
        builder.addByte(1, respType, 0);
    }

    public static void addResp(FlatBufferBuilder builder, int respOffset) {
        builder.addOffset(2, respOffset, 0);
    }

    public static int endResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
