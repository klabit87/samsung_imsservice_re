package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MediaConfig extends Table {
    public static MediaConfig getRootAsMediaConfig(ByteBuffer _bb) {
        return getRootAsMediaConfig(_bb, new MediaConfig());
    }

    public static MediaConfig getRootAsMediaConfig(ByteBuffer _bb, MediaConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public MediaConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long audioPort() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long audioDscp() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isAmrOctecAlign() {
        int o = __offset(8);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String audioCodec() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioCodecAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public long dtmfMode() {
        int o = __offset(12);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String audioAs() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioAsAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String audioRs() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioRsAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String audioRr() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer audioRrAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String amrModeChangeCapability() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer amrModeChangeCapabilityAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public long amrMaxRed() {
        int o = __offset(22);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String amrMode() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer amrModeAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String amrWbMode() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer amrWbModeAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public long amrPayload() {
        int o = __offset(28);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long amrbePayload() {
        int o = __offset(30);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long amrWbPayload() {
        int o = __offset(32);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long amrbeWbPayload() {
        int o = __offset(34);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long dtmfPayload() {
        int o = __offset(36);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long dtmfWbPayload() {
        int o = __offset(38);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long pTime() {
        int o = __offset(40);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long maxTime() {
        int o = __offset(42);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String videoCodec() {
        int o = __offset(44);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer videoCodecAsByteBuffer() {
        return __vector_as_bytebuffer(44, 1);
    }

    public long videoPort() {
        int o = __offset(46);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long frameRate() {
        int o = __offset(48);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String displayFormat() {
        int o = __offset(50);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayFormatAsByteBuffer() {
        return __vector_as_bytebuffer(50, 1);
    }

    public String displayFormatHevc() {
        int o = __offset(52);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayFormatHevcAsByteBuffer() {
        return __vector_as_bytebuffer(52, 1);
    }

    public String packetizationMode() {
        int o = __offset(54);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer packetizationModeAsByteBuffer() {
        return __vector_as_bytebuffer(54, 1);
    }

    public long h265Hd720pPayload() {
        int o = __offset(56);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h265Hd720plPayload() {
        int o = __offset(58);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h265VgaPayload() {
        int o = __offset(60);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h265VgalPayload() {
        int o = __offset(62);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h265QvgaPayload() {
        int o = __offset(64);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h265QvgalPayload() {
        int o = __offset(66);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264720pPayload() {
        int o = __offset(68);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264720plPayload() {
        int o = __offset(70);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264VgaPayload() {
        int o = __offset(72);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264VgalPayload() {
        int o = __offset(74);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264QvgaPayload() {
        int o = __offset(76);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264QvgalPayload() {
        int o = __offset(78);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264CifPayload() {
        int o = __offset(80);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h264CiflPayload() {
        int o = __offset(82);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long h263QcifPayload() {
        int o = __offset(84);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long videoAs() {
        int o = __offset(86);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long videoRs() {
        int o = __offset(88);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long videoRr() {
        int o = __offset(90);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long textAs() {
        int o = __offset(92);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long textRs() {
        int o = __offset(94);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long textRr() {
        int o = __offset(96);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long textPort() {
        int o = __offset(98);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int localSendStrengthtag() {
        int o = __offset(100);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int localReceivedStrengthtag() {
        int o = __offset(102);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int remoteSendStrengthtag() {
        int o = __offset(104);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int remoteReceivedStrengthtag() {
        int o = __offset(106);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public boolean audioAvpf() {
        int o = __offset(108);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean audioSrtp() {
        int o = __offset(110);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean videoAvpf() {
        int o = __offset(112);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean videoSrtp() {
        int o = __offset(114);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean textAvpf() {
        int o = __offset(116);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean textSrtp() {
        int o = __offset(118);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean videoCapabilities() {
        int o = __offset(120);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean enableScr() {
        int o = __offset(122);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long rtpTimeout() {
        int o = __offset(124);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long rtcpTimeout() {
        int o = __offset(126);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean audioRtcpXr() {
        int o = __offset(128);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean videoRtcpXr() {
        int o = __offset(130);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean enableEvsCodec() {
        int o = __offset(132);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String evsDiscontinuousTransmission() {
        int o = __offset(134);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsDiscontinuousTransmissionAsByteBuffer() {
        return __vector_as_bytebuffer(134, 1);
    }

    public String evsDtxRecv() {
        int o = __offset(136);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsDtxRecvAsByteBuffer() {
        return __vector_as_bytebuffer(136, 1);
    }

    public String evsHeaderFull() {
        int o = __offset(138);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsHeaderFullAsByteBuffer() {
        return __vector_as_bytebuffer(138, 1);
    }

    public String evsModeSwitch() {
        int o = __offset(140);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsModeSwitchAsByteBuffer() {
        return __vector_as_bytebuffer(140, 1);
    }

    public String evsChannelSend() {
        int o = __offset(142);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsChannelSendAsByteBuffer() {
        return __vector_as_bytebuffer(142, 1);
    }

    public String evsChannelRecv() {
        int o = __offset(144);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsChannelRecvAsByteBuffer() {
        return __vector_as_bytebuffer(144, 1);
    }

    public String evsChannelAwareReceive() {
        int o = __offset(146);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsChannelAwareReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(146, 1);
    }

    public String evsCodecModeRequest() {
        int o = __offset(148);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsCodecModeRequestAsByteBuffer() {
        return __vector_as_bytebuffer(148, 1);
    }

    public String evsBitRateSend() {
        int o = __offset(150);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBitRateSendAsByteBuffer() {
        return __vector_as_bytebuffer(150, 1);
    }

    public String evsBitRateReceive() {
        int o = __offset(152);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBitRateReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(152, 1);
    }

    public String evsBandwidthSend() {
        int o = __offset(MNO.PERSONAL_ARGENTINA);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBandwidthSendAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.PERSONAL_ARGENTINA, 1);
    }

    public String evsBandwidthReceive() {
        int o = __offset(MNO.TANGO_LUXEMBOURG);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBandwidthReceiveAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TANGO_LUXEMBOURG, 1);
    }

    public long evsPayload() {
        int o = __offset(MNO.STC_KSA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String evsDefaultBandwidth() {
        int o = __offset(MNO.UMOBILE);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsDefaultBandwidthAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.UMOBILE, 1);
    }

    public String evsDefaultBitrate() {
        int o = __offset(MNO.TMOBILE_ROMANIA);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsDefaultBitrateAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TMOBILE_ROMANIA, 1);
    }

    public boolean enableRtcpOnActiveCall() {
        int o = __offset(MNO.CLARO_COLOMBIA);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long amrOpenPayload() {
        int o = __offset(MNO.TELENOR_BG);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean enableAvSync() {
        int o = __offset(MNO.TELIA_FI);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean ignoreRtcpTimeoutOnHoldCall() {
        int o = __offset(MNO.ALTAN_MEXICO);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long amrbeMaxRed() {
        int o = __offset(MNO.MOVISTAR_PANAMA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long amrWbMaxRed() {
        int o = __offset(MNO.VODAFONE_ROMANIA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long amrbeWbMaxRed() {
        int o = __offset(MNO.ORANGE_SENEGAL);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long evsMaxRed() {
        int o = __offset(MNO.MAGTICOM_GE);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long evs2ndPayload() {
        int o = __offset(MNO.EVR_ESN);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long evsPayloadExt() {
        int o = __offset(MNO.TPG_SG);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String evsBitRateSendExt() {
        int o = __offset(MNO.WOM_CHILE);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBitRateSendExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.WOM_CHILE, 1);
    }

    public String evsBitRateReceiveExt() {
        int o = __offset(MNO.MTN_IRAN);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBitRateReceiveExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.MTN_IRAN, 1);
    }

    public String evsBandwidthSendExt() {
        int o = __offset(MNO.CLARO_URUGUAY);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBandwidthSendExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.CLARO_URUGUAY, 1);
    }

    public String evsBandwidthReceiveExt() {
        int o = __offset(MNO.MTN_GHANA);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsBandwidthReceiveExtAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.MTN_GHANA, 1);
    }

    public String evsLimitedCodec() {
        int o = __offset(MNO.TELEFONICA_SPAIN);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer evsLimitedCodecAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TELEFONICA_SPAIN, 1);
    }

    public static int createMediaConfig(FlatBufferBuilder builder, long audio_port, long audio_dscp, boolean is_amr_octec_align, int audio_codecOffset, long dtmf_mode, int audio_asOffset, int audio_rsOffset, int audio_rrOffset, int amr_mode_change_capabilityOffset, long amr_max_red, int amr_modeOffset, int amr_wb_modeOffset, long amr_payload, long amrbe_payload, long amr_wb_payload, long amrbe_wb_payload, long dtmf_payload, long dtmf_wb_payload, long p_time, long max_time, int video_codecOffset, long video_port, long frame_rate, int display_formatOffset, int display_format_hevcOffset, int packetization_modeOffset, long h265_hd720p_payload, long h265_hd720pl_payload, long h265_vga_payload, long h265_vgal_payload, long h265_qvga_payload, long h265_qvgal_payload, long h264_720p_payload, long h264_720pl_payload, long h264_vga_payload, long h264_vgal_payload, long h264_qvga_payload, long h264_qvgal_payload, long h264_cif_payload, long h264_cifl_payload, long h263_qcif_payload, long video_as, long video_rs, long video_rr, long text_as, long text_rs, long text_rr, long text_port, int local_send_strengthtag, int local_received_strengthtag, int remote_send_strengthtag, int remote_received_strengthtag, boolean audio_avpf, boolean audio_srtp, boolean video_avpf, boolean video_srtp, boolean text_avpf, boolean text_srtp, boolean video_capabilities, boolean enable_scr, long rtp_timeout, long rtcp_timeout, boolean audio_rtcp_xr, boolean video_rtcp_xr, boolean enable_evs_codec, int evs_discontinuous_transmissionOffset, int evs_dtx_recvOffset, int evs_header_fullOffset, int evs_mode_switchOffset, int evs_channel_sendOffset, int evs_channel_recvOffset, int evs_channel_aware_receiveOffset, int evs_codec_mode_requestOffset, int evs_bit_rate_sendOffset, int evs_bit_rate_receiveOffset, int evs_bandwidth_sendOffset, int evs_bandwidth_receiveOffset, long evs_payload, int evs_default_bandwidthOffset, int evs_default_bitrateOffset, boolean enable_rtcp_on_active_call, long amr_open_payload, boolean enable_av_sync, boolean ignore_rtcp_timeout_on_hold_call, long amrbe_max_red, long amr_wb_max_red, long amrbe_wb_max_red, long evs_max_red, long evs_2nd_payload, long evs_payload_ext, int evs_bit_rate_send_extOffset, int evs_bit_rate_receive_extOffset, int evs_bandwidth_send_extOffset, int evs_bandwidth_receive_extOffset, int evs_limited_codecOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(95);
        addEvsLimitedCodec(flatBufferBuilder, evs_limited_codecOffset);
        addEvsBandwidthReceiveExt(flatBufferBuilder, evs_bandwidth_receive_extOffset);
        addEvsBandwidthSendExt(flatBufferBuilder, evs_bandwidth_send_extOffset);
        addEvsBitRateReceiveExt(flatBufferBuilder, evs_bit_rate_receive_extOffset);
        addEvsBitRateSendExt(flatBufferBuilder, evs_bit_rate_send_extOffset);
        addEvsPayloadExt(flatBufferBuilder, evs_payload_ext);
        addEvs2ndPayload(flatBufferBuilder, evs_2nd_payload);
        addEvsMaxRed(flatBufferBuilder, evs_max_red);
        addAmrbeWbMaxRed(flatBufferBuilder, amrbe_wb_max_red);
        addAmrWbMaxRed(flatBufferBuilder, amr_wb_max_red);
        addAmrbeMaxRed(flatBufferBuilder, amrbe_max_red);
        addAmrOpenPayload(flatBufferBuilder, amr_open_payload);
        addEvsDefaultBitrate(flatBufferBuilder, evs_default_bitrateOffset);
        addEvsDefaultBandwidth(flatBufferBuilder, evs_default_bandwidthOffset);
        addEvsPayload(flatBufferBuilder, evs_payload);
        addEvsBandwidthReceive(flatBufferBuilder, evs_bandwidth_receiveOffset);
        addEvsBandwidthSend(flatBufferBuilder, evs_bandwidth_sendOffset);
        addEvsBitRateReceive(flatBufferBuilder, evs_bit_rate_receiveOffset);
        addEvsBitRateSend(flatBufferBuilder, evs_bit_rate_sendOffset);
        addEvsCodecModeRequest(flatBufferBuilder, evs_codec_mode_requestOffset);
        addEvsChannelAwareReceive(flatBufferBuilder, evs_channel_aware_receiveOffset);
        addEvsChannelRecv(flatBufferBuilder, evs_channel_recvOffset);
        addEvsChannelSend(flatBufferBuilder, evs_channel_sendOffset);
        addEvsModeSwitch(flatBufferBuilder, evs_mode_switchOffset);
        addEvsHeaderFull(flatBufferBuilder, evs_header_fullOffset);
        addEvsDtxRecv(flatBufferBuilder, evs_dtx_recvOffset);
        addEvsDiscontinuousTransmission(flatBufferBuilder, evs_discontinuous_transmissionOffset);
        addRtcpTimeout(flatBufferBuilder, rtcp_timeout);
        addRtpTimeout(flatBufferBuilder, rtp_timeout);
        addRemoteReceivedStrengthtag(flatBufferBuilder, remote_received_strengthtag);
        addRemoteSendStrengthtag(flatBufferBuilder, remote_send_strengthtag);
        addLocalReceivedStrengthtag(flatBufferBuilder, local_received_strengthtag);
        addLocalSendStrengthtag(flatBufferBuilder, local_send_strengthtag);
        addTextPort(flatBufferBuilder, text_port);
        addTextRr(flatBufferBuilder, text_rr);
        addTextRs(flatBufferBuilder, text_rs);
        addTextAs(flatBufferBuilder, text_as);
        addVideoRr(flatBufferBuilder, video_rr);
        addVideoRs(flatBufferBuilder, video_rs);
        addVideoAs(flatBufferBuilder, video_as);
        addH263QcifPayload(flatBufferBuilder, h263_qcif_payload);
        addH264CiflPayload(flatBufferBuilder, h264_cifl_payload);
        addH264CifPayload(flatBufferBuilder, h264_cif_payload);
        addH264QvgalPayload(flatBufferBuilder, h264_qvgal_payload);
        addH264QvgaPayload(flatBufferBuilder, h264_qvga_payload);
        addH264VgalPayload(flatBufferBuilder, h264_vgal_payload);
        addH264VgaPayload(flatBufferBuilder, h264_vga_payload);
        addH264720plPayload(flatBufferBuilder, h264_720pl_payload);
        addH264720pPayload(flatBufferBuilder, h264_720p_payload);
        addH265QvgalPayload(flatBufferBuilder, h265_qvgal_payload);
        addH265QvgaPayload(flatBufferBuilder, h265_qvga_payload);
        addH265VgalPayload(flatBufferBuilder, h265_vgal_payload);
        addH265VgaPayload(flatBufferBuilder, h265_vga_payload);
        addH265Hd720plPayload(flatBufferBuilder, h265_hd720pl_payload);
        addH265Hd720pPayload(flatBufferBuilder, h265_hd720p_payload);
        addPacketizationMode(flatBufferBuilder, packetization_modeOffset);
        addDisplayFormatHevc(flatBufferBuilder, display_format_hevcOffset);
        addDisplayFormat(flatBufferBuilder, display_formatOffset);
        addFrameRate(flatBufferBuilder, frame_rate);
        addVideoPort(flatBufferBuilder, video_port);
        addVideoCodec(flatBufferBuilder, video_codecOffset);
        addMaxTime(flatBufferBuilder, max_time);
        addPTime(flatBufferBuilder, p_time);
        addDtmfWbPayload(flatBufferBuilder, dtmf_wb_payload);
        addDtmfPayload(flatBufferBuilder, dtmf_payload);
        addAmrbeWbPayload(flatBufferBuilder, amrbe_wb_payload);
        addAmrWbPayload(flatBufferBuilder, amr_wb_payload);
        addAmrbePayload(flatBufferBuilder, amrbe_payload);
        addAmrPayload(flatBufferBuilder, amr_payload);
        addAmrWbMode(flatBufferBuilder, amr_wb_modeOffset);
        addAmrMode(flatBufferBuilder, amr_modeOffset);
        addAmrMaxRed(flatBufferBuilder, amr_max_red);
        addAmrModeChangeCapability(flatBufferBuilder, amr_mode_change_capabilityOffset);
        addAudioRr(flatBufferBuilder, audio_rrOffset);
        addAudioRs(flatBufferBuilder, audio_rsOffset);
        addAudioAs(flatBufferBuilder, audio_asOffset);
        addDtmfMode(flatBufferBuilder, dtmf_mode);
        addAudioCodec(flatBufferBuilder, audio_codecOffset);
        addAudioDscp(flatBufferBuilder, audio_dscp);
        addAudioPort(builder, audio_port);
        addIgnoreRtcpTimeoutOnHoldCall(flatBufferBuilder, ignore_rtcp_timeout_on_hold_call);
        addEnableAvSync(flatBufferBuilder, enable_av_sync);
        addEnableRtcpOnActiveCall(flatBufferBuilder, enable_rtcp_on_active_call);
        addEnableEvsCodec(flatBufferBuilder, enable_evs_codec);
        addVideoRtcpXr(flatBufferBuilder, video_rtcp_xr);
        addAudioRtcpXr(flatBufferBuilder, audio_rtcp_xr);
        addEnableScr(flatBufferBuilder, enable_scr);
        addVideoCapabilities(flatBufferBuilder, video_capabilities);
        addTextSrtp(flatBufferBuilder, text_srtp);
        addTextAvpf(flatBufferBuilder, text_avpf);
        addVideoSrtp(flatBufferBuilder, video_srtp);
        addVideoAvpf(flatBufferBuilder, video_avpf);
        addAudioSrtp(flatBufferBuilder, audio_srtp);
        addAudioAvpf(flatBufferBuilder, audio_avpf);
        addIsAmrOctecAlign(flatBufferBuilder, is_amr_octec_align);
        return endMediaConfig(builder);
    }

    public static void startMediaConfig(FlatBufferBuilder builder) {
        builder.startObject(95);
    }

    public static void addAudioPort(FlatBufferBuilder builder, long audioPort) {
        builder.addInt(0, (int) audioPort, 0);
    }

    public static void addAudioDscp(FlatBufferBuilder builder, long audioDscp) {
        builder.addInt(1, (int) audioDscp, 0);
    }

    public static void addIsAmrOctecAlign(FlatBufferBuilder builder, boolean isAmrOctecAlign) {
        builder.addBoolean(2, isAmrOctecAlign, false);
    }

    public static void addAudioCodec(FlatBufferBuilder builder, int audioCodecOffset) {
        builder.addOffset(3, audioCodecOffset, 0);
    }

    public static void addDtmfMode(FlatBufferBuilder builder, long dtmfMode) {
        builder.addInt(4, (int) dtmfMode, 0);
    }

    public static void addAudioAs(FlatBufferBuilder builder, int audioAsOffset) {
        builder.addOffset(5, audioAsOffset, 0);
    }

    public static void addAudioRs(FlatBufferBuilder builder, int audioRsOffset) {
        builder.addOffset(6, audioRsOffset, 0);
    }

    public static void addAudioRr(FlatBufferBuilder builder, int audioRrOffset) {
        builder.addOffset(7, audioRrOffset, 0);
    }

    public static void addAmrModeChangeCapability(FlatBufferBuilder builder, int amrModeChangeCapabilityOffset) {
        builder.addOffset(8, amrModeChangeCapabilityOffset, 0);
    }

    public static void addAmrMaxRed(FlatBufferBuilder builder, long amrMaxRed) {
        builder.addInt(9, (int) amrMaxRed, 0);
    }

    public static void addAmrMode(FlatBufferBuilder builder, int amrModeOffset) {
        builder.addOffset(10, amrModeOffset, 0);
    }

    public static void addAmrWbMode(FlatBufferBuilder builder, int amrWbModeOffset) {
        builder.addOffset(11, amrWbModeOffset, 0);
    }

    public static void addAmrPayload(FlatBufferBuilder builder, long amrPayload) {
        builder.addInt(12, (int) amrPayload, 0);
    }

    public static void addAmrbePayload(FlatBufferBuilder builder, long amrbePayload) {
        builder.addInt(13, (int) amrbePayload, 0);
    }

    public static void addAmrWbPayload(FlatBufferBuilder builder, long amrWbPayload) {
        builder.addInt(14, (int) amrWbPayload, 0);
    }

    public static void addAmrbeWbPayload(FlatBufferBuilder builder, long amrbeWbPayload) {
        builder.addInt(15, (int) amrbeWbPayload, 0);
    }

    public static void addDtmfPayload(FlatBufferBuilder builder, long dtmfPayload) {
        builder.addInt(16, (int) dtmfPayload, 0);
    }

    public static void addDtmfWbPayload(FlatBufferBuilder builder, long dtmfWbPayload) {
        builder.addInt(17, (int) dtmfWbPayload, 0);
    }

    public static void addPTime(FlatBufferBuilder builder, long pTime) {
        builder.addInt(18, (int) pTime, 0);
    }

    public static void addMaxTime(FlatBufferBuilder builder, long maxTime) {
        builder.addInt(19, (int) maxTime, 0);
    }

    public static void addVideoCodec(FlatBufferBuilder builder, int videoCodecOffset) {
        builder.addOffset(20, videoCodecOffset, 0);
    }

    public static void addVideoPort(FlatBufferBuilder builder, long videoPort) {
        builder.addInt(21, (int) videoPort, 0);
    }

    public static void addFrameRate(FlatBufferBuilder builder, long frameRate) {
        builder.addInt(22, (int) frameRate, 0);
    }

    public static void addDisplayFormat(FlatBufferBuilder builder, int displayFormatOffset) {
        builder.addOffset(23, displayFormatOffset, 0);
    }

    public static void addDisplayFormatHevc(FlatBufferBuilder builder, int displayFormatHevcOffset) {
        builder.addOffset(24, displayFormatHevcOffset, 0);
    }

    public static void addPacketizationMode(FlatBufferBuilder builder, int packetizationModeOffset) {
        builder.addOffset(25, packetizationModeOffset, 0);
    }

    public static void addH265Hd720pPayload(FlatBufferBuilder builder, long h265Hd720pPayload) {
        builder.addInt(26, (int) h265Hd720pPayload, 0);
    }

    public static void addH265Hd720plPayload(FlatBufferBuilder builder, long h265Hd720plPayload) {
        builder.addInt(27, (int) h265Hd720plPayload, 0);
    }

    public static void addH265VgaPayload(FlatBufferBuilder builder, long h265VgaPayload) {
        builder.addInt(28, (int) h265VgaPayload, 0);
    }

    public static void addH265VgalPayload(FlatBufferBuilder builder, long h265VgalPayload) {
        builder.addInt(29, (int) h265VgalPayload, 0);
    }

    public static void addH265QvgaPayload(FlatBufferBuilder builder, long h265QvgaPayload) {
        builder.addInt(30, (int) h265QvgaPayload, 0);
    }

    public static void addH265QvgalPayload(FlatBufferBuilder builder, long h265QvgalPayload) {
        builder.addInt(31, (int) h265QvgalPayload, 0);
    }

    public static void addH264720pPayload(FlatBufferBuilder builder, long h264720pPayload) {
        builder.addInt(32, (int) h264720pPayload, 0);
    }

    public static void addH264720plPayload(FlatBufferBuilder builder, long h264720plPayload) {
        builder.addInt(33, (int) h264720plPayload, 0);
    }

    public static void addH264VgaPayload(FlatBufferBuilder builder, long h264VgaPayload) {
        builder.addInt(34, (int) h264VgaPayload, 0);
    }

    public static void addH264VgalPayload(FlatBufferBuilder builder, long h264VgalPayload) {
        builder.addInt(35, (int) h264VgalPayload, 0);
    }

    public static void addH264QvgaPayload(FlatBufferBuilder builder, long h264QvgaPayload) {
        builder.addInt(36, (int) h264QvgaPayload, 0);
    }

    public static void addH264QvgalPayload(FlatBufferBuilder builder, long h264QvgalPayload) {
        builder.addInt(37, (int) h264QvgalPayload, 0);
    }

    public static void addH264CifPayload(FlatBufferBuilder builder, long h264CifPayload) {
        builder.addInt(38, (int) h264CifPayload, 0);
    }

    public static void addH264CiflPayload(FlatBufferBuilder builder, long h264CiflPayload) {
        builder.addInt(39, (int) h264CiflPayload, 0);
    }

    public static void addH263QcifPayload(FlatBufferBuilder builder, long h263QcifPayload) {
        builder.addInt(40, (int) h263QcifPayload, 0);
    }

    public static void addVideoAs(FlatBufferBuilder builder, long videoAs) {
        builder.addInt(41, (int) videoAs, 0);
    }

    public static void addVideoRs(FlatBufferBuilder builder, long videoRs) {
        builder.addInt(42, (int) videoRs, 0);
    }

    public static void addVideoRr(FlatBufferBuilder builder, long videoRr) {
        builder.addInt(43, (int) videoRr, 0);
    }

    public static void addTextAs(FlatBufferBuilder builder, long textAs) {
        builder.addInt(44, (int) textAs, 0);
    }

    public static void addTextRs(FlatBufferBuilder builder, long textRs) {
        builder.addInt(45, (int) textRs, 0);
    }

    public static void addTextRr(FlatBufferBuilder builder, long textRr) {
        builder.addInt(46, (int) textRr, 0);
    }

    public static void addTextPort(FlatBufferBuilder builder, long textPort) {
        builder.addInt(47, (int) textPort, 0);
    }

    public static void addLocalSendStrengthtag(FlatBufferBuilder builder, int localSendStrengthtag) {
        builder.addInt(48, localSendStrengthtag, 0);
    }

    public static void addLocalReceivedStrengthtag(FlatBufferBuilder builder, int localReceivedStrengthtag) {
        builder.addInt(49, localReceivedStrengthtag, 0);
    }

    public static void addRemoteSendStrengthtag(FlatBufferBuilder builder, int remoteSendStrengthtag) {
        builder.addInt(50, remoteSendStrengthtag, 0);
    }

    public static void addRemoteReceivedStrengthtag(FlatBufferBuilder builder, int remoteReceivedStrengthtag) {
        builder.addInt(51, remoteReceivedStrengthtag, 0);
    }

    public static void addAudioAvpf(FlatBufferBuilder builder, boolean audioAvpf) {
        builder.addBoolean(52, audioAvpf, false);
    }

    public static void addAudioSrtp(FlatBufferBuilder builder, boolean audioSrtp) {
        builder.addBoolean(53, audioSrtp, false);
    }

    public static void addVideoAvpf(FlatBufferBuilder builder, boolean videoAvpf) {
        builder.addBoolean(54, videoAvpf, false);
    }

    public static void addVideoSrtp(FlatBufferBuilder builder, boolean videoSrtp) {
        builder.addBoolean(55, videoSrtp, false);
    }

    public static void addTextAvpf(FlatBufferBuilder builder, boolean textAvpf) {
        builder.addBoolean(56, textAvpf, false);
    }

    public static void addTextSrtp(FlatBufferBuilder builder, boolean textSrtp) {
        builder.addBoolean(57, textSrtp, false);
    }

    public static void addVideoCapabilities(FlatBufferBuilder builder, boolean videoCapabilities) {
        builder.addBoolean(58, videoCapabilities, false);
    }

    public static void addEnableScr(FlatBufferBuilder builder, boolean enableScr) {
        builder.addBoolean(59, enableScr, false);
    }

    public static void addRtpTimeout(FlatBufferBuilder builder, long rtpTimeout) {
        builder.addInt(60, (int) rtpTimeout, 0);
    }

    public static void addRtcpTimeout(FlatBufferBuilder builder, long rtcpTimeout) {
        builder.addInt(61, (int) rtcpTimeout, 0);
    }

    public static void addAudioRtcpXr(FlatBufferBuilder builder, boolean audioRtcpXr) {
        builder.addBoolean(62, audioRtcpXr, false);
    }

    public static void addVideoRtcpXr(FlatBufferBuilder builder, boolean videoRtcpXr) {
        builder.addBoolean(63, videoRtcpXr, false);
    }

    public static void addEnableEvsCodec(FlatBufferBuilder builder, boolean enableEvsCodec) {
        builder.addBoolean(64, enableEvsCodec, false);
    }

    public static void addEvsDiscontinuousTransmission(FlatBufferBuilder builder, int evsDiscontinuousTransmissionOffset) {
        builder.addOffset(65, evsDiscontinuousTransmissionOffset, 0);
    }

    public static void addEvsDtxRecv(FlatBufferBuilder builder, int evsDtxRecvOffset) {
        builder.addOffset(66, evsDtxRecvOffset, 0);
    }

    public static void addEvsHeaderFull(FlatBufferBuilder builder, int evsHeaderFullOffset) {
        builder.addOffset(67, evsHeaderFullOffset, 0);
    }

    public static void addEvsModeSwitch(FlatBufferBuilder builder, int evsModeSwitchOffset) {
        builder.addOffset(68, evsModeSwitchOffset, 0);
    }

    public static void addEvsChannelSend(FlatBufferBuilder builder, int evsChannelSendOffset) {
        builder.addOffset(69, evsChannelSendOffset, 0);
    }

    public static void addEvsChannelRecv(FlatBufferBuilder builder, int evsChannelRecvOffset) {
        builder.addOffset(70, evsChannelRecvOffset, 0);
    }

    public static void addEvsChannelAwareReceive(FlatBufferBuilder builder, int evsChannelAwareReceiveOffset) {
        builder.addOffset(71, evsChannelAwareReceiveOffset, 0);
    }

    public static void addEvsCodecModeRequest(FlatBufferBuilder builder, int evsCodecModeRequestOffset) {
        builder.addOffset(72, evsCodecModeRequestOffset, 0);
    }

    public static void addEvsBitRateSend(FlatBufferBuilder builder, int evsBitRateSendOffset) {
        builder.addOffset(73, evsBitRateSendOffset, 0);
    }

    public static void addEvsBitRateReceive(FlatBufferBuilder builder, int evsBitRateReceiveOffset) {
        builder.addOffset(74, evsBitRateReceiveOffset, 0);
    }

    public static void addEvsBandwidthSend(FlatBufferBuilder builder, int evsBandwidthSendOffset) {
        builder.addOffset(75, evsBandwidthSendOffset, 0);
    }

    public static void addEvsBandwidthReceive(FlatBufferBuilder builder, int evsBandwidthReceiveOffset) {
        builder.addOffset(76, evsBandwidthReceiveOffset, 0);
    }

    public static void addEvsPayload(FlatBufferBuilder builder, long evsPayload) {
        builder.addInt(77, (int) evsPayload, 0);
    }

    public static void addEvsDefaultBandwidth(FlatBufferBuilder builder, int evsDefaultBandwidthOffset) {
        builder.addOffset(78, evsDefaultBandwidthOffset, 0);
    }

    public static void addEvsDefaultBitrate(FlatBufferBuilder builder, int evsDefaultBitrateOffset) {
        builder.addOffset(79, evsDefaultBitrateOffset, 0);
    }

    public static void addEnableRtcpOnActiveCall(FlatBufferBuilder builder, boolean enableRtcpOnActiveCall) {
        builder.addBoolean(80, enableRtcpOnActiveCall, false);
    }

    public static void addAmrOpenPayload(FlatBufferBuilder builder, long amrOpenPayload) {
        builder.addInt(81, (int) amrOpenPayload, 0);
    }

    public static void addEnableAvSync(FlatBufferBuilder builder, boolean enableAvSync) {
        builder.addBoolean(82, enableAvSync, false);
    }

    public static void addIgnoreRtcpTimeoutOnHoldCall(FlatBufferBuilder builder, boolean ignoreRtcpTimeoutOnHoldCall) {
        builder.addBoolean(83, ignoreRtcpTimeoutOnHoldCall, false);
    }

    public static void addAmrbeMaxRed(FlatBufferBuilder builder, long amrbeMaxRed) {
        builder.addInt(84, (int) amrbeMaxRed, 0);
    }

    public static void addAmrWbMaxRed(FlatBufferBuilder builder, long amrWbMaxRed) {
        builder.addInt(85, (int) amrWbMaxRed, 0);
    }

    public static void addAmrbeWbMaxRed(FlatBufferBuilder builder, long amrbeWbMaxRed) {
        builder.addInt(86, (int) amrbeWbMaxRed, 0);
    }

    public static void addEvsMaxRed(FlatBufferBuilder builder, long evsMaxRed) {
        builder.addInt(87, (int) evsMaxRed, 0);
    }

    public static void addEvs2ndPayload(FlatBufferBuilder builder, long evs2ndPayload) {
        builder.addInt(88, (int) evs2ndPayload, 0);
    }

    public static void addEvsPayloadExt(FlatBufferBuilder builder, long evsPayloadExt) {
        builder.addInt(89, (int) evsPayloadExt, 0);
    }

    public static void addEvsBitRateSendExt(FlatBufferBuilder builder, int evsBitRateSendExtOffset) {
        builder.addOffset(90, evsBitRateSendExtOffset, 0);
    }

    public static void addEvsBitRateReceiveExt(FlatBufferBuilder builder, int evsBitRateReceiveExtOffset) {
        builder.addOffset(91, evsBitRateReceiveExtOffset, 0);
    }

    public static void addEvsBandwidthSendExt(FlatBufferBuilder builder, int evsBandwidthSendExtOffset) {
        builder.addOffset(92, evsBandwidthSendExtOffset, 0);
    }

    public static void addEvsBandwidthReceiveExt(FlatBufferBuilder builder, int evsBandwidthReceiveExtOffset) {
        builder.addOffset(93, evsBandwidthReceiveExtOffset, 0);
    }

    public static void addEvsLimitedCodec(FlatBufferBuilder builder, int evsLimitedCodecOffset) {
        builder.addOffset(94, evsLimitedCodecOffset, 0);
    }

    public static int endMediaConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
