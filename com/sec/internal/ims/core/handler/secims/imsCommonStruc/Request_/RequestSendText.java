package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendText extends Table {
    public static RequestSendText getRootAsRequestSendText(ByteBuffer _bb) {
        return getRootAsRequestSendText(_bb, new RequestSendText());
    }

    public static RequestSendText getRootAsRequestSendText(ByteBuffer _bb, RequestSendText obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendText __assign(int _i, ByteBuffer _bb) {
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

    public String text() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long len() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendText(FlatBufferBuilder builder, long handle, long session, int textOffset, long len) {
        builder.startObject(4);
        addLen(builder, len);
        addText(builder, textOffset);
        addSession(builder, session);
        addHandle(builder, handle);
        return endRequestSendText(builder);
    }

    public static void startRequestSendText(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addText(FlatBufferBuilder builder, int textOffset) {
        builder.addOffset(2, textOffset, 0);
    }

    public static void addLen(FlatBufferBuilder builder, long len) {
        builder.addInt(3, (int) len, 0);
    }

    public static int endRequestSendText(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        return o;
    }
}
