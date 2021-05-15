package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUploadPresRules extends Table {
    public static RequestXdmUploadPresRules getRootAsRequestXdmUploadPresRules(ByteBuffer _bb) {
        return getRootAsRequestXdmUploadPresRules(_bb, new RequestXdmUploadPresRules());
    }

    public static RequestXdmUploadPresRules getRootAsRequestXdmUploadPresRules(ByteBuffer _bb, RequestXdmUploadPresRules obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmUploadPresRules __assign(int _i, ByteBuffer _bb) {
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

    public String impu() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String rules() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer rulesAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String btid() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer btidAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String gbaKey() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer gbaKeyAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestXdmUploadPresRules(FlatBufferBuilder builder, long rid, int impuOffset, int rulesOffset, int btidOffset, int gba_keyOffset) {
        builder.startObject(5);
        addGbaKey(builder, gba_keyOffset);
        addBtid(builder, btidOffset);
        addRules(builder, rulesOffset);
        addImpu(builder, impuOffset);
        addRid(builder, rid);
        return endRequestXdmUploadPresRules(builder);
    }

    public static void startRequestXdmUploadPresRules(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(1, impuOffset, 0);
    }

    public static void addRules(FlatBufferBuilder builder, int rulesOffset) {
        builder.addOffset(2, rulesOffset, 0);
    }

    public static void addBtid(FlatBufferBuilder builder, int btidOffset) {
        builder.addOffset(3, btidOffset, 0);
    }

    public static void addGbaKey(FlatBufferBuilder builder, int gbaKeyOffset) {
        builder.addOffset(4, gbaKeyOffset, 0);
    }

    public static int endRequestXdmUploadPresRules(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
