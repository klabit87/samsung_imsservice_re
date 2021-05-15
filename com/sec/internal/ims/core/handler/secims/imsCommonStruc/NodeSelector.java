package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NodeSelector extends Table {
    public static NodeSelector getRootAsNodeSelector(ByteBuffer _bb) {
        return getRootAsNodeSelector(_bb, new NodeSelector());
    }

    public static NodeSelector getRootAsNodeSelector(ByteBuffer _bb, NodeSelector obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public NodeSelector __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String node() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nodeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long pos() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String attr() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer attrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String attrVal() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer attrValAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createNodeSelector(FlatBufferBuilder builder, int nodeOffset, long pos, int attrOffset, int attr_valOffset) {
        builder.startObject(4);
        addAttrVal(builder, attr_valOffset);
        addAttr(builder, attrOffset);
        addPos(builder, pos);
        addNode(builder, nodeOffset);
        return endNodeSelector(builder);
    }

    public static void startNodeSelector(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addNode(FlatBufferBuilder builder, int nodeOffset) {
        builder.addOffset(0, nodeOffset, 0);
    }

    public static void addPos(FlatBufferBuilder builder, long pos) {
        builder.addInt(1, (int) pos, 0);
    }

    public static void addAttr(FlatBufferBuilder builder, int attrOffset) {
        builder.addOffset(2, attrOffset, 0);
    }

    public static void addAttrVal(FlatBufferBuilder builder, int attrValOffset) {
        builder.addOffset(3, attrValOffset, 0);
    }

    public static int endNodeSelector(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
