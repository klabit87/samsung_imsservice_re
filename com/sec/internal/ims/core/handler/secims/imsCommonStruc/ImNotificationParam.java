package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImNotificationParam extends Table {
    public static ImNotificationParam getRootAsImNotificationParam(ByteBuffer _bb) {
        return getRootAsImNotificationParam(_bb, new ImNotificationParam());
    }

    public static ImNotificationParam getRootAsImNotificationParam(ByteBuffer _bb, ImNotificationParam obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImNotificationParam __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String imdnMessageId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnMessageIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String imdnDateTime() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnDateTimeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int status(int j) {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int statusLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public String imdnOriginalTo() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imdnOriginalToAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public ImdnRecRoute imdnRecRoute(int j) {
        return imdnRecRoute(new ImdnRecRoute(), j);
    }

    public ImdnRecRoute imdnRecRoute(ImdnRecRoute obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int imdnRecRouteLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createImNotificationParam(FlatBufferBuilder builder, int imdn_message_idOffset, int imdn_date_timeOffset, int statusOffset, int imdn_original_toOffset, int imdn_rec_routeOffset) {
        builder.startObject(5);
        addImdnRecRoute(builder, imdn_rec_routeOffset);
        addImdnOriginalTo(builder, imdn_original_toOffset);
        addStatus(builder, statusOffset);
        addImdnDateTime(builder, imdn_date_timeOffset);
        addImdnMessageId(builder, imdn_message_idOffset);
        return endImNotificationParam(builder);
    }

    public static void startImNotificationParam(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addImdnMessageId(FlatBufferBuilder builder, int imdnMessageIdOffset) {
        builder.addOffset(0, imdnMessageIdOffset, 0);
    }

    public static void addImdnDateTime(FlatBufferBuilder builder, int imdnDateTimeOffset) {
        builder.addOffset(1, imdnDateTimeOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(2, statusOffset, 0);
    }

    public static int createStatusVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startStatusVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addImdnOriginalTo(FlatBufferBuilder builder, int imdnOriginalToOffset) {
        builder.addOffset(3, imdnOriginalToOffset, 0);
    }

    public static void addImdnRecRoute(FlatBufferBuilder builder, int imdnRecRouteOffset) {
        builder.addOffset(4, imdnRecRouteOffset, 0);
    }

    public static int createImdnRecRouteVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startImdnRecRouteVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endImNotificationParam(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
