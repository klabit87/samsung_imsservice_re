package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImSessionParam extends Table {
    public static ImSessionParam getRootAsImSessionParam(ByteBuffer _bb) {
        return getRootAsImSessionParam(_bb, new ImSessionParam());
    }

    public static ImSessionParam getRootAsImSessionParam(ByteBuffer _bb, ImSessionParam obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImSessionParam __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public BaseSessionData baseSessionData() {
        return baseSessionData(new BaseSessionData());
    }

    public BaseSessionData baseSessionData(BaseSessionData obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String sender() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String subject() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean isRejoin() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isClosedGroupchat() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isInviteforbye() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isExtension() {
        int o = __offset(16);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String acceptTypes(int j) {
        int o = __offset(18);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int acceptTypesLength() {
        int o = __offset(18);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String acceptWrappedTypes(int j) {
        int o = __offset(20);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int acceptWrappedTypesLength() {
        int o = __offset(20);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean isMsgRevokeSupported() {
        int o = __offset(22);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isMsgFallbackSupported() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isGeolocationPush() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isSendOnly() {
        int o = __offset(28);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createImSessionParam(FlatBufferBuilder builder, int base_session_dataOffset, int senderOffset, int subjectOffset, boolean is_rejoin, boolean is_closed_groupchat, boolean is_inviteforbye, boolean is_extension, int accept_typesOffset, int accept_wrapped_typesOffset, boolean is_msg_revoke_supported, boolean is_msg_fallback_supported, boolean is_geolocation_push, boolean is_send_only) {
        builder.startObject(13);
        addAcceptWrappedTypes(builder, accept_wrapped_typesOffset);
        addAcceptTypes(builder, accept_typesOffset);
        addSubject(builder, subjectOffset);
        addSender(builder, senderOffset);
        addBaseSessionData(builder, base_session_dataOffset);
        addIsSendOnly(builder, is_send_only);
        addIsGeolocationPush(builder, is_geolocation_push);
        addIsMsgFallbackSupported(builder, is_msg_fallback_supported);
        addIsMsgRevokeSupported(builder, is_msg_revoke_supported);
        addIsExtension(builder, is_extension);
        addIsInviteforbye(builder, is_inviteforbye);
        addIsClosedGroupchat(builder, is_closed_groupchat);
        addIsRejoin(builder, is_rejoin);
        return endImSessionParam(builder);
    }

    public static void startImSessionParam(FlatBufferBuilder builder) {
        builder.startObject(13);
    }

    public static void addBaseSessionData(FlatBufferBuilder builder, int baseSessionDataOffset) {
        builder.addOffset(0, baseSessionDataOffset, 0);
    }

    public static void addSender(FlatBufferBuilder builder, int senderOffset) {
        builder.addOffset(1, senderOffset, 0);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(2, subjectOffset, 0);
    }

    public static void addIsRejoin(FlatBufferBuilder builder, boolean isRejoin) {
        builder.addBoolean(3, isRejoin, false);
    }

    public static void addIsClosedGroupchat(FlatBufferBuilder builder, boolean isClosedGroupchat) {
        builder.addBoolean(4, isClosedGroupchat, false);
    }

    public static void addIsInviteforbye(FlatBufferBuilder builder, boolean isInviteforbye) {
        builder.addBoolean(5, isInviteforbye, false);
    }

    public static void addIsExtension(FlatBufferBuilder builder, boolean isExtension) {
        builder.addBoolean(6, isExtension, false);
    }

    public static void addAcceptTypes(FlatBufferBuilder builder, int acceptTypesOffset) {
        builder.addOffset(7, acceptTypesOffset, 0);
    }

    public static int createAcceptTypesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startAcceptTypesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addAcceptWrappedTypes(FlatBufferBuilder builder, int acceptWrappedTypesOffset) {
        builder.addOffset(8, acceptWrappedTypesOffset, 0);
    }

    public static int createAcceptWrappedTypesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startAcceptWrappedTypesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsMsgRevokeSupported(FlatBufferBuilder builder, boolean isMsgRevokeSupported) {
        builder.addBoolean(9, isMsgRevokeSupported, false);
    }

    public static void addIsMsgFallbackSupported(FlatBufferBuilder builder, boolean isMsgFallbackSupported) {
        builder.addBoolean(10, isMsgFallbackSupported, false);
    }

    public static void addIsGeolocationPush(FlatBufferBuilder builder, boolean isGeolocationPush) {
        builder.addBoolean(11, isGeolocationPush, false);
    }

    public static void addIsSendOnly(FlatBufferBuilder builder, boolean isSendOnly) {
        builder.addBoolean(12, isSendOnly, false);
    }

    public static int endImSessionParam(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
