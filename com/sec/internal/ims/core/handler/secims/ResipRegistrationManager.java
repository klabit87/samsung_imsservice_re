package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.handler.RegistrationHandler;
import com.sec.internal.ims.core.handler.secims.CallProfile;
import com.sec.internal.ims.core.handler.secims.ResipRegistrationManager;
import com.sec.internal.ims.core.handler.secims.UaProfile;
import com.sec.internal.ims.core.handler.secims.UserAgent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.Cert;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.X509CertVerifyRequest;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.xq.att.ImsXqReporter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ResipRegistrationManager extends RegistrationHandler {
    private static final String LOG_TAG = "ResipRegiMgr";
    private static final boolean SHIP_BUILD = IMSLog.isShipBuild();
    protected Context mContext;
    protected SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework;
    protected PaniGenerator mPaniGenerator;
    protected IPdnController mPdnController;
    protected IRegistrationHandlerNotifiable mRegistrationHandler;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected Handler mUaHandler = null;
    protected HandlerThread mUaHandlerThread = null;
    protected final Map<Integer, UserAgent> mUaList = new ConcurrentHashMap();

    public ResipRegistrationManager(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = imsFramework;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
    }

    public void setEventLog(SimpleEventLog eventLog) {
        this.mEventLog = eventLog;
    }

    public void setRegistrationHandler(IRegistrationHandlerNotifiable regHandler) {
        this.mRegistrationHandler = regHandler;
    }

    public void setSimManagers(List<ISimManager> simManagerList) {
        this.mSimManagers = simManagerList;
    }

    public void setPdnController(IPdnController pdnController) {
        this.mPdnController = pdnController;
    }

    public void init() {
        HandlerThread handlerThread = new HandlerThread("UaHandler");
        this.mUaHandlerThread = handlerThread;
        handlerThread.start();
        this.mUaHandler = new Handler(this.mUaHandlerThread.getLooper());
        this.mPaniGenerator = new PaniGenerator(this.mContext, this.mPdnController);
        StackIF.getInstance().registerUaListener(0, new StackEventListener() {
            public void onX509CertVerifyRequested(X509CertVerifyRequest request) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onX509CertVerifyRequested");
                List<X509Certificate> certList = new ArrayList<>();
                try {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    for (int i = 0; i < request.certLength(); i++) {
                        Cert cert = request.cert(i);
                        int certDataLen = 0;
                        if (cert != null) {
                            certDataLen = cert.certDataLength();
                        }
                        byte[] certData = new byte[certDataLen];
                        for (int index = 0; index < certDataLen; index++) {
                            certData[index] = (byte) cert.certData(index);
                        }
                        X509Certificate curCert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
                        certList.add(curCert);
                        IMSLog.s(ResipRegistrationManager.LOG_TAG, "Subject: " + curCert.getSubjectDN().toString() + ", Issuer: " + curCert.getIssuerDN().toString());
                    }
                } catch (CertificateException e) {
                    Log.i(ResipRegistrationManager.LOG_TAG, "something wrong with certificate", e);
                }
                ResipRegistrationManager.this.mRegistrationHandler.notifyX509CertVerificationRequested((X509Certificate[]) certList.toArray(new X509Certificate[0]));
            }

            public void onDnsResponse(String hostname, List<String> ipAddrList, int port, int handle) {
                IMSLog.s(ResipRegistrationManager.LOG_TAG, "onDnsResponse: hostname " + hostname + " ipAddrList " + ipAddrList + " port " + port + ", handle " + handle);
                ResipRegistrationManager.this.mRegistrationHandler.notifyDnsResponse(ipAddrList, port, handle);
            }
        });
    }

    public boolean registerInternal(IRegisterTask task, String ifacename, String pcscf, Set<String> services, Capabilities ownCap, String domain, String impu, String impi, String instanceId, String uuid, String cmcSaServerUrl, String cmcRelayType, List<String> thirdPartyFeatureTags, Bundle rcsBundle, boolean isVoWiFiSupported) {
        boolean z;
        Capabilities capabilities;
        UserAgent ua;
        IRegisterTask iRegisterTask = task;
        String str = pcscf;
        Set<String> set = services;
        Capabilities capabilities2 = ownCap;
        int phoneId = task.getPhoneId();
        StringBuilder sb = new StringBuilder();
        sb.append("registerInternal: task=");
        sb.append(iRegisterTask);
        sb.append(" pcscf=");
        sb.append(!SHIP_BUILD ? str : "");
        sb.append(" services=");
        sb.append(set);
        sb.append(" reason=");
        sb.append(task.getReason());
        IMSLog.i(LOG_TAG, phoneId, sb.toString());
        ImsProfile profile = task.getProfile();
        if (task.getRegistrationRat() == 18 && profile.getSupportedGeolocationPhase() >= 2) {
            if (task.getMno().isOneOf(Mno.ATT, Mno.SPRINT, Mno.VZW, Mno.CELLC_SOUTHAFRICA) && isVoWiFiSupported) {
                IMSLog.e(LOG_TAG, phoneId, "update geo location");
                IGeolocationController geolocationCon = this.mImsFramework.getGeolocationController();
                if (geolocationCon != null) {
                    geolocationCon.startGeolocationUpdate(phoneId, false);
                }
            }
        }
        configureRCS(iRegisterTask, this.mSimManagers.get(phoneId).getSimMno(), true, phoneId);
        int convertedRegId = IRegistrationManager.getRegistrationInfoId(profile.getId(), phoneId);
        UserAgent ua2 = this.mUaList.get(Integer.valueOf(convertedRegId));
        if (ua2 == null) {
            IMSLog.i(LOG_TAG, phoneId, "register: creating UserAgent.");
            IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable = this.mRegistrationHandler;
            UserAgent userAgent = ua2;
            z = true;
            String str2 = LOG_TAG;
            int phoneId2 = phoneId;
            UserAgent ua3 = createUserAgent(task, ifacename, pcscf, services, ownCap, domain, impu, impi, instanceId, uuid, cmcSaServerUrl, cmcRelayType, rcsBundle, iRegistrationHandlerNotifiable);
            if (ua3 != null) {
                this.mUaList.put(Integer.valueOf(convertedRegId), ua3);
                IRegisterTask iRegisterTask2 = task;
                iRegisterTask2.setUserAgent(ua3);
                iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.REGISTERING);
                iRegisterTask2.setRegiRequestType(DiagnosisConstants.REGI_REQC.INITIAL);
                IMSLog.lazer(iRegisterTask2, "SEND SIP REGISTER");
                if (!task.getProfile().hasEmergencySupport()) {
                    this.mRegistrationHandler.notifyTriggeringRecoveryAction(iRegisterTask2, ((long) task.getProfile().getTimerF()) * 2);
                }
                String str3 = pcscf;
                Capabilities capabilities3 = ownCap;
                String str4 = impu;
                List<String> list = thirdPartyFeatureTags;
                UserAgent userAgent2 = ua3;
                int i = phoneId2;
                Set<String> set2 = services;
            } else {
                IRegisterTask iRegisterTask3 = task;
                IMSLog.e(str2, phoneId2, "register: fail creating UserAgent.");
                return false;
            }
        } else {
            UserAgent ua4 = ua2;
            z = true;
            String str5 = LOG_TAG;
            int phoneId3 = phoneId;
            IRegisterTask iRegisterTask4 = iRegisterTask;
            UaProfile up = ua4.getUaProfile();
            String str6 = pcscf;
            if (str6 != null) {
                up.setPcscfIp(str6);
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("register: Re-Register with new services=");
            Set<String> set3 = services;
            sb2.append(set3);
            Log.i(str5, sb2.toString());
            iRegisterTask4.setRegiRequestType(DiagnosisConstants.REGI_REQC.RE_REGI);
            if (!profile.hasEmergencySupport()) {
                capabilities = ownCap;
            } else if (set3.contains("mmtel-video")) {
                capabilities = ownCap;
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL_VIDEO));
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(phoneId3, iRegisterTask4, "createUserAgent: add mmtel, mmtel-video to Capabilities for E-REGI");
            } else {
                capabilities = ownCap;
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(phoneId3, iRegisterTask4, "createUserAgent : add mmtel to Capabilities for E-REGI");
            }
            up.setOwnCapabilities(capabilities);
            up.setServiceList(new HashSet(set3));
            up.setLinkedImpuList(profile.getExtImpuList());
            up.setImpu(impu);
            if (!profile.hasEmergencySupport()) {
                ua = ua4;
                ua.setThirdPartyFeatureTags(thirdPartyFeatureTags);
            } else {
                List<String> list2 = thirdPartyFeatureTags;
                ua = ua4;
            }
            this.mImsFramework.getServiceModuleManager().notifyReRegistering(phoneId3, set3);
            ua.register();
        }
        return z;
    }

    public void deregisterInternal(IRegisterTask task, boolean local) {
        int phoneId = task.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, task, "deregisterInternal : " + task.getReason() + "(" + task.getDeregiReason() + ")");
        IMSLog.c(LogClass.REGI_DEREGISTER_INTERNAL, task.getPhoneId() + ",DEREGI:" + task.getReason() + ":" + task.getDeregiReason());
        configureRCS(task, this.mSimManagers.get(phoneId).getSimMno(), false, task.getPhoneId());
        UserAgent ua = (UserAgent) task.getUserAgent();
        boolean isRcsRegistered = false;
        if (task.getImsRegistration() != null) {
            isRcsRegistered = task.getImsRegistration().hasRcsService();
        }
        ua.deregister(local, isRcsRegistered);
        if ((task.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY && !task.needKeepEmergencyTask()) || (task.getMno() != Mno.KDDI && task.getProfile().hasEmergencySupport() && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING)) {
            removeUserAgent(task);
        }
    }

    public void onRegisterError(IRegisterTask task, int handle, SipError error, int retryAfter) {
        if (!task.getProfile().hasEmergencySupport() || task.getMno() == Mno.KDDI) {
            if (task.getMno() == Mno.TMOUS && SipErrorBase.MISSING_P_ASSOCIATED_URI.equals(error)) {
                return;
            }
            if (!task.isRefreshReg() || (task.getMno() != Mno.KDDI && !task.getGovernor().needImsNotAvailable())) {
                UserAgent taskUa = (UserAgent) task.getUserAgent();
                if (taskUa != null && taskUa.getHandle() == handle) {
                    removeUserAgent(task);
                } else if (getUserAgent(handle) != null) {
                    int phoneId = task.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId, "remove user agent not in the IRegisterTask: " + handle);
                    UserAgent ua = getUserAgent(handle);
                    ua.terminate();
                    ua.unRegisterListener();
                }
            } else {
                IMSLog.i(LOG_TAG, task.getPhoneId(), "Dont Remove the user Agent for Refresh Reg ,Re-register to be triggered.");
            }
        } else if (task.getMno() == Mno.VZW && SipErrorBase.SIP_TIMEOUT.equals(error) && task.getGovernor().getFailureCount() < 2) {
            removeUserAgent(task);
        }
    }

    public void onDeregistered(IRegisterTask task, boolean isRequestedDeregi, SipError error, int retryAfter) {
        removeUserAgent(task);
    }

    public boolean suspended(IRegisterTask task, boolean state) {
        if (task.getUserAgent() == null) {
            return false;
        }
        UserAgent ua = (UserAgent) task.getUserAgent();
        if (ua.getSuspendState() == state) {
            return false;
        }
        NetworkEvent networkEvent = SlotBasedConfig.getInstance(task.getPhoneId()).getNetworkEvent();
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = task.getPhoneId();
        StringBuilder sb = new StringBuilder();
        sb.append(state ? "Suspend : " : "Resume : ");
        sb.append(task.getProfile().getName());
        sb.append(" ");
        sb.append(networkEvent);
        simpleEventLog.logAndAdd(phoneId, sb.toString());
        ua.suspended(state);
        if (state) {
            this.mRegistrationHandler.removeRecoveryAction();
            return true;
        } else if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
            return true;
        } else {
            this.mRegistrationHandler.notifyTriggeringRecoveryAction(task, ((long) task.getProfile().getTimerF()) * 2);
            return true;
        }
    }

    private UaProfile.Builder configureTimerTS(ImsProfile profile, UaProfile.Builder builder) {
        int timerTS = 32000;
        Mno mno = Mno.fromName(profile.getMnoName());
        if (mno == Mno.SPRINT) {
            timerTS = 1000;
        } else if (mno == Mno.KDDI) {
            timerTS = 200000;
        }
        Log.i(LOG_TAG, "timerTS=%" + timerTS);
        builder.setTimerTS(timerTS);
        return builder;
    }

    private CallProfile configureMedia(ImsProfile profile) {
        Log.i(LOG_TAG, "configureMedia:");
        String filteredHwSupportedCodecs = "";
        boolean z = false;
        try {
            filteredHwSupportedCodecs = (String) Class.forName("com.sec.internal.ims.core.handler.secims.ResipMediaHandler").getMethod("getHwSupportedVideoCodecs", new Class[]{String.class}).invoke(this.mImsFramework.getHandlerFactory().getMediaHandler(), new Object[]{profile.getVideoCodec()});
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException e2) {
            e2.printStackTrace();
        }
        CallProfile.Builder dtmfMode = CallProfile.Builder.newBuilder().setAudioCodec(profile.getAudioCodec()).setAudioPort(profile.getAudioPortStart()).setAudioDscp(profile.getAudioDscp()).setAmrPayloadType(profile.getAmrnbbePayload()).setAmrOaPayloadType(profile.getAmrnboaPayload()).setAmrWbPayloadType(profile.getAmrwbbePayload()).setAmrWbOaPayloadType(profile.getAmrwboaPayload()).setAmrOpenPayloadType(profile.getAmropenPayload()).setDtmfPayloadType(profile.getDtmfNbPayload()).setDtmfWbPayloadType(profile.getDtmfWbPayload()).setAmrOaMaxRed(profile.getAmrnboaMaxRed()).setAmrBeMaxRed(profile.getAmrnbbeMaxRed()).setAmrOaWbMaxRed(profile.getAmrwboaMaxRed()).setAmrBeWbMaxRed(profile.getAmrwbbeMaxRed()).setEvsMaxRed(profile.getEvsMaxRed()).setAmrMode(profile.getAmrnbMode()).setAmrWbMode(profile.getAmrwbMode()).setAudioAs(profile.getAudioAS()).setAudioRs(profile.getAudioRS()).setAudioRr(profile.getAudioRR()).setPTime(profile.getPTime()).setMaxPTime(profile.getMaxPTime()).setVideoCodec(filteredHwSupportedCodecs).setVideoPort(profile.getVideoPortStart()).setFrameRate(profile.getFramerate()).setDisplayFormat(profile.getDisplayFormat()).setDisplayFormatHevc(profile.getDisplayFormatHevc()).setPacketizationMode(profile.getPacketizationMode()).setH265QvgaPayloadType(profile.getH265QvgaPayload()).setH265QvgaLPayloadType(profile.getH265QvgalPayload()).setH265VgaPayloadType(profile.getH265VgaPayload()).setH265VgaLPayloadType(profile.getH265VgalPayload()).setH265Hd720pPayloadType(profile.getH265Hd720pPayload()).setH265Hd720pLPayloadType(profile.getH265Hd720plPayload()).setH264720pPayloadType(profile.getH264720pPayload()).setH264720pLPayloadType(profile.getH264720plPayload()).setH264VgaPayloadType(profile.getH264VgaPayload()).setH264VgaLPayloadType(profile.getH264VgalPayload()).setH264QvgaPayloadType(profile.getH264QvgaPayload()).setH264QvgaLPayloadType(profile.getH264QvgalPayload()).setH264CifPayloadType(profile.getH264CifPayload()).setH264CifLPayloadType(profile.getH264CiflPayload()).setH263QcifPayloadType(profile.getH263QcifPayload()).setVideoAs(profile.getVideoAS()).setVideoRs(profile.getVideoRS()).setVideoRr(profile.getVideoRR()).setTextAs(profile.getTextAS()).setTextRs(profile.getTextRS()).setTextRr(profile.getTextRR()).setTextPort(profile.getTextPort()).setAudioAvpf(profile.getAudioAvpf() == 1).setAudioSrtp(profile.getAudioSrtp() == 1).setVideoAvpf(profile.getVideoAvpf() == 1).setVideoSrtp(profile.getVideoSrtp() == 1).setVideoCapabilities(profile.isSupportVideoCapabilities()).setRtpTimeout(profile.getRTPTimeout()).setRtcpTimeout(profile.getRTCPTimeout()).setIgnoreRtcpTimeoutOnHoldCall(profile.getIgnoreRtcpTimeoutOnHoldCall()).setEnableRtcpOnActiveCall(profile.getEnableRtcpOnActiveCall()).setEnableAvSync(profile.getEnableAvSync()).setEnableScr(profile.getEnableScr()).setAudioRtcpXr(profile.getAudioRtcpXr() == 1).setVideoRtcpXr(profile.getVideoRtcpXr() == 1).setDtmfMode(profile.getDtmfMode());
        if (profile.getEnableEvsCodec() == 1) {
            z = true;
        }
        return dtmfMode.setEnableEvsCodec(z).setEvsDiscontinuousTransmission(profile.getEvsDiscontinuousTransmission()).setEvsDtxRecv(profile.getEvsDtxRecv()).setEvsHeaderFull(profile.getEvsHeaderFull()).setEvsModeSwitch(profile.getEvsModeSwitch()).setEvsChannelSend(profile.getEvsChannelSend()).setEvsChannelRecv(profile.getEvsChannelRecv()).setEvsChannelAwareReceive(profile.getEvsChannelAwareReceive()).setEvsCodecModeRequest(profile.getEvsCodecModeRequest()).setEvsBitRateSend(profile.getEvsBitRateSend()).setEvsBitRateReceive(profile.getEvsBitRateReceive()).setEvsBandwidthSend(profile.getEvsBandwidthSend()).setEvsBandwidthReceive(profile.getEvsBandwidthReceive()).setEvsPayload(profile.getEvsPayload()).setEvs2ndPayload(profile.getEvs2ndPayload()).setEvsDefaultBandwidth(profile.getEvsDefaultBandwidth()).setEvsDefaultBitrate(profile.getEvsDefaultBitrate()).setEvsPayloadExt(profile.getEvsPayloadExt()).setEvsBitRateSendExt(profile.getEvsBitRateSendExt()).setEvsBitRateReceiveExt(profile.getEvsBitRateReceiveExt()).setEvsBandwidthSendExt(profile.getEvsBandwidthSendExt()).setEvsBandwidthReceiveExt(profile.getEvsBandwidthReceiveExt()).setEvsLimitedCodec(profile.getEvsLimitedCodec()).build();
    }

    private UserAgent createUserAgent(IRegisterTask task, String ifacename, String pcscf, Set<String> services, Capabilities ownCap, String domain, String impu, String impi, String instanceId, String uuid, String cmcSaServerUrl, String cmcRelayType, Bundle rcsBundle, IRegistrationHandlerNotifiable regHandler) {
        boolean isSupportUpgradePrecondition;
        Long netId;
        int regRetryMaxTime;
        int regRetryBaseTime;
        int timerT4;
        String authAlgo;
        Mno mno;
        int pdn;
        String msrpTransType;
        int rcsProfile;
        int timerT2;
        String password;
        int timerT1;
        String authAlgo2;
        boolean useKeepAlive;
        String imMsgTech;
        String msrpTransType2;
        int timerT12;
        CallProfile cp;
        boolean isGcfConfigEnable;
        List<String> acb;
        CallProfile cp2;
        boolean useKeepAlive2;
        ISimManager sm;
        String realm;
        UaProfile.Builder builder;
        int textMode;
        Mno mno2;
        String sessionRefresher;
        String str;
        String str2 = ifacename;
        String str3 = pcscf;
        Set<String> set = services;
        Capabilities capabilities = ownCap;
        String str4 = domain;
        String str5 = impu;
        String str6 = impi;
        String str7 = uuid;
        Bundle bundle = rcsBundle;
        ImsProfile profile = task.getProfile();
        String hostname = task.getPcscfHostname();
        int pdn2 = task.getPdnType();
        Network network = task.getNetworkConnected();
        int rat = task.getRegistrationRat();
        final int phoneId = task.getPhoneId();
        if (str2 == null) {
            String str8 = hostname;
            IMSLog.e(LOG_TAG, phoneId, "createUserAgent: ifacename is null");
            return null;
        }
        String hostname2 = hostname;
        String transport = profile.getTransportName();
        boolean isEmergency = profile.hasEmergencySupport();
        int rat2 = rat;
        boolean isPrecondEnabled = profile.getUsePrecondition() != 0;
        boolean isPrecondInitialSendrecv = profile.getPrecondtionInitialSendrecv();
        int sessionExpires = profile.getSessionExpire();
        int minSe = profile.getMinSe();
        String sessionRefresher2 = profile.getSessionRefresher();
        int regExpires = profile.getRegExpire();
        int mssPacketSize = profile.getMssSize();
        String pdnName = getPdnName(pdn2);
        boolean isGcfConfigEnable2 = DeviceUtil.getGcfMode().booleanValue();
        int sipMobility = profile.getSipMobility();
        boolean isEnableGruu = profile.isEnableGruu() != 0;
        int sipMobility2 = sipMobility;
        boolean isEnableSessionId = profile.isEnableSessionId();
        int audioEngineType = getAudioEngineType();
        boolean isSubscribeReg = profile.getSubscribeForReg() != 0;
        int ttyType = profile.getTtyType();
        boolean isSupportUpgradePrecondition2 = profile.getAsBoolean("support_upgrade_precondition").booleanValue();
        boolean isSimMobility = profile.getSimMobility();
        List<String> acb2 = profile.getAcb();
        IMSLog.i(LOG_TAG, phoneId, "isSupportUpgradePrecondition " + isSupportUpgradePrecondition2);
        if (isEmergency) {
            if (set.contains("mmtel-video")) {
                isSupportUpgradePrecondition = isSupportUpgradePrecondition2;
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL_VIDEO));
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(phoneId, "createUserAgent: add mmtel, mmtel-video to Capabilities for E-REGI");
            } else {
                isSupportUpgradePrecondition = isSupportUpgradePrecondition2;
                capabilities.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(phoneId, "createUserAgent: add mmtel to Capabilities for E-REGI");
            }
            SlotBasedConfig.getInstance(phoneId).setRTTMode(Boolean.valueOf(ImsUtil.isRttModeOnFromCallSettings(this.mContext, phoneId)));
        } else {
            isSupportUpgradePrecondition = isSupportUpgradePrecondition2;
        }
        IMSLog.s(LOG_TAG, phoneId, "createUserAgent: ownCap= " + capabilities);
        boolean isSupportUpgradePrecondition3 = isSupportUpgradePrecondition;
        ImsProfile profile2 = profile;
        if (ownCap.getFeature() == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            if (services.size() != 1 || !set.contains("smsip")) {
                IMSLog.e(LOG_TAG, phoneId, "createUserAgent: empty capabilities. fail to create");
                return null;
            }
            IMSLog.e(LOG_TAG, phoneId, "createUserAgent: empty capabilities. smsip only registration");
        }
        ISimManager sm2 = this.mSimManagers.get(phoneId);
        UserAgent ua = new UserAgent(this.mContext, this.mUaHandler, StackIF.getInstance(), this.mTelephonyManager, this.mPdnController, sm2, this.mImsFramework);
        IMSLog.i(LOG_TAG, phoneId, "createUserAgent: pdn " + pdn2 + "(" + pdnName + ") services " + services.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("createUserAgent: uuid ");
        sb.append(str7);
        IMSLog.i(LOG_TAG, phoneId, sb.toString());
        int cmcType = profile2.getCmcType();
        if (cmcType == 7 || cmcType == 8 || cmcType == 5) {
            netId = getP2pNetworkHandle(cmcType);
        } else {
            netId = Long.valueOf(network.getNetworkHandle());
        }
        IMSLog.i(LOG_TAG, phoneId, "createUserAgent: profile=" + profile2.getName() + " iface=" + str2 + " NetId=" + netId);
        String regiAlgo = profile2.getRegistrationAlgorithm();
        if (regiAlgo == null) {
            regiAlgo = "md5";
        }
        String authAlgo3 = profile2.getAuthAlgorithm();
        if (authAlgo3 == null) {
            authAlgo3 = "both";
        }
        String encAlgo = profile2.getEncAlgorithm();
        if (encAlgo == null) {
            encAlgo = "all";
        }
        int needAutoConf = 0;
        int i = cmcType;
        Network network2 = network;
        ImsProfile profile3 = profile2;
        String rcs_profile = ConfigUtil.getRcsProfileWithFeature(this.mContext, phoneId, profile3);
        UserAgent ua2 = ua;
        int rcsProfile2 = ImsProfile.getRcsProfileType(rcs_profile);
        String realm2 = null;
        int timerT13 = profile3.getTimer1();
        int timerT22 = profile3.getTimer2();
        int timerT42 = profile3.getTimer4();
        int regRetryBaseTime2 = profile3.getRegRetryBaseTime();
        int regRetryMaxTime2 = profile3.getRegRetryMaxTime();
        Integer qVal = Integer.valueOf(profile3.getQValue());
        String str9 = rcs_profile;
        Mno mno3 = sm2.getSimMno();
        if (RcsUtils.isAutoConfigNeeded(profile3.getAllServiceSetFromAllNetwork())) {
            needAutoConf = 1;
        }
        if (needAutoConf == 0) {
            pdn = pdn2;
            password = profile3.getPassword();
            StringBuilder sb2 = new StringBuilder();
            mno = mno3;
            sb2.append("createUserAgent: AUTOCONFIG_NOT_NEEDED password=");
            sb2.append(password);
            IMSLog.s(LOG_TAG, phoneId, sb2.toString());
            authAlgo = authAlgo3;
            imMsgTech = "CPM";
            authAlgo2 = "";
            timerT4 = timerT42;
            regRetryBaseTime = regRetryBaseTime2;
            regRetryMaxTime = regRetryMaxTime2;
            useKeepAlive = false;
            rcsProfile = rcsProfile2;
            msrpTransType = encAlgo;
            msrpTransType2 = transport;
            timerT1 = timerT13;
            timerT2 = timerT22;
        } else {
            mno = mno3;
            pdn = pdn2;
            Bundle bundle2 = rcsBundle;
            password = bundle2.getString(CloudMessageProviderContract.VVMAccountInfoColumns.PASSWORD);
            realm2 = bundle2.getString("realm");
            imMsgTech = bundle2.getString(ConfigConstants.ConfigTable.IM_IM_MSG_TECH);
            rcsProfile = rcsProfile2;
            String msrpTransType3 = bundle2.getString("msrpTransType");
            msrpTransType = encAlgo;
            String transport2 = bundle2.getString("transport");
            boolean useKeepAlive3 = bundle2.getBoolean("useKeepAlive");
            qVal = Integer.valueOf(bundle2.getInt("qVal"));
            StringBuilder sb3 = new StringBuilder();
            authAlgo = authAlgo3;
            sb3.append("getRcsConfig - password : ");
            sb3.append(IMSLog.checker(password));
            sb3.append(", imMsgTech : ");
            sb3.append(imMsgTech);
            sb3.append(", msrpTransType : ");
            sb3.append(msrpTransType3);
            IMSLog.i(LOG_TAG, phoneId, sb3.toString());
            authAlgo2 = msrpTransType3;
            msrpTransType2 = transport2;
            timerT1 = bundle2.getInt(ConfigConstants.ConfigTable.TIMER_T1);
            timerT2 = bundle2.getInt(ConfigConstants.ConfigTable.TIMER_T2);
            timerT4 = bundle2.getInt(ConfigConstants.ConfigTable.TIMER_T4);
            regRetryBaseTime = bundle2.getInt(ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME);
            regRetryMaxTime = bundle2.getInt(ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME);
            useKeepAlive = useKeepAlive3;
        }
        if (!Mno.fromName(profile3.getMnoName()).isKor() || ConfigUtil.isRcsOnly(profile3)) {
            timerT12 = timerT1;
            cp = configureMedia(profile3);
        } else {
            timerT12 = timerT1;
            ImsProfile imsDmProfile = DmProfileLoader.getProfile(this.mContext, profile3, phoneId);
            CallProfile cp3 = configureMedia(imsDmProfile);
            ImsProfile imsProfile = imsDmProfile;
            IMSLog.i(LOG_TAG, phoneId, "createUserAgent: imsDmProfile from DmProfileLoader");
            cp = cp3;
        }
        String msrpTransType4 = authAlgo2;
        if (!isEmergency || !sm2.hasNoSim()) {
            sm = sm2;
            useKeepAlive2 = useKeepAlive;
            cp2 = cp;
            String str10 = "null";
            if (TextUtils.isEmpty(impi)) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("createUserAgent: impi is ");
                if (impi != null) {
                    str10 = "empty";
                }
                sb4.append(str10);
                IMSLog.e(LOG_TAG, phoneId, sb4.toString());
                return null;
            }
            String str11 = impi;
            if (TextUtils.isEmpty(domain)) {
                StringBuilder sb5 = new StringBuilder();
                String str12 = str10;
                sb5.append("createUserAgent: domain is ");
                List<String> list = acb2;
                String str13 = "empty";
                List<String> acb3 = list;
                if (domain == null) {
                    String str14 = str12;
                    List<String> list2 = acb3;
                    str = str14;
                } else {
                    List<String> list3 = acb3;
                    str = str13;
                }
                sb5.append(str);
                IMSLog.e(LOG_TAG, phoneId, sb5.toString());
                return null;
            }
            String str15 = domain;
            acb = acb2;
            if (TextUtils.isEmpty(realm2)) {
                realm2 = domain;
            }
            UaProfile.Builder impi2 = UaProfile.Builder.newBuilder().setImpi(str11);
            String str16 = impu;
            isGcfConfigEnable = isGcfConfigEnable2;
            builder = impi2.setImpu(str16).setPreferredId(str16).setDomain(str15).setPassword(password).setIsIpSec(profile3.isIpSecEnabled() && task.getGovernor().isIPSecAllow()).setWifiPreConditionEnabled(profile3.isWifiPreConditionEnabled()).setUseCompactHeader(profile3.shouldUseCompactHeader()).setEmergencyProfile(isEmergency).setIsServerHeaderEnabled(this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Call.IS_SERVER_HEADER_ENABLED, false));
            realm = realm2;
        } else {
            sm = sm2;
            useKeepAlive2 = useKeepAlive;
            cp2 = cp;
            builder = UaProfile.Builder.newBuilder().setImpi("").setImpu("sip:anonymous@anonymous.invalid").setPreferredId("sip:anonymous@anonymous.invalid").setDomain("").setPassword("").setIsIpSec(false).setWifiPreConditionEnabled(profile3.isWifiPreConditionEnabled()).setUseCompactHeader(profile3.shouldUseCompactHeader()).setEmergencyProfile(true).setIsServerHeaderEnabled(this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Call.IS_SERVER_HEADER_ENABLED, false));
            isGcfConfigEnable = isGcfConfigEnable2;
            acb = acb2;
            realm = realm2;
        }
        IMSLog.i(LOG_TAG, phoneId, "###set profile id, id = " + profile3.getId());
        int ttyType2 = ttyType;
        int textMode2 = ttyType2;
        if (ttyType2 == 4) {
            if (SlotBasedConfig.getInstance(phoneId).getRTTMode()) {
                textMode = 3;
            } else {
                textMode = 2;
            }
        } else if (ttyType2 != 3) {
            textMode = textMode2;
        } else if (SlotBasedConfig.getInstance(phoneId).getRTTMode()) {
            textMode = 3;
        } else {
            textMode = 0;
        }
        int textMode3 = isEmergency;
        StringBuilder sb6 = new StringBuilder();
        String str17 = password;
        sb6.append("TTY Type ");
        sb6.append(ttyType2);
        sb6.append(" convert to TextMode ");
        sb6.append(textMode);
        IMSLog.i(LOG_TAG, phoneId, sb6.toString());
        int i2 = ttyType2;
        int srvccVersion = this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.Call.SRVCC_VERSION, 0);
        IImsFramework iImsFramework = this.mImsFramework;
        String str18 = LOG_TAG;
        boolean ignoreDisplayName = iImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Call.IGNORE_DISPLAY_NAME, false);
        Long l = netId;
        boolean isPrecondEnabled2 = isPrecondEnabled;
        boolean isEnableGruu2 = isEnableGruu;
        boolean z = isEnableGruu2;
        UaProfile.Builder subscriberTimer = builder.setIface(str2).setProfileId(profile3.getId()).setNetId(netId.longValue()).setPdn(pdnName).setServiceList(services).setOwnCapabilities(ownCap).setQparam(qVal.intValue()).setRemoteUriType(RcsConfigurationHelper.getNetworkUriType(this.mContext, profile3.getRemoteUriType(), profile3.getNeedAutoconfig(), phoneId)).setControlDscp(profile3.getControlDscp()).setTransportType(msrpTransType2).setPcscfPort(profile3.getSipPort()).setRegiAlgorithm(regiAlgo).setAuthAlg(authAlgo).setEncrAlg(msrpTransType).setPrecondEnabled(isPrecondEnabled2).setPrecondInitialSendrecv(isPrecondInitialSendrecv).setSessionExpires(sessionExpires).setRegExpires(regExpires).setMinSe(minSe).setUserAgent(profile3.getSipUserAgent()).setDisplayName(profile3.getDisplayName()).setRealm(realm).setImMsgTech(imMsgTech).setRingbackTimer(profile3.getRingbackTimer()).setRingingTimer(profile3.getRingingTimer()).setCallProfile(cp2).setIsSoftphoneEnabled(profile3.isSoftphoneEnabled()).setIsCdmalessEnabled(ImsUtil.isCdmalessEnabled(phoneId)).setMssSize(mssPacketSize).setSipMobility(sipMobility2).setIsEnableGruu(isEnableGruu2).setIsEnableSessionId(isEnableSessionId).setAudioEngineType(audioEngineType).setTextMode(textMode).setVceConfigEnabled(profile3.isVceConfigEnabled()).setGcfConfigEnabled(isGcfConfigEnable).setNsdsServiceEnabled(false).setMsrpBearerUsed(profile3.isMsrpBearerUsed()).setSubscriberTimer(profile3.getSubscriberTimer());
        boolean isSubscribeReg2 = isSubscribeReg;
        boolean z2 = isSubscribeReg2;
        UaProfile.Builder timer2 = subscriberTimer.setSubscribeReg(isSubscribeReg2).setUseKeepAlive(useKeepAlive2).setSelfPort(profile3.getSelfPort()).setScmVersion(profile3.getScmVersion()).setMsrpTransType(msrpTransType4).setIsFullCodecOfferRequired(profile3.getFullCodecOfferRequired()).setIsRcsTelephonyFeatureTagRequired(profile3.getRcsTelephonyFeatureTagRequired()).setRcsProfile(rcsProfile).setIsTransportNeeded(profile3.getIsTransportNeeded()).setRat(rat2).setDbrTimer(profile3.getDbrTimer()).setIsTcpGracefulShutdownEnabled(profile3.isTcpGracefulShutdownEnabled()).setTcpRstUacErrorcode(profile3.getTcpRstUacErrorcode()).setTcpRstUasErrorcode(profile3.getTcpRstUasErrorcode()).setPrivacyHeaderRestricted(profile3.getPrivacyHeaderRestricted()).setUsePemHeader(profile3.getUsePemHeader()).setSupportEct(profile3.getSupportEct()).setEarlyMediaRtpTimeoutTimer(profile3.getEarlyMediaRtpTimeoutTimer()).setAddHistinfo(profile3.getAddHistinfo()).setSupportedGeolocationPhase(profile3.getSupportedGeolocationPhase()).setNeedPidfSipMsg(profile3.getNeedPidfSipMsg()).setUseSubcontactWhenResub(profile3.getUseSubcontactWhenResub()).setUseProvisionalResponse100rel(profile3.getUseProvisionalResponse100rel()).setUse183OnProgressIncoming(profile3.getUse183OnProgressIncoming()).setUseQ850causeOn480(profile3.getUseQ850causeOn480()).setSupport183ForIr92v9Precondition(profile3.getSupport183ForIr92v9Precondition()).setSupportImsNotAvailable(profile3.getSupportImsNotAvailable()).setSupportLtePreferred(profile3.getSupportLtePreferred()).setSupportUpgradePrecondition(isSupportUpgradePrecondition3).setSupportReplaceMerge(profile3.getSupportReplaceMerge()).setSupportAccessType(profile3.getSupportAccessType()).setLastPaniHeader(profile3.getLastPaniHeader()).setSelectTransportAfterTcpReset(profile3.getSelectTransportAfterTcpReset()).setSrvccVersion(srvccVersion).setIsSimMobility(Boolean.valueOf(isSimMobility)).setCmcType(profile3.getCmcType()).setVideoCrbtSupportType(profile3.getVideoCrbtSupportType()).setRetryInviteOnTcpReset(profile3.getRetryInviteOnTcpReset()).setEanbleVerstat(profile3.getEnableVerstat()).setTimer1(timerT12).setTimer2(timerT2);
        int timerT43 = timerT4;
        int i3 = timerT43;
        UaProfile.Builder timerK = timer2.setTimer4(timerT43).setTimerA(profile3.getTimerA()).setTimerB(profile3.getTimerB()).setTimerC(profile3.getTimerC()).setTimerD(profile3.getTimerD()).setTimerE(profile3.getTimerE()).setTimerF(profile3.getTimerF()).setTimerG(profile3.getTimerG()).setTimerH(profile3.getTimerH()).setTimerI(profile3.getTimerI()).setTimerJ(profile3.getTimerJ()).setTimerK(profile3.getTimerK());
        int regRetryBaseTime3 = regRetryBaseTime;
        UaProfile.Builder regRetryBaseTime4 = timerK.setRegRetryBaseTime(regRetryBaseTime3);
        int i4 = regRetryBaseTime3;
        int regRetryMaxTime3 = regRetryMaxTime;
        int i5 = regRetryMaxTime3;
        regRetryBaseTime4.setRegRetryMaxTime(regRetryMaxTime3).setSupportDualRcs(RcsUtils.DualRcs.isDualRcsReg()).setPttSupported(ImsUtil.isPttSupported()).setTryReregisterFromKeepalive(profile3.getTryReregisterFromKeepalive()).setSslType(profile3.getSslType()).setSupport199ProvisionalResponse(profile3.getSupport199ProvisionalResponse()).setAcb(acb).setIgnoreDisplayName(ignoreDisplayName).setSupportNetworkInitUssi(profile3.getSupportNetworkInitUssi()).setSendByeForUssi(profile3.getSendByeForUssi()).setSupportRfc6337ForDelayedOffer(profile3.getSupportRfc6337ForDelayedOffer()).setHashAlgoType(profile3.getHashAlgoType());
        if (profile3.isSamsungMdmnEnabled()) {
            builder.setMno(Mno.MDMN);
            String token = profile3.getAccessToken();
            builder.setAccessToken(token);
            if (!TextUtils.isEmpty(cmcSaServerUrl)) {
                builder.setAuthServerUrl(cmcSaServerUrl);
            } else {
                String str19 = cmcSaServerUrl;
            }
            if (!TextUtils.isEmpty(cmcRelayType)) {
                String str20 = token;
                builder.setCmcRelayType(cmcRelayType);
            } else {
                String token2 = cmcRelayType;
            }
            mno2 = mno;
        } else {
            String str21 = cmcSaServerUrl;
            String str22 = cmcRelayType;
            if (!sm.getDevMno().isAus() || !profile3.hasEmergencySupport()) {
                mno2 = mno;
                builder.setMno(mno2);
            } else {
                builder.setMno(Mno.fromName(profile3.getMnoName()));
                mno2 = mno;
            }
        }
        Mno mno4 = mno2;
        builder.setInstanceId(instanceId);
        builder.setUuid(uuid);
        String str23 = str18;
        IMSLog.i(str23, phoneId, "createUserAgent: TransportType=" + msrpTransType2 + " port=" + profile3.getSipPort());
        if (!TextUtils.isEmpty(sessionRefresher2)) {
            StringBuilder sb7 = new StringBuilder();
            String str24 = msrpTransType2;
            sb7.append("createUserAgent: sessionRefresher=");
            sessionRefresher = sessionRefresher2;
            sb7.append(sessionRefresher);
            IMSLog.i(str23, phoneId, sb7.toString());
            builder.setSessionRefresher(sessionRefresher);
        } else {
            String transport3 = msrpTransType2;
            sessionRefresher = sessionRefresher2;
        }
        if (profile3.getExtImpuList().size() > 0) {
            builder.setLinkedImpuList(profile3.getExtImpuList());
        }
        StringBuilder sb8 = new StringBuilder();
        String str25 = str23;
        sb8.append("createUserAgent: hostname=");
        String hostname3 = hostname2;
        sb8.append(hostname3);
        String str26 = sessionRefresher;
        sb8.append(", P-CSCF=");
        String str27 = pcscf;
        String str28 = str25;
        boolean z3 = isPrecondEnabled2;
        String str29 = str28;
        sb8.append(str27);
        IMSLog.s(str29, phoneId, sb8.toString());
        builder.setHostname(hostname3).setPcscfIp(str27);
        builder.setContactDisplayName(profile3.getAppId());
        builder.setPhoneId(phoneId);
        configureTimerTS(profile3, builder);
        UserAgent userAgent = ua2;
        String str30 = hostname3;
        UserAgent ua3 = userAgent;
        ua3.setPdn(pdn);
        ua3.setNetwork(network2);
        String str31 = imMsgTech;
        String pani = this.mPaniGenerator.generate(ua3.getPdn(), profile3.getOperator(), phoneId);
        if (pani == null) {
            IMSLog.e(str29, phoneId, "createUserAgent: pani is null");
            return null;
        }
        builder.setCurPani(pani);
        builder.setIsXqEnabled(ImsXqReporter.isXqEnabled(this.mContext));
        if (SimUtil.isSoftphoneEnabled() || profile3.isVceConfigEnabled()) {
            IMSLog.e(str29, phoneId, "enable subscribe dialog");
            builder.setSubscribeDialogEvent(true);
        }
        ua3.setImsProfile(profile3);
        ua3.setUaProfile(builder.build());
        StringBuilder sb9 = new StringBuilder();
        String str32 = pani;
        sb9.append("createUserAgent:mno=");
        sb9.append(builder.build().getMno());
        IMSLog.i(str29, phoneId, sb9.toString());
        final IRegisterTask iRegisterTask = task;
        final IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable = regHandler;
        ua3.registerListener(new UserAgent.UaEventListener() {
            public void onCreated(UserAgent ua) {
                if (ua == null || phoneId < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(phoneId);
                    sb.append(",UA FAIL");
                    sb.append(ua == null ? ":null" : "");
                    IMSLog.c(LogClass.REGI_UA_CREAT_FAIL, sb.toString());
                }
                if (ua == null) {
                    Log.e(ResipRegistrationManager.LOG_TAG, "failed to create UA. restart imsService.");
                    ResipRegistrationManager.this.mEventLog.logAndAdd(phoneId, RegistrationConstants.RecoveryReason.UA_CREATION_FAILED);
                    System.exit(0);
                    return;
                }
                int i = phoneId;
                if (i < 0) {
                    Log.e(ResipRegistrationManager.LOG_TAG, "onCreated: Invalid phoneId");
                    ResipRegistrationManager.this.mEventLog.logAndAdd(phoneId, RegistrationConstants.RecoveryReason.UA_CREATION_FAILED);
                    System.exit(0);
                    return;
                }
                IMSLog.i(ResipRegistrationManager.LOG_TAG, i, "onCreated: UA handle " + ua.getHandle());
                ImsProfile profile = ua.getImsProfile();
                IGeolocationController geolocationCon = ResipRegistrationManager.this.mImsFramework.getGeolocationController();
                if (!(geolocationCon == null || geolocationCon.getGeolocation() == null || ua.getImsProfile().getSupportedGeolocationPhase() < 2)) {
                    IMSLog.i(ResipRegistrationManager.LOG_TAG, phoneId, "updating geolocation for UA Creation");
                    ua.updateGeolocation(geolocationCon.getGeolocation());
                }
                String pani = ResipRegistrationManager.this.mPaniGenerator.generate(ua.getPdn(), profile.getOperator(), phoneId);
                if (pani == null) {
                    IMSLog.e(ResipRegistrationManager.LOG_TAG, phoneId, "onCreated: pani is null");
                    return;
                }
                if (!TextUtils.isEmpty(pani)) {
                    String lastPani = "";
                    if (pani.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
                        lastPani = ResipRegistrationManager.this.mPaniGenerator.getLastPani(phoneId, profile, new Date());
                        if (ResipRegistrationManager.this.mPaniGenerator.needCellInfoAge(profile)) {
                            ua.updateTimeInPlani(ResipRegistrationManager.this.mPaniGenerator.getTimeInPlani(phoneId));
                        }
                    }
                    ua.updatePani(pani, lastPani);
                    iRegisterTask.setPaniSet(pani, lastPani);
                }
                if (!ua.getImsProfile().isUicclessEmergency() || !ua.getImsProfile().hasEmergencySupport()) {
                    Log.i(ResipRegistrationManager.LOG_TAG, "trigger registration.");
                    ua.register();
                    ua.updateCallwaitingStatus();
                    return;
                }
                iRegistrationHandlerNotifiable.notifyEmergencyReady(profile.getId());
            }

            public void onRegistered(UserAgent ua) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onRegistered: UA handle " + ua.getHandle());
                ImsRegistration reg = ua.getImsRegistration();
                if (reg != null) {
                    iRegistrationHandlerNotifiable.notifyRegistered(ua.getPhoneId(), ua.getImsProfile().getId(), reg);
                }
            }

            public void onDeregistered(UserAgent ua, boolean isRequestedDeregi, SipError error, int retryAfter) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onDeregistered: UA handle " + ua.getHandle() + " isRequestedDeregi " + isRequestedDeregi + " error " + error + " retryAfter " + retryAfter);
                Bundle bundle = new Bundle();
                bundle.putParcelable("error", error);
                bundle.putInt("phoneId", ua.getPhoneId());
                bundle.putInt("profileId", ua.getImsProfile().getId());
                bundle.putBoolean("isRequestedDeregi", isRequestedDeregi);
                bundle.putInt("retryAfter", retryAfter);
                iRegistrationHandlerNotifiable.notifyDeRegistered(bundle);
            }

            public void onRegistrationError(UserAgent ua, SipError error, int retryAfter) {
                Log.e(ResipRegistrationManager.LOG_TAG, "onRegistrationError: UA handle " + ua.getHandle() + " errorCode " + error + " retryAfter " + retryAfter);
                Bundle bundle = new Bundle();
                bundle.putInt("phoneId", ua.getPhoneId());
                bundle.putInt("profileId", ua.getImsProfile().getId());
                bundle.putInt(EucTestIntent.Extras.HANDLE, ua.getHandle());
                bundle.putParcelable("error", error);
                bundle.putInt("retryAfter", retryAfter);
                iRegistrationHandlerNotifiable.notifyRegistrationError(bundle);
            }

            public void onSubscribeError(UserAgent ua, SipError error) {
                Log.e(ResipRegistrationManager.LOG_TAG, "onSubscribeError: UA handle " + ua.getHandle() + " errorCode " + error);
                Bundle bundle = new Bundle();
                bundle.putInt("phoneId", ua.getPhoneId());
                bundle.putInt("profileId", ua.getImsProfile().getId());
                bundle.putParcelable("error", error);
                iRegistrationHandlerNotifiable.notifySubscribeError(bundle);
            }

            public void onUpdatePani(UserAgent ua) {
                new Thread(new Runnable(ua) {
                    public final /* synthetic */ UserAgent f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ResipRegistrationManager.AnonymousClass2.this.lambda$onUpdatePani$0$ResipRegistrationManager$2(this.f$1);
                    }
                }).start();
            }

            public /* synthetic */ void lambda$onUpdatePani$0$ResipRegistrationManager$2(UserAgent ua) {
                Log.i(ResipRegistrationManager.LOG_TAG, "Sync CellInfo with RIL");
                ResipRegistrationManager.this.mTelephonyManager.getAllCellInfo();
                ResipRegistrationManager.this.mPdnController.getCellLocation(ua.getPhoneId(), true);
            }

            public void onRefreshRegNotification(int mHandle) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onRefreshRegNotification : handle" + mHandle);
                iRegistrationHandlerNotifiable.notifyRefreshRegNotification(mHandle);
            }

            public void onContactActivated(UserAgent ua, int handle) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onContactActivated: handle-" + handle);
                iRegistrationHandlerNotifiable.notifyContactActivated(ua.getPhoneId(), ua.getImsProfile().getId());
            }

            public void onRegEventContactUriNotification(int handle, List<ImsUri> contactUriList, int isRegi, String contactUriType) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onRegEventContactUriNotification: handle-" + handle);
                Bundle bundle = new Bundle();
                bundle.putInt(EucTestIntent.Extras.HANDLE, handle);
                bundle.putParcelableArrayList("contact_uri_list", new ArrayList(contactUriList));
                bundle.putInt("isRegi", isRegi);
                bundle.putString("contactUriType", contactUriType);
                iRegistrationHandlerNotifiable.notifyRegEventContactUriNotification(bundle);
            }
        });
        ua3.create();
        return ua3;
    }

    private Long getP2pNetworkHandle(int cmcType) {
        IMSLog.d(LOG_TAG, "getP2pNetworkHandle, cmcType: " + cmcType);
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        long netId = 0L;
        for (Network network : cm.getAllNetworks()) {
            NetworkInfo ni = cm.getNetworkInfo(network);
            if (ni != null) {
                IMSLog.d(LOG_TAG, "getP2pNetworkHandle, getTypeName[" + ni.getTypeName() + "], getSubtypeName[" + ni.getSubtypeName() + "], getType[" + ni.getType() + "], getSubtype[" + ni.getSubtype() + "], isConnected[" + ni.isConnected() + "]");
                netId = Long.valueOf(network.getNetworkHandle());
                StringBuilder sb = new StringBuilder();
                sb.append("netId (NetworkHandle): ");
                sb.append(netId);
                IMSLog.d(LOG_TAG, sb.toString());
                if ((cmcType == 7 || cmcType == 8) && ni.getType() == ConnectivityManagerExt.TYPE_WIFI_P2P) {
                    IMSLog.d(LOG_TAG, "Found netId for cmcType: " + cmcType + ", netId: " + netId);
                    return netId;
                }
            }
        }
        return netId;
    }

    private String getPdnName(int pdn) {
        if (pdn == -1) {
            return "default";
        }
        if (pdn == 0) {
            return "internet";
        }
        if (pdn == 1) {
            return "wifi";
        }
        if (pdn == 5) {
            return "internet";
        }
        if (pdn == 11) {
            return DeviceConfigManager.IMS;
        }
        if (pdn == 15) {
            return "emergency";
        }
        return "unknown(" + pdn + ")";
    }

    private void configureRCS(IRegisterTask task, Mno simMno, boolean requestregi, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "configureRCS:");
        RcsConfig config = RcsPolicyManager.getRcsConfig(this.mContext, simMno, task.getProfile(), requestregi, phoneId);
        if (config == null) {
            return;
        }
        if (!requestregi) {
            StackIF.getInstance().configRCSOff(phoneId, config.getSuspendUser());
        } else {
            StackIF.getInstance().configRCS(phoneId, config);
        }
    }

    public void configure(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "configure:");
        StackIF stackIf = StackIF.getInstance();
        String imei = this.mTelephonyManager.getDeviceId();
        if (!TextUtils.isEmpty(imei)) {
            imei = IRegistrationManager.getFormattedDeviceId(imei);
        }
        stackIf.configRegistration(phoneId, imei);
        stackIf.configSrvcc(phoneId, this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.Call.SRVCC_VERSION, 0));
    }

    public void setSilentLogEnabled(boolean onoff) {
        Log.i(LOG_TAG, "setSilentLogEnabled:");
        StackIF.getInstance().setSilentLogEnabled(onoff);
    }

    public boolean isUserAgentInRegistered(IRegisterTask task) {
        return ((Boolean) Optional.ofNullable(this.mUaList.get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(task.getProfile().getId(), task.getPhoneId())))).map($$Lambda$ResipRegistrationManager$P4U0oIVZ2BmiD4O3yWjCGjP4qE.INSTANCE).orElse(false)).booleanValue();
    }

    public void sendDnsQuery(int handle, String intf, String hostname, List<String> dnses, String type, String transport, String family, long netId) {
        StringBuilder sb = new StringBuilder();
        sb.append("sendDnsQuery: handle ");
        int i = handle;
        sb.append(handle);
        Log.i(LOG_TAG, sb.toString());
        StackIF.getInstance().sendDnsQuery(handle, intf, hostname, dnses, type, transport, family, netId);
    }

    public void updatePani(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        PaniGenerator paniGenerator = this.mPaniGenerator;
        int pdnType = task.getPdnType();
        String pani = paniGenerator.generate(pdnType, profile.getMcc() + profile.getMnc(), phoneId);
        if (!TextUtils.isEmpty(pani)) {
            this.mPaniGenerator.setLkcForLastPani(phoneId, pani, profile, new Date());
            if (task.getUserAgent() != null) {
                UserAgent ua = (UserAgent) task.getUserAgent();
                String lastPani = "";
                if (pani.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
                    lastPani = this.mPaniGenerator.getLastPani(phoneId, profile, new Date());
                    if (this.mPaniGenerator.needCellInfoAge(profile)) {
                        ua.updateTimeInPlani(this.mPaniGenerator.getTimeInPlani(phoneId));
                    }
                }
                ua.getUaProfile().setCurPani(pani);
                ua.updatePani(pani, lastPani);
                task.setPaniSet(pani, lastPani);
            }
        }
    }

    public void updateTimeInPlani(int phoneId, ImsProfile profile) {
        String plani = this.mPaniGenerator.getLastPani(phoneId, profile, new Date());
        if (this.mPaniGenerator.needCellInfoAge(profile) && !TextUtils.isEmpty(plani)) {
            long time = System.currentTimeMillis() / 1000;
            int cid = this.mPaniGenerator.getCid(phoneId);
            boolean isChanged = this.mPaniGenerator.isChangedPlani(phoneId, plani);
            IMSLog.s(LOG_TAG, phoneId, "updateTimeInPlani: plani " + plani + ", time " + time);
            if (this.mPaniGenerator.getTimeInPlani(phoneId) == 0) {
                this.mPaniGenerator.setTimeInPlani(phoneId, time);
            }
            if (cid != 0 && isChanged) {
                this.mPaniGenerator.setTimeInPlani(phoneId, time);
                Log.i(LOG_TAG, "updateTimeInPlani: plani " + plani + ", time " + time);
            }
        }
    }

    public void removePreviousLastPani(int phoneId) {
        this.mPaniGenerator.removePreviousPlani(phoneId);
    }

    public void updateRat(IRegisterTask task, int network) {
        if (task.getUserAgent() != null) {
            task.getUserAgent().updateRat(network);
        }
    }

    public void updateVceConfig(IRegisterTask task, boolean config) {
        if (task.getUserAgent() == null) {
            Log.i(LOG_TAG, "updateVceConfig: no pending task, simply return");
        } else {
            task.getUserAgent().updateVceConfig(config);
        }
    }

    public void updateGeolocation(IRegisterTask task, LocationInfo geolocation) {
        if (task.getUserAgent() == null) {
            Log.i(LOG_TAG, "updateGeolocation: ua is null. return");
        } else {
            task.getUserAgent().updateGeolocation(geolocation);
        }
    }

    public void dump() {
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Dump of UserAgents:");
        for (UserAgent ua : this.mUaList.values()) {
            ImsProfile profile = ua.getImsProfile();
            IMSLog.dump(LOG_TAG, "UserAgent [" + ua.getHandle() + "] State: [" + ua.getStateName() + "], Profile: [" + profile.getName() + "(#" + profile.getId() + ")]");
        }
        StackIF.getInstance().dump();
    }

    /* access modifiers changed from: protected */
    public int getAudioEngineType() {
        return DeviceUtil.getModemBoardName().startsWith("SHANNON") ? 1 : 0;
    }

    public void removeUserAgent(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "removeUserAgent: task " + task.getProfile().getName());
        int uaRegId = IRegistrationManager.getRegistrationInfoId(task.getProfile().getId(), task.getPhoneId());
        UserAgent ua = this.mUaList.get(Integer.valueOf(uaRegId));
        if (ua == null) {
            IMSLog.e(LOG_TAG, task.getPhoneId(), "removeUserAgent: UserAgent null");
            task.clearUserAgent();
            return;
        }
        int phoneId2 = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId2, "removeUserAgent: UserAgent handle " + ua.getHandle());
        ua.terminate();
        this.mUaList.remove(Integer.valueOf(uaRegId));
        task.clearUserAgent();
    }

    public UserAgent getUserAgentByRegId(int regId) {
        for (UserAgent ua : this.mUaList.values()) {
            ImsRegistration reg = ua.getImsRegistration();
            if (reg != null && reg.getHandle() == regId) {
                return ua;
            }
        }
        return null;
    }

    public String getImsiByUserAgent(IUserAgent ua) {
        if (ua != null) {
            return (String) Optional.ofNullable(this.mSimManagers.get(ua.getPhoneId())).map($$Lambda$zdzd6Q66HcnL6ih8zJVTBJogbp0.INSTANCE).orElse((Object) null);
        }
        IMSLog.e(LOG_TAG, "getImsiByUserAgent: ua is null!");
        return null;
    }

    public String getImsiByUserAgentHandle(int handle) {
        UserAgent ua = getUserAgent(handle);
        if (ua != null) {
            return getImsiByUserAgent(ua);
        }
        return null;
    }

    public IUserAgent getUserAgentByImsi(String service, String imsi) {
        if (imsi == null || imsi.equals("")) {
            return getUserAgent(service, (ImsUri) null);
        }
        IMSLog.s(LOG_TAG, "getUserAgentByImsi : Argument imsi = " + imsi);
        for (UserAgent ua : this.mUaList.values()) {
            ImsRegistration reg = ua.getImsRegistration();
            ImsProfile profile = ua.getImsProfile();
            if (profile != null && !profile.hasEmergencySupport() && reg != null && reg.hasService(service)) {
                int phoneId = reg.getPhoneId();
                int subId = SimUtil.getSubId(phoneId);
                Log.i(LOG_TAG, "getUserAgentByImsi, phoneId=" + phoneId + ",subId=" + subId);
                String imsiReg = this.mTelephonyManager.getSubscriberId(subId);
                if (imsiReg != null) {
                    IMSLog.s(LOG_TAG, phoneId, "getUserAgentByImsi imsi = " + imsiReg);
                    if (!imsiReg.equals("") && imsiReg.equals(imsi)) {
                        return ua;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    public IUserAgent getUserAgent(String service) {
        return getUserAgent(service, (ImsUri) null);
    }

    public IUserAgent getUserAgent(String service, int phoneId) {
        Log.i(LOG_TAG, "getUserAgent, phoneId=" + phoneId);
        for (UserAgent ua : this.mUaList.values()) {
            ImsRegistration reg = ua.getImsRegistration();
            ImsProfile profile = ua.getImsProfile();
            if (profile != null && !profile.hasEmergencySupport() && reg != null && reg.hasService(service)) {
                Log.i(LOG_TAG, "getUserAgent, reg.getPhoneId()=" + reg.getPhoneId());
                if (reg.getPhoneId() == phoneId) {
                    return ua;
                }
            }
        }
        return null;
    }

    private UserAgent getUserAgent(String service, ImsUri impu) {
        for (UserAgent ua : this.mUaList.values()) {
            ImsRegistration reg = ua.getImsRegistration();
            ImsProfile profile = ua.getImsProfile();
            if (profile != null && !profile.hasEmergencySupport() && reg != null && reg.hasService(service)) {
                if (impu == null) {
                    return ua;
                }
                for (NameAddr addr : reg.getImpuList()) {
                    if (impu.equals(addr.getUri())) {
                        return ua;
                    }
                }
                continue;
            }
        }
        if (impu != null) {
            return getUserAgent(service, (ImsUri) null);
        }
        return null;
    }

    public UserAgent getUserAgent(int handle) {
        for (UserAgent ua : this.mUaList.values()) {
            if (ua.getHandle() == handle) {
                return ua;
            }
        }
        return null;
    }

    public UserAgent getUserAgentOnPdn(int pdn, int phoneId) {
        for (UserAgent ua : this.mUaList.values()) {
            if (ua.getPdn() == pdn && ua.getPhoneId() == phoneId) {
                return ua;
            }
        }
        return null;
    }

    public UserAgent[] getUserAgentByPhoneId(int phoneId, String service) {
        List<UserAgent> res = new ArrayList<>();
        for (UserAgent ua : this.mUaList.values()) {
            if (ua.getPhoneId() == phoneId) {
                ImsRegistration reg = ua.getImsRegistration();
                ImsProfile profile = ua.getImsProfile();
                if (profile != null && !profile.hasEmergencySupport() && reg != null && reg.hasService(service)) {
                    res.add(ua);
                }
            }
        }
        return (UserAgent[]) res.toArray(new UserAgent[0]);
    }
}
