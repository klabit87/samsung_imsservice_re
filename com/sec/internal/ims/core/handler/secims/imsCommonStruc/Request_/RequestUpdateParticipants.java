package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateParticipants extends Table {
    public static RequestUpdateParticipants getRootAsRequestUpdateParticipants(ByteBuffer _bb) {
        return getRootAsRequestUpdateParticipants(_bb, new RequestUpdateParticipants());
    }

    public static RequestUpdateParticipants getRootAsRequestUpdateParticipants(ByteBuffer _bb, RequestUpdateParticipants obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUpdateParticipants __assign(int _i, ByteBuffer _bb) {
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

    public int reqType() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String receiver(int j) {
        int o = __offset(8);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int receiverLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String userAlias() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String subject() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subjectAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String reqKey() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reqKeyAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public ImFileAttr iconAttr() {
        return iconAttr(new ImFileAttr());
    }

    public ImFileAttr iconAttr(ImFileAttr obj) {
        int o = __offset(16);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createRequestUpdateParticipants(FlatBufferBuilder builder, long session_handle, int req_type, int receiverOffset, int user_aliasOffset, int subjectOffset, int req_keyOffset, int icon_attrOffset) {
        builder.startObject(7);
        addIconAttr(builder, icon_attrOffset);
        addReqKey(builder, req_keyOffset);
        addSubject(builder, subjectOffset);
        addUserAlias(builder, user_aliasOffset);
        addReceiver(builder, receiverOffset);
        addReqType(builder, req_type);
        addSessionHandle(builder, session_handle);
        return endRequestUpdateParticipants(builder);
    }

    public static void startRequestUpdateParticipants(FlatBufferBuilder builder) {
        builder.startObject(7);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addReqType(FlatBufferBuilder builder, int reqType) {
        builder.addInt(1, reqType, 0);
    }

    public static void addReceiver(FlatBufferBuilder builder, int receiverOffset) {
        builder.addOffset(2, receiverOffset, 0);
    }

    public static int createReceiverVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startReceiverVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(3, userAliasOffset, 0);
    }

    public static void addSubject(FlatBufferBuilder builder, int subjectOffset) {
        builder.addOffset(4, subjectOffset, 0);
    }

    public static void addReqKey(FlatBufferBuilder builder, int reqKeyOffset) {
        builder.addOffset(5, reqKeyOffset, 0);
    }

    public static void addIconAttr(FlatBufferBuilder builder, int iconAttrOffset) {
        builder.addOffset(6, iconAttrOffset, 0);
    }

    public static int endRequestUpdateParticipants(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
