package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PresencePublishStatus extends Table {
    public static PresencePublishStatus getRootAsPresencePublishStatus(ByteBuffer _bb) {
        return getRootAsPresencePublishStatus(_bb, new PresencePublishStatus());
    }

    public static PresencePublishStatus getRootAsPresencePublishStatus(ByteBuffer _bb, PresencePublishStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public PresencePublishStatus __assign(int _i, ByteBuffer _bb) {
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

    public boolean isSuccess() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long remoteExpires() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long sipErrorCode() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String sipErrorPhrase() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipErrorPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String etag() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer etagAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long minExpires() {
        int o = __offset(16);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isRefresh() {
        int o = __offset(18);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long retryAfter() {
        int o = __offset(20);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createPresencePublishStatus(FlatBufferBuilder builder, long handle, boolean is_success, long remote_expires, long sip_error_code, int sip_error_phraseOffset, int etagOffset, long min_expires, boolean is_refresh, long retry_after) {
        builder.startObject(9);
        addRetryAfter(builder, retry_after);
        addMinExpires(builder, min_expires);
        addEtag(builder, etagOffset);
        addSipErrorPhrase(builder, sip_error_phraseOffset);
        addSipErrorCode(builder, sip_error_code);
        addRemoteExpires(builder, remote_expires);
        addHandle(builder, handle);
        addIsRefresh(builder, is_refresh);
        addIsSuccess(builder, is_success);
        return endPresencePublishStatus(builder);
    }

    public static void startPresencePublishStatus(FlatBufferBuilder builder) {
        builder.startObject(9);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addIsSuccess(FlatBufferBuilder builder, boolean isSuccess) {
        builder.addBoolean(1, isSuccess, false);
    }

    public static void addRemoteExpires(FlatBufferBuilder builder, long remoteExpires) {
        builder.addInt(2, (int) remoteExpires, 0);
    }

    public static void addSipErrorCode(FlatBufferBuilder builder, long sipErrorCode) {
        builder.addInt(3, (int) sipErrorCode, 0);
    }

    public static void addSipErrorPhrase(FlatBufferBuilder builder, int sipErrorPhraseOffset) {
        builder.addOffset(4, sipErrorPhraseOffset, 0);
    }

    public static void addEtag(FlatBufferBuilder builder, int etagOffset) {
        builder.addOffset(5, etagOffset, 0);
    }

    public static void addMinExpires(FlatBufferBuilder builder, long minExpires) {
        builder.addInt(6, (int) minExpires, 0);
    }

    public static void addIsRefresh(FlatBufferBuilder builder, boolean isRefresh) {
        builder.addBoolean(7, isRefresh, false);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, long retryAfter) {
        builder.addInt(8, (int) retryAfter, 0);
    }

    public static int endPresencePublishStatus(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 12);
        return o;
    }
}
