package com.sec.internal.ims.core.handler.secims;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;

public class CallProfile {
    int amrBeMaxRed;
    int amrBeWbMaxRed;
    String amrMode;
    int amrOaMaxRed;
    int amrOaPayloadType;
    int amrOaWbMaxRed;
    int amrOpenPayloadType;
    int amrPayloadType;
    String amrWbMode;
    int amrWbOaPayloadType;
    int amrWbPayloadType;
    int audioAs;
    boolean audioAvpf;
    String audioCodec;
    int audioDscp;
    int audioPort;
    int audioRr;
    int audioRs;
    boolean audioRtcpXr;
    boolean audioSrtp;
    String displayFormat;
    String displayFormatHevc;
    int dtmfMode;
    int dtmfPayloadType;
    int dtmfWbPayloadType;
    boolean enableAvSync;
    boolean enableEvsCodec;
    boolean enableRtcpOnActiveCall;
    boolean enableScr;
    int evs2ndPayload;
    String evsBandwidthReceive;
    String evsBandwidthReceiveExt;
    String evsBandwidthSend;
    String evsBandwidthSendExt;
    String evsBitRateReceive;
    String evsBitRateReceiveExt;
    String evsBitRateSend;
    String evsBitRateSendExt;
    String evsChannelAwareReceive;
    String evsChannelRecv;
    String evsChannelSend;
    String evsCodecModeRequest;
    String evsDefaultBandwidth;
    String evsDefaultBitrate;
    String evsDiscontinuousTransmission;
    String evsDtxRecv;
    String evsHeaderFull;
    String evsLimitedCodec;
    int evsMaxRed;
    String evsModeSwitch;
    int evsPayload;
    int evsPayloadExt;
    int frameRate;
    int h263QcifPayloadType;
    int h264720pLPayloadType;
    int h264720pPayloadType;
    int h264CifLPayloadType;
    int h264CifPayloadType;
    int h264QvgaLPayloadType;
    int h264QvgaPayloadType;
    int h264VgaLPayloadType;
    int h264VgaPayloadType;
    int h265Hd720pLPayloadType;
    int h265Hd720pPayloadType;
    int h265QvgaLPayloadType;
    int h265QvgaPayloadType;
    int h265VgaLPayloadType;
    int h265VgaPayloadType;
    boolean ignoreRtcpTimeoutOnHoldCall;
    int maxPTime;
    int pTime;
    String packetizationMode;
    int rtcpTimeout;
    int rtpTimeout;
    int textAs;
    boolean textAvpf;
    int textPort;
    int textRr;
    int textRs;
    boolean textSrtp;
    int videoAs;
    boolean videoAvpf;
    boolean videoCapabilities;
    String videoCodec;
    int videoPort;
    int videoRr;
    int videoRs;
    boolean videoRtcpXr;
    boolean videoSrtp;

