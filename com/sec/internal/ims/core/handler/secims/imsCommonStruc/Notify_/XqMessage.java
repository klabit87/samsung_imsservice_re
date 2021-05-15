package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_.XqContent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XqMessage extends Table {
    public static XqMessage getRootAsXqMessage(ByteBuffer _bb) {
        return getRootAsXqMessage(_bb, new XqMessage());
    }

    public static XqMessage getRootAsXqMessage(ByteBuffer _bb, XqMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public XqMessage __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int mtrip() {
        int o = __offset(4);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long sequence() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public XqContent mContent(int j) {
        return mContent(new XqContent(), j);
    }

    public XqContent mContent(XqContent obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int mContentLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createXqMessage(FlatBufferBuilder builder, int mtrip, long sequence, int mContentOffset) {
        builder.startObject(3);
        addMContent(builder, mContentOffset);
        addSequence(builder, sequence);
        addMtrip(builder, mtrip);
        return endXqMessage(builder);
    }

    public static void startXqMessage(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addMtrip(FlatBufferBuilder builder, int mtrip) {
        builder.addInt(0, mtrip, 0);
    }

    public static void addSequence(FlatBufferBuilder builder, long sequence) {
        builder.addInt(1, (int) sequence, 0);
    }

    public static void addMContent(FlatBufferBuilder builder, int mContentOffset) {
        builder.addOffset(2, mContentOffset, 0);
    }

    public static int createMContentVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startMContentVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endXqMessage(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
