package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImExtension extends Table {
    public static ImExtension getRootAsImExtension(ByteBuffer _bb) {
        return getRootAsImExtension(_bb, new ImExtension());
    }

    public static ImExtension getRootAsImExtension(ByteBuffer _bb, ImExtension obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImExtension __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public ExtraHeader sipExtensions() {
        return sipExtensions(new ExtraHeader());
    }

    public ExtraHeader sipExtensions(ExtraHeader obj) {
        int o = __offset(4);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createImExtension(FlatBufferBuilder builder, int sip_extensionsOffset) {
        builder.startObject(1);
        addSipExtensions(builder, sip_extensionsOffset);
        return endImExtension(builder);
    }

    public static void startImExtension(FlatBufferBuilder builder) {
        builder.startObject(1);
    }

    public static void addSipExtensions(FlatBufferBuilder builder, int sipExtensionsOffset) {
        builder.addOffset(0, sipExtensionsOffset, 0);
    }

    public static int endImExtension(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