    public CallProfile(Builder builder) {
        this.audioCodec = builder.audioCodec;
        this.audioPort = builder.audioPort;
        this.audioDscp = builder.audioDscp;
        this.amrPayloadType = builder.amrPayloadType;
        this.amrOaPayloadType = builder.amrOaPayloadType;
        this.amrWbPayloadType = builder.amrWbPayloadType;
        this.amrWbOaPayloadType = builder.amrWbOaPayloadType;
        this.amrOpenPayloadType = builder.amrOpenPayloadType;
        this.dtmfPayloadType = builder.dtmfPayloadType;
        this.dtmfWbPayloadType = builder.dtmfWbPayloadType;
        this.amrOaMaxRed = builder.amrOaMaxRed;
        this.amrBeMaxRed = builder.amrBeMaxRed;
        this.amrOaWbMaxRed = builder.amrOaWbMaxRed;
        this.amrBeWbMaxRed = builder.amrBeWbMaxRed;
        this.evsMaxRed = builder.evsMaxRed;
        this.amrMode = builder.amrMode;
        this.amrWbMode = builder.amrWbMode;
        this.audioAs = builder.audioAs;
        this.audioRs = builder.audioRs;
        this.audioRr = builder.audioRr;
        this.pTime = builder.pTime;
        this.maxPTime = builder.maxPTime;
        this.videoCodec = builder.videoCodec;
        this.videoPort = builder.videoPort;
        this.frameRate = builder.frameRate;
        this.displayFormat = builder.displayFormat;
        this.displayFormatHevc = builder.displayFormatHevc;
        this.packetizationMode = builder.packetizationMode;
        this.h265QvgaPayloadType = builder.h265QvgaPayloadType;
        this.h265QvgaLPayloadType = builder.h265QvgaLPayloadType;
        this.h265VgaPayloadType = builder.h265VgaPayloadType;
        this.h265VgaLPayloadType = builder.h265VgaLPayloadType;
        this.h265Hd720pPayloadType = builder.h265Hd720pPayloadType;
        this.h265Hd720pLPayloadType = builder.h265Hd720pLPayloadType;
        this.h264720pPayloadType = builder.h264720pPayloadType;
        this.h264720pLPayloadType = builder.h264720pLPayloadType;
        this.h264VgaPayloadType = builder.h264VgaPayloadType;
        this.h264VgaLPayloadType = builder.h264VgaLPayloadType;
        this.h264QvgaPayloadType = builder.h264QvgaPayloadType;
        this.h264QvgaLPayloadType = builder.h264QvgaLPayloadType;
        this.h264CifPayloadType = builder.h264CifPayloadType;
        this.h264CifLPayloadType = builder.h264CifLPayloadType;
        this.h263QcifPayloadType = builder.h263QcifPayloadType;
        this.videoAs = builder.videoAs;
        this.videoRs = builder.videoRs;
        this.videoRr = builder.videoRr;
        this.textAs = builder.textAs;
        this.textRs = builder.textRs;
        this.textRr = builder.textRr;
        this.textPort = builder.textPort;
        this.audioAvpf = builder.audioAvpf;
        this.audioSrtp = builder.audioSrtp;
        this.videoAvpf = builder.videoAvpf;
        this.videoSrtp = builder.videoSrtp;
        this.textAvpf = builder.textAvpf;
        this.textSrtp = builder.textSrtp;
        this.videoCapabilities = builder.videoCapabilities;
        this.rtpTimeout = builder.rtpTimeout;
        this.rtcpTimeout = builder.rtcpTimeout;
        this.ignoreRtcpTimeoutOnHoldCall = builder.ignoreRtcpTimeoutOnHoldCall;
        this.enableRtcpOnActiveCall = builder.enableRtcpOnActiveCall;
        this.enableAvSync = builder.enableAvSync;
        this.enableScr = builder.enableScr;
        this.audioRtcpXr = builder.audioRtcpXr;
        this.videoRtcpXr = builder.videoRtcpXr;
        this.dtmfMode = builder.dtmfMode;
        this.enableEvsCodec = builder.enableEvsCodec;
        this.evsDiscontinuousTransmission = builder.evsDiscontinuousTransmission;
        this.evsDtxRecv = builder.evsDtxRecv;
        this.evsHeaderFull = builder.evsHeaderFull;
        this.evsModeSwitch = builder.evsModeSwitch;
        this.evsChannelSend = builder.evsChannelSend;
        this.evsChannelRecv = builder.evsChannelRecv;
        this.evsChannelAwareReceive = builder.evsChannelAwareReceive;
        this.evsCodecModeRequest = builder.evsCodecModeRequest;
        this.evsBitRateSend = builder.evsBitRateSend;
        this.evsBitRateReceive = builder.evsBitRateReceive;
        this.evsBandwidthSend = builder.evsBandwidthSend;
        this.evsBandwidthReceive = builder.evsBandwidthReceive;
        this.evsPayload = builder.evsPayload;
        this.evs2ndPayload = builder.evs2ndPayload;
        this.evsDefaultBandwidth = builder.evsDefaultBandwidth;
        this.evsDefaultBitrate = builder.evsDefaultBitrate;
        this.evsPayloadExt = builder.evsPayloadExt;
        this.evsBitRateSendExt = builder.evsBitRateSendExt;
        this.evsBitRateReceiveExt = builder.evsBitRateReceiveExt;
        this.evsBandwidthSendExt = builder.evsBandwidthSendExt;
        this.evsBandwidthReceiveExt = builder.evsBandwidthReceiveExt;
        this.evsLimitedCodec = builder.evsLimitedCodec;
    }

