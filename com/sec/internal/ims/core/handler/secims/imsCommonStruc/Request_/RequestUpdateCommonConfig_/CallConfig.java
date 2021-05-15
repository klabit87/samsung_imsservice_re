package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallConfig extends Table {
    public static CallConfig getRootAsCallConfig(ByteBuffer _bb) {
        return getRootAsCallConfig(_bb, new CallConfig());
    }

    public static CallConfig getRootAsCallConfig(ByteBuffer _bb, CallConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CallConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public boolean ttySessionRequired() {
        int o = __offset(4);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean rttSessionRequired() {
        int o = __offset(6);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean automaticMode() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createCallConfig(FlatBufferBuilder builder, boolean tty_session_required, boolean rtt_session_required, boolean automatic_mode) {
        builder.startObject(3);
        addAutomaticMode(builder, automatic_mode);
        addRttSessionRequired(builder, rtt_session_required);
        addTtySessionRequired(builder, tty_session_required);
        return endCallConfig(builder);
    }

    public static void startCallConfig(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addTtySessionRequired(FlatBufferBuilder builder, boolean ttySessionRequired) {
        builder.addBoolean(0, ttySessionRequired, false);
    }

    public static void addRttSessionRequired(FlatBufferBuilder builder, boolean rttSessionRequired) {
        builder.addBoolean(1, rttSessionRequired, false);
    }

    public static void addAutomaticMode(FlatBufferBuilder builder, boolean automaticMode) {
        builder.addBoolean(2, automaticMode, false);
    }

    public static int endCallConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
