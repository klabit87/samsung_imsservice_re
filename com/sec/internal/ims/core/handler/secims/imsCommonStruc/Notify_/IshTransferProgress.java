package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IshTransferProgress extends Table {
    public static IshTransferProgress getRootAsIshTransferProgress(ByteBuffer _bb) {
        return getRootAsIshTransferProgress(_bb, new IshTransferProgress());
    }

    public static IshTransferProgress getRootAsIshTransferProgress(ByteBuffer _bb, IshTransferProgress obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public IshTransferProgress __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long total() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long transferred() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createIshTransferProgress(FlatBufferBuilder builder, long session_id, long total, long transferred) {
        builder.startObject(3);
        addTransferred(builder, transferred);
        addTotal(builder, total);
        addSessionId(builder, session_id);
        return endIshTransferProgress(builder);
    }

    public static void startIshTransferProgress(FlatBufferBuilder builder) {
        builder.startObject(3);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addTotal(FlatBufferBuilder builder, long total) {
        builder.addInt(1, (int) total, 0);
    }

    public static void addTransferred(FlatBufferBuilder builder, long transferred) {
        builder.addInt(2, (int) transferred, 0);
    }

    public static int endIshTransferProgress(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
