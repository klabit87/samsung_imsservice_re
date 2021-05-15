package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BaseMessage extends Table {
    public static BaseMessage getRootAsBaseMessage(ByteBuffer _bb) {
        return getRootAsBaseMessage(_bb, new BaseMessage());
    }

    public static BaseMessage getRootAsBaseMessage(ByteBuffer _bb, BaseMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public BaseMessage __assign(int _i, ByteBuffer _bb) {
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

    public String id() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String remoteUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long timestamp() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public static int createBaseMessage(FlatBufferBuilder builder, long handle, int idOffset, int remote_uriOffset, long timestamp) {
        builder.startObject(4);
        addTimestamp(builder, timestamp);
        addRemoteUri(builder, remote_uriOffset);
        addId(builder, idOffset);
        addHandle(builder, handle);
        return endBaseMessage(builder);
    }

    public static void startBaseMessage(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addId(FlatBufferBuilder builder, int idOffset) {
        builder.addOffset(1, idOffset, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(2, remoteUriOffset, 0);
    }

    public static void addTimestamp(FlatBufferBuilder builder, long timestamp) {
        builder.addLong(3, timestamp, 0);
    }

    public static int endBaseMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
