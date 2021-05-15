package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FtPayloadParam extends Table {
    public static FtPayloadParam getRootAsFtPayloadParam(ByteBuffer _bb) {
        return getRootAsFtPayloadParam(_bb, new FtPayloadParam());
    }

    public static FtPayloadParam getRootAsFtPayloadParam(ByteBuffer _bb, FtPayloadParam obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public FtPayloadParam __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public ImFileAttr fileAttr() {
        return fileAttr(new ImFileAttr());
    }

    public ImFileAttr fileAttr(ImFileAttr obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ImFileAttr iconAttr() {
        return iconAttr(new ImFileAttr());
    }

    public ImFileAttr iconAttr(ImFileAttr obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ImdnParams imdn() {
        return imdn(new ImdnParams());
    }

    public ImdnParams imdn(ImdnParams obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String deviceName() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceNameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String reliableMessage() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reliableMessageAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public boolean extraFt() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPublicAccountMsg() {
        int o = __offset(16);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPush() {
        int o = __offset(18);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public CpimNamespace cpimNamespaces(int j) {
        return cpimNamespaces(new CpimNamespace(), j);
    }

    public CpimNamespace cpimNamespaces(CpimNamespace obj, int j) {
        int o = __offset(20);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int cpimNamespacesLength() {
        int o = __offset(20);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String sender() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer senderAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String receiver() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer receiverAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public boolean silenceSupported() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String fileFingerPrint() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer fileFingerPrintAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String pAssertedId() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pAssertedIdAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String requestUri() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer requestUriAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public static int createFtPayloadParam(FlatBufferBuilder builder, int file_attrOffset, int icon_attrOffset, int imdnOffset, int device_nameOffset, int reliable_messageOffset, boolean extra_ft, boolean is_public_account_msg, boolean is_push, int cpim_namespacesOffset, int senderOffset, int receiverOffset, boolean silence_supported, int file_finger_printOffset, int p_asserted_idOffset, int request_uriOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(15);
        addRequestUri(flatBufferBuilder, request_uriOffset);
        addPAssertedId(flatBufferBuilder, p_asserted_idOffset);
        addFileFingerPrint(flatBufferBuilder, file_finger_printOffset);
        addReceiver(flatBufferBuilder, receiverOffset);
        addSender(flatBufferBuilder, senderOffset);
        addCpimNamespaces(flatBufferBuilder, cpim_namespacesOffset);
        addReliableMessage(flatBufferBuilder, reliable_messageOffset);
        addDeviceName(flatBufferBuilder, device_nameOffset);
        addImdn(flatBufferBuilder, imdnOffset);
        addIconAttr(flatBufferBuilder, icon_attrOffset);
        addFileAttr(builder, file_attrOffset);
        addSilenceSupported(flatBufferBuilder, silence_supported);
        addIsPush(flatBufferBuilder, is_push);
        addIsPublicAccountMsg(flatBufferBuilder, is_public_account_msg);
        addExtraFt(flatBufferBuilder, extra_ft);
        return endFtPayloadParam(builder);
    }

    public static void startFtPayloadParam(FlatBufferBuilder builder) {
        builder.startObject(15);
    }

    public static void addFileAttr(FlatBufferBuilder builder, int fileAttrOffset) {
        builder.addOffset(0, fileAttrOffset, 0);
    }

    public static void addIconAttr(FlatBufferBuilder builder, int iconAttrOffset) {
        builder.addOffset(1, iconAttrOffset, 0);
    }

    public static void addImdn(FlatBufferBuilder builder, int imdnOffset) {
        builder.addOffset(2, imdnOffset, 0);
    }

    public static void addDeviceName(FlatBufferBuilder builder, int deviceNameOffset) {
        builder.addOffset(3, deviceNameOffset, 0);
    }

    public static void addReliableMessage(FlatBufferBuilder builder, int reliableMessageOffset) {
        builder.addOffset(4, reliableMessageOffset, 0);
    }

    public static void addExtraFt(FlatBufferBuilder builder, boolean extraFt) {
        builder.addBoolean(5, extraFt, false);
    }

    public static void addIsPublicAccountMsg(FlatBufferBuilder builder, boolean isPublicAccountMsg) {
        builder.addBoolean(6, isPublicAccountMsg, false);
    }

    public static void addIsPush(FlatBufferBuilder builder, boolean isPush) {
        builder.addBoolean(7, isPush, false);
    }

    public static void addCpimNamespaces(FlatBufferBuilder builder, int cpimNamespacesOffset) {
        builder.addOffset(8, cpimNamespacesOffset, 0);
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

    public static void addSender(FlatBufferBuilder builder, int senderOffset) {
        builder.addOffset(9, senderOffset, 0);
    }

    public static void addReceiver(FlatBufferBuilder builder, int receiverOffset) {
        builder.addOffset(10, receiverOffset, 0);
    }

    public static void addSilenceSupported(FlatBufferBuilder builder, boolean silenceSupported) {
        builder.addBoolean(11, silenceSupported, false);
    }

    public static void addFileFingerPrint(FlatBufferBuilder builder, int fileFingerPrintOffset) {
        builder.addOffset(12, fileFingerPrintOffset, 0);
    }

    public static void addPAssertedId(FlatBufferBuilder builder, int pAssertedIdOffset) {
        builder.addOffset(13, pAssertedIdOffset, 0);
    }

    public static void addRequestUri(FlatBufferBuilder builder, int requestUriOffset) {
        builder.addOffset(14, requestUriOffset, 0);
    }

    public static int endFtPayloadParam(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
