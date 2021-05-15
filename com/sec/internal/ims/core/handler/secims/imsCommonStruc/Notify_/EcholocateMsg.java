package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateRtpMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateSignalMsg;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateMsg extends Table {
    public static EcholocateMsg getRootAsEcholocateMsg(ByteBuffer _bb) {
        return getRootAsEcholocateMsg(_bb, new EcholocateMsg());
    }

    public static EcholocateMsg getRootAsEcholocateMsg(ByteBuffer _bb, EcholocateMsg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EcholocateMsg __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int msgtype() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public EcholocateSignalMsg echolocateSignalData() {
        return echolocateSignalData(new EcholocateSignalMsg());
    }

    public EcholocateSignalMsg echolocateSignalData(EcholocateSignalMsg obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public EcholocateRtpMsg echolocateRtpData() {
        return echolocateRtpData(new EcholocateRtpMsg());
    }

    public EcholocateRtpMsg echolocateRtpData(EcholocateRtpMsg obj) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createEcholocateMsg(FlatBufferBuilder builder, long handle, int msgtype, int echolocate_signal_dataOffset, int echolocate_rtp_dataOffset) {
        builder.startObject(4);
        addEcholocateRtpData(builder, echolocate_rtp_dataOffset);
        addEcholocateSignalData(builder, echolocate_signal_dataOffset);
        addMsgtype(builder, msgtype);
        addHandle(builder, handle);
        return endEcholocateMsg(builder);
    }

    public static void startEcholocateMsg(FlatBufferBuilder builder) {
        builder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addMsgtype(FlatBufferBuilder builder, int msgtype) {
        builder.addInt(1, msgtype, 0);
    }

    public static void addEcholocateSignalData(FlatBufferBuilder builder, int echolocateSignalDataOffset) {
        builder.addOffset(2, echolocateSignalDataOffset, 0);
    }

    public static void addEcholocateRtpData(FlatBufferBuilder builder, int echolocateRtpDataOffset) {
        builder.addOffset(3, echolocateRtpDataOffset, 0);
    }

    public static int endEcholocateMsg(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
