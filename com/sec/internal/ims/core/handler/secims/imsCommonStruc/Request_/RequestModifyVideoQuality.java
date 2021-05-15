package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestModifyVideoQuality extends Table {
    public static RequestModifyVideoQuality getRootAsRequestModifyVideoQuality(ByteBuffer _bb) {
        return getRootAsRequestModifyVideoQuality(_bb, new RequestModifyVideoQuality());
    }

    public static RequestModifyVideoQuality getRootAsRequestModifyVideoQuality(ByteBuffer _bb, RequestModifyVideoQuality obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestModifyVideoQuality __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long session() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int oldQual() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int newQual() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRequestModifyVideoQuality(FlatBufferBuilder builder, long session, int old_qual, int new_qual) {
        builder.startObject(3);
        addNewQual(builder, new_qual);
        addOldQual(builder, old_qual);
        addSession(builder, session);
        return endRequestModifyVideoQuality(builder);
    }

    public static void startRequestModifyVideoQuality(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(0, (int) session, 0);
    }

    public static void addOldQual(FlatBufferBuilder builder, int oldQual) {
        builder.addInt(1, oldQual, 0);
    }

    public static void addNewQual(FlatBufferBuilder builder, int newQual) {
        builder.addInt(2, newQual, 0);
    }

    public static int endRequestModifyVideoQuality(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
