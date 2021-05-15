package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMessage extends Table {
    public static RequestMessage getRootAsRequestMessage(ByteBuffer _bb) {
        return getRootAsRequestMessage(_bb, new RequestMessage());
    }

    public static RequestMessage getRootAsRequestMessage(ByteBuffer _bb, RequestMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public BaseMessage base() {
        return base(new BaseMessage());
    }

    public BaseMessage base(BaseMessage obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public EucContent content() {
        return content(new EucContent());
    }

    public EucContent content(EucContent obj) {
        int o = __offset(6);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public TextLangPair acceptButtons(int j) {
        return acceptButtons(new TextLangPair(), j);
    }

    public TextLangPair acceptButtons(TextLangPair obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int acceptButtonsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public TextLangPair rejectButtons(int j) {
        return rejectButtons(new TextLangPair(), j);
    }

    public TextLangPair rejectButtons(TextLangPair obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int rejectButtonsLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean pin() {
        int o = __offset(12);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean externalEucr() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestMessage(FlatBufferBuilder builder, int baseOffset, int contentOffset, int accept_buttonsOffset, int reject_buttonsOffset, boolean pin, boolean external_eucr) {
        builder.startObject(6);
        addRejectButtons(builder, reject_buttonsOffset);
        addAcceptButtons(builder, accept_buttonsOffset);
        addContent(builder, contentOffset);
        addBase(builder, baseOffset);
        addExternalEucr(builder, external_eucr);
        addPin(builder, pin);
        return endRequestMessage(builder);
    }

    public static void startRequestMessage(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addBase(FlatBufferBuilder builder, int baseOffset) {
        builder.addOffset(0, baseOffset, 0);
    }

    public static void addContent(FlatBufferBuilder builder, int contentOffset) {
        builder.addOffset(1, contentOffset, 0);
    }

    public static void addAcceptButtons(FlatBufferBuilder builder, int acceptButtonsOffset) {
        builder.addOffset(2, acceptButtonsOffset, 0);
    }

    public static int createAcceptButtonsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startAcceptButtonsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addRejectButtons(FlatBufferBuilder builder, int rejectButtonsOffset) {
        builder.addOffset(3, rejectButtonsOffset, 0);
    }

    public static int createRejectButtonsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startRejectButtonsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addPin(FlatBufferBuilder builder, boolean pin) {
        builder.addBoolean(4, pin, false);
    }

    public static void addExternalEucr(FlatBufferBuilder builder, boolean externalEucr) {
        builder.addBoolean(5, externalEucr, false);
    }

    public static int endRequestMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
