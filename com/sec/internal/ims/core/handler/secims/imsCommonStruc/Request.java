package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Request extends Table {
    public static Request getRootAsRequest(ByteBuffer _bb) {
        return getRootAsRequest(_bb, new Request());
    }

    public static Request getRootAsRequest(ByteBuffer _bb, Request obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Request __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int reqid() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public byte reqType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.get(this.bb_pos + o);
        }
        return 0;
    }

    public Table req(Table obj) {
        int o = __offset(8);
        if (o != 0) {
            return __union(obj, o);
        }
        return null;
    }

    public static int createRequest(FlatBufferBuilder builder, int reqid, byte req_type, int reqOffset) {
        builder.startObject(3);
        addReq(builder, reqOffset);
        addReqid(builder, reqid);
        addReqType(builder, req_type);
        return endRequest(builder);
    }

    public static void startRequest(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addReqid(FlatBufferBuilder builder, int reqid) {
        builder.addInt(0, reqid, 0);
    }

    public static void addReqType(FlatBufferBuilder builder, byte reqType) {
        builder.addByte(1, reqType, 0);
    }

    public static void addReq(FlatBufferBuilder builder, int reqOffset) {
        builder.addOffset(2, reqOffset, 0);
    }

    public static int endRequest(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
