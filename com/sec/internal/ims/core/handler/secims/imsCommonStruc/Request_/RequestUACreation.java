package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_.MediaConfig;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.xbill.DNS.Type;

public final class RequestUACreation extends Table {
    public static RequestUACreation getRootAsRequestUACreation(ByteBuffer _bb) {
        return getRootAsRequestUACreation(_bb, new RequestUACreation());
    }

    public static RequestUACreation getRootAsRequestUACreation(ByteBuffer _bb, RequestUACreation obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestUACreation __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String interfaceNw() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer interfaceNwAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String pdn() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pdnAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String impu() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String impi() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer impiAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String domain() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer domainAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public boolean isSipOutbound() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long qParam() {
        int o = __offset(16);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long controlDscp() {
        int o = __offset(18);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String transType() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer transTypeAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean isEmergencySupport() {
        int o = __offset(22);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isIpsec() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String registerAlgo() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer registerAlgoAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String prefId() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer prefIdAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String remoteUriType() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer remoteUriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String authName() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer authNameAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String password() {
        int o = __offset(34);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer passwordAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public String encrAlg() {
        int o = __offset(36);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer encrAlgAsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String authAlg() {
        int o = __offset(38);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer authAlgAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String regdomain() {
        int o = __offset(40);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer regdomainAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public String realm() {
        int o = __offset(42);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer realmAsByteBuffer() {
        return __vector_as_bytebuffer(42, 1);
    }

    public boolean isSipCompactHeader() {
        int o = __offset(44);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isSigComp() {
        int o = __offset(46);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long secureClientPort() {
        int o = __offset(48);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long secureServerPort() {
        int o = __offset(50);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long selfPort() {
        int o = __offset(52);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSubscribeRegEvent() {
        int o = __offset(54);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String userAgent() {
        int o = __offset(56);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer userAgentAsByteBuffer() {
        return __vector_as_bytebuffer(56, 1);
    }

    public boolean isKeepAlive() {
        int o = __offset(58);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPrecondEnabled() {
        int o = __offset(60);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPrecondInitialSendrecv() {
        int o = __offset(62);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int sessionExpires() {
        int o = __offset(64);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return -1;
    }

    public String sessionRefresher() {
        int o = __offset(66);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sessionRefresherAsByteBuffer() {
        return __vector_as_bytebuffer(66, 1);
    }

    public int mno() {
        int o = __offset(68);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String displayName() {
        int o = __offset(70);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(70, 1);
    }

    public String uuid() {
        int o = __offset(72);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uuidAsByteBuffer() {
        return __vector_as_bytebuffer(72, 1);
    }

    public String contactDisplayName() {
        int o = __offset(74);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer contactDisplayNameAsByteBuffer() {
        return __vector_as_bytebuffer(74, 1);
    }

    public String instanceId() {
        int o = __offset(76);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer instanceIdAsByteBuffer() {
        return __vector_as_bytebuffer(76, 1);
    }

    public String imMsgTech() {
        int o = __offset(78);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer imMsgTechAsByteBuffer() {
        return __vector_as_bytebuffer(78, 1);
    }

    public long timer1() {
        int o = __offset(80);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timer2() {
        int o = __offset(82);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timer4() {
        int o = __offset(84);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerA() {
        int o = __offset(86);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerB() {
        int o = __offset(88);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerC() {
        int o = __offset(90);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerD() {
        int o = __offset(92);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerE() {
        int o = __offset(94);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerF() {
        int o = __offset(96);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerG() {
        int o = __offset(98);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerH() {
        int o = __offset(100);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerI() {
        int o = __offset(102);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerJ() {
        int o = __offset(104);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long timerK() {
        int o = __offset(106);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSoftphoneEnabled() {
        int o = __offset(108);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isCdmalessEnabled() {
        int o = __offset(110);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long mssSize() {
        int o = __offset(112);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long sipMobility() {
        int o = __offset(114);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long minse() {
        int o = __offset(116);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 90;
    }

    public long ringbackTimer() {
        int o = __offset(118);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long ringingTimer() {
        int o = __offset(120);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isEnableGruu() {
        int o = __offset(122);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isEnableSessionId() {
        int o = __offset(124);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long audioEngineType() {
        int o = __offset(126);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String curPani() {
        int o = __offset(128);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer curPaniAsByteBuffer() {
        return __vector_as_bytebuffer(128, 1);
    }

    public long netId() {
        int o = __offset(130);
        if (o != 0) {
            return this.bb.getLong(this.bb_pos + o);
        }
        return 0;
    }

    public boolean isVceConfigEnabled() {
        int o = __offset(132);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isGcfConfigEnabled() {
        int o = __offset(134);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String serviceList(int j) {
        int o = __offset(136);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int serviceListLength() {
        int o = __offset(136);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public int featureTagList(int j) {
        int o = __offset(138);
        if (o != 0) {
            return this.bb.getInt(__vector(o) + (j * 4));
        }
        return 0;
    }

    public int featureTagListLength() {
        int o = __offset(138);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ByteBuffer featureTagListAsByteBuffer() {
        return __vector_as_bytebuffer(138, 4);
    }

    public boolean isNsdsServiceEnabled() {
        int o = __offset(140);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long profileId() {
        int o = __offset(142);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean wifiPreConditionEnabled() {
        int o = __offset(144);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long timerTs() {
        int o = __offset(146);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isMsrpBearerUsed() {
        int o = __offset(148);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long subscriberTimer() {
        int o = __offset(150);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSubscribeReg() {
        int o = __offset(152);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean useKeepAlive() {
        int o = __offset(MNO.PERSONAL_ARGENTINA);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String msrpTransType() {
        int o = __offset(MNO.TANGO_LUXEMBOURG);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer msrpTransTypeAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TANGO_LUXEMBOURG, 1);
    }

    public String hostname() {
        int o = __offset(MNO.STC_KSA);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer hostnameAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.STC_KSA, 1);
    }

    public boolean isFullCodecOfferRequired() {
        int o = __offset(MNO.UMOBILE);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isRcsTelephonyFeatureTagRequired() {
        int o = __offset(MNO.TMOBILE_ROMANIA);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long scmVersion() {
        int o = __offset(MNO.CLARO_COLOMBIA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isXqEnabled() {
        int o = __offset(MNO.TELENOR_BG);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long textMode() {
        int o = __offset(MNO.TELIA_FI);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int rcsProfile() {
        int o = __offset(MNO.ALTAN_MEXICO);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public boolean needTransportInContact() {
        int o = __offset(MNO.MOVISTAR_PANAMA);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long rat() {
        int o = __offset(MNO.VODAFONE_ROMANIA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long dbrTimer() {
        int o = __offset(MNO.ORANGE_SENEGAL);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean isTcpGracefulShutdownEnabled() {
        int o = __offset(MNO.MAGTICOM_GE);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int tcpRstUacErrorcode() {
        int o = __offset(MNO.EVR_ESN);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int tcpRstUasErrorcode() {
        int o = __offset(MNO.TPG_SG);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String privacyHeaderRestricted() {
        int o = __offset(MNO.WOM_CHILE);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer privacyHeaderRestrictedAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.WOM_CHILE, 1);
    }

    public boolean usePemHeader() {
        int o = __offset(MNO.MTN_IRAN);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportEct() {
        int o = __offset(MNO.CLARO_URUGUAY);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long earlyMediaRtpTimeoutTimer() {
        int o = __offset(MNO.MTN_GHANA);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean addHistinfo() {
        int o = __offset(MNO.TELEFONICA_SPAIN);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long supportedGeolocationPhase() {
        int o = __offset(MNO.KOODO);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long phoneId() {
        int o = __offset(MNO.BATELCO_BAHRAIN);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long needPidfSipMsg() {
        int o = __offset(MNO.WINDTRE_IT);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean useProvisionalResponse100rel() {
        int o = __offset(200);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean use183OnProgressIncoming() {
        int o = __offset(202);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean useQ850causeOn480() {
        int o = __offset(204);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean support183ForIr92v9Precondition() {
        int o = __offset(206);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long configDualIms() {
        int o = __offset(208);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean supportImsNotAvailable() {
        int o = __offset(210);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportLtePreferred() {
        int o = __offset(212);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean useSubcontactWhenResub() {
        int o = __offset(214);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportUpgradePrecondition() {
        int o = __offset(216);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportReplaceMerge() {
        int o = __offset(218);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isServerHeaderEnabled() {
        int o = __offset(220);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportAccessType() {
        int o = __offset(222);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String lastPaniHeader() {
        int o = __offset(224);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer lastPaniHeaderAsByteBuffer() {
        return __vector_as_bytebuffer(224, 1);
    }

    public String selectTransportAfterTcpReset() {
        int o = __offset(226);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer selectTransportAfterTcpResetAsByteBuffer() {
        return __vector_as_bytebuffer(226, 1);
    }

    public long srvccVersion() {
        int o = __offset(228);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean supportSubscribeDialogEvent() {
        int o = __offset(230);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isSimMobility() {
        int o = __offset(232);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long cmcType() {
        int o = __offset(234);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String cmcRelayType() {
        int o = __offset(236);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcRelayTypeAsByteBuffer() {
        return __vector_as_bytebuffer(236, 1);
    }

    public long videoCrbtSupportType() {
        int o = __offset(Id.REQUEST_HANDLE_CMC_CSFB);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public boolean retryInviteOnTcpReset() {
        int o = __offset(Id.REQUEST_STOP_RECORD);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean enableVerstat() {
        int o = __offset(Id.REQUEST_START_CMC_RECORD);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int regRetryBaseTime() {
        int o = __offset(Id.REQUEST_UPDATE_CMC_EXT_CALL_COUNT);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int regRetryMaxTime() {
        int o = __offset(246);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public boolean supportDualRcs() {
        int o = __offset(248);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isPttSupported() {
        int o = __offset(Type.TSIG);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean tryReregisterFromKeepalive() {
        int o = __offset(252);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int sslType() {
        int o = __offset(254);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public boolean support199ProvisionalResponse() {
        int o = __offset(256);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String acb(int j) {
        int o = __offset(258);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int acbLength() {
        int o = __offset(258);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public boolean supportNetworkInitUssi() {
        int o = __offset(260);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean sendByeForUssi() {
        int o = __offset(262);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean supportRfc6337ForDelayedOffer() {
        int o = __offset(264);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public MediaConfig mediaConfig() {
        return mediaConfig(new MediaConfig());
    }

    public MediaConfig mediaConfig(MediaConfig obj) {
        int o = __offset(266);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public boolean ignoreDisplayName() {
        int o = __offset(268);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public long hashAlgoType() {
        int o = __offset(270);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public static void startRequestUACreation(FlatBufferBuilder builder) {
        builder.startObject(134);
    }

    public static void addInterfaceNw(FlatBufferBuilder builder, int interfaceNwOffset) {
        builder.addOffset(0, interfaceNwOffset, 0);
    }

    public static void addPdn(FlatBufferBuilder builder, int pdnOffset) {
        builder.addOffset(1, pdnOffset, 0);
    }

    public static void addImpu(FlatBufferBuilder builder, int impuOffset) {
        builder.addOffset(2, impuOffset, 0);
    }

    public static void addImpi(FlatBufferBuilder builder, int impiOffset) {
        builder.addOffset(3, impiOffset, 0);
    }

    public static void addDomain(FlatBufferBuilder builder, int domainOffset) {
        builder.addOffset(4, domainOffset, 0);
    }

    public static void addIsSipOutbound(FlatBufferBuilder builder, boolean isSipOutbound) {
        builder.addBoolean(5, isSipOutbound, false);
    }

    public static void addQParam(FlatBufferBuilder builder, long qParam) {
        builder.addInt(6, (int) qParam, 0);
    }

    public static void addControlDscp(FlatBufferBuilder builder, long controlDscp) {
        builder.addInt(7, (int) controlDscp, 0);
    }

    public static void addTransType(FlatBufferBuilder builder, int transTypeOffset) {
        builder.addOffset(8, transTypeOffset, 0);
    }

    public static void addIsEmergencySupport(FlatBufferBuilder builder, boolean isEmergencySupport) {
        builder.addBoolean(9, isEmergencySupport, false);
    }

    public static void addIsIpsec(FlatBufferBuilder builder, boolean isIpsec) {
        builder.addBoolean(10, isIpsec, false);
    }

    public static void addRegisterAlgo(FlatBufferBuilder builder, int registerAlgoOffset) {
        builder.addOffset(11, registerAlgoOffset, 0);
    }

    public static void addPrefId(FlatBufferBuilder builder, int prefIdOffset) {
        builder.addOffset(12, prefIdOffset, 0);
    }

    public static void addRemoteUriType(FlatBufferBuilder builder, int remoteUriTypeOffset) {
        builder.addOffset(13, remoteUriTypeOffset, 0);
    }

    public static void addAuthName(FlatBufferBuilder builder, int authNameOffset) {
        builder.addOffset(14, authNameOffset, 0);
    }

    public static void addPassword(FlatBufferBuilder builder, int passwordOffset) {
        builder.addOffset(15, passwordOffset, 0);
    }

    public static void addEncrAlg(FlatBufferBuilder builder, int encrAlgOffset) {
        builder.addOffset(16, encrAlgOffset, 0);
    }

    public static void addAuthAlg(FlatBufferBuilder builder, int authAlgOffset) {
        builder.addOffset(17, authAlgOffset, 0);
    }

    public static void addRegdomain(FlatBufferBuilder builder, int regdomainOffset) {
        builder.addOffset(18, regdomainOffset, 0);
    }

    public static void addRealm(FlatBufferBuilder builder, int realmOffset) {
        builder.addOffset(19, realmOffset, 0);
    }

    public static void addIsSipCompactHeader(FlatBufferBuilder builder, boolean isSipCompactHeader) {
        builder.addBoolean(20, isSipCompactHeader, false);
    }

    public static void addIsSigComp(FlatBufferBuilder builder, boolean isSigComp) {
        builder.addBoolean(21, isSigComp, false);
    }

    public static void addSecureClientPort(FlatBufferBuilder builder, long secureClientPort) {
        builder.addInt(22, (int) secureClientPort, 0);
    }

    public static void addSecureServerPort(FlatBufferBuilder builder, long secureServerPort) {
        builder.addInt(23, (int) secureServerPort, 0);
    }

    public static void addSelfPort(FlatBufferBuilder builder, long selfPort) {
        builder.addInt(24, (int) selfPort, 0);
    }

    public static void addIsSubscribeRegEvent(FlatBufferBuilder builder, boolean isSubscribeRegEvent) {
        builder.addBoolean(25, isSubscribeRegEvent, false);
    }

    public static void addUserAgent(FlatBufferBuilder builder, int userAgentOffset) {
        builder.addOffset(26, userAgentOffset, 0);
    }

    public static void addIsKeepAlive(FlatBufferBuilder builder, boolean isKeepAlive) {
        builder.addBoolean(27, isKeepAlive, false);
    }

    public static void addIsPrecondEnabled(FlatBufferBuilder builder, boolean isPrecondEnabled) {
        builder.addBoolean(28, isPrecondEnabled, false);
    }

    public static void addIsPrecondInitialSendrecv(FlatBufferBuilder builder, boolean isPrecondInitialSendrecv) {
        builder.addBoolean(29, isPrecondInitialSendrecv, false);
    }

    public static void addSessionExpires(FlatBufferBuilder builder, int sessionExpires) {
        builder.addInt(30, sessionExpires, -1);
    }

    public static void addSessionRefresher(FlatBufferBuilder builder, int sessionRefresherOffset) {
        builder.addOffset(31, sessionRefresherOffset, 0);
    }

    public static void addMno(FlatBufferBuilder builder, int mno) {
        builder.addInt(32, mno, 0);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(33, displayNameOffset, 0);
    }

    public static void addUuid(FlatBufferBuilder builder, int uuidOffset) {
        builder.addOffset(34, uuidOffset, 0);
    }

    public static void addContactDisplayName(FlatBufferBuilder builder, int contactDisplayNameOffset) {
        builder.addOffset(35, contactDisplayNameOffset, 0);
    }

    public static void addInstanceId(FlatBufferBuilder builder, int instanceIdOffset) {
        builder.addOffset(36, instanceIdOffset, 0);
    }

    public static void addImMsgTech(FlatBufferBuilder builder, int imMsgTechOffset) {
        builder.addOffset(37, imMsgTechOffset, 0);
    }

    public static void addTimer1(FlatBufferBuilder builder, long timer1) {
        builder.addInt(38, (int) timer1, 0);
    }

    public static void addTimer2(FlatBufferBuilder builder, long timer2) {
        builder.addInt(39, (int) timer2, 0);
    }

    public static void addTimer4(FlatBufferBuilder builder, long timer4) {
        builder.addInt(40, (int) timer4, 0);
    }

    public static void addTimerA(FlatBufferBuilder builder, long timerA) {
        builder.addInt(41, (int) timerA, 0);
    }

    public static void addTimerB(FlatBufferBuilder builder, long timerB) {
        builder.addInt(42, (int) timerB, 0);
    }

    public static void addTimerC(FlatBufferBuilder builder, long timerC) {
        builder.addInt(43, (int) timerC, 0);
    }

    public static void addTimerD(FlatBufferBuilder builder, long timerD) {
        builder.addInt(44, (int) timerD, 0);
    }

    public static void addTimerE(FlatBufferBuilder builder, long timerE) {
        builder.addInt(45, (int) timerE, 0);
    }

    public static void addTimerF(FlatBufferBuilder builder, long timerF) {
        builder.addInt(46, (int) timerF, 0);
    }

    public static void addTimerG(FlatBufferBuilder builder, long timerG) {
        builder.addInt(47, (int) timerG, 0);
    }

    public static void addTimerH(FlatBufferBuilder builder, long timerH) {
        builder.addInt(48, (int) timerH, 0);
    }

    public static void addTimerI(FlatBufferBuilder builder, long timerI) {
        builder.addInt(49, (int) timerI, 0);
    }

    public static void addTimerJ(FlatBufferBuilder builder, long timerJ) {
        builder.addInt(50, (int) timerJ, 0);
    }

    public static void addTimerK(FlatBufferBuilder builder, long timerK) {
        builder.addInt(51, (int) timerK, 0);
    }

    public static void addIsSoftphoneEnabled(FlatBufferBuilder builder, boolean isSoftphoneEnabled) {
        builder.addBoolean(52, isSoftphoneEnabled, false);
    }

    public static void addIsCdmalessEnabled(FlatBufferBuilder builder, boolean isCdmalessEnabled) {
        builder.addBoolean(53, isCdmalessEnabled, false);
    }

    public static void addMssSize(FlatBufferBuilder builder, long mssSize) {
        builder.addInt(54, (int) mssSize, 0);
    }

    public static void addSipMobility(FlatBufferBuilder builder, long sipMobility) {
        builder.addInt(55, (int) sipMobility, 0);
    }

    public static void addMinse(FlatBufferBuilder builder, long minse) {
        builder.addInt(56, (int) minse, 90);
    }

    public static void addRingbackTimer(FlatBufferBuilder builder, long ringbackTimer) {
        builder.addInt(57, (int) ringbackTimer, 0);
    }

    public static void addRingingTimer(FlatBufferBuilder builder, long ringingTimer) {
        builder.addInt(58, (int) ringingTimer, 0);
    }

    public static void addIsEnableGruu(FlatBufferBuilder builder, boolean isEnableGruu) {
        builder.addBoolean(59, isEnableGruu, false);
    }

    public static void addIsEnableSessionId(FlatBufferBuilder builder, boolean isEnableSessionId) {
        builder.addBoolean(60, isEnableSessionId, false);
    }

    public static void addAudioEngineType(FlatBufferBuilder builder, long audioEngineType) {
        builder.addInt(61, (int) audioEngineType, 0);
    }

    public static void addCurPani(FlatBufferBuilder builder, int curPaniOffset) {
        builder.addOffset(62, curPaniOffset, 0);
    }

    public static void addNetId(FlatBufferBuilder builder, long netId) {
        builder.addLong(63, netId, 0);
    }

    public static void addIsVceConfigEnabled(FlatBufferBuilder builder, boolean isVceConfigEnabled) {
        builder.addBoolean(64, isVceConfigEnabled, false);
    }

    public static void addIsGcfConfigEnabled(FlatBufferBuilder builder, boolean isGcfConfigEnabled) {
        builder.addBoolean(65, isGcfConfigEnabled, false);
    }

    public static void addServiceList(FlatBufferBuilder builder, int serviceListOffset) {
        builder.addOffset(66, serviceListOffset, 0);
    }

    public static int createServiceListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startServiceListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addFeatureTagList(FlatBufferBuilder builder, int featureTagListOffset) {
        builder.addOffset(67, featureTagListOffset, 0);
    }

    public static int createFeatureTagListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addInt(data[i]);
        }
        return builder.endVector();
    }

    public static void startFeatureTagListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIsNsdsServiceEnabled(FlatBufferBuilder builder, boolean isNsdsServiceEnabled) {
        builder.addBoolean(68, isNsdsServiceEnabled, false);
    }

    public static void addProfileId(FlatBufferBuilder builder, long profileId) {
        builder.addInt(69, (int) profileId, 0);
    }

    public static void addWifiPreConditionEnabled(FlatBufferBuilder builder, boolean wifiPreConditionEnabled) {
        builder.addBoolean(70, wifiPreConditionEnabled, false);
    }

    public static void addTimerTs(FlatBufferBuilder builder, long timerTs) {
        builder.addInt(71, (int) timerTs, 0);
    }

    public static void addIsMsrpBearerUsed(FlatBufferBuilder builder, boolean isMsrpBearerUsed) {
        builder.addBoolean(72, isMsrpBearerUsed, false);
    }

    public static void addSubscriberTimer(FlatBufferBuilder builder, long subscriberTimer) {
        builder.addInt(73, (int) subscriberTimer, 0);
    }

    public static void addIsSubscribeReg(FlatBufferBuilder builder, boolean isSubscribeReg) {
        builder.addBoolean(74, isSubscribeReg, false);
    }

    public static void addUseKeepAlive(FlatBufferBuilder builder, boolean useKeepAlive) {
        builder.addBoolean(75, useKeepAlive, false);
    }

    public static void addMsrpTransType(FlatBufferBuilder builder, int msrpTransTypeOffset) {
        builder.addOffset(76, msrpTransTypeOffset, 0);
    }

    public static void addHostname(FlatBufferBuilder builder, int hostnameOffset) {
        builder.addOffset(77, hostnameOffset, 0);
    }

    public static void addIsFullCodecOfferRequired(FlatBufferBuilder builder, boolean isFullCodecOfferRequired) {
        builder.addBoolean(78, isFullCodecOfferRequired, false);
    }

    public static void addIsRcsTelephonyFeatureTagRequired(FlatBufferBuilder builder, boolean isRcsTelephonyFeatureTagRequired) {
        builder.addBoolean(79, isRcsTelephonyFeatureTagRequired, false);
    }

    public static void addScmVersion(FlatBufferBuilder builder, long scmVersion) {
        builder.addInt(80, (int) scmVersion, 0);
    }

    public static void addIsXqEnabled(FlatBufferBuilder builder, boolean isXqEnabled) {
        builder.addBoolean(81, isXqEnabled, false);
    }

    public static void addTextMode(FlatBufferBuilder builder, long textMode) {
        builder.addInt(82, (int) textMode, 0);
    }

    public static void addRcsProfile(FlatBufferBuilder builder, int rcsProfile) {
        builder.addInt(83, rcsProfile, 0);
    }

    public static void addNeedTransportInContact(FlatBufferBuilder builder, boolean needTransportInContact) {
        builder.addBoolean(84, needTransportInContact, false);
    }

    public static void addRat(FlatBufferBuilder builder, long rat) {
        builder.addInt(85, (int) rat, 0);
    }

    public static void addDbrTimer(FlatBufferBuilder builder, long dbrTimer) {
        builder.addInt(86, (int) dbrTimer, 0);
    }

    public static void addIsTcpGracefulShutdownEnabled(FlatBufferBuilder builder, boolean isTcpGracefulShutdownEnabled) {
        builder.addBoolean(87, isTcpGracefulShutdownEnabled, false);
    }

    public static void addTcpRstUacErrorcode(FlatBufferBuilder builder, int tcpRstUacErrorcode) {
        builder.addInt(88, tcpRstUacErrorcode, 0);
    }

    public static void addTcpRstUasErrorcode(FlatBufferBuilder builder, int tcpRstUasErrorcode) {
        builder.addInt(89, tcpRstUasErrorcode, 0);
    }

    public static void addPrivacyHeaderRestricted(FlatBufferBuilder builder, int privacyHeaderRestrictedOffset) {
        builder.addOffset(90, privacyHeaderRestrictedOffset, 0);
    }

    public static void addUsePemHeader(FlatBufferBuilder builder, boolean usePemHeader) {
        builder.addBoolean(91, usePemHeader, false);
    }

    public static void addSupportEct(FlatBufferBuilder builder, boolean supportEct) {
        builder.addBoolean(92, supportEct, false);
    }

    public static void addEarlyMediaRtpTimeoutTimer(FlatBufferBuilder builder, long earlyMediaRtpTimeoutTimer) {
        builder.addInt(93, (int) earlyMediaRtpTimeoutTimer, 0);
    }

    public static void addAddHistinfo(FlatBufferBuilder builder, boolean addHistinfo) {
        builder.addBoolean(94, addHistinfo, false);
    }

    public static void addSupportedGeolocationPhase(FlatBufferBuilder builder, long supportedGeolocationPhase) {
        builder.addInt(95, (int) supportedGeolocationPhase, 0);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(96, (int) phoneId, 0);
    }

    public static void addNeedPidfSipMsg(FlatBufferBuilder builder, long needPidfSipMsg) {
        builder.addInt(97, (int) needPidfSipMsg, 0);
    }

    public static void addUseProvisionalResponse100rel(FlatBufferBuilder builder, boolean useProvisionalResponse100rel) {
        builder.addBoolean(98, useProvisionalResponse100rel, false);
    }

    public static void addUse183OnProgressIncoming(FlatBufferBuilder builder, boolean use183OnProgressIncoming) {
        builder.addBoolean(99, use183OnProgressIncoming, false);
    }

    public static void addUseQ850causeOn480(FlatBufferBuilder builder, boolean useQ850causeOn480) {
        builder.addBoolean(100, useQ850causeOn480, false);
    }

    public static void addSupport183ForIr92v9Precondition(FlatBufferBuilder builder, boolean support183ForIr92v9Precondition) {
        builder.addBoolean(101, support183ForIr92v9Precondition, false);
    }

    public static void addConfigDualIms(FlatBufferBuilder builder, long configDualIms) {
        builder.addInt(102, (int) configDualIms, 0);
    }

    public static void addSupportImsNotAvailable(FlatBufferBuilder builder, boolean supportImsNotAvailable) {
        builder.addBoolean(103, supportImsNotAvailable, false);
    }

    public static void addSupportLtePreferred(FlatBufferBuilder builder, boolean supportLtePreferred) {
        builder.addBoolean(104, supportLtePreferred, false);
    }

    public static void addUseSubcontactWhenResub(FlatBufferBuilder builder, boolean useSubcontactWhenResub) {
        builder.addBoolean(105, useSubcontactWhenResub, false);
    }

    public static void addSupportUpgradePrecondition(FlatBufferBuilder builder, boolean supportUpgradePrecondition) {
        builder.addBoolean(106, supportUpgradePrecondition, false);
    }

    public static void addSupportReplaceMerge(FlatBufferBuilder builder, boolean supportReplaceMerge) {
        builder.addBoolean(107, supportReplaceMerge, false);
    }

    public static void addIsServerHeaderEnabled(FlatBufferBuilder builder, boolean isServerHeaderEnabled) {
        builder.addBoolean(108, isServerHeaderEnabled, false);
    }

    public static void addSupportAccessType(FlatBufferBuilder builder, boolean supportAccessType) {
        builder.addBoolean(109, supportAccessType, false);
    }

    public static void addLastPaniHeader(FlatBufferBuilder builder, int lastPaniHeaderOffset) {
        builder.addOffset(110, lastPaniHeaderOffset, 0);
    }

    public static void addSelectTransportAfterTcpReset(FlatBufferBuilder builder, int selectTransportAfterTcpResetOffset) {
        builder.addOffset(111, selectTransportAfterTcpResetOffset, 0);
    }

    public static void addSrvccVersion(FlatBufferBuilder builder, long srvccVersion) {
        builder.addInt(112, (int) srvccVersion, 0);
    }

    public static void addSupportSubscribeDialogEvent(FlatBufferBuilder builder, boolean supportSubscribeDialogEvent) {
        builder.addBoolean(113, supportSubscribeDialogEvent, false);
    }

    public static void addIsSimMobility(FlatBufferBuilder builder, boolean isSimMobility) {
        builder.addBoolean(114, isSimMobility, false);
    }

    public static void addCmcType(FlatBufferBuilder builder, long cmcType) {
        builder.addInt(115, (int) cmcType, 0);
    }

    public static void addCmcRelayType(FlatBufferBuilder builder, int cmcRelayTypeOffset) {
        builder.addOffset(116, cmcRelayTypeOffset, 0);
    }

    public static void addVideoCrbtSupportType(FlatBufferBuilder builder, long videoCrbtSupportType) {
        builder.addInt(117, (int) videoCrbtSupportType, 0);
    }

    public static void addRetryInviteOnTcpReset(FlatBufferBuilder builder, boolean retryInviteOnTcpReset) {
        builder.addBoolean(118, retryInviteOnTcpReset, false);
    }

    public static void addEnableVerstat(FlatBufferBuilder builder, boolean enableVerstat) {
        builder.addBoolean(119, enableVerstat, false);
    }

    public static void addRegRetryBaseTime(FlatBufferBuilder builder, int regRetryBaseTime) {
        builder.addInt(120, regRetryBaseTime, 0);
    }

    public static void addRegRetryMaxTime(FlatBufferBuilder builder, int regRetryMaxTime) {
        builder.addInt(121, regRetryMaxTime, 0);
    }

    public static void addSupportDualRcs(FlatBufferBuilder builder, boolean supportDualRcs) {
        builder.addBoolean(122, supportDualRcs, false);
    }

    public static void addIsPttSupported(FlatBufferBuilder builder, boolean isPttSupported) {
        builder.addBoolean(123, isPttSupported, false);
    }

    public static void addTryReregisterFromKeepalive(FlatBufferBuilder builder, boolean tryReregisterFromKeepalive) {
        builder.addBoolean(124, tryReregisterFromKeepalive, false);
    }

    public static void addSslType(FlatBufferBuilder builder, int sslType) {
        builder.addInt(125, sslType, 0);
    }

    public static void addSupport199ProvisionalResponse(FlatBufferBuilder builder, boolean support199ProvisionalResponse) {
        builder.addBoolean(126, support199ProvisionalResponse, false);
    }

    public static void addAcb(FlatBufferBuilder builder, int acbOffset) {
        builder.addOffset(127, acbOffset, 0);
    }

    public static int createAcbVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startAcbVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addSupportNetworkInitUssi(FlatBufferBuilder builder, boolean supportNetworkInitUssi) {
        builder.addBoolean(128, supportNetworkInitUssi, false);
    }

    public static void addSendByeForUssi(FlatBufferBuilder builder, boolean sendByeForUssi) {
        builder.addBoolean(129, sendByeForUssi, false);
    }

    public static void addSupportRfc6337ForDelayedOffer(FlatBufferBuilder builder, boolean supportRfc6337ForDelayedOffer) {
        builder.addBoolean(130, supportRfc6337ForDelayedOffer, false);
    }

    public static void addMediaConfig(FlatBufferBuilder builder, int mediaConfigOffset) {
        builder.addOffset(131, mediaConfigOffset, 0);
    }

    public static void addIgnoreDisplayName(FlatBufferBuilder builder, boolean ignoreDisplayName) {
        builder.addBoolean(132, ignoreDisplayName, false);
    }

    public static void addHashAlgoType(FlatBufferBuilder builder, long hashAlgoType) {
        builder.addInt(133, (int) hashAlgoType, 0);
    }

    public static int endRequestUACreation(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 6);
        builder.required(o, 8);
        builder.required(o, 10);
        builder.required(o, 12);
        builder.required(o, 20);
        builder.required(o, 26);
        builder.required(o, 28);
        builder.required(o, 30);
        builder.required(o, 76);
        return o;
    }
}
