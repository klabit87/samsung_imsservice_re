package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestDnsQuery extends Table {
    public static RequestDnsQuery getRootAsRequestDnsQuery(ByteBuffer _bb) {
        return getRootAsRequestDnsQuery(_bb, new RequestDnsQuery());
    }

    public static RequestDnsQuery getRootAsRequestDnsQuery(ByteBuffer _bb, RequestDnsQuery obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestDnsQuery __assign(int _i, ByteBuffer _bb) {
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

    public long netId() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public String interfaceNw() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer interfaceNwAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String hostname() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer hostnameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String dnsServerList(int j) {
        int o = __offset(12);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int dnsServerListLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String type() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String transport() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer transportAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String family() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer familyAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createRequestDnsQuery(FlatBufferBuilder builder, long handle, long net_id, int interface_nwOffset, int hostnameOffset, int dns_server_listOffset, int typeOffset, int transportOffset, int familyOffset) {
        builder.startObject(8);
        addNetId(builder, net_id);
        addFamily(builder, familyOffset);
        addTransport(builder, transportOffset);
        addType(builder, typeOffset);
        addDnsServerList(builder, dns_server_listOffset);
        addHostname(builder, hostnameOffset);
        addInterfaceNw(builder, interface_nwOffset);
        addHandle(builder, handle);
        return endRequestDnsQuery(builder);
    }

    public static void startRequestDnsQuery(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addNetId(FlatBufferBuilder builder, long netId) {
        builder.addLong(1, netId, 0);
    }

    public static void addInterfaceNw(FlatBufferBuilder builder, int interfaceNwOffset) {
        builder.addOffset(2, interfaceNwOffset, 0);
    }

    public static void addHostname(FlatBufferBuilder builder, int hostnameOffset) {
        builder.addOffset(3, hostnameOffset, 0);
    }

    public static void addDnsServerList(FlatBufferBuilder builder, int dnsServerListOffset) {
        builder.addOffset(4, dnsServerListOffset, 0);
    }

    public static int createDnsServerListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startDnsServerListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addType(FlatBufferBuilder builder, int typeOffset) {
        builder.addOffset(5, typeOffset, 0);
    }

    public static void addTransport(FlatBufferBuilder builder, int transportOffset) {
        builder.addOffset(6, transportOffset, 0);
    }

    public static void addFamily(FlatBufferBuilder builder, int familyOffset) {
        builder.addOffset(7, familyOffset, 0);
    }

    public static int endRequestDnsQuery(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 8);
        builder.required(o, 10);
        return o;
    }
}
