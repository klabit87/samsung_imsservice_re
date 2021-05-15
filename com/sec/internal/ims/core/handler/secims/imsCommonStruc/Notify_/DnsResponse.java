package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DnsResponse extends Table {
    public static DnsResponse getRootAsDnsResponse(ByteBuffer _bb) {
        return getRootAsDnsResponse(_bb, new DnsResponse());
    }

    public static DnsResponse getRootAsDnsResponse(ByteBuffer _bb, DnsResponse obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public DnsResponse __assign(int _i, ByteBuffer _bb) {
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

    public String ipAddrList(int j) {
        int o = __offset(6);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int ipAddrListLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long port() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
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

    public static int createDnsResponse(FlatBufferBuilder builder, long handle, int ip_addr_listOffset, long port, int hostnameOffset) {
        builder.startObject(4);
        addHostname(builder, hostnameOffset);
        addPort(builder, port);
        addIpAddrList(builder, ip_addr_listOffset);
        addHandle(builder, handle);
        return endDnsResponse(builder);
    }

    public static void startDnsResponse(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addIpAddrList(FlatBufferBuilder builder, int ipAddrListOffset) {
        builder.addOffset(1, ipAddrListOffset, 0);
    }

    public static int createIpAddrListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startIpAddrListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addPort(FlatBufferBuilder builder, long port) {
        builder.addInt(2, (int) port, 0);
    }

    public static void addHostname(FlatBufferBuilder builder, int hostnameOffset) {
        builder.addOffset(3, hostnameOffset, 0);
    }

    public static int endDnsResponse(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        return o;
    }
}
