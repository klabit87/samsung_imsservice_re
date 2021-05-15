package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfUser extends Table {
    public static ImConfUser getRootAsImConfUser(ByteBuffer _bb) {
        return getRootAsImConfUser(_bb, new ImConfUser());
    }

    public static ImConfUser getRootAsImConfUser(ByteBuffer _bb, ImConfUser obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImConfUser __assign(int _i, ByteBuffer _bb) {
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

    public String state() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer stateAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public boolean yourOwn() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public ImConfUserEndpoint endpoint() {
        return endpoint(new ImConfUserEndpoint());
    }

    public ImConfUserEndpoint endpoint(ImConfUserEndpoint obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String roles() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer rolesAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String userAlias() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createImConfUser(FlatBufferBuilder builder, int entityOffset, int stateOffset, boolean your_own, int endpointOffset, int rolesOffset, int user_aliasOffset) {
        builder.startObject(6);
        addUserAlias(builder, user_aliasOffset);
        addRoles(builder, rolesOffset);
        addEndpoint(builder, endpointOffset);
        addState(builder, stateOffset);
        addEntity(builder, entityOffset);
        addYourOwn(builder, your_own);
        return endImConfUser(builder);
    }

    public static void startImConfUser(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addEntity(FlatBufferBuilder builder, int entityOffset) {
        builder.addOffset(0, entityOffset, 0);
    }

    public static void addState(FlatBufferBuilder builder, int stateOffset) {
        builder.addOffset(1, stateOffset, 0);
    }

    public static void addYourOwn(FlatBufferBuilder builder, boolean yourOwn) {
        builder.addBoolean(2, yourOwn, false);
    }

    public static void addEndpoint(FlatBufferBuilder builder, int endpointOffset) {
        builder.addOffset(3, endpointOffset, 0);
    }

    public static void addRoles(FlatBufferBuilder builder, int rolesOffset) {
        builder.addOffset(4, rolesOffset, 0);
    }

    public static void addUserAlias(FlatBufferBuilder builder, int userAliasOffset) {
        builder.addOffset(5, userAliasOffset, 0);
    }

    public static int endImConfUser(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 10);
        return o;
    }
}
