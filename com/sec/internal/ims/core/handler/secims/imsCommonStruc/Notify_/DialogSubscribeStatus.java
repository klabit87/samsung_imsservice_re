package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DialogSubscribeStatus extends Table {
    public static DialogSubscribeStatus getRootAsDialogSubscribeStatus(ByteBuffer _bb) {
        return getRootAsDialogSubscribeStatus(_bb, new DialogSubscribeStatus());
    }

    public static DialogSubscribeStatus getRootAsDialogSubscribeStatus(ByteBuffer _bb, DialogSubscribeStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DialogSubscribeStatus __assign(int _i, ByteBuffer _bb) {
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

    public long statusCode() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createDialogSubscribeStatus(FlatBufferBuilder builder, long handle, long status_code, int reason_phraseOffset) {
        builder.startObject(3);
        addReasonPhrase(builder, reason_phraseOffset);
        addStatusCode(builder, status_code);
        addHandle(builder, handle);
        return endDialogSubscribeStatus(builder);
    }

    public static void startDialogSubscribeStatus(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(1, (int) statusCode, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder builder, int reasonPhraseOffset) {
        builder.addOffset(2, reasonPhraseOffset, 0);
    }

    public static int endDialogSubscribeStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
