package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateSrvccVersion extends Table {
    public static RequestUpdateSrvccVersion getRootAsRequestUpdateSrvccVersion(ByteBuffer _bb) {
        return getRootAsRequestUpdateSrvccVersion(_bb, new RequestUpdateSrvccVersion());
    }

    public static RequestUpdateSrvccVersion getRootAsRequestUpdateSrvccVersion(ByteBuffer _bb, RequestUpdateSrvccVersion obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateSrvccVersion __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long phoneId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long version() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestUpdateSrvccVersion(FlatBufferBuilder builder, long phone_id, long version) {
        builder.startObject(2);
        addVersion(builder, version);
        addPhoneId(builder, phone_id);
        return endRequestUpdateSrvccVersion(builder);
    }

    public static void startRequestUpdateSrvccVersion(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(0, (int) phoneId, 0);
    }

    public static void addVersion(FlatBufferBuilder builder, long version) {
        builder.addInt(1, (int) version, 0);
    }

    public static int endRequestUpdateSrvccVersion(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