    public static class Builder {
        int amrBeMaxRed;
        int amrBeWbMaxRed;
        String amrMode;
        int amrOaMaxRed;
        int amrOaPayloadType;
        int amrOaWbMaxRed;
        int amrOpenPayloadType;
        int amrPayloadType;
        String amrWbMode;
        int amrWbOaPayloadType;
        int amrWbPayloadType;
        int audioAs;
        boolean audioAvpf;
        String audioCodec;
        int audioDscp;
        int audioPort;
        int audioRr;
        int audioRs;
        boolean audioRtcpXr;
        boolean audioSrtp;
        String displayFormat;
        String displayFormatHevc;
        int dtmfMode;
        int dtmfPayloadType;
        int dtmfWbPayloadType;
        boolean enableAvSync;
        boolean enableEvsCodec;
        boolean enableRtcpOnActiveCall;
        boolean enableScr;
        int evs2ndPayload;
        String evsBandwidthReceive;
        String evsBandwidthReceiveExt;
        String evsBandwidthSend;
        String evsBandwidthSendExt;
        String evsBitRateReceive;
        String evsBitRateReceiveExt;
        String evsBitRateSend;
        String evsBitRateSendExt;
        String evsChannelAwareReceive;
        String evsChannelRecv;
        String evsChannelSend;
        String evsCodecModeRequest;
        String evsDefaultBandwidth;
        String evsDefaultBitrate;
        String evsDiscontinuousTransmission;
        String evsDtxRecv;
        String evsHeaderFull;
        String evsLimitedCodec;
        int evsMaxRed;
        String evsModeSwitch;
        int evsPayload;
        int evsPayloadExt;
        int frameRate;
        int h263QcifPayloadType;
        int h264720pLPayloadType;
        int h264720pPayloadType;
        int h264CifLPayloadType;
        int h264CifPayloadType;
        int h264QvgaLPayloadType;
        int h264QvgaPayloadType;
        int h264VgaLPayloadType;
        int h264VgaPayloadType;
        int h265Hd720pLPayloadType;
        int h265Hd720pPayloadType;
        int h265QvgaLPayloadType;
        int h265QvgaPayloadType;
        int h265VgaLPayloadType;
        int h265VgaPayloadType;
        boolean ignoreRtcpTimeoutOnHoldCall;
        int maxPTime = Id.REQUEST_STOP_RECORD;
        int pTime = 20;
        String packetizationMode;
        int rtcpTimeout;
        int rtpTimeout;
        int textAs;
        boolean textAvpf;
        int textPort;
        int textRr;
        int textRs;
        boolean textSrtp;
        int videoAs;
        boolean videoAvpf;
        boolean videoCapabilities;
        String videoCodec;
        int videoPort;
        int videoRr;
        int videoRs;
        boolean videoRtcpXr;
        boolean videoSrtp;

        public static Builder newBuilder() {
            return new Builder();
        }

        public CallProfile build() {
            return new CallProfile(this);
        }

        public Builder setAudioCodec(String audioCodec2) {
            this.audioCodec = audioCodec2;
            return this;
        }

        public Builder setAudioPort(int audioPort2) {
            this.audioPort = audioPort2;
            return this;
        }

        public Builder setAudioDscp(int audioDscp2) {
            this.audioDscp = audioDscp2;
            return this;
        }

        public Builder setAmrPayloadType(int amrPayloadType2) {
            this.amrPayloadType = amrPayloadType2;
            return this;
        }

        public Builder setAmrOaPayloadType(int amrOaPayloadType2) {
            this.amrOaPayloadType = amrOaPayloadType2;
            return this;
        }

        public Builder setAmrWbPayloadType(int amrWbPayloadType2) {
            this.amrWbPayloadType = amrWbPayloadType2;
            return this;
        }

        public Builder setAmrWbOaPayloadType(int amrWbOaPayloadType2) {
            this.amrWbOaPayloadType = amrWbOaPayloadType2;
            return this;
        }

