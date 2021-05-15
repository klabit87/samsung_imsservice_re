package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OptionsReceivedInfo extends Table {
    public static OptionsReceivedInfo getRootAsOptionsReceivedInfo(ByteBuffer _bb) {
        return getRootAsOptionsReceivedInfo(_bb, new OptionsReceivedInfo());
    }

    public static OptionsReceivedInfo getRootAsOptionsReceivedInfo(ByteBuffer _bb, OptionsReceivedInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public OptionsReceivedInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String remoteUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean isResponse() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean success() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int reason() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int tags(int j) {
        int o = __offset(14);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int tagsLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer tagsAsByteBuffer() {
        return __vector_as_bytebuffer(14, 4);
    }

    public String txId() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer txIdAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public int lastSeen() {
        int o = __offset(18);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String extFeature() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer extFeatureAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String pAssertedId(int j) {
        int o = __offset(22);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int pAssertedIdLength() {
        int o = __offset(22);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean isChatbotParticipant() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isCmcCheck() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createOptionsReceivedInfo(FlatBufferBuilder builder, long session_id, int remote_uriOffset, boolean is_response, boolean success, int reason, int tagsOffset, int tx_idOffset, int last_seen, int extFeatureOffset, int p_asserted_idOffset, boolean is_chatbot_participant, boolean is_cmc_check) {
        builder.startObject(12);
        addPAssertedId(builder, p_asserted_idOffset);
        addExtFeature(builder, extFeatureOffset);
        addLastSeen(builder, last_seen);
        addTxId(builder, tx_idOffset);
        addTags(builder, tagsOffset);
        addReason(builder, reason);
        addRemoteUri(builder, remote_uriOffset);
        addSessionId(builder, session_id);
        addIsCmcCheck(builder, is_cmc_check);
        addIsChatbotParticipant(builder, is_chatbot_participant);
        addSuccess(builder, success);
        addIsResponse(builder, is_response);
        return endOptionsReceivedInfo(builder);
    }

    public static void startOptionsReceivedInfo(FlatBufferBuilder builder) {
        builder.startObject(12);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(1, remoteUriOffset, 0);
    }

    public static void addIsResponse(FlatBufferBuilder builder, boolean isResponse) {
        builder.addBoolean(2, isResponse, false);
    }

    public static void addSuccess(FlatBufferBuilder builder, boolean success) {
        builder.addBoolean(3, success, false);
    }

    public static void addReason(FlatBufferBuilder builder, int reason) {
        builder.addInt(4, reason, 0);
    }

    public static void addTags(FlatBufferBuilder builder, int tagsOffset) {
        builder.addOffset(5, tagsOffset, 0);
    }

    public static int createTagsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startTagsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addTxId(FlatBufferBuilder builder, int txIdOffset) {
        builder.addOffset(6, txIdOffset, 0);
    }

    public static void addLastSeen(FlatBufferBuilder builder, int lastSeen) {
        builder.addInt(7, lastSeen, 0);
    }

    public static void addExtFeature(FlatBufferBuilder builder, int extFeatureOffset) {
        builder.addOffset(8, extFeatureOffset, 0);
    }

    public static void addPAssertedId(FlatBufferBuilder builder, int pAssertedIdOffset) {
        builder.addOffset(9, pAssertedIdOffset, 0);
    }

    public static int createPAssertedIdVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startPAssertedIdVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsChatbotParticipant(FlatBufferBuilder builder, boolean isChatbotParticipant) {
        builder.addBoolean(10, isChatbotParticipant, false);
    }

    public static void addIsCmcCheck(FlatBufferBuilder builder, boolean isCmcCheck) {
        builder.addBoolean(11, isCmcCheck, false);
    }

    public static int endOptionsReceivedInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 16);
        return o;
    }
}
