package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateGeolocation extends Table {
    public static RequestUpdateGeolocation getRootAsRequestUpdateGeolocation(ByteBuffer _bb) {
        return getRootAsRequestUpdateGeolocation(_bb, new RequestUpdateGeolocation());
    }

    public static RequestUpdateGeolocation getRootAsRequestUpdateGeolocation(ByteBuffer _bb, RequestUpdateGeolocation obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateGeolocation __assign(int _i, ByteBuffer _bb) {
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

    public String latitude() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer latitudeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String longitude() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer longitudeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String altitude() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer altitudeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String accuracy() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer accuracyAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String providertype() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer providertypeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String retentionexpires() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer retentionexpiresAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String srsname() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer srsnameAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String radiusuom() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer radiusuomAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String os() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer osAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String deviceid() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceidAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String country() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer countryAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String a1() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer a1AsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String a3() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer a3AsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String a6() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer a6AsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String hno() {
        int o = __offset(34);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer hnoAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public String pc() {
        int o = __offset(36);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pcAsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String locationtime() {
        int o = __offset(38);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer locationtimeAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public static int createRequestUpdateGeolocation(FlatBufferBuilder builder, long handle, int latitudeOffset, int longitudeOffset, int altitudeOffset, int accuracyOffset, int providertypeOffset, int retentionexpiresOffset, int srsnameOffset, int radiusuomOffset, int osOffset, int deviceidOffset, int countryOffset, int a1Offset, int a3Offset, int a6Offset, int hnoOffset, int pcOffset, int locationtimeOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(18);
        addLocationtime(flatBufferBuilder, locationtimeOffset);
        addPc(flatBufferBuilder, pcOffset);
        addHno(flatBufferBuilder, hnoOffset);
        addA6(flatBufferBuilder, a6Offset);
        addA3(flatBufferBuilder, a3Offset);
        addA1(flatBufferBuilder, a1Offset);
        addCountry(flatBufferBuilder, countryOffset);
        addDeviceid(flatBufferBuilder, deviceidOffset);
        addOs(flatBufferBuilder, osOffset);
        addRadiusuom(flatBufferBuilder, radiusuomOffset);
        addSrsname(flatBufferBuilder, srsnameOffset);
        addRetentionexpires(flatBufferBuilder, retentionexpiresOffset);
        addProvidertype(flatBufferBuilder, providertypeOffset);
        addAccuracy(flatBufferBuilder, accuracyOffset);
        addAltitude(flatBufferBuilder, altitudeOffset);
        addLongitude(flatBufferBuilder, longitudeOffset);
        addLatitude(flatBufferBuilder, latitudeOffset);
        addHandle(builder, handle);
        return endRequestUpdateGeolocation(builder);
    }

    public static void startRequestUpdateGeolocation(FlatBufferBuilder builder) {
        builder.startObject(18);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addLatitude(FlatBufferBuilder builder, int latitudeOffset) {
        builder.addOffset(1, latitudeOffset, 0);
    }

    public static void addLongitude(FlatBufferBuilder builder, int longitudeOffset) {
        builder.addOffset(2, longitudeOffset, 0);
    }

    public static void addAltitude(FlatBufferBuilder builder, int altitudeOffset) {
        builder.addOffset(3, altitudeOffset, 0);
    }

    public static void addAccuracy(FlatBufferBuilder builder, int accuracyOffset) {
        builder.addOffset(4, accuracyOffset, 0);
    }

    public static void addProvidertype(FlatBufferBuilder builder, int providertypeOffset) {
        builder.addOffset(5, providertypeOffset, 0);
    }

    public static void addRetentionexpires(FlatBufferBuilder builder, int retentionexpiresOffset) {
        builder.addOffset(6, retentionexpiresOffset, 0);
    }

    public static void addSrsname(FlatBufferBuilder builder, int srsnameOffset) {
        builder.addOffset(7, srsnameOffset, 0);
    }

    public static void addRadiusuom(FlatBufferBuilder builder, int radiusuomOffset) {
        builder.addOffset(8, radiusuomOffset, 0);
    }

    public static void addOs(FlatBufferBuilder builder, int osOffset) {
        builder.addOffset(9, osOffset, 0);
    }

    public static void addDeviceid(FlatBufferBuilder builder, int deviceidOffset) {
        builder.addOffset(10, deviceidOffset, 0);
    }

    public static void addCountry(FlatBufferBuilder builder, int countryOffset) {
        builder.addOffset(11, countryOffset, 0);
    }

    public static void addA1(FlatBufferBuilder builder, int a1Offset) {
        builder.addOffset(12, a1Offset, 0);
    }

    public static void addA3(FlatBufferBuilder builder, int a3Offset) {
        builder.addOffset(13, a3Offset, 0);
    }

    public static void addA6(FlatBufferBuilder builder, int a6Offset) {
        builder.addOffset(14, a6Offset, 0);
    }

    public static void addHno(FlatBufferBuilder builder, int hnoOffset) {
        builder.addOffset(15, hnoOffset, 0);
    }

    public static void addPc(FlatBufferBuilder builder, int pcOffset) {
        builder.addOffset(16, pcOffset, 0);
    }

    public static void addLocationtime(FlatBufferBuilder builder, int locationtimeOffset) {
        builder.addOffset(17, locationtimeOffset, 0);
    }

    public static int endRequestUpdateGeolocation(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