        public Builder setAmrOpenPayloadType(int amrOpenPayloadType2) {
            this.amrOpenPayloadType = amrOpenPayloadType2;
            return this;
        }

        public Builder setDtmfWbPayloadType(int dtmfWbPayloadType2) {
            this.dtmfWbPayloadType = dtmfWbPayloadType2;
            return this;
        }

        public Builder setDtmfPayloadType(int dtmfPayloadType2) {
            this.dtmfPayloadType = dtmfPayloadType2;
            return this;
        }

        public Builder setAmrOaMaxRed(int amrOaMaxRed2) {
            this.amrOaMaxRed = amrOaMaxRed2;
            return this;
        }

        public Builder setAmrBeMaxRed(int amrBeMaxRed2) {
            this.amrBeMaxRed = amrBeMaxRed2;
            return this;
        }

        public Builder setAmrOaWbMaxRed(int amrOaWbMaxRed2) {
            this.amrOaWbMaxRed = amrOaWbMaxRed2;
            return this;
        }

        public Builder setAmrBeWbMaxRed(int amrBeWbMaxRed2) {
            this.amrBeWbMaxRed = amrBeWbMaxRed2;
            return this;
        }

        public Builder setEvsMaxRed(int evsMaxRed2) {
            this.evsMaxRed = evsMaxRed2;
            return this;
        }

        public Builder setAmrMode(String amrMode2) {
            this.amrMode = amrMode2;
            return this;
        }

        public Builder setAmrWbMode(String amrWbMode2) {
            this.amrWbMode = amrWbMode2;
            return this;
        }

        public Builder setAudioAs(int audioAs2) {
            this.audioAs = audioAs2;
            return this;
        }

        public Builder setAudioRs(int audioRs2) {
            this.audioRs = audioRs2;
            return this;
        }

        public Builder setAudioRr(int audioRr2) {
            this.audioRr = audioRr2;
            return this;
        }

        public Builder setPTime(int pTime2) {
            this.pTime = pTime2;
            return this;
        }

        public Builder setMaxPTime(int maxPTime2) {
            this.maxPTime = maxPTime2;
            return this;
        }

        public Builder setVideoCodec(String videoCodec2) {
            this.videoCodec = videoCodec2;
            return this;
        }

        public Builder setVideoPort(int videoPort2) {
            this.videoPort = videoPort2;
            return this;
        }

        public Builder setFrameRate(int frameRate2) {
            this.frameRate = frameRate2;
            return this;
        }

        public Builder setDisplayFormat(String displayFormat2) {
            this.displayFormat = displayFormat2;
            return this;
        }

        public Builder setDisplayFormatHevc(String displayFormatHevc2) {
            this.displayFormatHevc = displayFormatHevc2;
            return this;
        }

        public Builder setPacketizationMode(String packetizationMode2) {
            this.packetizationMode = packetizationMode2;
            return this;
        }

        public Builder setH265QvgaPayloadType(int h265QvgaPayloadType2) {
            this.h265QvgaPayloadType = h265QvgaPayloadType2;
            return this;
        }

        public Builder setH265QvgaLPayloadType(int h265QvgaLPayloadType2) {
            this.h265QvgaLPayloadType = h265QvgaLPayloadType2;
            return this;
        }

        public Builder setH265VgaPayloadType(int h265VgaPayloadType2) {
            this.h265VgaPayloadType = h265VgaPayloadType2;
            return this;
        }

        public Builder setH265VgaLPayloadType(int h265VgaLPayloadType2) {
            this.h265VgaLPayloadType = h265VgaLPayloadType2;
            return this;
        }

        public Builder setH265Hd720pPayloadType(int h265Hd720pPayloadType2) {
            this.h265Hd720pPayloadType = h265Hd720pPayloadType2;
            return this;
        }

        public Builder setH265Hd720pLPayloadType(int h265Hd720pLPayloadType2) {
            this.h265Hd720pLPayloadType = h265Hd720pLPayloadType2;
            return this;
        }

        public Builder setH264720pPayloadType(int h264720pPayloadType2) {
            this.h264720pPayloadType = h264720pPayloadType2;
            return this;
        }

