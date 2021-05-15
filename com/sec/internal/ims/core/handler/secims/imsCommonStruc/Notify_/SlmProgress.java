package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SlmProgress extends Table {
    public static SlmProgress getRootAsSlmProgress(ByteBuffer _bb) {
        return getRootAsSlmProgress(_bb, new SlmProgress());
    }

    public static SlmProgress getRootAsSlmProgress(ByteBuffer _bb, SlmProgress obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SlmProgress __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String imdnMessageId() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public long total() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long transferred() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long state() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createSlmProgress(FlatBufferBuilder builder, long session_handle, int imdn_message_idOffset, long total, long transferred, long state, int im_errorOffset) {
        builder.startObject(6);
        addImError(builder, im_errorOffset);
        addState(builder, state);
        addTransferred(builder, transferred);
        addTotal(builder, total);
        addImdnMessageId(builder, imdn_message_idOffset);
        addSessionHandle(builder, session_handle);
        return endSlmProgress(builder);
    }

    public static void startSlmProgress(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(1, imdnMessageIdOffset, 0);
    }

    public static void addTotal(FlatBufferBuilder builder, long total) {
        builder.addInt(2, (int) total, 0);
    }

    public static void addTransferred(FlatBufferBuilder builder, long transferred) {
        builder.addInt(3, (int) transferred, 0);
    }

    public static void addState(FlatBufferBuilder builder, long state) {
        builder.addInt(4, (int) state, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(5, imErrorOffset, 0);
    }

    public static int endSlmProgress(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 14);
        return o;
    }
}
