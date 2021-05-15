package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImMessageParam extends Table {
    public static ImMessageParam getRootAsImMessageParam(ByteBuffer _bb) {
        return getRootAsImMessageParam(_bb, new ImMessageParam());
    }

    public static ImMessageParam getRootAsImMessageParam(ByteBuffer _bb, ImMessageParam obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImMessageParam __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String sender() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String receiver() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer receiverAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String userAlias() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String body() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer bodyAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String contentType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public ImdnParams imdn() {
        return imdn(new ImdnParams());
    }

    public ImdnParams imdn(ImdnParams obj) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String deviceName() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceNameAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String reliableMessage() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reliableMessageAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String xmsMessage() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer xmsMessageAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean extraFt() {
        int o = __offset(22);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPublicAccountMsg() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean silenceSupported() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public CpimNamespace cpimNamespaces(int j) {
        return cpimNamespaces(new CpimNamespace(), j);
    }

    public CpimNamespace cpimNamespaces(CpimNamespace obj, int j) {
        int o = __offset(28);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int cpimNamespacesLength() {
        int o = __offset(28);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String ccParticipants(int j) {
        int o = __offset(30);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int ccParticipantsLength() {
        int o = __offset(30);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String pAssertedId() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pAssertedIdAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String requestUri() {
        int o = __offset(34);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer requestUriAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public static int createImMessageParam(FlatBufferBuilder builder, int senderOffset, int receiverOffset, int user_aliasOffset, int bodyOffset, int content_typeOffset, int imdnOffset, int device_nameOffset, int reliable_messageOffset, int xms_messageOffset, boolean extra_ft, boolean is_public_account_msg, boolean silence_supported, int cpim_namespacesOffset, int cc_participantsOffset, int p_asserted_idOffset, int request_uriOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(16);
        addRequestUri(flatBufferBuilder, request_uriOffset);
        addPAssertedId(flatBufferBuilder, p_asserted_idOffset);
        addCcParticipants(flatBufferBuilder, cc_participantsOffset);
        addCpimNamespaces(flatBufferBuilder, cpim_namespacesOffset);
        addXmsMessage(flatBufferBuilder, xms_messageOffset);
        addReliableMessage(flatBufferBuilder, reliable_messageOffset);
        addDeviceName(flatBufferBuilder, device_nameOffset);
        addImdn(flatBufferBuilder, imdnOffset);
        addContentType(flatBufferBuilder, content_typeOffset);
        addBody(flatBufferBuilder, bodyOffset);
        addUserAlias(flatBufferBuilder, user_aliasOffset);
        addReceiver(flatBufferBuilder, receiverOffset);
        addSender(builder, senderOffset);
        addSilenceSupported(flatBufferBuilder, silence_supported);
        addIsPublicAccountMsg(flatBufferBuilder, is_public_account_msg);
        addExtraFt(flatBufferBuilder, extra_ft);
        return endImMessageParam(builder);
    }

    public static void startImMessageParam(FlatBufferBuilder builder) {
        builder.startObject(16);
    }

    public static void addSender(FlatBufferBuilder builder, int senderOffset) {
        builder.addOffset(0, senderOffset, 0);
    }

    public static void addReceiver(FlatBufferBuilder builder, int receiverOffset) {
        builder.addOffset(1, receiverOffset, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(2, userAliasOffset, 0);
    }

    public static void addBody(FlatBufferBuilder builder, int bodyOffset) {
        builder.addOffset(3, bodyOffset, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(4, contentTypeOffset, 0);
    }

    public static void addImdn(FlatBufferBuilder builder, int imdnOffset) {
        builder.addOffset(5, imdnOffset, 0);
    }

    public static void addDeviceName(FlatBufferBuilder builder, int deviceNameOffset) {
        builder.addOffset(6, deviceNameOffset, 0);
    }

    public static void addReliableMessage(FlatBufferBuilder builder, int reliableMessageOffset) {
        builder.addOffset(7, reliableMessageOffset, 0);
    }

    public static void addXmsMessage(FlatBufferBuilder builder, int xmsMessageOffset) {
        builder.addOffset(8, xmsMessageOffset, 0);
    }

    public static void addExtraFt(FlatBufferBuilder builder, boolean extraFt) {
        builder.addBoolean(9, extraFt, false);
    }

    public static void addIsPublicAccountMsg(FlatBufferBuilder builder, boolean isPublicAccountMsg) {
        builder.addBoolean(10, isPublicAccountMsg, false);
    }

    public static void addSilenceSupported(FlatBufferBuilder builder, boolean silenceSupported) {
        builder.addBoolean(11, silenceSupported, false);
    }

    public static void addCpimNamespaces(FlatBufferBuilder builder, int cpimNamespacesOffset) {
        builder.addOffset(12, cpimNamespacesOffset, 0);
    }

    public static int createCpimNamespacesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startCpimNamespacesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addCcParticipants(FlatBufferBuilder builder, int ccParticipantsOffset) {
        builder.addOffset(13, ccParticipantsOffset, 0);
    }

    public static int createCcParticipantsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startCcParticipantsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addPAssertedId(FlatBufferBuilder builder, int pAssertedIdOffset) {
        builder.addOffset(14, pAssertedIdOffset, 0);
    }

    public static void addRequestUri(FlatBufferBuilder builder, int requestUriOffset) {
        builder.addOffset(15, requestUriOffset, 0);
    }

    public static int endImMessageParam(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        builder.required(o, 12);
        return o;
    }
}