        public Builder setH264720pLPayloadType(int h264720pLPayloadType2) {
            this.h264720pLPayloadType = h264720pLPayloadType2;
            return this;
        }

        public Builder setH264VgaPayloadType(int h264VgaPayloadType2) {
            this.h264VgaPayloadType = h264VgaPayloadType2;
            return this;
        }

        public Builder setH264VgaLPayloadType(int h264VgaLPayloadType2) {
            this.h264VgaLPayloadType = h264VgaLPayloadType2;
            return this;
        }

        public Builder setH264QvgaPayloadType(int h264QvgaPayloadType2) {
            this.h264QvgaPayloadType = h264QvgaPayloadType2;
            return this;
        }

        public Builder setH264QvgaLPayloadType(int h264QvgaLPayloadType2) {
            this.h264QvgaLPayloadType = h264QvgaLPayloadType2;
            return this;
        }

        public Builder setH264CifPayloadType(int h264CifPayloadType2) {
            this.h264CifPayloadType = h264CifPayloadType2;
            return this;
        }

        public Builder setH264CifLPayloadType(int h264CifLPayloadType2) {
            this.h264CifLPayloadType = h264CifLPayloadType2;
            return this;
        }

        public Builder setH263QcifPayloadType(int h263QcifPayloadType2) {
            this.h263QcifPayloadType = h263QcifPayloadType2;
            return this;
        }

        public Builder setVideoAs(int videoAs2) {
            this.videoAs = videoAs2;
            return this;
        }

        public Builder setVideoRs(int videoRs2) {
            this.videoRs = videoRs2;
            return this;
        }

        public Builder setVideoRr(int videoRr2) {
            this.videoRr = videoRr2;
            return this;
        }

        public Builder setAudioAvpf(boolean audioAvpf2) {
            this.audioAvpf = audioAvpf2;
            return this;
        }

        public Builder setAudioSrtp(boolean audioSrtp2) {
            this.audioSrtp = audioSrtp2;
            return this;
        }

        public Builder setVideoAvpf(boolean videoAvpf2) {
            this.videoAvpf = videoAvpf2;
            return this;
        }

        public Builder setVideoSrtp(boolean videoSrtp2) {
            this.videoSrtp = videoSrtp2;
            return this;
        }

        public Builder setTextAvpf(boolean textAvpf2) {
            this.textAvpf = textAvpf2;
            return this;
        }

        public Builder setTextSrtp(boolean textSrtp2) {
            this.textSrtp = textSrtp2;
            return this;
        }

        public Builder setVideoCapabilities(boolean videoCapabilities2) {
            this.videoCapabilities = videoCapabilities2;
            return this;
        }

        public Builder setTextAs(int textAs2) {
            this.textAs = textAs2;
            return this;
        }

        public Builder setTextRs(int textRs2) {
            this.textRs = textRs2;
            return this;
        }

        public Builder setTextRr(int textRr2) {
            this.textRr = textRr2;
            return this;
        }

        public Builder setTextPort(int textPort2) {
            this.textPort = textPort2;
            return this;
        }

        public Builder setRtpTimeout(int rtpTimeout2) {
            this.rtpTimeout = rtpTimeout2;
            return this;
        }

        public Builder setRtcpTimeout(int rtcpTimeout2) {
            this.rtcpTimeout = rtcpTimeout2;
            return this;
        }

        public Builder setIgnoreRtcpTimeoutOnHoldCall(boolean ignoreRtcpTimeoutOnHoldCall2) {
            this.ignoreRtcpTimeoutOnHoldCall = ignoreRtcpTimeoutOnHoldCall2;
            return this;
        }

        public Builder setEnableRtcpOnActiveCall(boolean enableRtcpOnActiveCall2) {
            this.enableRtcpOnActiveCall = enableRtcpOnActiveCall2;
            return this;
        }

        public Builder setEnableAvSync(boolean enableAvSync2) {
            this.enableAvSync = enableAvSync2;
            return this;
        }

        public Builder setEnableScr(boolean enableScr2) {
            this.enableScr = enableScr2;
            return this;
        }

        public Builder setAudioRtcpXr(boolean audioRtcpXr2) {
            this.audioRtcpXr = audioRtcpXr2;
            return this;
        }

