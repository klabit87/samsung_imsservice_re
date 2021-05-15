package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestIshStartSession extends Table {
    public static RequestIshStartSession getRootAsRequestIshStartSession(ByteBuffer _bb) {
        return getRootAsRequestIshStartSession(_bb, new RequestIshStartSession());
    }

    public static RequestIshStartSession getRootAsRequestIshStartSession(ByteBuffer _bb, RequestIshStartSession obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestIshStartSession __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long registrationHandle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String remoteUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String filePath() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer filePathAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public long size() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String contentType() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contentTypeAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public static int createRequestIshStartSession(FlatBufferBuilder builder, long registration_handle, int remote_uriOffset, int file_pathOffset, long size, int content_typeOffset) {
        builder.startObject(5);
        addContentType(builder, content_typeOffset);
        addSize(builder, size);
        addFilePath(builder, file_pathOffset);
        addRemoteUri(builder, remote_uriOffset);
        addRegistrationHandle(builder, registration_handle);
        return endRequestIshStartSession(builder);
    }

    public static void startRequestIshStartSession(FlatBufferBuilder builder) {
        builder.startObject(5);
    }

    public static void addRegistrationHandle(FlatBufferBuilder builder, long registrationHandle) {
        builder.addInt(0, (int) registrationHandle, 0);
    }

    public static void addRemoteUri(FlatBufferBuilder builder, int remoteUriOffset) {
        builder.addOffset(1, remoteUriOffset, 0);
    }

    public static void addFilePath(FlatBufferBuilder builder, int filePathOffset) {
        builder.addOffset(2, filePathOffset, 0);
    }

    public static void addSize(FlatBufferBuilder builder, long size) {
        builder.addInt(3, (int) size, 0);
    }

    public static void addContentType(FlatBufferBuilder builder, int contentTypeOffset) {
        builder.addOffset(4, contentTypeOffset, 0);
    }

    public static int endRequestIshStartSession(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 12);
        return o;
    }
}
