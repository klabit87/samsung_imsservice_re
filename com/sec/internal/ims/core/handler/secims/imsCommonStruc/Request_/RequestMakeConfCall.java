package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMakeConfCall extends Table {
    public static RequestMakeConfCall getRootAsRequestMakeConfCall(ByteBuffer _bb) {
        return getRootAsRequestMakeConfCall(_bb, new RequestMakeConfCall());
    }

    public static RequestMakeConfCall getRootAsRequestMakeConfCall(ByteBuffer _bb, RequestMakeConfCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestMakeConfCall __assign(int _i, ByteBuffer _bb) {
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

    public String confuri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer confuriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int callType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int confType() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String eventSubscribe() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer eventSubscribeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String dialogType() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dialogTypeAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int sessionId(int j) {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int sessionIdLength() {
        int o = __offset(16);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer sessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 4);
    }

    public String participants(int j) {
        int o = __offset(18);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int participantsLength() {
        int o = __offset(18);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String origUri() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String referuriType() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer referuriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String removeReferuriType() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer removeReferuriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String referuriAsserted() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer referuriAssertedAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String useAnonymousUpdate() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer useAnonymousUpdateAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public boolean supportPrematureEnd() {
        int o = __offset(30);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public ExtraHeader extraHeaders() {
        return extraHeaders(new ExtraHeader());
    }

    public ExtraHeader extraHeaders(ExtraHeader obj) {
        int o = __offset(32);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestMakeConfCall(FlatBufferBuilder builder, long handle, int confuriOffset, int call_type, int conf_type, int event_subscribeOffset, int dialog_typeOffset, int sessionIdOffset, int participantsOffset, int orig_uriOffset, int referuri_typeOffset, int remove_referuri_typeOffset, int referuri_assertedOffset, int use_anonymous_updateOffset, boolean support_premature_end, int extra_headersOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(15);
        addExtraHeaders(flatBufferBuilder, extra_headersOffset);
        addUseAnonymousUpdate(flatBufferBuilder, use_anonymous_updateOffset);
        addReferuriAsserted(flatBufferBuilder, referuri_assertedOffset);
        addRemoveReferuriType(flatBufferBuilder, remove_referuri_typeOffset);
        addReferuriType(flatBufferBuilder, referuri_typeOffset);
        addOrigUri(flatBufferBuilder, orig_uriOffset);
        addParticipants(flatBufferBuilder, participantsOffset);
        addSessionId(flatBufferBuilder, sessionIdOffset);
        addDialogType(flatBufferBuilder, dialog_typeOffset);
        addEventSubscribe(flatBufferBuilder, event_subscribeOffset);
        addConfType(flatBufferBuilder, conf_type);
        addCallType(flatBufferBuilder, call_type);
        addConfuri(flatBufferBuilder, confuriOffset);
        addHandle(builder, handle);
        addSupportPrematureEnd(flatBufferBuilder, support_premature_end);
        return endRequestMakeConfCall(builder);
    }

    public static void startRequestMakeConfCall(FlatBufferBuilder builder) {
        builder.startObject(15);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addConfuri(FlatBufferBuilder builder, int confuriOffset) {
        builder.addOffset(1, confuriOffset, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(2, callType, 0);
    }

    public static void addConfType(FlatBufferBuilder builder, int confType) {
        builder.addInt(3, confType, 0);
    }

    public static void addEventSubscribe(FlatBufferBuilder builder, int eventSubscribeOffset) {
        builder.addOffset(4, eventSubscribeOffset, 0);
    }

    public static void addDialogType(FlatBufferBuilder builder, int dialogTypeOffset) {
        builder.addOffset(5, dialogTypeOffset, 0);
    }

    public static void addSessionId(FlatBufferBuilder builder, int sessionIdOffset) {
        builder.addOffset(6, sessionIdOffset, 0);
    }

    public static int createSessionIdVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startSessionIdVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addParticipants(FlatBufferBuilder builder, int participantsOffset) {
        builder.addOffset(7, participantsOffset, 0);
    }

    public static int createParticipantsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startParticipantsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addOrigUri(FlatBufferBuilder builder, int origUriOffset) {
        builder.addOffset(8, origUriOffset, 0);
    }

    public static void addReferuriType(FlatBufferBuilder builder, int referuriTypeOffset) {
        builder.addOffset(9, referuriTypeOffset, 0);
    }

    public static void addRemoveReferuriType(FlatBufferBuilder builder, int removeReferuriTypeOffset) {
        builder.addOffset(10, removeReferuriTypeOffset, 0);
    }

    public static void addReferuriAsserted(FlatBufferBuilder builder, int referuriAssertedOffset) {
        builder.addOffset(11, referuriAssertedOffset, 0);
    }

    public static void addUseAnonymousUpdate(FlatBufferBuilder builder, int useAnonymousUpdateOffset) {
        builder.addOffset(12, useAnonymousUpdateOffset, 0);
    }

    public static void addSupportPrematureEnd(FlatBufferBuilder builder, boolean supportPrematureEnd) {
        builder.addBoolean(13, supportPrematureEnd, false);
    }

    public static void addExtraHeaders(FlatBufferBuilder builder, int extraHeadersOffset) {
        builder.addOffset(14, extraHeadersOffset, 0);
    }

    public static int endRequestMakeConfCall(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 12);
        builder.required(o, 14);
        builder.required(o, 22);
        builder.required(o, 24);
        builder.required(o, 26);
        builder.required(o, 28);
        return o;
    }
}
