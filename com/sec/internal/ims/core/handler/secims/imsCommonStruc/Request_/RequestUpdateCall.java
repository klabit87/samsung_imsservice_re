package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateCall extends Table {
    public static RequestUpdateCall getRootAsRequestUpdateCall(ByteBuffer _bb) {
        return getRootAsRequestUpdateCall(_bb, new RequestUpdateCall());
    }

    public static RequestUpdateCall getRootAsRequestUpdateCall(ByteBuffer _bb, RequestUpdateCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateCall __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long session() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int action() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int codecType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long cause() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String reasonText() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonTextAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestUpdateCall(FlatBufferBuilder builder, long session, int action, int codec_type, long cause, int reason_textOffset) {
        builder.startObject(5);
        addReasonText(builder, reason_textOffset);
        addCause(builder, cause);
        addCodecType(builder, codec_type);
        addAction(builder, action);
        addSession(builder, session);
        return endRequestUpdateCall(builder);
    }

    public static void startRequestUpdateCall(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addAction(FlatBufferBuilder builder, int action) {
        builder.addInt(1, action, 0);
    }

    public static void addCodecType(FlatBufferBuilder builder, int codecType) {
        builder.addInt(2, codecType, 0);
    }

    public static void addCause(FlatBufferBuilder builder, long cause) {
        builder.addInt(3, (int) cause, 0);
    }

    public static void addReasonText(FlatBufferBuilder builder, int reasonTextOffset) {
        builder.addOffset(4, reasonTextOffset, 0);
    }

    public static int endRequestUpdateCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
