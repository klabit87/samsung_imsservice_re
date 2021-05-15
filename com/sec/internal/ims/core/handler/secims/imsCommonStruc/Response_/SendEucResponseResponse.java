package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SendEucResponseResponse extends Table {
    public static SendEucResponseResponse getRootAsSendEucResponseResponse(ByteBuffer _bb) {
        return getRootAsSendEucResponseResponse(_bb, new SendEucResponseResponse());
    }

    public static SendEucResponseResponse getRootAsSendEucResponseResponse(ByteBuffer _bb, SendEucResponseResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SendEucResponseResponse __assign(int _i, ByteBuffer _bb) {
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

    public int type() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String remoteUri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public int status() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createSendEucResponseResponse(FlatBufferBuilder builder, long handle, int idOffset, int type, int remote_uriOffset, int status) {
        builder.startObject(5);
        addStatus(builder, status);
        addRemoteUri(builder, remote_uriOffset);
        addType(builder, type);
        addId(builder, idOffset);
        addHandle(builder, handle);
        return endSendEucResponseResponse(builder);
    }

    public static void startSendEucResponseResponse(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addId(FlatBufferBuilder builder, int idOffset) {
        builder.addOffset(1, idOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int type) {
        builder.addInt(2, type, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(3, remoteUriOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int status) {
        builder.addInt(4, status, 0);
    }

    public static int endSendEucResponseResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 10);
        return o;
    }
}
