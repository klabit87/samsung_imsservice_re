package com.sec.internal.ims.core.handler.secims;

import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.options.Capabilities;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestDnsQuery;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestNetworkSuspended;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRegistration;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSetPreferredImpu;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSetTextMode;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_.MediaConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUADeletion;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateAkaResp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.CallConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RegiConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.ScreenConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.ServiceVersionConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateFeatureTag;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateSrvccVersion;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateVceConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateXqEnable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationRequestBuilder {
    private static final String LOG_TAG = StackRequestBuilderUtil.class.getSimpleName();

    static StackRequest makeConfigSrvcc(int phone_id, int version) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestUpdateSrvccVersion.startRequestUpdateSrvccVersion(builder);
        RequestUpdateSrvccVersion.addPhoneId(builder, (long) phone_id);
        RequestUpdateSrvccVersion.addVersion(builder, (long) version);
        int setSrvccVersion = RequestUpdateSrvccVersion.endRequestUpdateSrvccVersion(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 113);
        Request.addReqType(builder, (byte) 10);
        Request.addReq(builder, setSrvccVersion);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int addUaCreationReq(FlatBufferBuilder builder, UaProfile ua) {
        int hostname;
        int sessionRefresher;
        int encralg;
        int authalg;
        int password;
        int displayName;
        int uuid;
        int contactDisplayName;
        int realm;
        int imMsgTech;
        int cmcRelayType;
        int instanceId;
        int serviceListOffSet;
        int sessionRefresher2;
        int authalg2;
        FlatBufferBuilder flatBufferBuilder = builder;
        UaProfile uaProfile = ua;
        int mediaOffest = addMediaParameters(builder, ua);
        int interface_nw = flatBufferBuilder.createString((CharSequence) uaProfile.iface);
        int pdn = flatBufferBuilder.createString((CharSequence) uaProfile.pdn);
        int impi = flatBufferBuilder.createString((CharSequence) uaProfile.impi);
        int impu = flatBufferBuilder.createString((CharSequence) uaProfile.impu);
        int domain = flatBufferBuilder.createString((CharSequence) uaProfile.domain);
        int transtype = flatBufferBuilder.createString((CharSequence) uaProfile.transtype);
        int encr_alg = flatBufferBuilder.createString((CharSequence) "");
        int auth_alg = flatBufferBuilder.createString((CharSequence) "");
        int registeralgo = flatBufferBuilder.createString((CharSequence) uaProfile.registeralgo);
        int prefid = flatBufferBuilder.createString((CharSequence) uaProfile.impu);
        int uritype = flatBufferBuilder.createString((CharSequence) ua.getRemoteuritype().toString());
        int userAgent = flatBufferBuilder.createString((CharSequence) uaProfile.userAgent);
        int instanceId2 = flatBufferBuilder.createString((CharSequence) uaProfile.instanceId);
        int mediaOffest2 = mediaOffest;
        int curpani = flatBufferBuilder.createString((CharSequence) ua.getCurPani());
        int msrpTransType = flatBufferBuilder.createString((CharSequence) uaProfile.msrpTransType);
        int privacyHederRestricted = flatBufferBuilder.createString((CharSequence) ua.getPrivacyHeaderRestricted());
        int lastPaniHeader = flatBufferBuilder.createString((CharSequence) ua.getLastPaniHeader());
        int selectTransportAfterTcpReset = flatBufferBuilder.createString((CharSequence) ua.getSelectTransportAfterTcpReset());
        if (!TextUtils.isEmpty(uaProfile.hostname)) {
            hostname = flatBufferBuilder.createString((CharSequence) uaProfile.hostname);
        } else {
            hostname = -1;
        }
        int hostname2 = hostname;
        if (uaProfile.sessionRefresher != null) {
            sessionRefresher = flatBufferBuilder.createString((CharSequence) uaProfile.sessionRefresher);
        } else {
            sessionRefresher = -1;
        }
        int sessionRefresher3 = sessionRefresher;
        if (uaProfile.isipsec != 0) {
            authalg = flatBufferBuilder.createString((CharSequence) uaProfile.authalg);
            encralg = flatBufferBuilder.createString((CharSequence) uaProfile.encralg);
        } else {
            authalg = -1;
            encralg = -1;
        }
        int authalg3 = authalg;
        if (uaProfile.password != null) {
            password = flatBufferBuilder.createString((CharSequence) uaProfile.password);
        } else {
            password = -1;
        }
        int password2 = password;
        if (uaProfile.displayName != null) {
            displayName = flatBufferBuilder.createString((CharSequence) uaProfile.displayName);
        } else {
            displayName = -1;
        }
        int displayName2 = displayName;
        if (uaProfile.uuid != null) {
            uuid = flatBufferBuilder.createString((CharSequence) uaProfile.uuid);
        } else {
            uuid = -1;
        }
        int uuid2 = uuid;
        if (uaProfile.contactDisplayName != null) {
            contactDisplayName = flatBufferBuilder.createString((CharSequence) uaProfile.contactDisplayName);
        } else {
            contactDisplayName = -1;
        }
        int contactDisplayName2 = contactDisplayName;
        if (uaProfile.realm != null) {
            realm = flatBufferBuilder.createString((CharSequence) uaProfile.realm);
        } else {
            realm = -1;
        }
        int realm2 = realm;
        if (uaProfile.imMsgTech != null) {
            imMsgTech = flatBufferBuilder.createString((CharSequence) uaProfile.imMsgTech);
        } else {
            imMsgTech = -1;
        }
        int imMsgTech2 = imMsgTech;
        if (uaProfile.cmcRelayType != null) {
            cmcRelayType = flatBufferBuilder.createString((CharSequence) uaProfile.cmcRelayType);
        } else {
            cmcRelayType = -1;
        }
        int cmcRelayType2 = cmcRelayType;
        if (uaProfile.serviceList != null) {
            instanceId = instanceId2;
            serviceListOffSet = RequestUACreation.createServiceListVector(flatBufferBuilder, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder, uaProfile.serviceList, uaProfile.serviceList.size()));
        } else {
            instanceId = instanceId2;
            serviceListOffSet = -1;
        }
        int featureTagList = -1;
        if (ua.getOwnCapabilities() != null) {
            List<Integer> featureList = StackRequestBuilderUtil.translateFeatureTag(ua.getOwnCapabilities().getFeature());
            int[] featuretags = new int[featureList.size()];
            int i = 0;
            for (Integer featureTag : featureList) {
                featuretags[i] = featureTag.intValue();
                i++;
            }
            featureTagList = RequestUACreation.createFeatureTagListVector(flatBufferBuilder, featuretags);
        }
        int featureTagList2 = featureTagList;
        int serviceListOffSet2 = serviceListOffSet;
        int acbOffset = RequestUACreation.createAcbVector(flatBufferBuilder, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder, uaProfile.acb, uaProfile.acb.size()));
        RequestUACreation.startRequestUACreation(builder);
        RequestUACreation.addProfileId(flatBufferBuilder, (long) ua.getProfileId());
        RequestUACreation.addInterfaceNw(flatBufferBuilder, interface_nw);
        RequestUACreation.addNetId(flatBufferBuilder, ua.getNetId());
        RequestUACreation.addPdn(flatBufferBuilder, pdn);
        RequestUACreation.addImpi(flatBufferBuilder, impi);
        RequestUACreation.addImpu(flatBufferBuilder, impu);
        RequestUACreation.addDomain(flatBufferBuilder, domain);
        RequestUACreation.addIsSipOutbound(flatBufferBuilder, uaProfile.issipoutbound);
        RequestUACreation.addQParam(flatBufferBuilder, (long) uaProfile.qparam);
        RequestUACreation.addControlDscp(flatBufferBuilder, (long) ua.getControlDscp());
        RequestUACreation.addTransType(flatBufferBuilder, transtype);
        RequestUACreation.addIsEmergencySupport(flatBufferBuilder, uaProfile.isEmergencyProfile);
        RequestUACreation.addIsIpsec(flatBufferBuilder, uaProfile.isipsec);
        RequestUACreation.addEncrAlg(flatBufferBuilder, encr_alg);
        RequestUACreation.addAuthAlg(flatBufferBuilder, auth_alg);
        RequestUACreation.addRegisterAlgo(flatBufferBuilder, registeralgo);
        RequestUACreation.addPrefId(flatBufferBuilder, prefid);
        RequestUACreation.addRemoteUriType(flatBufferBuilder, uritype);
        RequestUACreation.addIsPrecondEnabled(flatBufferBuilder, uaProfile.isPrecondEnabled);
        RequestUACreation.addIsPrecondInitialSendrecv(flatBufferBuilder, uaProfile.isPrecondInitialSendrecv);
        RequestUACreation.addWifiPreConditionEnabled(flatBufferBuilder, uaProfile.mIsWifiPreConditionEnabled);
        RequestUACreation.addIsSipCompactHeader(flatBufferBuilder, uaProfile.mUseCompactHeader);
        RequestUACreation.addSessionExpires(flatBufferBuilder, uaProfile.sessionExpires);
        RequestUACreation.addMinse(flatBufferBuilder, (long) uaProfile.minSe);
        RequestUACreation.addUserAgent(flatBufferBuilder, userAgent);
        RequestUACreation.addInstanceId(flatBufferBuilder, instanceId);
        int i2 = interface_nw;
        RequestUACreation.addIsSoftphoneEnabled(flatBufferBuilder, uaProfile.isSoftphoneEnabled);
        RequestUACreation.addIsCdmalessEnabled(flatBufferBuilder, uaProfile.isCdmalessEnabled);
        int i3 = pdn;
        RequestUACreation.addRingbackTimer(flatBufferBuilder, (long) uaProfile.ringbackTimer);
        RequestUACreation.addRingingTimer(flatBufferBuilder, (long) uaProfile.ringingTimer);
        RequestUACreation.addTimer1(flatBufferBuilder, (long) ua.getTimer1());
        RequestUACreation.addTimer2(flatBufferBuilder, (long) ua.getTimer2());
        RequestUACreation.addTimer4(flatBufferBuilder, (long) ua.getTimer4());
        RequestUACreation.addTimerA(flatBufferBuilder, (long) ua.getTimerA());
        RequestUACreation.addTimerD(flatBufferBuilder, (long) ua.getTimerD());
        RequestUACreation.addTimerB(flatBufferBuilder, (long) ua.getTimerB());
        RequestUACreation.addTimerC(flatBufferBuilder, (long) ua.getTimerC());
        RequestUACreation.addTimerE(flatBufferBuilder, (long) ua.getTimerE());
        RequestUACreation.addTimerF(flatBufferBuilder, (long) ua.getTimerF());
        RequestUACreation.addTimerG(flatBufferBuilder, (long) ua.getTimerG());
        RequestUACreation.addTimerH(flatBufferBuilder, (long) ua.getTimerH());
        RequestUACreation.addTimerI(flatBufferBuilder, (long) ua.getTimerI());
        RequestUACreation.addTimerJ(flatBufferBuilder, (long) ua.getTimerJ());
        RequestUACreation.addTimerK(flatBufferBuilder, (long) ua.getTimerK());
        RequestUACreation.addTimerTs(flatBufferBuilder, (long) ua.getTimerTS());
        RequestUACreation.addMssSize(flatBufferBuilder, (long) ua.getMssSize());
        RequestUACreation.addSipMobility(flatBufferBuilder, (long) ua.getSipMobility());
        RequestUACreation.addIsEnableGruu(flatBufferBuilder, ua.getIsEnableGruu());
        RequestUACreation.addIsEnableSessionId(flatBufferBuilder, ua.getIsEnableSessionId());
        RequestUACreation.addAudioEngineType(flatBufferBuilder, (long) ua.getAudioEngineType());
        RequestUACreation.addTextMode(flatBufferBuilder, (long) ua.getTextMode());
        RequestUACreation.addCurPani(flatBufferBuilder, curpani);
        RequestUACreation.addIsVceConfigEnabled(flatBufferBuilder, uaProfile.isVceConfigEnabled);
        RequestUACreation.addIsGcfConfigEnabled(flatBufferBuilder, uaProfile.isGcfConfigEnabled);
        RequestUACreation.addIsNsdsServiceEnabled(flatBufferBuilder, uaProfile.isNsdsServiceEnabled);
        RequestUACreation.addIsMsrpBearerUsed(flatBufferBuilder, uaProfile.isMsrpBearerUsed);
        RequestUACreation.addSubscriberTimer(flatBufferBuilder, (long) uaProfile.subscriberTimer);
        RequestUACreation.addIsSubscribeReg(flatBufferBuilder, uaProfile.isSubscribeReg);
        RequestUACreation.addUseKeepAlive(flatBufferBuilder, uaProfile.useKeepAlive);
        RequestUACreation.addSelfPort(flatBufferBuilder, (long) uaProfile.selfPort);
        RequestUACreation.addScmVersion(flatBufferBuilder, (long) uaProfile.scmVersion);
        RequestUACreation.addMsrpTransType(flatBufferBuilder, msrpTransType);
        RequestUACreation.addIsFullCodecOfferRequired(flatBufferBuilder, uaProfile.isFullCodecOfferRequired);
        RequestUACreation.addIsRcsTelephonyFeatureTagRequired(flatBufferBuilder, uaProfile.isRcsTelephonyFeatureTagRequired);
        RequestUACreation.addIsXqEnabled(flatBufferBuilder, uaProfile.isXqEnabled);
        RequestUACreation.addRcsProfile(flatBufferBuilder, ua.getRcsProfile());
        RequestUACreation.addNeedTransportInContact(flatBufferBuilder, ua.getIsTransportNeeded());
        RequestUACreation.addRat(flatBufferBuilder, (long) ua.getRat());
        RequestUACreation.addDbrTimer(flatBufferBuilder, (long) ua.getDbrTimer());
        RequestUACreation.addIsTcpGracefulShutdownEnabled(flatBufferBuilder, ua.getIsTcpGracefulShutdownEnabled());
        RequestUACreation.addTcpRstUacErrorcode(flatBufferBuilder, ua.getTcpRstUacErrorcode());
        RequestUACreation.addTcpRstUasErrorcode(flatBufferBuilder, ua.getTcpRstUasErrorcode());
        RequestUACreation.addPrivacyHeaderRestricted(flatBufferBuilder, privacyHederRestricted);
        RequestUACreation.addUsePemHeader(flatBufferBuilder, ua.getUsePemHeader());
        RequestUACreation.addPhoneId(flatBufferBuilder, (long) ua.getPhoneId());
        RequestUACreation.addSupportEct(flatBufferBuilder, ua.getSupportEct());
        RequestUACreation.addEarlyMediaRtpTimeoutTimer(flatBufferBuilder, (long) ua.getEarlyMediaRtpTimeoutTimer());
        RequestUACreation.addAddHistinfo(flatBufferBuilder, ua.getAddHistinfo());
        RequestUACreation.addSupportedGeolocationPhase(flatBufferBuilder, (long) ua.getSupportedGeolocationPhase());
        RequestUACreation.addNeedPidfSipMsg(flatBufferBuilder, (long) ua.getNeedPidfSipMsg());
        RequestUACreation.addUseProvisionalResponse100rel(flatBufferBuilder, ua.getUseProvisionalResponse100rel());
        RequestUACreation.addUse183OnProgressIncoming(flatBufferBuilder, ua.getUse183OnProgressIncoming());
        RequestUACreation.addUseQ850causeOn480(flatBufferBuilder, ua.getUseQ850causeOn480());
        RequestUACreation.addSupport183ForIr92v9Precondition(flatBufferBuilder, ua.getSupport183ForIr92v9Precondition());
        RequestUACreation.addSupportImsNotAvailable(flatBufferBuilder, ua.getSupportImsNotAvailable());
        RequestUACreation.addSupportLtePreferred(flatBufferBuilder, ua.getSupportLtePreferred());
        RequestUACreation.addUseSubcontactWhenResub(flatBufferBuilder, ua.getUseSubcontactWhenResub());
        RequestUACreation.addSupportUpgradePrecondition(flatBufferBuilder, ua.getSupportUpgradePrecondition());
        RequestUACreation.addSupportReplaceMerge(flatBufferBuilder, ua.getSupportReplaceMerge());
        RequestUACreation.addIsServerHeaderEnabled(flatBufferBuilder, ua.isServerHeaderEnabled());
        RequestUACreation.addSupportAccessType(flatBufferBuilder, uaProfile.supportAccessType);
        RequestUACreation.addLastPaniHeader(flatBufferBuilder, lastPaniHeader);
        RequestUACreation.addSelectTransportAfterTcpReset(flatBufferBuilder, selectTransportAfterTcpReset);
        RequestUACreation.addSrvccVersion(flatBufferBuilder, (long) ua.getSrvccVersion());
        RequestUACreation.addSupportSubscribeDialogEvent(flatBufferBuilder, uaProfile.supportSubscribeDialogEvent);
        RequestUACreation.addIsSimMobility(flatBufferBuilder, uaProfile.isSimMobility);
        RequestUACreation.addCmcType(flatBufferBuilder, (long) ua.getCmcType());
        RequestUACreation.addVideoCrbtSupportType(flatBufferBuilder, (long) ua.getVideoCrbtSupportType());
        RequestUACreation.addRetryInviteOnTcpReset(flatBufferBuilder, ua.getRetryInviteOnTcpReset());
        RequestUACreation.addEnableVerstat(flatBufferBuilder, ua.getEnableVerstat());
        RequestUACreation.addRegRetryBaseTime(flatBufferBuilder, ua.getRegRetryBaseTime());
        RequestUACreation.addRegRetryMaxTime(flatBufferBuilder, ua.getRegRetryMaxTime());
        RequestUACreation.addSupportDualRcs(flatBufferBuilder, ua.getSupportDualRcs());
        RequestUACreation.addIsPttSupported(flatBufferBuilder, ua.getIsPttSupported());
        RequestUACreation.addTryReregisterFromKeepalive(flatBufferBuilder, ua.getTryReregisterFromKeepalive());
        RequestUACreation.addSslType(flatBufferBuilder, ua.getSslType());
        RequestUACreation.addHashAlgoType(flatBufferBuilder, (long) ua.getHashAlgoTypeType());
        RequestUACreation.addSupport199ProvisionalResponse(flatBufferBuilder, ua.getSupport199ProvisionalResponse());
        RequestUACreation.addSupportNetworkInitUssi(flatBufferBuilder, ua.getSupportNetworkInitUssi());
        RequestUACreation.addSendByeForUssi(flatBufferBuilder, ua.getSendByeForUssi());
        RequestUACreation.addSupportRfc6337ForDelayedOffer(flatBufferBuilder, ua.getSupportRfc6337ForDelayedOffer());
        int hostname3 = hostname2;
        if (hostname3 != -1) {
            RequestUACreation.addHostname(flatBufferBuilder, hostname3);
        }
        int i4 = hostname3;
        int sessionRefresher4 = sessionRefresher3;
        if (sessionRefresher4 != -1) {
            RequestUACreation.addSessionRefresher(flatBufferBuilder, sessionRefresher4);
        }
        if (uaProfile.isipsec) {
            RequestUACreation.addIsIpsec(flatBufferBuilder, uaProfile.isipsec);
            int authalg4 = authalg3;
            int authalg5 = sessionRefresher4;
            sessionRefresher2 = -1;
            if (authalg4 != -1) {
                RequestUACreation.addAuthAlg(flatBufferBuilder, authalg4);
            }
            int i5 = authalg4;
            authalg2 = encralg;
            if (authalg2 != -1) {
                RequestUACreation.addEncrAlg(flatBufferBuilder, authalg2);
            }
        } else {
            authalg2 = encralg;
            int authalg6 = sessionRefresher4;
            sessionRefresher2 = -1;
        }
        int encralg2 = authalg2;
        int password3 = password2;
        if (password3 != sessionRefresher2) {
            RequestUACreation.addPassword(flatBufferBuilder, password3);
        }
        if (uaProfile.mno != null) {
            int mno = StackRequestBuilderUtil.translateMno(uaProfile.mno);
            if (mno != 0) {
                String str = LOG_TAG;
                int i6 = password3;
                StringBuilder sb = new StringBuilder();
                int i7 = impi;
                sb.append("translateMno: ");
                sb.append(mno);
                Log.i(str, sb.toString());
                RequestUACreation.addMno(flatBufferBuilder, mno);
            } else {
                int i8 = impi;
            }
        } else {
            int i9 = impi;
        }
        int displayName3 = displayName2;
        if (displayName3 != -1) {
            RequestUACreation.addDisplayName(flatBufferBuilder, displayName3);
        }
        int uuid3 = uuid2;
        if (uuid3 != -1) {
            RequestUACreation.addUuid(flatBufferBuilder, uuid3);
        }
        int contactDisplayName3 = contactDisplayName2;
        if (contactDisplayName3 != -1) {
            RequestUACreation.addContactDisplayName(flatBufferBuilder, contactDisplayName3);
        }
        int i10 = displayName3;
        int realm3 = realm2;
        if (realm3 != -1) {
            RequestUACreation.addRealm(flatBufferBuilder, realm3);
        }
        int i11 = realm3;
        int imMsgTech3 = imMsgTech2;
        if (imMsgTech3 != -1) {
            RequestUACreation.addImMsgTech(flatBufferBuilder, imMsgTech3);
        }
        int i12 = imMsgTech3;
        int imMsgTech4 = cmcRelayType2;
        if (imMsgTech4 != -1) {
            RequestUACreation.addCmcRelayType(flatBufferBuilder, imMsgTech4);
        }
        int i13 = imMsgTech4;
        int serviceListOffSet3 = serviceListOffSet2;
        if (serviceListOffSet3 != -1) {
            RequestUACreation.addServiceList(flatBufferBuilder, serviceListOffSet3);
        }
        int i14 = serviceListOffSet3;
        int serviceListOffSet4 = featureTagList2;
        if (serviceListOffSet4 != -1) {
            RequestUACreation.addFeatureTagList(flatBufferBuilder, serviceListOffSet4);
        }
        int i15 = uuid3;
        RequestUACreation.addConfigDualIms(flatBufferBuilder, (long) StackRequestBuilderUtil.translateConfigDualIms());
        RequestUACreation.addMediaConfig(flatBufferBuilder, mediaOffest2);
        if (acbOffset != -1) {
            RequestUACreation.addAcb(flatBufferBuilder, acbOffset);
        }
        RequestUACreation.addIgnoreDisplayName(flatBufferBuilder, ua.isDisplayNameIgnored());
        return RequestUACreation.endRequestUACreation(builder);
    }

    private static int addMediaParameters(FlatBufferBuilder builder, UaProfile ua) {
        FlatBufferBuilder flatBufferBuilder = builder;
        CallProfile cp = ua.getCallProfile();
        int audioCodec = flatBufferBuilder.createString((CharSequence) cp.audioCodec);
        int amrMode = flatBufferBuilder.createString((CharSequence) cp.amrMode);
        int amrWbMode = flatBufferBuilder.createString((CharSequence) cp.amrWbMode);
        int audioAs = flatBufferBuilder.createString((CharSequence) Integer.toString(cp.audioAs));
        int audioRs = flatBufferBuilder.createString((CharSequence) Integer.toString(cp.audioRs));
        int audioRr = flatBufferBuilder.createString((CharSequence) Integer.toString(cp.audioRr));
        int videoCodec = flatBufferBuilder.createString((CharSequence) cp.videoCodec);
        int displayFormat = flatBufferBuilder.createString((CharSequence) cp.displayFormat);
        int packetizationMode = flatBufferBuilder.createString((CharSequence) cp.packetizationMode);
        int evsDiscontinuousTransmission = flatBufferBuilder.createString((CharSequence) cp.evsDiscontinuousTransmission);
        int evsDtxRecv = flatBufferBuilder.createString((CharSequence) cp.evsDtxRecv);
        int evsHeaderFull = flatBufferBuilder.createString((CharSequence) cp.evsHeaderFull);
        int evsModeSwitch = flatBufferBuilder.createString((CharSequence) cp.evsModeSwitch);
        int evsChannelSend = flatBufferBuilder.createString((CharSequence) cp.evsChannelSend);
        int evsChannelRecv = flatBufferBuilder.createString((CharSequence) cp.evsChannelRecv);
        int evsChannelAwareReceive = flatBufferBuilder.createString((CharSequence) cp.evsChannelAwareReceive);
        int evsCodecModeRequest = flatBufferBuilder.createString((CharSequence) cp.evsCodecModeRequest);
        int evsBitRateSend = flatBufferBuilder.createString((CharSequence) cp.evsBitRateSend);
        int evsBitRateReceive = flatBufferBuilder.createString((CharSequence) cp.evsBitRateReceive);
        int evsBandwidthSend = flatBufferBuilder.createString((CharSequence) cp.evsBandwidthSend);
        int evsBandwidthReceive = flatBufferBuilder.createString((CharSequence) cp.evsBandwidthReceive);
        int evsDefaultBandwidth = flatBufferBuilder.createString((CharSequence) cp.evsDefaultBandwidth);
        int evsDefaultBitrate = flatBufferBuilder.createString((CharSequence) cp.evsDefaultBitrate);
        int displayFormatHevc = flatBufferBuilder.createString((CharSequence) cp.displayFormatHevc);
        int evsModeSwitch2 = evsModeSwitch;
        int evsBitRateSendExt = flatBufferBuilder.createString((CharSequence) cp.evsBitRateSendExt);
        int evsBitRateReceiveExt = flatBufferBuilder.createString((CharSequence) cp.evsBitRateReceiveExt);
        int evsBandwidthSendExt = flatBufferBuilder.createString((CharSequence) cp.evsBandwidthSendExt);
        int evsBandwidthReceiveExt = flatBufferBuilder.createString((CharSequence) cp.evsBandwidthReceiveExt);
        int evsLimitedCodec = flatBufferBuilder.createString((CharSequence) cp.evsLimitedCodec);
        MediaConfig.startMediaConfig(builder);
        MediaConfig.addAudioCodec(flatBufferBuilder, audioCodec);
        int i = audioCodec;
        MediaConfig.addAudioPort(flatBufferBuilder, (long) cp.audioPort);
        MediaConfig.addAudioDscp(flatBufferBuilder, (long) cp.audioDscp);
        MediaConfig.addAmrPayload(flatBufferBuilder, (long) cp.amrOaPayloadType);
        MediaConfig.addAmrbePayload(flatBufferBuilder, (long) cp.amrPayloadType);
        MediaConfig.addAmrWbPayload(flatBufferBuilder, (long) cp.amrWbOaPayloadType);
        MediaConfig.addAmrbeWbPayload(flatBufferBuilder, (long) cp.amrWbPayloadType);
        MediaConfig.addAmrOpenPayload(flatBufferBuilder, (long) cp.amrOpenPayloadType);
        MediaConfig.addDtmfPayload(flatBufferBuilder, (long) cp.dtmfPayloadType);
        MediaConfig.addDtmfWbPayload(flatBufferBuilder, (long) cp.dtmfWbPayloadType);
        MediaConfig.addAmrMaxRed(flatBufferBuilder, (long) cp.amrOaMaxRed);
        MediaConfig.addAmrbeMaxRed(flatBufferBuilder, (long) cp.amrBeMaxRed);
        MediaConfig.addAmrWbMaxRed(flatBufferBuilder, (long) cp.amrOaWbMaxRed);
        MediaConfig.addAmrbeWbMaxRed(flatBufferBuilder, (long) cp.amrBeWbMaxRed);
        MediaConfig.addEvsMaxRed(flatBufferBuilder, (long) cp.evsMaxRed);
        MediaConfig.addAmrMode(flatBufferBuilder, amrMode);
        MediaConfig.addAmrWbMode(flatBufferBuilder, amrWbMode);
        MediaConfig.addAudioAs(flatBufferBuilder, audioAs);
        MediaConfig.addAudioRs(flatBufferBuilder, audioRs);
        MediaConfig.addAudioRr(flatBufferBuilder, audioRr);
        MediaConfig.addPTime(flatBufferBuilder, (long) cp.pTime);
        MediaConfig.addMaxTime(flatBufferBuilder, (long) cp.maxPTime);
        MediaConfig.addVideoCodec(flatBufferBuilder, videoCodec);
        MediaConfig.addVideoPort(flatBufferBuilder, (long) cp.videoPort);
        MediaConfig.addFrameRate(flatBufferBuilder, (long) cp.frameRate);
        MediaConfig.addDisplayFormat(flatBufferBuilder, displayFormat);
        MediaConfig.addDisplayFormatHevc(flatBufferBuilder, displayFormatHevc);
        MediaConfig.addPacketizationMode(flatBufferBuilder, packetizationMode);
        MediaConfig.addH265QvgaPayload(flatBufferBuilder, (long) cp.h265QvgaPayloadType);
        MediaConfig.addH265QvgalPayload(flatBufferBuilder, (long) cp.h265QvgaLPayloadType);
        MediaConfig.addH265VgaPayload(flatBufferBuilder, (long) cp.h265VgaPayloadType);
        MediaConfig.addH265VgalPayload(flatBufferBuilder, (long) cp.h265VgaLPayloadType);
        MediaConfig.addH265Hd720pPayload(flatBufferBuilder, (long) cp.h265Hd720pPayloadType);
        MediaConfig.addH265Hd720plPayload(flatBufferBuilder, (long) cp.h265Hd720pLPayloadType);
        MediaConfig.addH264720pPayload(flatBufferBuilder, (long) cp.h264720pPayloadType);
        MediaConfig.addH264720plPayload(flatBufferBuilder, (long) cp.h264720pLPayloadType);
        MediaConfig.addH264VgaPayload(flatBufferBuilder, (long) cp.h264VgaPayloadType);
        MediaConfig.addH264VgalPayload(flatBufferBuilder, (long) cp.h264VgaLPayloadType);
        MediaConfig.addH264QvgaPayload(flatBufferBuilder, (long) cp.h264QvgaPayloadType);
        MediaConfig.addH264QvgalPayload(flatBufferBuilder, (long) cp.h264QvgaLPayloadType);
        MediaConfig.addH264CifPayload(flatBufferBuilder, (long) cp.h264CifPayloadType);
        MediaConfig.addH264CiflPayload(flatBufferBuilder, (long) cp.h264CifLPayloadType);
        MediaConfig.addVideoAs(flatBufferBuilder, (long) cp.videoAs);
        MediaConfig.addVideoRs(flatBufferBuilder, (long) cp.videoRs);
        MediaConfig.addVideoRr(flatBufferBuilder, (long) cp.videoRr);
        MediaConfig.addTextAs(flatBufferBuilder, (long) cp.textAs);
        MediaConfig.addTextRs(flatBufferBuilder, (long) cp.textRs);
        MediaConfig.addTextRr(flatBufferBuilder, (long) cp.textRr);
        MediaConfig.addTextPort(flatBufferBuilder, (long) cp.textPort);
        MediaConfig.addAudioAvpf(flatBufferBuilder, cp.audioAvpf);
        MediaConfig.addAudioSrtp(flatBufferBuilder, cp.audioSrtp);
        MediaConfig.addVideoAvpf(flatBufferBuilder, cp.videoAvpf);
        MediaConfig.addVideoSrtp(flatBufferBuilder, cp.videoSrtp);
        MediaConfig.addTextAvpf(flatBufferBuilder, cp.textAvpf);
        MediaConfig.addTextSrtp(flatBufferBuilder, cp.textSrtp);
        MediaConfig.addVideoCapabilities(flatBufferBuilder, cp.videoCapabilities);
        MediaConfig.addEnableScr(flatBufferBuilder, cp.enableScr);
        MediaConfig.addRtpTimeout(flatBufferBuilder, (long) cp.rtpTimeout);
        MediaConfig.addRtcpTimeout(flatBufferBuilder, (long) cp.rtcpTimeout);
        MediaConfig.addH263QcifPayload(flatBufferBuilder, (long) cp.h263QcifPayloadType);
        MediaConfig.addAudioRtcpXr(flatBufferBuilder, cp.audioRtcpXr);
        MediaConfig.addVideoRtcpXr(flatBufferBuilder, cp.videoRtcpXr);
        MediaConfig.addDtmfMode(flatBufferBuilder, (long) cp.dtmfMode);
        MediaConfig.addEnableEvsCodec(flatBufferBuilder, cp.enableEvsCodec);
        MediaConfig.addEvsDiscontinuousTransmission(flatBufferBuilder, evsDiscontinuousTransmission);
        MediaConfig.addEvsDtxRecv(flatBufferBuilder, evsDtxRecv);
        MediaConfig.addEvsHeaderFull(flatBufferBuilder, evsHeaderFull);
        MediaConfig.addEvsModeSwitch(flatBufferBuilder, evsModeSwitch2);
        MediaConfig.addEvsChannelSend(flatBufferBuilder, evsChannelSend);
        MediaConfig.addEvsChannelRecv(flatBufferBuilder, evsChannelRecv);
        MediaConfig.addEvsChannelAwareReceive(flatBufferBuilder, evsChannelAwareReceive);
        MediaConfig.addEvsCodecModeRequest(flatBufferBuilder, evsCodecModeRequest);
        MediaConfig.addEvsBitRateSend(flatBufferBuilder, evsBitRateSend);
        MediaConfig.addEvsBitRateReceive(flatBufferBuilder, evsBitRateReceive);
        MediaConfig.addEvsBandwidthSend(flatBufferBuilder, evsBandwidthSend);
        MediaConfig.addEvsBandwidthReceive(flatBufferBuilder, evsBandwidthReceive);
        int i2 = amrMode;
        MediaConfig.addEvsPayload(flatBufferBuilder, (long) cp.evsPayload);
        MediaConfig.addEvs2ndPayload(flatBufferBuilder, (long) cp.evs2ndPayload);
        MediaConfig.addEvsDefaultBandwidth(flatBufferBuilder, evsDefaultBandwidth);
        MediaConfig.addEvsDefaultBitrate(flatBufferBuilder, evsDefaultBitrate);
        MediaConfig.addEnableRtcpOnActiveCall(flatBufferBuilder, cp.enableRtcpOnActiveCall);
        MediaConfig.addEnableAvSync(flatBufferBuilder, cp.enableAvSync);
        MediaConfig.addDisplayFormatHevc(flatBufferBuilder, displayFormatHevc);
        MediaConfig.addH264720pPayload(flatBufferBuilder, (long) cp.h264720pPayloadType);
        MediaConfig.addH264720plPayload(flatBufferBuilder, (long) cp.h264720pLPayloadType);
        MediaConfig.addIgnoreRtcpTimeoutOnHoldCall(flatBufferBuilder, cp.ignoreRtcpTimeoutOnHoldCall);
        MediaConfig.addEvsPayloadExt(flatBufferBuilder, (long) cp.evsPayloadExt);
        MediaConfig.addEvsBitRateSendExt(flatBufferBuilder, evsBitRateSendExt);
        MediaConfig.addEvsBitRateReceiveExt(flatBufferBuilder, evsBitRateReceiveExt);
        CallProfile callProfile = cp;
        MediaConfig.addEvsBandwidthSendExt(flatBufferBuilder, evsBandwidthSendExt);
        MediaConfig.addEvsBandwidthReceiveExt(flatBufferBuilder, evsBandwidthReceiveExt);
        MediaConfig.addEvsLimitedCodec(flatBufferBuilder, evsLimitedCodec);
        return MediaConfig.endMediaConfig(builder);
    }

    static StackRequest makeCreateUA(UaProfile ua) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uaCreation = addUaCreationReq(builder, ua);
        Request.startRequest(builder);
        Request.addReqid(builder, 102);
        Request.addReqType(builder, (byte) 2);
        Request.addReq(builder, uaCreation);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int deleteUaReq(FlatBufferBuilder builder, long handle) {
        RequestUADeletion.startRequestUADeletion(builder);
        RequestUADeletion.addHandle(builder, handle);
        return RequestUADeletion.endRequestUADeletion(builder);
    }

    static StackRequest makeDeleteUA(int handle) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uaDeletion = deleteUaReq(builder, (long) handle);
        Request.startRequest(builder);
        Request.addReqid(builder, 103);
        Request.addReqType(builder, (byte) 3);
        Request.addReq(builder, uaDeletion);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int registrationReq(int handle, FlatBufferBuilder builder, String pcscfAddr, int pcscfPort, int regExpires, List<String> serviceList, List<String> impuList, Capabilities ownCap, List<String> thirdPartyFeatureTags, String accessToken, String authServerUrl) {
        FlatBufferBuilder flatBufferBuilder = builder;
        List<String> list = serviceList;
        List<String> list2 = impuList;
        List<String> list3 = thirdPartyFeatureTags;
        String str = accessToken;
        String str2 = authServerUrl;
        int pcscsfOffSet = -1;
        if (pcscfAddr != null) {
            pcscsfOffSet = RequestRegistration.createPcscfAddrListVector(flatBufferBuilder, new int[]{builder.createString((CharSequence) pcscfAddr)});
        }
        int serviceListOffSet = -1;
        if (list != null) {
            serviceListOffSet = RequestRegistration.createServiceListVector(flatBufferBuilder, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder, list, serviceList.size()));
        }
        int impuListOffSet = -1;
        if (list2 != null) {
            impuListOffSet = RequestRegistration.createImpuListVector(flatBufferBuilder, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder, list2, impuList.size()));
        }
        int tParyFetrTagOffSet = -1;
        if (list3 != null) {
            tParyFetrTagOffSet = RequestRegistration.createThirdpartyFeatureListVector(flatBufferBuilder, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder, list3, thirdPartyFeatureTags.size()));
        }
        int accessTokenOffst = -1;
        if (str != null) {
            accessTokenOffst = flatBufferBuilder.createString((CharSequence) str);
        }
        int featureTagOffSet = -1;
        if (ownCap != null) {
            List<Integer> featureList = StackRequestBuilderUtil.translateFeatureTag(ownCap.getFeature());
            int[] featuretags = new int[featureList.size()];
            int i = 0;
            for (Integer intValue : featureList) {
                featuretags[i] = intValue.intValue();
                i++;
            }
            featureTagOffSet = RequestUACreation.createFeatureTagListVector(flatBufferBuilder, featuretags);
        }
        int authServerUrlOffst = -1;
        if (str2 != null) {
            authServerUrlOffst = flatBufferBuilder.createString((CharSequence) str2);
        }
        RequestRegistration.startRequestRegistration(builder);
        RequestRegistration.addHandle(flatBufferBuilder, (long) handle);
        RequestRegistration.addRegExp(flatBufferBuilder, (long) regExpires);
        if (featureTagOffSet != -1) {
            RequestRegistration.addFeatureTagList(flatBufferBuilder, featureTagOffSet);
        }
        if (pcscsfOffSet != -1) {
            RequestRegistration.addPcscfAddrList(flatBufferBuilder, pcscsfOffSet);
        }
        if (serviceListOffSet != -1) {
            RequestRegistration.addServiceList(flatBufferBuilder, serviceListOffSet);
        }
        if (impuListOffSet != -1) {
            RequestRegistration.addImpuList(flatBufferBuilder, impuListOffSet);
        }
        if (list3 != null) {
            RequestRegistration.addThirdpartyFeatureList(flatBufferBuilder, tParyFetrTagOffSet);
        }
        if (str != null) {
            RequestRegistration.addAccessToken(flatBufferBuilder, accessTokenOffst);
        }
        if (str2 != null) {
            RequestRegistration.addAuthServerUrl(flatBufferBuilder, authServerUrlOffst);
        }
        RequestRegistration.addPcscfPort(flatBufferBuilder, (long) pcscfPort);
        return RequestRegistration.endRequestRegistration(builder);
    }

    static StackRequest makeRegister(int handle, String pcscfAddr, int pcscfPort, int regExpires, List<String> serviceList, List<String> impuList, Capabilities ownCap, List<String> thirdPartyFeatureTags, String accessToken, String authServerUrl) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int registerReq = registrationReq(handle, builder, pcscfAddr, pcscfPort, regExpires, serviceList, impuList, ownCap, thirdPartyFeatureTags, accessToken, authServerUrl);
        Request.startRequest(builder);
        Request.addReqid(builder, 104);
        Request.addReqType(builder, (byte) 4);
        Request.addReq(builder, registerReq);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int akaAuthInfoReq(FlatBufferBuilder builder, long handle, int recvMng, String resp) {
        int akaRespOffset = builder.createString((CharSequence) resp);
        RequestUpdateAkaResp.startRequestUpdateAkaResp(builder);
        RequestUpdateAkaResp.addHandle(builder, handle);
        RequestUpdateAkaResp.addRecvMng(builder, (long) recvMng);
        RequestUpdateAkaResp.addAkaResp(builder, akaRespOffset);
        return RequestUpdateAkaResp.endRequestUpdateAkaResp(builder);
    }

    static StackRequest makeSendAuthResponse(int handle, int tid, String response) {
        String str = LOG_TAG;
        Log.i(str, "sendAuthResponse: handle " + handle + " tid " + tid + " response " + response);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int akaAuthInfo = akaAuthInfoReq(builder, (long) handle, tid, response);
        Request.startRequest(builder);
        Request.addReqid(builder, 105);
        Request.addReqType(builder, (byte) 5);
        Request.addReq(builder, akaAuthInfo);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeSendDnsQuery(int handle, String intf, String hostname, List<String> dnsServers, String type, String transport, String family, long netId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int familyOffset = -1;
        if (!TextUtils.isEmpty(family)) {
            familyOffset = builder.createString((CharSequence) family);
        } else {
            String str = family;
        }
        int transportOffset = -1;
        if (!TextUtils.isEmpty(transport)) {
            transportOffset = builder.createString((CharSequence) transport);
        } else {
            String str2 = transport;
        }
        int typeOffset = -1;
        if (!TextUtils.isEmpty(type)) {
            typeOffset = builder.createString((CharSequence) type);
        } else {
            String str3 = type;
        }
        int hostnameOffset = -1;
        if (!TextUtils.isEmpty(hostname)) {
            hostnameOffset = builder.createString((CharSequence) hostname);
        } else {
            String str4 = hostname;
        }
        int interface_nwOffset = -1;
        if (!TextUtils.isEmpty(intf)) {
            interface_nwOffset = builder.createString((CharSequence) intf);
        } else {
            String str5 = intf;
        }
        int[] dnsServerArr = new int[dnsServers.size()];
        for (int i = 0; i < dnsServerArr.length; i++) {
            dnsServerArr[i] = builder.createString((CharSequence) dnsServers.get(i));
        }
        List<String> list = dnsServers;
        int dnsServerListOffset = RequestDnsQuery.createDnsServerListVector(builder, dnsServerArr);
        RequestDnsQuery.startRequestDnsQuery(builder);
        if (familyOffset != -1) {
            RequestDnsQuery.addFamily(builder, familyOffset);
        }
        if (transportOffset != -1) {
            RequestDnsQuery.addTransport(builder, transportOffset);
        }
        if (typeOffset != -1) {
            RequestDnsQuery.addType(builder, typeOffset);
        }
        RequestDnsQuery.addDnsServerList(builder, dnsServerListOffset);
        if (hostnameOffset != -1) {
            RequestDnsQuery.addHostname(builder, hostnameOffset);
        }
        if (interface_nwOffset != -1) {
            RequestDnsQuery.addInterfaceNw(builder, interface_nwOffset);
        }
        int i2 = familyOffset;
        RequestDnsQuery.addHandle(builder, (long) handle);
        RequestDnsQuery.addNetId(builder, netId);
        int sendDnsQueryOffset = RequestDnsQuery.endRequestDnsQuery(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 106);
        Request.addReqType(builder, (byte) 69);
        Request.addReq(builder, sendDnsQueryOffset);
        int i3 = sendDnsQueryOffset;
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int preferredImpuReq(FlatBufferBuilder builder, long handle, String impu) {
        int mpuOffset = builder.createString((CharSequence) impu);
        RequestSetPreferredImpu.startRequestSetPreferredImpu(builder);
        RequestSetPreferredImpu.addHandle(builder, handle);
        RequestSetPreferredImpu.addImpu(builder, mpuOffset);
        return RequestSetPreferredImpu.endRequestSetPreferredImpu(builder);
    }

    static StackRequest makeSetPreferredImpu(int handle, String impu) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int impuOffset = preferredImpuReq(builder, (long) handle, impu);
        Request.startRequest(builder);
        Request.addReqid(builder, 107);
        Request.addReqType(builder, (byte) 6);
        Request.addReq(builder, impuOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int networkSuspendReq(FlatBufferBuilder builder, long handle, boolean state) {
        RequestNetworkSuspended.startRequestNetworkSuspended(builder);
        RequestNetworkSuspended.addHandle(builder, handle);
        RequestNetworkSuspended.addState(builder, state);
        return RequestNetworkSuspended.endRequestNetworkSuspended(builder);
    }

    static StackRequest makeNetworkSuspended(int handle, boolean state) {
        String str = LOG_TAG;
        Log.i(str, "register: handle " + handle + " state " + state);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int nwkSuspended = networkSuspendReq(builder, (long) handle, state);
        Request.startRequest(builder);
        Request.addReqid(builder, 108);
        Request.addReqType(builder, (byte) 7);
        Request.addReq(builder, nwkSuspended);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeRequestUpdateFeatureTag(int handle, long features) {
        Log.i(LOG_TAG, "requestUpdateFeatureTag");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        List<Integer> featureTagList = StackRequestBuilderUtil.translateFeatureTag(features);
        int[] featureTagArray = new int[featureTagList.size()];
        for (int i = 0; i < featureTagArray.length; i++) {
            featureTagArray[i] = featureTagList.get(i).intValue();
        }
        int i2 = RequestUpdateFeatureTag.createFeatureTagListVector(builder, featureTagArray);
        RequestUpdateFeatureTag.startRequestUpdateFeatureTag(builder);
        RequestUpdateFeatureTag.addFeatureTagList(builder, i2);
        RequestUpdateFeatureTag.addHandle(builder, (long) handle);
        int requestUpdateFeatureTagOffset = RequestUpdateFeatureTag.endRequestUpdateFeatureTag(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 109);
        Request.addReqType(builder, (byte) 12);
        Request.addReq(builder, requestUpdateFeatureTagOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateVceConfig(int handle, boolean config) {
        String str = LOG_TAG;
        Log.i(str, "updateVceConfig: handle: " + handle + ", vceEnabled: " + config);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestUpdateVceConfig.startRequestUpdateVceConfig(builder);
        RequestUpdateVceConfig.addHandle(builder, (long) handle);
        RequestUpdateVceConfig.addVceConfig(builder, config);
        int UpdateVceConfigOffset = RequestUpdateVceConfig.endRequestUpdateVceConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 110);
        Request.addReqType(builder, (byte) 99);
        Request.addReq(builder, UpdateVceConfigOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeSetTextMode(int phoneId, int mode) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestSetTextMode.startRequestSetTextMode(builder);
        RequestSetTextMode.addTextMode(builder, (long) mode);
        RequestSetTextMode.addPhoneId(builder, (long) phoneId);
        int setTextModeOffset = RequestSetTextMode.endRequestSetTextMode(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 112);
        Request.addReqType(builder, (byte) 9);
        Request.addReq(builder, setTextModeOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateXqEnable(int phone_id, boolean enable) {
        Log.i(LOG_TAG + "[" + phone_id + "]", "updateXqEnable():  enable: " + enable);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestUpdateXqEnable.startRequestUpdateXqEnable(builder);
        RequestUpdateXqEnable.addPhoneId(builder, (long) phone_id);
        RequestUpdateXqEnable.addEnable(builder, enable);
        int offsetXqEanble = RequestUpdateXqEnable.endRequestUpdateXqEnable(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 114);
        Request.addReqType(builder, (byte) 11);
        Request.addReq(builder, offsetXqEanble);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateServiceVersion(int phoneId, HashMap<String, String> svMap) {
        Log.i(LOG_TAG + "[" + phoneId + "]", "updateServiceVersion:phoneId:" + phoneId);
        for (Map.Entry<String, String> entry : svMap.entrySet()) {
            Log.i(LOG_TAG + "[" + phoneId + "]", entry.getKey() + " : " + entry.getValue());
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        List<Integer> pairList = StackRequestBuilderUtil.translateExtraHeader(builder, svMap);
        int[] pairOffsetArr = new int[pairList.size()];
        int i = 0;
        for (Integer intValue : pairList) {
            pairOffsetArr[i] = intValue.intValue();
            i++;
        }
        int pairOffset = ExtraHeader.createPairVector(builder, pairOffsetArr);
        ExtraHeader.startExtraHeader(builder);
        ExtraHeader.addPair(builder, pairOffset);
        int extraHeaderOffset = ExtraHeader.endExtraHeader(builder);
        ServiceVersionConfig.startServiceVersionConfig(builder);
        ServiceVersionConfig.addExtraHeaders(builder, extraHeaderOffset);
        int svOffset = ServiceVersionConfig.endServiceVersionConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 5);
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        RequestUpdateCommonConfig.addConfig(builder, svOffset);
        int reqOffset = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeConfigRCS(int phoneId, int ftchunksize, int ishchunksize, String confUri, boolean isMsrpCema, String downloadsPath, boolean isConfSubscribeEnabled, String exploderUri, int pagerModeLimit, boolean useMsrpDiscardPort, boolean isAggrImdnSupported, boolean isPrivacyDisable, int cbMsgTech, String endUserConfReqId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uriOffset = builder.createString((CharSequence) confUri);
        int pathOffset = builder.createString((CharSequence) downloadsPath);
        int exUriOffset = builder.createString((CharSequence) exploderUri);
        int endUserConfOffset = builder.createString((CharSequence) endUserConfReqId);
        RcsConfig.startRcsConfig(builder);
        RcsConfig.addRcsFtChunkSize(builder, (long) ftchunksize);
        RcsConfig.addRcsIshChunkSize(builder, (long) ishchunksize);
        RcsConfig.addConfUri(builder, uriOffset);
        RcsConfig.addIsMsrpCema(builder, isMsrpCema);
        RcsConfig.addDownloadsPath(builder, pathOffset);
        RcsConfig.addIsConfSubscribeEnabled(builder, isConfSubscribeEnabled);
        RcsConfig.addExploderUri(builder, exUriOffset);
        RcsConfig.addPagerModeSizeLimit(builder, (long) pagerModeLimit);
        RcsConfig.addUseMsrpDiscardPort(builder, useMsrpDiscardPort);
        RcsConfig.addIsAggrImdnSupported(builder, isAggrImdnSupported);
        RcsConfig.addIsCbPrivacyDisable(builder, isPrivacyDisable);
        RcsConfig.addCbMsgTech(builder, cbMsgTech);
        RcsConfig.addEndUserConfReqId(builder, endUserConfOffset);
        int configOffset = RcsConfig.endRcsConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        int i = uriOffset;
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 4);
        RequestUpdateCommonConfig.addConfig(builder, configOffset);
        int i2 = pathOffset;
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        int reqOffset = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, reqOffset);
        int i3 = configOffset;
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeConfigRCSOff(int phoneId, String suspenduser) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int userOffset = builder.createString((CharSequence) suspenduser);
        RcsConfig.startRcsConfig(builder);
        RcsConfig.addSuspendUser(builder, userOffset);
        int configOffset = RcsConfig.endRcsConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 4);
        RequestUpdateCommonConfig.addConfig(builder, configOffset);
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        int reqOffset = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateScreenOnOff(int phoneId, int on) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        ScreenConfig.startScreenConfig(builder);
        ScreenConfig.addOn(builder, (long) on);
        int configOffset = ScreenConfig.endScreenConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 6);
        RequestUpdateCommonConfig.addConfig(builder, configOffset);
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        int reqOffset = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    private static int imeiReq(int phoneId, FlatBufferBuilder builder, String imei) {
        int imeioffset = builder.createString((CharSequence) imei);
        RegiConfig.startRegiConfig(builder);
        RegiConfig.addImei(builder, imeioffset);
        int regiOffset = RegiConfig.endRegiConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 1);
        RequestUpdateCommonConfig.addConfig(builder, regiOffset);
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        return RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
    }

    static StackRequest makeConfigRegistration(int phoneId, String imei) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int regioffset = imeiReq(phoneId, builder, imei);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, regioffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeConfigCall(int phoneId, boolean ttySessionRequired, boolean rttSessionRequired, boolean automode) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        CallConfig.startCallConfig(builder);
        CallConfig.addTtySessionRequired(builder, ttySessionRequired);
        CallConfig.addAutomaticMode(builder, automode);
        CallConfig.addRttSessionRequired(builder, rttSessionRequired);
        int configOffset = CallConfig.endCallConfig(builder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(builder);
        RequestUpdateCommonConfig.addConfigType(builder, (byte) 3);
        RequestUpdateCommonConfig.addConfig(builder, configOffset);
        RequestUpdateCommonConfig.addPhoneId(builder, (long) phoneId);
        int reqOffset = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 101);
        Request.addReqType(builder, (byte) 1);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }
}
