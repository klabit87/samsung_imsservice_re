package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EcholocateSignalMsg extends Table {
    public static EcholocateSignalMsg getRootAsEcholocateSignalMsg(ByteBuffer _bb) {
        return getRootAsEcholocateSignalMsg(_bb, new EcholocateSignalMsg());
    }

    public static EcholocateSignalMsg getRootAsEcholocateSignalMsg(ByteBuffer _bb, EcholocateSignalMsg obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public EcholocateSignalMsg __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String origin() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer originAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String line1() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer line1AsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String callid() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer callidAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String cseq() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cseqAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String sessionid() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionidAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String reason() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String contents() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentsAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String dispname() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dispnameAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public boolean isEpdgCall() {
        int o = __offset(20);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createEcholocateSignalMsg(FlatBufferBuilder builder, int originOffset, int line1Offset, int callidOffset, int cseqOffset, int sessionidOffset, int reasonOffset, int contentsOffset, int dispnameOffset, boolean is_epdg_call) {
        builder.startObject(9);
        addDispname(builder, dispnameOffset);
        addContents(builder, contentsOffset);
        addReason(builder, reasonOffset);
        addSessionid(builder, sessionidOffset);
        addCseq(builder, cseqOffset);
        addCallid(builder, callidOffset);
        addLine1(builder, line1Offset);
        addOrigin(builder, originOffset);
        addIsEpdgCall(builder, is_epdg_call);
        return endEcholocateSignalMsg(builder);
    }

    public static void startEcholocateSignalMsg(FlatBufferBuilder builder) {
        builder.startObject(9);
    }

    public static void addOrigin(FlatBufferBuilder builder, int originOffset) {
        builder.addOffset(0, originOffset, 0);
    }

    public static void addLine1(FlatBufferBuilder builder, int line1Offset) {
        builder.addOffset(1, line1Offset, 0);
    }

    public static void addCallid(FlatBufferBuilder builder, int callidOffset) {
        builder.addOffset(2, callidOffset, 0);
    }

    public static void addCseq(FlatBufferBuilder builder, int cseqOffset) {
        builder.addOffset(3, cseqOffset, 0);
    }

    public static void addSessionid(FlatBufferBuilder builder, int sessionidOffset) {
        builder.addOffset(4, sessionidOffset, 0);
    }

    public static void addReason(FlatBufferBuilder builder, int reasonOffset) {
        builder.addOffset(5, reasonOffset, 0);
    }

    public static void addContents(FlatBufferBuilder builder, int contentsOffset) {
        builder.addOffset(6, contentsOffset, 0);
    }

    public static void addDispname(FlatBufferBuilder builder, int dispnameOffset) {
        builder.addOffset(7, dispnameOffset, 0);
    }

    public static void addIsEpdgCall(FlatBufferBuilder builder, boolean isEpdgCall) {
        builder.addBoolean(8, isEpdgCall, false);
    }

    public static int endEcholocateSignalMsg(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        return o;
    }
}
