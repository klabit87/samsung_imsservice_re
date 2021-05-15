package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NotificationMessage extends Table {
    public static NotificationMessage getRootAsNotificationMessage(ByteBuffer _bb) {
        return getRootAsNotificationMessage(_bb, new NotificationMessage());
    }

    public static NotificationMessage getRootAsNotificationMessage(ByteBuffer _bb, NotificationMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public NotificationMessage __assign(int _i, ByteBuffer _bb) {
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

    public TextLangPair okButtons(int j) {
        return okButtons(new TextLangPair(), j);
    }

    public TextLangPair okButtons(TextLangPair obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int okButtonsLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createNotificationMessage(FlatBufferBuilder builder, int baseOffset, int contentOffset, int ok_buttonsOffset) {
        builder.startObject(3);
        addOkButtons(builder, ok_buttonsOffset);
        addContent(builder, contentOffset);
        addBase(builder, baseOffset);
        return endNotificationMessage(builder);
    }

    public static void startNotificationMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addBase(FlatBufferBuilder builder, int baseOffset) {
        builder.addOffset(0, baseOffset, 0);
    }

    public static void addContent(FlatBufferBuilder builder, int contentOffset) {
        builder.addOffset(1, contentOffset, 0);
    }

    public static void addOkButtons(FlatBufferBuilder builder, int okButtonsOffset) {
        builder.addOffset(2, okButtonsOffset, 0);
    }

    public static int createOkButtonsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startOkButtonsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endNotificationMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
