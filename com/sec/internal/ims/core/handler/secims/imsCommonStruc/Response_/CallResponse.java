package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallResponse extends Table {
    public static CallResponse getRootAsCallResponse(ByteBuffer _bb) {
        return getRootAsCallResponse(_bb, new CallResponse());
    }

    public static CallResponse getRootAsCallResponse(ByteBuffer _bb, CallResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CallResponse __assign(int _i, ByteBuffer _bb) {
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

    public int session() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int result() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int reason() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String sipCallId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createCallResponse(FlatBufferBuilder builder, long handle, int session, int result, int reason, int sip_call_idOffset) {
        builder.startObject(5);
        addSipCallId(builder, sip_call_idOffset);
        addReason(builder, reason);
        addResult(builder, result);
        addSession(builder, session);
        addHandle(builder, handle);
        return endCallResponse(builder);
    }

    public static void startCallResponse(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, int session) {
        builder.addInt(1, session, 0);
    }

    public static void addResult(FlatBufferBuilder builder, int result) {
        builder.addInt(2, result, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(3, reason, 0);
    }

    public static void addSipCallId(FlatBufferBuilder builder, int sipCallIdOffset) {
        builder.addOffset(4, sipCallIdOffset, 0);
    }

    public static int endCallResponse(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
