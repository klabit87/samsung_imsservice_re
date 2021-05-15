package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmUpdateGbaData extends Table {
    public static RequestXdmUpdateGbaData getRootAsRequestXdmUpdateGbaData(ByteBuffer _bb) {
        return getRootAsRequestXdmUpdateGbaData(_bb, new RequestXdmUpdateGbaData());
    }

    public static RequestXdmUpdateGbaData getRootAsRequestXdmUpdateGbaData(ByteBuffer _bb, RequestXdmUpdateGbaData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmUpdateGbaData __assign(int _i, ByteBuffer _bb) {
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

    public String imsi() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imsiAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean gbaUiccSupported() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestXdmUpdateGbaData(FlatBufferBuilder builder, long rid, int imsiOffset, boolean gba_uicc_supported) {
        builder.startObject(3);
        addImsi(builder, imsiOffset);
        addRid(builder, rid);
        addGbaUiccSupported(builder, gba_uicc_supported);
        return endRequestXdmUpdateGbaData(builder);
    }

    public static void startRequestXdmUpdateGbaData(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addImsi(FlatBufferBuilder builder, int imsiOffset) {
        builder.addOffset(1, imsiOffset, 0);
    }

    public static void addGbaUiccSupported(FlatBufferBuilder builder, boolean gbaUiccSupported) {
        builder.addBoolean(2, gbaUiccSupported, false);
    }

    public static int endRequestXdmUpdateGbaData(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
