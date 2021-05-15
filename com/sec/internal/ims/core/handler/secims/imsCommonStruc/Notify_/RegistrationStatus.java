package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RegistrationStatus extends Table {
    public static RegistrationStatus getRootAsRegistrationStatus(ByteBuffer _bb) {
        return getRootAsRegistrationStatus(_bb, new RegistrationStatus());
    }

    public static RegistrationStatus getRootAsRegistrationStatus(ByteBuffer _bb, RegistrationStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RegistrationStatus __assign(int _i, ByteBuffer _bb) {
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

    public int regiType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String serviceList(int j) {
        int o = __offset(8);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int serviceListLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String impuList(int j) {
        int o = __offset(10);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int impuListLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public int respCode() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String respReason() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer respReasonAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int retryAfter() {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int ecmpMode() {
        int o = __offset(18);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRegistrationStatus(FlatBufferBuilder builder, long handle, int regi_type, int service_listOffset, int impu_listOffset, int resp_code, int resp_reasonOffset, int retry_after, int ecmp_mode) {
        builder.startObject(8);
        addEcmpMode(builder, ecmp_mode);
        addRetryAfter(builder, retry_after);
        addRespReason(builder, resp_reasonOffset);
        addRespCode(builder, resp_code);
        addImpuList(builder, impu_listOffset);
        addServiceList(builder, service_listOffset);
        addRegiType(builder, regi_type);
        addHandle(builder, handle);
        return endRegistrationStatus(builder);
    }

    public static void startRegistrationStatus(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addRegiType(FlatBufferBuilder builder, int regiType) {
        builder.addInt(1, regiType, 0);
    }

    public static void addServiceList(FlatBufferBuilder builder, int serviceListOffset) {
        builder.addOffset(2, serviceListOffset, 0);
    }

    public static int createServiceListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startServiceListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addImpuList(FlatBufferBuilder builder, int impuListOffset) {
        builder.addOffset(3, impuListOffset, 0);
    }

    public static int createImpuListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startImpuListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addRespCode(FlatBufferBuilder builder, int respCode) {
        builder.addInt(4, respCode, 0);
    }

    public static void addRespReason(FlatBufferBuilder builder, int respReasonOffset) {
        builder.addOffset(5, respReasonOffset, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, int retryAfter) {
        builder.addInt(6, retryAfter, 0);
    }

    public static void addEcmpMode(FlatBufferBuilder builder, int ecmpMode) {
        builder.addInt(7, ecmpMode, 0);
    }

    public static int endRegistrationStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
