package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPullingCall extends Table {
    public static RequestPullingCall getRootAsRequestPullingCall(ByteBuffer _bb) {
        return getRootAsRequestPullingCall(_bb, new RequestPullingCall());
    }

    public static RequestPullingCall getRootAsRequestPullingCall(ByteBuffer _bb, RequestPullingCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPullingCall __assign(int _i, ByteBuffer _bb) {
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

    public String pullingUri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pullingUriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String targetUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer targetUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String origUri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String callId() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String localTag() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer localTagAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String remoteTag() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteTagAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public int callType() {
        int o = __offset(18);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int codec() {
        int o = __offset(20);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long audioDirection() {
        int o = __offset(22);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long videoDirection() {
        int o = __offset(24);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isVideoPortZero() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String mdmnExtNumber() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer mdmnExtNumberAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String p2pList(int j) {
        int o = __offset(30);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int p2pListLength() {
        int o = __offset(30);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public static int createRequestPullingCall(FlatBufferBuilder builder, long handle, int pulling_uriOffset, int target_uriOffset, int orig_uriOffset, int call_idOffset, int local_tagOffset, int remote_tagOffset, int call_type, int codec, long audio_direction, long video_direction, boolean is_video_port_zero, int mdmn_ext_numberOffset, int p2p_listOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(14);
        addP2pList(flatBufferBuilder, p2p_listOffset);
        addMdmnExtNumber(flatBufferBuilder, mdmn_ext_numberOffset);
        addVideoDirection(flatBufferBuilder, video_direction);
        addAudioDirection(flatBufferBuilder, audio_direction);
        addCodec(flatBufferBuilder, codec);
        addCallType(flatBufferBuilder, call_type);
        addRemoteTag(flatBufferBuilder, remote_tagOffset);
        addLocalTag(flatBufferBuilder, local_tagOffset);
        addCallId(flatBufferBuilder, call_idOffset);
        addOrigUri(flatBufferBuilder, orig_uriOffset);
        addTargetUri(flatBufferBuilder, target_uriOffset);
        addPullingUri(flatBufferBuilder, pulling_uriOffset);
        addHandle(builder, handle);
        addIsVideoPortZero(flatBufferBuilder, is_video_port_zero);
        return endRequestPullingCall(builder);
    }

    public static void startRequestPullingCall(FlatBufferBuilder builder) {
        builder.startObject(14);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addPullingUri(FlatBufferBuilder builder, int pullingUriOffset) {
        builder.addOffset(1, pullingUriOffset, 0);
    }

    public static void addTargetUri(FlatBufferBuilder builder, int targetUriOffset) {
        builder.addOffset(2, targetUriOffset, 0);
    }

    public static void addOrigUri(FlatBufferBuilder builder, int origUriOffset) {
        builder.addOffset(3, origUriOffset, 0);
    }

    public static void addCallId(FlatBufferBuilder builder, int callIdOffset) {
        builder.addOffset(4, callIdOffset, 0);
    }

    public static void addLocalTag(FlatBufferBuilder builder, int localTagOffset) {
        builder.addOffset(5, localTagOffset, 0);
    }

    public static void addRemoteTag(FlatBufferBuilder builder, int remoteTagOffset) {
        builder.addOffset(6, remoteTagOffset, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(7, callType, 0);
    }

    public static void addCodec(FlatBufferBuilder builder, int codec) {
        builder.addInt(8, codec, 0);
    }

    public static void addAudioDirection(FlatBufferBuilder builder, long audioDirection) {
        builder.addInt(9, (int) audioDirection, 0);
    }

    public static void addVideoDirection(FlatBufferBuilder builder, long videoDirection) {
        builder.addInt(10, (int) videoDirection, 0);
    }

    public static void addIsVideoPortZero(FlatBufferBuilder builder, boolean isVideoPortZero) {
        builder.addBoolean(11, isVideoPortZero, false);
    }

    public static void addMdmnExtNumber(FlatBufferBuilder builder, int mdmnExtNumberOffset) {
        builder.addOffset(12, mdmnExtNumberOffset, 0);
    }

    public static void addP2pList(FlatBufferBuilder builder, int p2pListOffset) {
        builder.addOffset(13, p2pListOffset, 0);
    }

    public static int createP2pListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startP2pListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endRequestPullingCall(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 14);
        builder.required(o, 16);
        builder.required(o, 28);
        return o;
    }
}
