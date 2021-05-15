package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class BaseSessionData extends Table {
    public static BaseSessionData getRootAsBaseSessionData(ByteBuffer _bb) {
        return getRootAsBaseSessionData(_bb, new BaseSessionData());
    }

    public static BaseSessionData getRootAsBaseSessionData(ByteBuffer _bb, BaseSessionData obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public BaseSessionData __assign(int _i, ByteBuffer _bb) {
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

    public String id() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer idAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean isConference() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String sessionUri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String userAlias() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String receivers(int j) {
        int o = __offset(14);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int receiversLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String contributionId() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String conversationId() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String inReplyToContributionId() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer inReplyToContributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String sessionReplaces() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionReplacesAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String sdpContentType() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sdpContentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String serviceId() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer serviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public boolean isChatbotParticipant() {
        int o = __offset(28);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String chatMode() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer chatModeAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public static int createBaseSessionData(FlatBufferBuilder builder, long session_handle, int idOffset, boolean is_conference, int session_uriOffset, int user_aliasOffset, int receiversOffset, int contribution_idOffset, int conversation_idOffset, int in_reply_to_contribution_idOffset, int session_replacesOffset, int sdp_content_typeOffset, int service_idOffset, boolean is_chatbot_participant, int chat_modeOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        builder.startObject(14);
        addChatMode(builder, chat_modeOffset);
        addServiceId(builder, service_idOffset);
        addSdpContentType(builder, sdp_content_typeOffset);
        addSessionReplaces(builder, session_replacesOffset);
        addInReplyToContributionId(builder, in_reply_to_contribution_idOffset);
        addConversationId(builder, conversation_idOffset);
        addContributionId(builder, contribution_idOffset);
        addReceivers(builder, receiversOffset);
        addUserAlias(builder, user_aliasOffset);
        addSessionUri(builder, session_uriOffset);
        addId(builder, idOffset);
        addSessionHandle(builder, session_handle);
        addIsChatbotParticipant(builder, is_chatbot_participant);
        addIsConference(builder, is_conference);
        return endBaseSessionData(builder);
    }

    public static void startBaseSessionData(FlatBufferBuilder builder) {
        builder.startObject(14);
    }

    public static void addSessionHandle(FlatBufferBuilder builder, long sessionHandle) {
        builder.addInt(0, (int) sessionHandle, 0);
    }

    public static void addId(FlatBufferBuilder builder, int idOffset) {
        builder.addOffset(1, idOffset, 0);
    }

    public static void addIsConference(FlatBufferBuilder builder, boolean isConference) {
        builder.addBoolean(2, isConference, false);
    }

    public static void addSessionUri(FlatBufferBuilder builder, int sessionUriOffset) {
        builder.addOffset(3, sessionUriOffset, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(4, userAliasOffset, 0);
    }

    public static void addReceivers(FlatBufferBuilder builder, int receiversOffset) {
        builder.addOffset(5, receiversOffset, 0);
    }

    public static int createReceiversVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startReceiversVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addContributionId(FlatBufferBuilder builder, int contributionIdOffset) {
        builder.addOffset(6, contributionIdOffset, 0);
    }

    public static void addConversationId(FlatBufferBuilder builder, int conversationIdOffset) {
        builder.addOffset(7, conversationIdOffset, 0);
    }

    public static void addInReplyToContributionId(FlatBufferBuilder builder, int inReplyToContributionIdOffset) {
        builder.addOffset(8, inReplyToContributionIdOffset, 0);
    }

    public static void addSessionReplaces(FlatBufferBuilder builder, int sessionReplacesOffset) {
        builder.addOffset(9, sessionReplacesOffset, 0);
    }

    public static void addSdpContentType(FlatBufferBuilder builder, int sdpContentTypeOffset) {
        builder.addOffset(10, sdpContentTypeOffset, 0);
    }

    public static void addServiceId(FlatBufferBuilder builder, int serviceIdOffset) {
        builder.addOffset(11, serviceIdOffset, 0);
    }

    public static void addIsChatbotParticipant(FlatBufferBuilder builder, boolean isChatbotParticipant) {
        builder.addBoolean(12, isChatbotParticipant, false);
    }

    public static void addChatMode(FlatBufferBuilder builder, int chatModeOffset) {
        builder.addOffset(13, chatModeOffset, 0);
    }

    public static int endBaseSessionData(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
