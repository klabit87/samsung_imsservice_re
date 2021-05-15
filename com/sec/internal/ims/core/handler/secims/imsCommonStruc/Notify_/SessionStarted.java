package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AllowHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.RetryHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SessionStarted extends Table {
    public static SessionStarted getRootAsSessionStarted(ByteBuffer _bb) {
        return getRootAsSessionStarted(_bb, new SessionStarted());
    }

    public static SessionStarted getRootAsSessionStarted(ByteBuffer _bb, SessionStarted obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SessionStarted __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImError imError() {
        return imError(new ImError());
    }

    public ImError imError(ImError obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String sessionUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public RetryHdr retryHdr() {
        return retryHdr(new RetryHdr());
    }

    public RetryHdr retryHdr(RetryHdr obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public WarningHdr warningHdr() {
        return warningHdr(new WarningHdr());
    }

    public WarningHdr warningHdr(WarningHdr obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public AllowHdr allowHdr() {
        return allowHdr(new AllowHdr());
    }

    public AllowHdr allowHdr(AllowHdr obj) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public boolean isMsgRevokeSupported() {
        int o = __offset(16);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isMsgFallbackSupported() {
        int o = __offset(18);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isChatbotRole() {
        int o = __offset(20);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String displayName() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public static int createSessionStarted(FlatBufferBuilder builder, long session_handle, int im_errorOffset, int session_uriOffset, int retry_hdrOffset, int warning_hdrOffset, int allow_hdrOffset, boolean is_msg_revoke_supported, boolean is_msg_fallback_supported, boolean is_chatbot_role, int display_nameOffset) {
        builder.startObject(10);
        addDisplayName(builder, display_nameOffset);
        addAllowHdr(builder, allow_hdrOffset);
        addWarningHdr(builder, warning_hdrOffset);
        addRetryHdr(builder, retry_hdrOffset);
        addSessionUri(builder, session_uriOffset);
        addImError(builder, im_errorOffset);
        addSessionHandle(builder, session_handle);
        addIsChatbotRole(builder, is_chatbot_role);
        addIsMsgFallbackSupported(builder, is_msg_fallback_supported);
        addIsMsgRevokeSupported(builder, is_msg_revoke_supported);
        return endSessionStarted(builder);
    }

    public static void startSessionStarted(FlatBufferBuilder builder) {
        builder.startObject(10);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static void addSessionUri(FlatBufferBuilder builder, int sessionUriOffset) {
        builder.addOffset(2, sessionUriOffset, 0);
    }

    public static void addRetryHdr(FlatBufferBuilder builder, int retryHdrOffset) {
        builder.addOffset(3, retryHdrOffset, 0);
    }

    public static void addWarningHdr(FlatBufferBuilder builder, int warningHdrOffset) {
        builder.addOffset(4, warningHdrOffset, 0);
    }

    public static void addAllowHdr(FlatBufferBuilder builder, int allowHdrOffset) {
        builder.addOffset(5, allowHdrOffset, 0);
    }

    public static void addIsMsgRevokeSupported(FlatBufferBuilder builder, boolean isMsgRevokeSupported) {
        builder.addBoolean(6, isMsgRevokeSupported, false);
    }

    public static void addIsMsgFallbackSupported(FlatBufferBuilder builder, boolean isMsgFallbackSupported) {
        builder.addBoolean(7, isMsgFallbackSupported, false);
    }

    public static void addIsChatbotRole(FlatBufferBuilder builder, boolean isChatbotRole) {
        builder.addBoolean(8, isChatbotRole, false);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(9, displayNameOffset, 0);
    }

    public static int endSessionStarted(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
