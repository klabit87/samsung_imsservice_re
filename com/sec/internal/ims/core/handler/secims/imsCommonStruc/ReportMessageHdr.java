package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ReportMessageHdr extends Table {
    public static ReportMessageHdr getRootAsReportMessageHdr(ByteBuffer _bb) {
        return getRootAsReportMessageHdr(_bb, new ReportMessageHdr());
    }

    public static ReportMessageHdr getRootAsReportMessageHdr(ByteBuffer _bb, ReportMessageHdr obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ReportMessageHdr __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String spamFrom() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer spamFromAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String spamTo() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer spamToAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String spamDate() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer spamDateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createReportMessageHdr(FlatBufferBuilder builder, int spam_fromOffset, int spam_toOffset, int spam_dateOffset) {
        builder.startObject(3);
        addSpamDate(builder, spam_dateOffset);
        addSpamTo(builder, spam_toOffset);
        addSpamFrom(builder, spam_fromOffset);
        return endReportMessageHdr(builder);
    }

    public static void startReportMessageHdr(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSpamFrom(FlatBufferBuilder builder, int spamFromOffset) {
        builder.addOffset(0, spamFromOffset, 0);
    }

    public static void addSpamTo(FlatBufferBuilder builder, int spamToOffset) {
        builder.addOffset(1, spamToOffset, 0);
    }

    public static void addSpamDate(FlatBufferBuilder builder, int spamDateOffset) {
        builder.addOffset(2, spamDateOffset, 0);
    }

    public static int endReportMessageHdr(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
