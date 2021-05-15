package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Contact extends Table {
    public static Contact getRootAsContact(ByteBuffer _bb) {
        return getRootAsContact(_bb, new Contact());
    }

    public static Contact getRootAsContact(ByteBuffer _bb, Contact obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public Contact __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public int state() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String displayName() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public int event() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createContact(FlatBufferBuilder builder, int uriOffset, int state, int display_nameOffset, int event) {
        builder.startObject(4);
        addEvent(builder, event);
        addDisplayName(builder, display_nameOffset);
        addState(builder, state);
        addUri(builder, uriOffset);
        return endContact(builder);
    }

    public static void startContact(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addState(FlatBufferBuilder builder, int state) {
        builder.addInt(1, state, 0);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(2, displayNameOffset, 0);
    }

    public static void addEvent(FlatBufferBuilder builder, int event) {
        builder.addInt(3, event, 0);
    }

    public static int endContact(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        return o;
    }
}
