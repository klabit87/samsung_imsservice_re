package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CallStatus extends Table {
    public static CallStatus getRootAsCallStatus(ByteBuffer _bb) {
        return getRootAsCallStatus(_bb, new CallStatus());
    }

    public static CallStatus getRootAsCallStatus(ByteBuffer _bb, CallStatus obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public CallStatus __assign(int _i, ByteBuffer _bb) {
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

    public int callType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int state() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public long statusCode() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String reasonPhrase() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer reasonPhraseAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public boolean remoteVideoCapa() {
        int o = __offset(16);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String audioCodecName() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioCodecNameAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents obj) {
        int o = __offset(20);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public long width() {
        int o = __offset(22);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long height() {
        int o = __offset(24);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String conferenceSupport() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer conferenceSupportAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public boolean isFocus() {
        int o = __offset(28);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean remoteMmtelCapa() {
        int o = __offset(30);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long localVideoRtpPort() {
        int o = __offset(32);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long localVideoRtcpPort() {
        int o = __offset(34);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long remoteVideoRtpPort() {
        int o = __offset(36);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long remoteVideoRtcpPort() {
        int o = __offset(38);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String serviceUrn() {
        int o = __offset(40);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer serviceUrnAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public long retryAfter() {
        int o = __offset(42);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean localHoldTone() {
        int o = __offset(44);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String historyInfo() {
        int o = __offset(46);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer historyInfoAsByteBuffer() {
        return __vector_as_bytebuffer(46, 1);
    }

    public String dtmfEvent() {
        int o = __offset(48);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dtmfEventAsByteBuffer() {
        return __vector_as_bytebuffer(48, 1);
    }

    public boolean cvoEnabled() {
        int o = __offset(50);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String alertInfo() {
        int o = __offset(52);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(52, 1);
    }

    public String cmcDeviceId() {
        int o = __offset(54);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcDeviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(54, 1);
    }

    public long videoCrbtType() {
        int o = __offset(56);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioRxTrackId() {
        int o = __offset(58);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String audioBitRate() {
        int o = __offset(60);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioBitRateAsByteBuffer() {
        return __vector_as_bytebuffer(60, 1);
    }

    public String cmcCallTime() {
        int o = __offset(62);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcCallTimeAsByteBuffer() {
        return __vector_as_bytebuffer(62, 1);
    }

    public String featureCaps() {
        int o = __offset(64);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer featureCapsAsByteBuffer() {
        return __vector_as_bytebuffer(64, 1);
    }

    public long audioEarlyMediaDir() {
        int o = __offset(66);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static int createCallStatus(FlatBufferBuilder builder, long handle, long session, int call_type, int state, long status_code, int reason_phraseOffset, boolean remote_video_capa, int audio_codec_nameOffset, int additional_contentsOffset, long width, long height, int conference_supportOffset, boolean is_focus, boolean remote_mmtel_capa, long local_video_rtp_port, long local_video_rtcp_port, long remote_video_rtp_port, long remote_video_rtcp_port, int service_urnOffset, long retry_after, boolean local_hold_tone, int history_infoOffset, int dtmf_eventOffset, boolean cvo_enabled, int alert_infoOffset, int cmc_device_idOffset, long video_crbt_type, long audio_rx_track_id, int audio_bit_rateOffset, int cmc_call_timeOffset, int feature_capsOffset, long audio_early_media_dir) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(32);
        addAudioEarlyMediaDir(flatBufferBuilder, audio_early_media_dir);
        addFeatureCaps(flatBufferBuilder, feature_capsOffset);
        addCmcCallTime(flatBufferBuilder, cmc_call_timeOffset);
        addAudioBitRate(flatBufferBuilder, audio_bit_rateOffset);
        addAudioRxTrackId(flatBufferBuilder, audio_rx_track_id);
        addVideoCrbtType(flatBufferBuilder, video_crbt_type);
        addCmcDeviceId(flatBufferBuilder, cmc_device_idOffset);
        addAlertInfo(flatBufferBuilder, alert_infoOffset);
        addDtmfEvent(flatBufferBuilder, dtmf_eventOffset);
        addHistoryInfo(flatBufferBuilder, history_infoOffset);
        addRetryAfter(flatBufferBuilder, retry_after);
        addServiceUrn(flatBufferBuilder, service_urnOffset);
        addRemoteVideoRtcpPort(flatBufferBuilder, remote_video_rtcp_port);
        addRemoteVideoRtpPort(flatBufferBuilder, remote_video_rtp_port);
        addLocalVideoRtcpPort(flatBufferBuilder, local_video_rtcp_port);
        addLocalVideoRtpPort(flatBufferBuilder, local_video_rtp_port);
        addConferenceSupport(flatBufferBuilder, conference_supportOffset);
        addHeight(flatBufferBuilder, height);
        addWidth(flatBufferBuilder, width);
        addAdditionalContents(flatBufferBuilder, additional_contentsOffset);
        addAudioCodecName(flatBufferBuilder, audio_codec_nameOffset);
        addReasonPhrase(flatBufferBuilder, reason_phraseOffset);
        addStatusCode(flatBufferBuilder, status_code);
        addState(flatBufferBuilder, state);
        addCallType(flatBufferBuilder, call_type);
        addSession(flatBufferBuilder, session);
        addHandle(builder, handle);
        addCvoEnabled(flatBufferBuilder, cvo_enabled);
        addLocalHoldTone(flatBufferBuilder, local_hold_tone);
        addRemoteMmtelCapa(flatBufferBuilder, remote_mmtel_capa);
        addIsFocus(flatBufferBuilder, is_focus);
        addRemoteVideoCapa(flatBufferBuilder, remote_video_capa);
        return endCallStatus(builder);
    }

    public static void startCallStatus(FlatBufferBuilder builder) {
        builder.startObject(32);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(2, callType, 0);
    }

    public static void addState(FlatBufferBuilder builder, int state) {
        builder.addInt(3, state, 0);
    }

    public static void addStatusCode(FlatBufferBuilder builder, long statusCode) {
        builder.addInt(4, (int) statusCode, 0);
    }

    public static void addReasonPhrase(FlatBufferBuilder builder, int reasonPhraseOffset) {
        builder.addOffset(5, reasonPhraseOffset, 0);
    }

    public static void addRemoteVideoCapa(FlatBufferBuilder builder, boolean remoteVideoCapa) {
        builder.addBoolean(6, remoteVideoCapa, false);
    }

    public static void addAudioCodecName(FlatBufferBuilder builder, int audioCodecNameOffset) {
        builder.addOffset(7, audioCodecNameOffset, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder builder, int additionalContentsOffset) {
        builder.addOffset(8, additionalContentsOffset, 0);
    }

    public static void addWidth(FlatBufferBuilder builder, long width) {
        builder.addInt(9, (int) width, 0);
    }

    public static void addHeight(FlatBufferBuilder builder, long height) {
        builder.addInt(10, (int) height, 0);
    }

    public static void addConferenceSupport(FlatBufferBuilder builder, int conferenceSupportOffset) {
        builder.addOffset(11, conferenceSupportOffset, 0);
    }

    public static void addIsFocus(FlatBufferBuilder builder, boolean isFocus) {
        builder.addBoolean(12, isFocus, false);
    }

    public static void addRemoteMmtelCapa(FlatBufferBuilder builder, boolean remoteMmtelCapa) {
        builder.addBoolean(13, remoteMmtelCapa, false);
    }

    public static void addLocalVideoRtpPort(FlatBufferBuilder builder, long localVideoRtpPort) {
        builder.addInt(14, (int) localVideoRtpPort, 0);
    }

    public static void addLocalVideoRtcpPort(FlatBufferBuilder builder, long localVideoRtcpPort) {
        builder.addInt(15, (int) localVideoRtcpPort, 0);
    }

    public static void addRemoteVideoRtpPort(FlatBufferBuilder builder, long remoteVideoRtpPort) {
        builder.addInt(16, (int) remoteVideoRtpPort, 0);
    }

    public static void addRemoteVideoRtcpPort(FlatBufferBuilder builder, long remoteVideoRtcpPort) {
        builder.addInt(17, (int) remoteVideoRtcpPort, 0);
    }

    public static void addServiceUrn(FlatBufferBuilder builder, int serviceUrnOffset) {
        builder.addOffset(18, serviceUrnOffset, 0);
    }

    public static void addRetryAfter(FlatBufferBuilder builder, long retryAfter) {
        builder.addInt(19, (int) retryAfter, 0);
    }

    public static void addLocalHoldTone(FlatBufferBuilder builder, boolean localHoldTone) {
        builder.addBoolean(20, localHoldTone, false);
    }

    public static void addHistoryInfo(FlatBufferBuilder builder, int historyInfoOffset) {
        builder.addOffset(21, historyInfoOffset, 0);
    }

    public static void addDtmfEvent(FlatBufferBuilder builder, int dtmfEventOffset) {
        builder.addOffset(22, dtmfEventOffset, 0);
    }

    public static void addCvoEnabled(FlatBufferBuilder builder, boolean cvoEnabled) {
        builder.addBoolean(23, cvoEnabled, false);
    }

    public static void addAlertInfo(FlatBufferBuilder builder, int alertInfoOffset) {
        builder.addOffset(24, alertInfoOffset, 0);
    }

    public static void addCmcDeviceId(FlatBufferBuilder builder, int cmcDeviceIdOffset) {
        builder.addOffset(25, cmcDeviceIdOffset, 0);
    }

    public static void addVideoCrbtType(FlatBufferBuilder builder, long videoCrbtType) {
        builder.addInt(26, (int) videoCrbtType, 0);
    }

    public static void addAudioRxTrackId(FlatBufferBuilder builder, long audioRxTrackId) {
        builder.addInt(27, (int) audioRxTrackId, 0);
    }

    public static void addAudioBitRate(FlatBufferBuilder builder, int audioBitRateOffset) {
        builder.addOffset(28, audioBitRateOffset, 0);
    }

    public static void addCmcCallTime(FlatBufferBuilder builder, int cmcCallTimeOffset) {
        builder.addOffset(29, cmcCallTimeOffset, 0);
    }

    public static void addFeatureCaps(FlatBufferBuilder builder, int featureCapsOffset) {
        builder.addOffset(30, featureCapsOffset, 0);
    }

    public static void addAudioEarlyMediaDir(FlatBufferBuilder builder, long audioEarlyMediaDir) {
        builder.addInt(31, (int) audioEarlyMediaDir, 0);
    }

    public static int endCallStatus(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
