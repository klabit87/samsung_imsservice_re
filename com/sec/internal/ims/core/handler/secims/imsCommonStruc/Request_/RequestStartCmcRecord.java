package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartCmcRecord extends Table {
    public static RequestStartCmcRecord getRootAsRequestStartCmcRecord(ByteBuffer _bb) {
        return getRootAsRequestStartCmcRecord(_bb, new RequestStartCmcRecord());
    }

    public static RequestStartCmcRecord getRootAsRequestStartCmcRecord(ByteBuffer _bb, RequestStartCmcRecord obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestStartCmcRecord __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioSource() {
        int o = __offset(8);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long outputFormat() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long maxFileSize() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public long maxDuration() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String outputPath() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer outputPathAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public long audioEncodingBr() {
        int o = __offset(18);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioChannels() {
        int o = __offset(20);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioSamplingRate() {
        int o = __offset(22);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioEncoder() {
        int o = __offset(24);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long durationInterval() {
        int o = __offset(26);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long fileSizeInterval() {
        int o = __offset(28);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public String author() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer authorAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public static int createRequestStartCmcRecord(FlatBufferBuilder builder, long handle, long session, long audio_source, long output_format, long max_file_size, long max_duration, int output_pathOffset, long audio_encoding_br, long audio_channels, long audio_sampling_rate, long audio_encoder, long duration_interval, long file_size_interval, int authorOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(14);
        addFileSizeInterval(flatBufferBuilder, file_size_interval);
        addMaxFileSize(flatBufferBuilder, max_file_size);
        addAuthor(flatBufferBuilder, authorOffset);
        addDurationInterval(flatBufferBuilder, duration_interval);
        addAudioEncoder(flatBufferBuilder, audio_encoder);
        addAudioSamplingRate(flatBufferBuilder, audio_sampling_rate);
        addAudioChannels(flatBufferBuilder, audio_channels);
        addAudioEncodingBr(flatBufferBuilder, audio_encoding_br);
        addOutputPath(flatBufferBuilder, output_pathOffset);
        addMaxDuration(flatBufferBuilder, max_duration);
        addOutputFormat(flatBufferBuilder, output_format);
        addAudioSource(flatBufferBuilder, audio_source);
        addSession(flatBufferBuilder, session);
        addHandle(builder, handle);
        return endRequestStartCmcRecord(builder);
    }

    public static void startRequestStartCmcRecord(FlatBufferBuilder builder) {
        builder.startObject(14);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addAudioSource(FlatBufferBuilder builder, long audioSource) {
        builder.addInt(2, (int) audioSource, 0);
    }

    public static void addOutputFormat(FlatBufferBuilder builder, long outputFormat) {
        builder.addInt(3, (int) outputFormat, 0);
    }

    public static void addMaxFileSize(FlatBufferBuilder builder, long maxFileSize) {
        builder.addLong(4, maxFileSize, 0);
    }

    public static void addMaxDuration(FlatBufferBuilder builder, long maxDuration) {
        builder.addInt(5, (int) maxDuration, 0);
    }

    public static void addOutputPath(FlatBufferBuilder builder, int outputPathOffset) {
        builder.addOffset(6, outputPathOffset, 0);
    }

    public static void addAudioEncodingBr(FlatBufferBuilder builder, long audioEncodingBr) {
        builder.addInt(7, (int) audioEncodingBr, 0);
    }

    public static void addAudioChannels(FlatBufferBuilder builder, long audioChannels) {
        builder.addInt(8, (int) audioChannels, 0);
    }

    public static void addAudioSamplingRate(FlatBufferBuilder builder, long audioSamplingRate) {
        builder.addInt(9, (int) audioSamplingRate, 0);
    }

    public static void addAudioEncoder(FlatBufferBuilder builder, long audioEncoder) {
        builder.addInt(10, (int) audioEncoder, 0);
    }

    public static void addDurationInterval(FlatBufferBuilder builder, long durationInterval) {
        builder.addInt(11, (int) durationInterval, 0);
    }

    public static void addFileSizeInterval(FlatBufferBuilder builder, long fileSizeInterval) {
        builder.addLong(12, fileSizeInterval, 0);
    }

    public static void addAuthor(FlatBufferBuilder builder, int authorOffset) {
        builder.addOffset(13, authorOffset, 0);
    }

    public static int endRequestStartCmcRecord(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 16);
        builder.required(o, 30);
        return o;
    }
}
