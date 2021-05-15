package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRegistration extends Table {
    public static RequestRegistration getRootAsRequestRegistration(ByteBuffer _bb) {
        return getRootAsRequestRegistration(_bb, new RequestRegistration());
    }

    public static RequestRegistration getRootAsRequestRegistration(ByteBuffer _bb, RequestRegistration obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestRegistration __assign(int _i, ByteBuffer _bb) {
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

    public String pcscfAddrList(int j) {
        int o = __offset(6);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int pcscfAddrListLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long pcscfPort() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String serviceList(int j) {
        int o = __offset(10);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int serviceListLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long regExp() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String impuList(int j) {
        int o = __offset(14);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int impuListLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public int featureTagList(int j) {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int featureTagListLength() {
        int o = __offset(16);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer featureTagListAsByteBuffer() {
        return __vector_as_bytebuffer(16, 4);
    }

    public boolean isExplicitDeregi() {
        int o = __offset(18);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String thirdpartyFeatureList(int j) {
        int o = __offset(20);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int thirdpartyFeatureListLength() {
        int o = __offset(20);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String accessToken() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer accessTokenAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String authServerUrl() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer authServerUrlAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public static int createRequestRegistration(FlatBufferBuilder builder, long handle, int pcscf_addr_listOffset, long pcscf_port, int service_listOffset, long reg_exp, int impu_listOffset, int feature_tag_listOffset, boolean is_explicit_deregi, int thirdparty_feature_listOffset, int access_tokenOffset, int auth_server_urlOffset) {
        builder.startObject(11);
        addAuthServerUrl(builder, auth_server_urlOffset);
        addAccessToken(builder, access_tokenOffset);
        addThirdpartyFeatureList(builder, thirdparty_feature_listOffset);
        addFeatureTagList(builder, feature_tag_listOffset);
        addImpuList(builder, impu_listOffset);
        addRegExp(builder, reg_exp);
        addServiceList(builder, service_listOffset);
        addPcscfPort(builder, pcscf_port);
        addPcscfAddrList(builder, pcscf_addr_listOffset);
        addHandle(builder, handle);
        addIsExplicitDeregi(builder, is_explicit_deregi);
        return endRequestRegistration(builder);
    }

    public static void startRequestRegistration(FlatBufferBuilder builder) {
        builder.startObject(11);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addPcscfAddrList(FlatBufferBuilder builder, int pcscfAddrListOffset) {
        builder.addOffset(1, pcscfAddrListOffset, 0);
    }

    public static int createPcscfAddrListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startPcscfAddrListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addPcscfPort(FlatBufferBuilder builder, long pcscfPort) {
        builder.addInt(2, (int) pcscfPort, 0);
    }

    public static void addServiceList(FlatBufferBuilder builder, int serviceListOffset) {
        builder.addOffset(3, serviceListOffset, 0);
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

    public static void addRegExp(FlatBufferBuilder builder, long regExp) {
        builder.addInt(4, (int) regExp, 0);
    }

    public static void addImpuList(FlatBufferBuilder builder, int impuListOffset) {
        builder.addOffset(5, impuListOffset, 0);
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

    public static void addFeatureTagList(FlatBufferBuilder builder, int featureTagListOffset) {
        builder.addOffset(6, featureTagListOffset, 0);
    }

    public static int createFeatureTagListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startFeatureTagListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsExplicitDeregi(FlatBufferBuilder builder, boolean isExplicitDeregi) {
        builder.addBoolean(7, isExplicitDeregi, false);
    }

    public static void addThirdpartyFeatureList(FlatBufferBuilder builder, int thirdpartyFeatureListOffset) {
        builder.addOffset(8, thirdpartyFeatureListOffset, 0);
    }

    public static int createThirdpartyFeatureListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startThirdpartyFeatureListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addAccessToken(FlatBufferBuilder builder, int accessTokenOffset) {
        builder.addOffset(9, accessTokenOffset, 0);
    }

    public static void addAuthServerUrl(FlatBufferBuilder builder, int authServerUrlOffset) {
        builder.addOffset(10, authServerUrlOffset, 0);
    }

    public static int endRequestRegistration(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
