package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XDMConfig extends Table {
    public static XDMConfig getRootAsXDMConfig(ByteBuffer _bb) {
        return getRootAsXDMConfig(_bb, new XDMConfig());
    }

    public static XDMConfig getRootAsXDMConfig(ByteBuffer _bb, XDMConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XDMConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String httpUserName() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer httpUserNameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String httpPasswd() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer httpPasswdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String xcapRootUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer xcapRootUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String authProxyServer() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer authProxyServerAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long authProxyPort() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String bsfServer() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer bsfServerAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long bsfServerPort() {
        int o = __offset(16);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String userAgent() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAgentAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public int mno() {
        int o = __offset(20);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String impu() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String impi() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impiAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public boolean enableGba() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createXDMConfig(FlatBufferBuilder builder, int http_user_nameOffset, int http_passwdOffset, int xcap_root_uriOffset, int auth_proxy_serverOffset, long auth_proxy_port, int bsf_serverOffset, long bsf_server_port, int user_agentOffset, int mno, int impuOffset, int impiOffset, boolean enable_gba) {
        builder.startObject(12);
        addImpi(builder, impiOffset);
        addImpu(builder, impuOffset);
        addMno(builder, mno);
        addUserAgent(builder, user_agentOffset);
        addBsfServerPort(builder, bsf_server_port);
        addBsfServer(builder, bsf_serverOffset);
        addAuthProxyPort(builder, auth_proxy_port);
        addAuthProxyServer(builder, auth_proxy_serverOffset);
        addXcapRootUri(builder, xcap_root_uriOffset);
        addHttpPasswd(builder, http_passwdOffset);
        addHttpUserName(builder, http_user_nameOffset);
        addEnableGba(builder, enable_gba);
        return endXDMConfig(builder);
    }

    public static void startXDMConfig(FlatBufferBuilder builder) {
        builder.startObject(12);
    }

    public static void addHttpUserName(FlatBufferBuilder builder, int httpUserNameOffset) {
        builder.addOffset(0, httpUserNameOffset, 0);
    }

    public static void addHttpPasswd(FlatBufferBuilder builder, int httpPasswdOffset) {
        builder.addOffset(1, httpPasswdOffset, 0);
    }

    public static void addXcapRootUri(FlatBufferBuilder builder, int xcapRootUriOffset) {
        builder.addOffset(2, xcapRootUriOffset, 0);
    }

    public static void addAuthProxyServer(FlatBufferBuilder builder, int authProxyServerOffset) {
        builder.addOffset(3, authProxyServerOffset, 0);
    }

    public static void addAuthProxyPort(FlatBufferBuilder builder, long authProxyPort) {
        builder.addInt(4, (int) authProxyPort, 0);
    }

    public static void addBsfServer(FlatBufferBuilder builder, int bsfServerOffset) {
        builder.addOffset(5, bsfServerOffset, 0);
    }

    public static void addBsfServerPort(FlatBufferBuilder builder, long bsfServerPort) {
        builder.addInt(6, (int) bsfServerPort, 0);
    }

    public static void addUserAgent(FlatBufferBuilder builder, int userAgentOffset) {
        builder.addOffset(7, userAgentOffset, 0);
    }

    public static void addMno(FlatBufferBuilder builder, int mno) {
        builder.addInt(8, mno, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(9, impuOffset, 0);
    }

    public static void addImpi(FlatBufferBuilder builder, int impiOffset) {
        builder.addOffset(10, impiOffset, 0);
    }

    public static void addEnableGba(FlatBufferBuilder builder, boolean enableGba) {
        builder.addBoolean(11, enableGba, false);
    }

    public static int endXDMConfig(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 14);
        builder.required(o, 18);
        builder.required(o, 22);
        builder.required(o, 24);
        return o;
    }
}
