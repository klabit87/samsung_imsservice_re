package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImExtension;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendImNotificationStatus extends Table {
    public static RequestSendImNotificationStatus getRootAsRequestSendImNotificationStatus(ByteBuffer _bb) {
        return getRootAsRequestSendImNotificationStatus(_bb, new RequestSendImNotificationStatus());
    }

    public static RequestSendImNotificationStatus getRootAsRequestSendImNotificationStatus(ByteBuffer _bb, RequestSendImNotificationStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestSendImNotificationStatus __assign(int _i, ByteBuffer _bb) {
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

    public ImNotificationParam notifications(int j) {
        return notifications(new ImNotificationParam(), j);
    }

    public ImNotificationParam notifications(ImNotificationParam obj, int j) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int notificationsLength() {
        int o = __offset(6);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public long registrationHandle() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String uri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String conversationId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer conversationIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String contributionId() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contributionIdAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public int service() {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String deviceId() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public ImExtension extension() {
        return extension(new ImExtension());
    }

    public ImExtension extension(ImExtension obj) {
        int o = __offset(20);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public boolean isGroupChat() {
        int o = __offset(22);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isBotSessionAnonymized() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String cpimDateTime() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cpimDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public static int createRequestSendImNotificationStatus(FlatBufferBuilder builder, long session_id, int notificationsOffset, long registration_handle, int uriOffset, int conversation_idOffset, int contribution_idOffset, int service, int device_idOffset, int extensionOffset, boolean is_group_chat, boolean is_bot_session_anonymized, int cpim_date_timeOffset) {
        builder.startObject(12);
        addCpimDateTime(builder, cpim_date_timeOffset);
        addExtension(builder, extensionOffset);
        addDeviceId(builder, device_idOffset);
        addService(builder, service);
        addContributionId(builder, contribution_idOffset);
        addConversationId(builder, conversation_idOffset);
        addUri(builder, uriOffset);
        addRegistrationHandle(builder, registration_handle);
        addNotifications(builder, notificationsOffset);
        addSessionId(builder, session_id);
        addIsBotSessionAnonymized(builder, is_bot_session_anonymized);
        addIsGroupChat(builder, is_group_chat);
        return endRequestSendImNotificationStatus(builder);
    }

    public static void startRequestSendImNotificationStatus(FlatBufferBuilder builder) {
        builder.startObject(12);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addNotifications(FlatBufferBuilder builder, int notificationsOffset) {
        builder.addOffset(1, notificationsOffset, 0);
    }

    public static int createNotificationsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startNotificationsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(2, (int) registrationHandle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(3, uriOffset, 0);
    }

    public static void addConversationId(FlatBufferBuilder builder, int conversationIdOffset) {
        builder.addOffset(4, conversationIdOffset, 0);
    }

    public static void addContributionId(FlatBufferBuilder builder, int contributionIdOffset) {
        builder.addOffset(5, contributionIdOffset, 0);
    }

    public static void addService(FlatBufferBuilder builder, int service) {
        builder.addInt(6, service, 0);
    }

    public static void addDeviceId(FlatBufferBuilder builder, int deviceIdOffset) {
        builder.addOffset(7, deviceIdOffset, 0);
    }

    public static void addExtension(FlatBufferBuilder builder, int extensionOffset) {
        builder.addOffset(8, extensionOffset, 0);
    }

    public static void addIsGroupChat(FlatBufferBuilder builder, boolean isGroupChat) {
        builder.addBoolean(9, isGroupChat, false);
    }

    public static void addIsBotSessionAnonymized(FlatBufferBuilder builder, boolean isBotSessionAnonymized) {
        builder.addBoolean(10, isBotSessionAnonymized, false);
    }

    public static void addCpimDateTime(FlatBufferBuilder builder, int cpimDateTimeOffset) {
        builder.addOffset(11, cpimDateTimeOffset, 0);
    }

    public static int endRequestSendImNotificationStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
