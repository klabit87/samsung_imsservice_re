package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestEucSendResponse extends Table {
    public static RequestEucSendResponse getRootAsRequestEucSendResponse(ByteBuffer _bb) {
        return getRootAsRequestEucSendResponse(_bb, new RequestEucSendResponse());
    }

    public static RequestEucSendResponse getRootAsRequestEucSendResponse(ByteBuffer _bb, RequestEucSendResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestEucSendResponse __assign(int _i, ByteBuffer _bb) {
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

    public int value() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String id() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int type() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String pin() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pinAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String remoteUri() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createRequestEucSendResponse(FlatBufferBuilder builder, long handle, int value, int idOffset, int type, int pinOffset, int remote_uriOffset) {
        builder.startObject(6);
        addRemoteUri(builder, remote_uriOffset);
        addPin(builder, pinOffset);
        addType(builder, type);
        addId(builder, idOffset);
        addValue(builder, value);
        addHandle(builder, handle);
        return endRequestEucSendResponse(builder);
    }

    public static void startRequestEucSendResponse(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addValue(FlatBufferBuilder builder, int value) {
        builder.addInt(1, value, 0);
    }

    public static void addId(FlatBufferBuilder builder, int idOffset) {
        builder.addOffset(2, idOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int type) {
        builder.addInt(3, type, 0);
    }

    public static void addPin(FlatBufferBuilder builder, int pinOffset) {
        builder.addOffset(4, pinOffset, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(5, remoteUriOffset, 0);
    }

    public static int endRequestEucSendResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        builder.required(o, 14);
        return o;
    }
}
