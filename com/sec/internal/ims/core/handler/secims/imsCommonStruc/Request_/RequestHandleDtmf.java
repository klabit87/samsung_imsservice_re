package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestHandleDtmf extends Table {
    public static RequestHandleDtmf getRootAsRequestHandleDtmf(ByteBuffer _bb) {
        return getRootAsRequestHandleDtmf(_bb, new RequestHandleDtmf());
    }

    public static RequestHandleDtmf getRootAsRequestHandleDtmf(ByteBuffer _bb, RequestHandleDtmf obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestHandleDtmf __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long code() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long mode() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long operation() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestHandleDtmf(FlatBufferBuilder builder, long handle, long session, long code, long mode, long operation) {
        builder.startObject(5);
        addOperation(builder, operation);
        addMode(builder, mode);
        addCode(builder, code);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestHandleDtmf(builder);
    }

    public static void startRequestHandleDtmf(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addCode(FlatBufferBuilder builder, long code) {
        builder.addInt(2, (int) code, 0);
    }

    public static void addMode(FlatBufferBuilder builder, long mode) {
        builder.addInt(3, (int) mode, 0);
    }

    public static void addOperation(FlatBufferBuilder builder, long operation) {
        builder.addInt(4, (int) operation, 0);
    }

    public static int endRequestHandleDtmf(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
