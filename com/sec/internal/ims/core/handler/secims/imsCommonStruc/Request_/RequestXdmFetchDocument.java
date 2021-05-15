package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.NodeSelector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestXdmFetchDocument extends Table {
    public static RequestXdmFetchDocument getRootAsRequestXdmFetchDocument(ByteBuffer _bb) {
        return getRootAsRequestXdmFetchDocument(_bb, new RequestXdmFetchDocument());
    }

    public static RequestXdmFetchDocument getRootAsRequestXdmFetchDocument(ByteBuffer _bb, RequestXdmFetchDocument obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestXdmFetchDocument __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rid() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String impu() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int type() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String name() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public NodeSelector nodeSelector() {
        return nodeSelector(new NodeSelector());
    }

    public NodeSelector nodeSelector(NodeSelector obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String accessToken() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer accessTokenAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createRequestXdmFetchDocument(FlatBufferBuilder builder, long rid, int impuOffset, int type, int nameOffset, int node_selectorOffset, int access_tokenOffset) {
        builder.startObject(6);
        addAccessToken(builder, access_tokenOffset);
        addNodeSelector(builder, node_selectorOffset);
        addName(builder, nameOffset);
        addType(builder, type);
        addImpu(builder, impuOffset);
        addRid(builder, rid);
        return endRequestXdmFetchDocument(builder);
    }

    public static void startRequestXdmFetchDocument(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addRid(FlatBufferBuilder builder, long rid) {
        builder.addInt(0, (int) rid, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(1, impuOffset, 0);
    }

    public static void addType(FlatBufferBuilder builder, int type) {
        builder.addInt(2, type, 0);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(3, nameOffset, 0);
    }

    public static void addNodeSelector(FlatBufferBuilder builder, int nodeSelectorOffset) {
        builder.addOffset(4, nodeSelectorOffset, 0);
    }

    public static void addAccessToken(FlatBufferBuilder builder, int accessTokenOffset) {
        builder.addOffset(5, accessTokenOffset, 0);
    }

    public static int endRequestXdmFetchDocument(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 10);
        return o;
    }
}
