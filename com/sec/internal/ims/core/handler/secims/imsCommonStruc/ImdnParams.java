package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImdnParams extends Table {
    public static ImdnParams getRootAsImdnParams(ByteBuffer _bb) {
        return getRootAsImdnParams(_bb, new ImdnParams());
    }

    public static ImdnParams getRootAsImdnParams(ByteBuffer _bb, ImdnParams obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImdnParams __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String messageId() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer messageIdAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String datetime() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer datetimeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int noti(int j) {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int notiLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer notiAsByteBuffer() {
        return __vector_as_bytebuffer(8, 4);
    }

    public ImdnRecRoute recRoute(int j) {
        return recRoute(new ImdnRecRoute(), j);
    }

    public ImdnRecRoute recRoute(ImdnRecRoute obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int recRouteLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String deviceId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer deviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String originalToHdr() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer originalToHdrAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createImdnParams(FlatBufferBuilder builder, int message_idOffset, int datetimeOffset, int notiOffset, int rec_routeOffset, int device_idOffset, int original_to_hdrOffset) {
        builder.startObject(6);
        addOriginalToHdr(builder, original_to_hdrOffset);
        addDeviceId(builder, device_idOffset);
        addRecRoute(builder, rec_routeOffset);
        addNoti(builder, notiOffset);
        addDatetime(builder, datetimeOffset);
        addMessageId(builder, message_idOffset);
        return endImdnParams(builder);
    }

    public static void startImdnParams(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addMessageId(FlatBufferBuilder builder, int messageIdOffset) {
        builder.addOffset(0, messageIdOffset, 0);
    }

    public static void addDatetime(FlatBufferBuilder builder, int datetimeOffset) {
        builder.addOffset(1, datetimeOffset, 0);
    }

    public static void addNoti(FlatBufferBuilder builder, int notiOffset) {
        builder.addOffset(2, notiOffset, 0);
    }

    public static int createNotiVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startNotiVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addRecRoute(FlatBufferBuilder builder, int recRouteOffset) {
        builder.addOffset(3, recRouteOffset, 0);
    }

    public static int createRecRouteVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startRecRouteVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addDeviceId(FlatBufferBuilder builder, int deviceIdOffset) {
        builder.addOffset(4, deviceIdOffset, 0);
    }

    public static void addOriginalToHdr(FlatBufferBuilder builder, int originalToHdrOffset) {
        builder.addOffset(5, originalToHdrOffset, 0);
    }

    public static int endImdnParams(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