        public Builder setVideoRtcpXr(boolean videoRtcpXr2) {
            this.videoRtcpXr = videoRtcpXr2;
            return this;
        }

        public Builder setDtmfMode(int dtmfMode2) {
            this.dtmfMode = dtmfMode2;
            return this;
        }

        public Builder setEnableEvsCodec(boolean enableEvsCodec2) {
            this.enableEvsCodec = enableEvsCodec2;
            return this;
        }

        public Builder setEvsDiscontinuousTransmission(String evsDiscontinuousTransmission2) {
            this.evsDiscontinuousTransmission = evsDiscontinuousTransmission2;
            return this;
        }

        public Builder setEvsDtxRecv(String evsDtxRecv2) {
            this.evsDtxRecv = evsDtxRecv2;
            return this;
        }

        public Builder setEvsHeaderFull(String evsHeaderFull2) {
            this.evsHeaderFull = evsHeaderFull2;
            return this;
        }

        public Builder setEvsModeSwitch(String evsModeSwitch2) {
            this.evsModeSwitch = evsModeSwitch2;
            return this;
        }

        public Builder setEvsChannelSend(String evsChannelSend2) {
            this.evsChannelSend = evsChannelSend2;
            return this;
        }

        public Builder setEvsChannelRecv(String evsChannelRecv2) {
            this.evsChannelRecv = evsChannelRecv2;
            return this;
        }

        public Builder setEvsChannelAwareReceive(String evsChannelAwareReceive2) {
            this.evsChannelAwareReceive = evsChannelAwareReceive2;
            return this;
        }

        public Builder setEvsCodecModeRequest(String evsCodecModeRequest2) {
            this.evsCodecModeRequest = evsCodecModeRequest2;
            return this;
        }

        public Builder setEvsBitRateSend(String evsBitRateSend2) {
            this.evsBitRateSend = evsBitRateSend2;
            return this;
        }

        public Builder setEvsBitRateReceive(String evsBitRateReceive2) {
            this.evsBitRateReceive = evsBitRateReceive2;
            return this;
        }

        public Builder setEvsBandwidthSend(String evsBandwidthSend2) {
            this.evsBandwidthSend = evsBandwidthSend2;
            return this;
        }

        public Builder setEvsBandwidthReceive(String evsBandwidthReceive2) {
            this.evsBandwidthReceive = evsBandwidthReceive2;
            return this;
        }

        public Builder setEvsPayload(int evsPayload2) {
            this.evsPayload = evsPayload2;
            return this;
        }

        public Builder setEvs2ndPayload(int evs2ndPayload2) {
            this.evs2ndPayload = evs2ndPayload2;
            return this;
        }

        public Builder setEvsDefaultBandwidth(String evsDefaultBandwidth2) {
            this.evsDefaultBandwidth = evsDefaultBandwidth2;
            return this;
        }

        public Builder setEvsDefaultBitrate(String evsDefaultBitrate2) {
            this.evsDefaultBitrate = evsDefaultBitrate2;
            return this;
        }

        public Builder setEvsPayloadExt(int evsPayloadExt2) {
            this.evsPayloadExt = evsPayloadExt2;
            return this;
        }

        public Builder setEvsBitRateSendExt(String evsBitRateSendExt2) {
            this.evsBitRateSendExt = evsBitRateSendExt2;
            return this;
        }

        public Builder setEvsBitRateReceiveExt(String evsBitRateReceiveExt2) {
            this.evsBitRateReceiveExt = evsBitRateReceiveExt2;
            return this;
        }

        public Builder setEvsBandwidthSendExt(String evsBandwidthSendExt2) {
            this.evsBandwidthSendExt = evsBandwidthSendExt2;
            return this;
        }

        public Builder setEvsBandwidthReceiveExt(String evsBandwidthReceiveExt2) {
            this.evsBandwidthReceiveExt = evsBandwidthReceiveExt2;
            return this;
        }

        public Builder setEvsLimitedCodec(String evsLimitedCodec2) {
            this.evsLimitedCodec = evsLimitedCodec2;
            return this;
        }
    }
}
