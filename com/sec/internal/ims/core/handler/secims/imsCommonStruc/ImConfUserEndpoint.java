package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUserEndpoint extends Table {
    public static ImConfUserEndpoint getRootAsImConfUserEndpoint(ByteBuffer _bb) {
        return getRootAsImConfUserEndpoint(_bb, new ImConfUserEndpoint());
    }

    public static ImConfUserEndpoint getRootAsImConfUserEndpoint(ByteBuffer _bb, ImConfUserEndpoint obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImConfUserEndpoint __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String entity() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer entityAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String status() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer statusAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String disconnectMethod() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer disconnectMethodAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public ImConfUserJoiningInfo joininginfo() {
        return joininginfo(new ImConfUserJoiningInfo());
    }

    public ImConfUserJoiningInfo joininginfo(ImConfUserJoiningInfo obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public ImConfUserDisconnectionInfo disconnectioninfo() {
        return disconnectioninfo(new ImConfUserDisconnectionInfo());
    }

    public ImConfUserDisconnectionInfo disconnectioninfo(ImConfUserDisconnectionInfo obj) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createImConfUserEndpoint(FlatBufferBuilder builder, int entityOffset, int statusOffset, int disconnect_methodOffset, int joininginfoOffset, int disconnectioninfoOffset) {
        builder.startObject(5);
        addDisconnectioninfo(builder, disconnectioninfoOffset);
        addJoininginfo(builder, joininginfoOffset);
        addDisconnectMethod(builder, disconnect_methodOffset);
        addStatus(builder, statusOffset);
        addEntity(builder, entityOffset);
        return endImConfUserEndpoint(builder);
    }

    public static void startImConfUserEndpoint(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addEntity(FlatBufferBuilder builder, int entityOffset) {
        builder.addOffset(0, entityOffset, 0);
    }

    public static void addStatus(FlatBufferBuilder builder, int statusOffset) {
        builder.addOffset(1, statusOffset, 0);
    }

    public static void addDisconnectMethod(FlatBufferBuilder builder, int disconnectMethodOffset) {
        builder.addOffset(2, disconnectMethodOffset, 0);
    }

    public static void addJoininginfo(FlatBufferBuilder builder, int joininginfoOffset) {
        builder.addOffset(3, joininginfoOffset, 0);
    }

    public static void addDisconnectioninfo(FlatBufferBuilder builder, int disconnectioninfoOffset) {
        builder.addOffset(4, disconnectioninfoOffset, 0);
    }

    public static int endImConfUserEndpoint(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        return o;
    }
}
