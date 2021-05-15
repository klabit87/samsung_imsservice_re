package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImdnResponseReceived extends Table {
    public static ImdnResponseReceived getRootAsImdnResponseReceived(ByteBuffer _bb) {
        return getRootAsImdnResponseReceived(_bb, new ImdnResponseReceived());
    }

    public static ImdnResponseReceived getRootAsImdnResponseReceived(ByteBuffer _bb, ImdnResponseReceived obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImdnResponseReceived __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String imdnMessageId(int j) {
        int o = __offset(4);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int imdnMessageIdLength() {
        int o = __offset(4);
        if (o != 0) {
            return __vector_len(o);
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

    public static int createImdnResponseReceived(FlatBufferBuilder builder, int imdn_message_idOffset, int im_errorOffset) {
        builder.startObject(2);
        addImError(builder, im_errorOffset);
        addImdnMessageId(builder, imdn_message_idOffset);
        return endImdnResponseReceived(builder);
    }

    public static void startImdnResponseReceived(FlatBufferBuilder builder) {
        builder.startObject(2);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(0, imdnMessageIdOffset, 0);
    }

    public static int createImdnMessageIdVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startImdnMessageIdVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addImError(FlatBufferBuilder builder, int imErrorOffset) {
        builder.addOffset(1, imErrorOffset, 0);
    }

    public static int endImdnResponseReceived(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
