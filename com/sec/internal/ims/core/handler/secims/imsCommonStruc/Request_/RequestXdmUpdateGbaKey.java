package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateGbaKey extends Table {
    public static RequestXdmUpdateGbaKey getRootAsRequestXdmUpdateGbaKey(ByteBuffer _bb) {
        return getRootAsRequestXdmUpdateGbaKey(_bb, new RequestXdmUpdateGbaKey());
    }

    public static RequestXdmUpdateGbaKey getRootAsRequestXdmUpdateGbaKey(ByteBuffer _bb, RequestXdmUpdateGbaKey obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmUpdateGbaKey __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String btid() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String gbaKey() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer gbaKeyAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createRequestXdmUpdateGbaKey(FlatBufferBuilder builder, long rid, int btidOffset, int gba_keyOffset) {
        builder.startObject(3);
        addGbaKey(builder, gba_keyOffset);
        addBtid(builder, btidOffset);
        addRid(builder, rid);
        return endRequestXdmUpdateGbaKey(builder);
    }

    public static void startRequestXdmUpdateGbaKey(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addBtid(FlatBufferBuilder builder, int btidOffset) {
        builder.addOffset(1, btidOffset, 0);
    }

    public static void addGbaKey(FlatBufferBuilder builder, int gbaKeyOffset) {
        builder.addOffset(2, gbaKeyOffset, 0);
    }

    public static int endRequestXdmUpdateGbaKey(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        return o;
    }
}
