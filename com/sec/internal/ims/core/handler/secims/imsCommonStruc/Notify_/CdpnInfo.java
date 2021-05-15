package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CdpnInfo extends Table {
    public static CdpnInfo getRootAsCdpnInfo(ByteBuffer _bb) {
        return getRootAsCdpnInfo(_bb, new CdpnInfo());
    }

    public static CdpnInfo getRootAsCdpnInfo(ByteBuffer _bb, CdpnInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CdpnInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String calledPartyNumber() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer calledPartyNumberAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public static int createCdpnInfo(FlatBufferBuilder builder, int called_party_numberOffset) {
        builder.startObject(1);
        addCalledPartyNumber(builder, called_party_numberOffset);
        return endCdpnInfo(builder);
    }

    public static void startCdpnInfo(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addCalledPartyNumber(FlatBufferBuilder builder, int calledPartyNumberOffset) {
        builder.addOffset(0, calledPartyNumberOffset, 0);
    }

    public static int endCdpnInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
