package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartRecord extends Table {
    public static RequestStartRecord getRootAsRequestStartRecord(ByteBuffer _bb) {
        return getRootAsRequestStartRecord(_bb, new RequestStartRecord());
    }

    public static RequestStartRecord getRootAsRequestStartRecord(ByteBuffer _bb, RequestStartRecord obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartRecord __assign(int _i, ByteBuffer _bb) {
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

    public String filepath() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer filepathAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestStartRecord(FlatBufferBuilder builder, long handle, long session, int filepathOffset) {
        builder.startObject(3);
        addFilepath(builder, filepathOffset);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestStartRecord(builder);
    }

    public static void startRequestStartRecord(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addFilepath(FlatBufferBuilder builder, int filepathOffset) {
        builder.addOffset(2, filepathOffset, 0);
    }

    public static int endRequestStartRecord(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
