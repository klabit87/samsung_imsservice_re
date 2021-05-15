package com.sec.internal.ims.core.handler.secims;

import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UaProfile {
    public static final int TEXT_MODE_CS_TTY = 1;
    public static final int TEXT_MODE_NONE = 0;
    public static final int TEXT_MODE_PS_TTY = 2;
    public static final int TEXT_MODE_RTT = 3;
    List<String> acb;
    String accessToken;
    boolean addHistinfo;
    int audioEngineType;
    String authServerUrl;
    String authalg;
    CallProfile callProfile;
    Capabilities capabilities;
    String cmcRelayType;
    int cmcType;
    String contactDisplayName;
    int controlDscp;
    String curPani;
    int dbrTimer;
    String displayName;
    String domain;
    int earlyMediaRtpTimeoutTimer;
    boolean enableVerstat;
    String encralg;
    int hashAlgoType;
    String hostname;
    String iface;
    boolean ignoreDisplayName;
    String imMsgTech;
    String impi;
    String impu;
    String instanceId;
    boolean isCdmalessEnabled;
    boolean isEmergencyProfile;
    boolean isEnableGruu;
    boolean isEnableSessionId;
    boolean isFullCodecOfferRequired;
    boolean isGcfConfigEnabled;
    boolean isMsrpBearerUsed;
    boolean isNsdsServiceEnabled;
    boolean isPrecondEnabled;
    boolean isPrecondInitialSendrecv;
    boolean isPttSupported;
    boolean isRcsTelephonyFeatureTagRequired;
    boolean isSimMobility;
    boolean isSoftphoneEnabled;
    boolean isSubscribeReg;
    boolean isTcpGracefulShutdownEnabled;
    boolean isTlsEnabled;
    boolean isTransportNeeded;
    boolean isVceConfigEnabled;
    boolean isXqEnabled;
    boolean isipsec;
    boolean issipoutbound;
    String lastPaniHeader;
    List<String> linkedImpuList;
    boolean mIsServerHeaderEnabled;
    boolean mIsWifiPreConditionEnabled;
    boolean mUseCompactHeader;
    int minSe;
    Mno mno;
    String msrpTransType;
    int mssSize;
    int needPidfSipMsg;
    long netId;
    String password;
    String pcscfIp;
    int pcscfPort;
    String pdn;
    int phoneId;
    String preferredId;
    String privacyHeaderRestricted;
    int profileId;
    int qparam;
    int rat;
    int rcsProfile;
    String realm;
    int regExpires;
    int regRetryBaseTime;
    int regRetryMaxTime;
    String registeralgo;
    ImsUri.UriType remoteuritype;
    boolean retryInviteOnTcpReset;
    int ringbackTimer;
    int ringingTimer;
    int scmVersion;
    String selectTransportAfterTcpReset;
    int selfPort;
    boolean sendByeForUssi;
    Set<String> serviceList;
    int sessionExpires;
    String sessionRefresher;
    int sipMobility;
    int srvccVersion;
    int sslType;
    int subscriberTimer;
    boolean support183ForIr92v9Precondition;
    boolean support199ProvisionalResponse;
    boolean supportAccessType;
    boolean supportDualRcs;
    boolean supportEct;
    boolean supportImsNotAvailable;
    boolean supportLtePreferred;
    boolean supportNetworkInitUssi;
    boolean supportReplaceMerge;
    boolean supportRfc6337ForDelayedOffer;
    boolean supportSubscribeDialogEvent;
    boolean supportUpgradePrecondition;
    int supportedGeolocationPhase;
    int tcpRstUacErrorcode;
    int tcpRstUasErrorcode;
    int textMode;
    int timer1;
    int timer2;
    int timer4;
    int timerA;
    int timerB;
    int timerC;
    int timerD;
    int timerE;
    int timerF;
    int timerG;
    int timerH;
    int timerI;
    int timerJ;
    int timerK;
    int timerTS;
    String transtype;
    boolean tryReregisterFromKeepalive;
    boolean use183OnProgressIncoming;
    boolean useKeepAlive;
    boolean usePemHeader;
    boolean useProvisionalResponse100rel;
    boolean useQ850causeOn480;
    boolean useSubcontactWhenResub;
    String userAgent;
    String uuid;
    int videoCrbtSupportType;

    public int getProfileId() {
        return this.profileId;
    }

    public String getIface() {
        return this.iface;
    }

    public long getNetId() {
        return this.netId;
    }

    public String getPdn() {
        return this.pdn;
    }

    public String getImpi() {
        return this.impi;
    }

    public String getImpu() {
        return this.impu;
    }

    public void setImpu(String impu2) {
        this.impu = impu2;
    }

    public List<String> getLinkedImpuList() {
        return this.linkedImpuList;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isIsSipOutbound() {
        return this.issipoutbound;
    }

    public int getQparam() {
        return this.qparam;
    }

    public int getControlDscp() {
        return this.controlDscp;
    }

    public String getTranstype() {
        return this.transtype;
    }

    public boolean isIsEmergencyProfile() {
        return this.isEmergencyProfile;
    }

    public boolean isIsipsec() {
        return this.isipsec;
    }

    public boolean isWifiPreConditionEnabled() {
        return this.mIsWifiPreConditionEnabled;
    }

    public boolean isServerHeaderEnabled() {
        return this.mIsServerHeaderEnabled;
    }

    public boolean shouldUseCompactHeader() {
        return this.mUseCompactHeader;
    }

    public String getEncralg() {
        return this.encralg;
    }

    public String getAuthalg() {
        return this.authalg;
    }

    public boolean isTlsEnabled() {
        return this.isTlsEnabled;
    }

    public String getRegisteralgo() {
        return this.registeralgo;
    }

    public String getpreferredId() {
        return this.preferredId;
    }

    public ImsUri.UriType getRemoteuritype() {
        return this.remoteuritype;
    }

    public String getPcscfIp() {
        return this.pcscfIp;
    }

    public void setPcscfIp(String ip) {
        this.pcscfIp = ip;
    }

    public int getPcscfPort() {
        return this.pcscfPort;
    }

    public int getRegExpires() {
        return this.regExpires;
    }

    public Set<String> getServiceList() {
        return this.serviceList;
    }

    public void setServiceList(Set<String> svcs) {
        this.serviceList = svcs;
    }

    public void setLinkedImpuList(List<String> linkedImpuLists) {
        this.linkedImpuList = linkedImpuLists;
    }

    public Capabilities getOwnCapabilities() {
        return this.capabilities;
    }

    public void setOwnCapabilities(Capabilities cap) {
        try {
            this.capabilities = cap.clone();
        } catch (CloneNotSupportedException e) {
            this.capabilities = null;
        }
    }

    public Mno getMno() {
        return this.mno;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public int getSipMobility() {
        return this.sipMobility;
    }

    public void setCallProfile(CallProfile cp) {
        this.callProfile = cp;
    }

    public CallProfile getCallProfile() {
        return this.callProfile;
    }

    public int getTimer1() {
        return this.timer1;
    }

    public int getTimer2() {
        return this.timer2;
    }

    public int getTimer4() {
        return this.timer4;
    }

    public int getTimerA() {
        return this.timerA;
    }

    public int getTimerB() {
        return this.timerB;
    }

    public int getTimerC() {
        return this.timerC;
    }

    public int getTimerD() {
        return this.timerD;
    }

    public int getTimerE() {
        return this.timerE;
    }

    public int getTimerF() {
        return this.timerF;
    }

    public int getTimerG() {
        return this.timerG;
    }

    public int getTimerH() {
        return this.timerH;
    }

    public int getTimerI() {
        return this.timerI;
    }

    public int getTimerJ() {
        return this.timerJ;
    }

    public int getTimerK() {
        return this.timerK;
    }

    public int getTimerTS() {
        return this.timerTS;
    }

    public int getMssSize() {
        return this.mssSize;
    }

    public int getRingbackTimer() {
        return this.ringbackTimer;
    }

    public int getRingingTimer() {
        return this.ringingTimer;
    }

    public boolean getIsEnableGruu() {
        return this.isEnableGruu;
    }

    public boolean getIsEnableSessionId() {
        return this.isEnableSessionId;
    }

    public int getAudioEngineType() {
        return this.audioEngineType;
    }

    public int getTextMode() {
        return this.textMode;
    }

    public String getCurPani() {
        return this.curPani;
    }

    public void setCurPani(String pani) {
        this.curPani = pani;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getUuid() {
        return this.uuid;
    }

    public boolean getisSubscribeReg() {
        return this.isSubscribeReg;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getAuthServerUrl() {
        return this.authServerUrl;
    }

    public boolean getIsXqEnabled() {
        return this.isXqEnabled;
    }

    public int getRcsProfile() {
        return this.rcsProfile;
    }

    public boolean getIsTransportNeeded() {
        return this.isTransportNeeded;
    }

    public int getRat() {
        return this.rat;
    }

    public int getDbrTimer() {
        return this.dbrTimer;
    }

    public boolean getIsTcpGracefulShutdownEnabled() {
        return this.isTcpGracefulShutdownEnabled;
    }

    public int getTcpRstUacErrorcode() {
        return this.tcpRstUacErrorcode;
    }

    public int getTcpRstUasErrorcode() {
        return this.tcpRstUasErrorcode;
    }

    public String getPrivacyHeaderRestricted() {
        return this.privacyHeaderRestricted;
    }

    public boolean getUsePemHeader() {
        return this.usePemHeader;
    }

    public int getPhoneId() {
        return this.phoneId;
    }

    public boolean getSupportEct() {
        return this.supportEct;
    }

    public boolean getAddHistinfo() {
        return this.addHistinfo;
    }

    public int getEarlyMediaRtpTimeoutTimer() {
        return this.earlyMediaRtpTimeoutTimer;
    }

    public int getSupportedGeolocationPhase() {
        return this.supportedGeolocationPhase;
    }

    public int getNeedPidfSipMsg() {
        return this.needPidfSipMsg;
    }

    public boolean getUseProvisionalResponse100rel() {
        return this.useProvisionalResponse100rel;
    }

    public boolean getUse183OnProgressIncoming() {
        return this.use183OnProgressIncoming;
    }

    public boolean getUseQ850causeOn480() {
        return this.useQ850causeOn480;
    }

    public boolean getSupport183ForIr92v9Precondition() {
        return this.support183ForIr92v9Precondition;
    }

    public boolean getSupportImsNotAvailable() {
        return this.supportImsNotAvailable;
    }

    public boolean getSupportLtePreferred() {
        return this.supportLtePreferred;
    }

    public boolean getUseSubcontactWhenResub() {
        return this.useSubcontactWhenResub;
    }

    public boolean getSupportUpgradePrecondition() {
        return this.supportUpgradePrecondition;
    }

    public boolean getSupportReplaceMerge() {
        return this.supportReplaceMerge;
    }

    public boolean getSupportAccessType() {
        return this.supportAccessType;
    }

    public String getLastPaniHeader() {
        return this.lastPaniHeader;
    }

    public String getSelectTransportAfterTcpReset() {
        return this.selectTransportAfterTcpReset;
    }

    public int getSrvccVersion() {
        return this.srvccVersion;
    }

    public boolean getIsSimMobility() {
        return this.isSimMobility;
    }

    public int getCmcType() {
        return this.cmcType;
    }

    public String getCmcRelayType() {
        return this.cmcRelayType;
    }

    public int getVideoCrbtSupportType() {
        return this.videoCrbtSupportType;
    }

    public boolean getRetryInviteOnTcpReset() {
        return this.retryInviteOnTcpReset;
    }

    public int getRegRetryBaseTime() {
        return this.regRetryBaseTime;
    }

    public int getRegRetryMaxTime() {
        return this.regRetryMaxTime;
    }

    public boolean getEnableVerstat() {
        return this.enableVerstat;
    }

    public boolean getSupportDualRcs() {
        return this.supportDualRcs;
    }

    public boolean getTryReregisterFromKeepalive() {
        return this.tryReregisterFromKeepalive;
    }

    public boolean getIsPttSupported() {
        return this.isPttSupported;
    }

    public int getSslType() {
        return this.sslType;
    }

    public boolean getSupport199ProvisionalResponse() {
        return this.support199ProvisionalResponse;
    }

    public List<String> getAcb() {
        return this.acb;
    }

    public boolean isDisplayNameIgnored() {
        return this.ignoreDisplayName;
    }

    public boolean getSupportNetworkInitUssi() {
        return this.supportNetworkInitUssi;
    }

    public boolean getSendByeForUssi() {
        return this.sendByeForUssi;
    }

    public boolean getSupportRfc6337ForDelayedOffer() {
        return this.supportRfc6337ForDelayedOffer;
    }

    public int getHashAlgoTypeType() {
        return this.hashAlgoType;
    }

    public UaProfile(Builder builder) {
        this.profileId = builder.profileId;
        this.iface = builder.iface;
        this.netId = builder.netId;
        this.pdn = builder.pdn;
        this.impi = builder.impi;
        this.impu = builder.impu;
        this.linkedImpuList = builder.impuList;
        this.domain = builder.domain;
        this.password = builder.password;
        this.issipoutbound = builder.issipoutbound;
        this.qparam = builder.qparam;
        this.controlDscp = builder.controlDscp;
        this.transtype = builder.transtype;
        this.isEmergencyProfile = builder.isemergencysupport;
        this.isipsec = builder.isipsec;
        this.mIsWifiPreConditionEnabled = builder.mIsWifiPreConditionEnabled;
        this.mIsServerHeaderEnabled = builder.mIsServerHeaderEnabled;
        this.mUseCompactHeader = builder.mUseCompactHeader;
        this.encralg = builder.encralg;
        this.authalg = builder.authalg;
        this.isTlsEnabled = builder.isenabletlsforsip;
        this.registeralgo = builder.registeralgo;
        this.preferredId = builder.preferredId;
        this.remoteuritype = builder.remoteuritype;
        this.mno = builder.mno;
        this.hostname = builder.hostname;
        this.pcscfIp = builder.pcscfIp;
        this.pcscfPort = builder.pcscfPort;
        this.serviceList = builder.serviceList;
        try {
            this.capabilities = builder.capabilities.clone();
        } catch (CloneNotSupportedException | NullPointerException e) {
            this.capabilities = null;
        }
        this.isPrecondEnabled = builder.isprecondenabled;
        this.isPrecondInitialSendrecv = builder.ispreconinitialsendrecv;
        this.isRcsTelephonyFeatureTagRequired = builder.isRcsTelephonyFeatureTagRequired;
        this.isFullCodecOfferRequired = builder.isFullCodecOfferRequired;
        this.sessionExpires = builder.sessionexpires;
        this.minSe = builder.minSe;
        this.sessionRefresher = builder.sessionrefresher;
        this.regExpires = builder.regExpires;
        this.userAgent = builder.userAgent;
        this.displayName = builder.displayName;
        this.contactDisplayName = builder.contactDisplayName;
        this.uuid = builder.uuid;
        this.instanceId = builder.instanceId;
        this.realm = builder.realm;
        this.imMsgTech = builder.imMsgTech;
        this.callProfile = builder.callProfile;
        this.mssSize = builder.mssSize;
        this.sipMobility = builder.sipMobility;
        this.timer1 = builder.timer1;
        this.timer2 = builder.timer2;
        this.timer4 = builder.timer4;
        this.timerA = builder.timerA;
        this.timerB = builder.timerB;
        this.timerC = builder.timerC;
        this.timerD = builder.timerD;
        this.timerE = builder.timerE;
        this.timerF = builder.timerF;
        this.timerG = builder.timerG;
        this.timerH = builder.timerH;
        this.timerI = builder.timerI;
        this.timerJ = builder.timerJ;
        this.timerK = builder.timerK;
        this.timerTS = builder.timerTS;
        this.isSoftphoneEnabled = builder.isSoftphoneEnabled;
        this.isCdmalessEnabled = builder.isCdmalessEnabled;
        this.ringbackTimer = builder.ringbackTimer;
        this.ringingTimer = builder.ringingTimer;
        this.isEnableGruu = builder.isEnableGruu;
        this.isEnableSessionId = builder.isEnableSessionId;
        this.audioEngineType = builder.audioEngineType;
        this.curPani = builder.curPani;
        this.isVceConfigEnabled = builder.isVceConfigEnabled;
        this.isGcfConfigEnabled = builder.isGcfConfigEnabled;
        this.isNsdsServiceEnabled = builder.isNsdsServiceEnabled;
        this.isMsrpBearerUsed = builder.isMsrpBearerUsed;
        this.subscriberTimer = builder.subscriberTimer;
        this.isSubscribeReg = builder.isSubscribeReg;
        this.accessToken = builder.accessToken;
        this.authServerUrl = builder.authServerUrl;
        this.useKeepAlive = builder.useKeepAlive;
        this.selfPort = builder.selfPort;
        this.scmVersion = builder.scmVersion;
        this.msrpTransType = builder.msrpTransType;
        this.isXqEnabled = builder.isXqEnabled;
        this.textMode = builder.textMode;
        this.rcsProfile = builder.rcsProfile;
        this.isTransportNeeded = builder.isTransportNeeded;
        this.rat = builder.rat;
        this.dbrTimer = builder.dbrTimer;
        this.isTcpGracefulShutdownEnabled = builder.isTcpGracefulShutdownEnabled;
        this.tcpRstUacErrorcode = builder.tcpRstUacErrorcode;
        this.tcpRstUasErrorcode = builder.tcpRstUasErrorcode;
        this.privacyHeaderRestricted = builder.privacyHeaderRestricted;
        this.usePemHeader = builder.usePemHeader;
        this.phoneId = builder.phoneId;
        this.supportEct = builder.supportEct;
        this.earlyMediaRtpTimeoutTimer = builder.earlyMediaRtpTimeoutTimer;
        this.addHistinfo = builder.addHistinfo;
        this.supportedGeolocationPhase = builder.supportedGeolocationPhase;
        this.needPidfSipMsg = builder.needPidfSipMsg;
        this.useProvisionalResponse100rel = builder.useProvisionalResponse100rel;
        this.use183OnProgressIncoming = builder.use183OnProgressIncoming;
        this.useQ850causeOn480 = builder.useQ850causeOn480;
        this.support183ForIr92v9Precondition = builder.support183ForIr92v9Precondition;
        this.supportImsNotAvailable = builder.supportImsNotAvailable;
        this.supportLtePreferred = builder.supportLtePreferred;
        this.useSubcontactWhenResub = builder.useSubcontactWhenResub;
        this.supportUpgradePrecondition = builder.supportUpgradePrecondition;
        this.supportReplaceMerge = builder.supportReplaceMerge;
        this.supportAccessType = builder.supportAccessType;
        this.lastPaniHeader = builder.lastPaniHeader;
        this.selectTransportAfterTcpReset = builder.selectTransportAfterTcpReset;
        this.srvccVersion = builder.srvccVersion;
        this.supportSubscribeDialogEvent = builder.supportScribeDialogEvent;
        this.isSimMobility = builder.isSimMobility;
        this.cmcType = builder.cmcType;
        this.cmcRelayType = builder.cmcRelayType;
        this.videoCrbtSupportType = builder.videoCrbtSupportType;
        this.retryInviteOnTcpReset = builder.retryInviteOnTcpReset;
        this.enableVerstat = builder.enableVerstat;
        this.regRetryBaseTime = builder.regRetryBaseTime;
        this.regRetryMaxTime = builder.regRetryMaxTime;
        this.supportDualRcs = builder.supportDualRcs;
        this.tryReregisterFromKeepalive = builder.tryReregisterFromKeepalive;
        this.isPttSupported = builder.isPttSupported;
        this.sslType = builder.sslType;
        this.support199ProvisionalResponse = builder.support199ProvisionalResponse;
        this.acb = builder.acb;
        this.ignoreDisplayName = builder.ignoreDisplayName;
        this.supportNetworkInitUssi = builder.supportNetworkInitUssi;
        this.sendByeForUssi = builder.sendByeForUssi;
        this.supportRfc6337ForDelayedOffer = builder.supportRfc6337ForDelayedOffer;
        this.hashAlgoType = builder.hashAlgoType;
    }

    public static class Builder {
        List<String> acb;
        String accessToken;
        boolean addHistinfo;
        int audioEngineType;
        String authServerUrl;
        String authalg;
        CallProfile callProfile;
        Capabilities capabilities;
        String cmcRelayType;
        int cmcType;
        String contactDisplayName;
        int controlDscp;
        String curPani;
        int dbrTimer;
        String displayName;
        String domain;
        int earlyMediaRtpTimeoutTimer;
        boolean enableVerstat;
        String encralg;
        int hashAlgoType;
        String hostname;
        String iface;
        boolean ignoreDisplayName;
        String imMsgTech;
        String impi;
        String impu;
        List<String> impuList;
        String instanceId;
        boolean isCdmalessEnabled;
        boolean isEnableGruu;
        boolean isEnableSessionId;
        boolean isFullCodecOfferRequired;
        boolean isGcfConfigEnabled;
        boolean isMsrpBearerUsed;
        boolean isNsdsServiceEnabled;
        boolean isPttSupported;
        boolean isRcsTelephonyFeatureTagRequired;
        boolean isSimMobility;
        boolean isSoftphoneEnabled;
        boolean isSubscribeReg;
        boolean isTcpGracefulShutdownEnabled;
        boolean isTransportNeeded;
        boolean isVceConfigEnabled;
        boolean isXqEnabled;
        boolean isemergencysupport;
        boolean isenabletlsforsip;
        boolean isipsec;
        boolean isprecondenabled;
        boolean ispreconinitialsendrecv;
        boolean issipoutbound;
        String lastPaniHeader;
        boolean mIsServerHeaderEnabled;
        boolean mIsWifiPreConditionEnabled;
        boolean mUseCompactHeader;
        int minSe;
        Mno mno;
        String msrpTransType;
        int mssSize;
        int needPidfSipMsg;
        long netId;
        String password;
        String pcscfIp;
        int pcscfPort;
        String pdn;
        int phoneId;
        String preferredId;
        String privacyHeaderRestricted;
        int profileId;
        int qparam;
        int rat;
        int rcsProfile;
        String realm;
        int regExpires;
        int regRetryBaseTime;
        int regRetryMaxTime;
        String registeralgo;
        ImsUri.UriType remoteuritype;
        boolean retryInviteOnTcpReset;
        int ringbackTimer;
        int ringingTimer;
        int scmVersion;
        String selectTransportAfterTcpReset;
        int selfPort;
        boolean sendByeForUssi;
        Set<String> serviceList;
        int sessionexpires;
        String sessionrefresher;
        int sipMobility;
        int srvccVersion;
        int sslType;
        int subscriberTimer;
        boolean support183ForIr92v9Precondition;
        boolean support199ProvisionalResponse;
        boolean supportAccessType;
        boolean supportDualRcs;
        boolean supportEct;
        boolean supportImsNotAvailable;
        boolean supportLtePreferred;
        boolean supportNetworkInitUssi;
        boolean supportReplaceMerge;
        boolean supportRfc6337ForDelayedOffer;
        boolean supportScribeDialogEvent;
        boolean supportUpgradePrecondition;
        int supportedGeolocationPhase;
        int tcpRstUacErrorcode;
        int tcpRstUasErrorcode;
        int textMode;
        int timer1;
        int timer2;
        int timer4;
        int timerA;
        int timerB;
        int timerC;
        int timerD;
        int timerE;
        int timerF;
        int timerG;
        int timerH;
        int timerI;
        int timerJ;
        int timerK;
        int timerTS;
        String transtype;
        boolean tryReregisterFromKeepalive;
        boolean use183OnProgressIncoming;
        boolean useKeepAlive;
        boolean usePemHeader;
        boolean useProvisionalResponse100rel;
        boolean useQ850causeOn480;
        boolean useSubcontactWhenResub;
        String userAgent;
        String uuid;
        int videoCrbtSupportType;

        public static Builder newBuilder() {
            return new Builder();
        }

        public UaProfile build() {
            return new UaProfile(this);
        }

        public Builder setProfileId(int profileId2) {
            this.profileId = profileId2;
            return this;
        }

        public Builder setIface(String iface2) {
            this.iface = iface2;
            return this;
        }

        public Builder setNetId(long netId2) {
            this.netId = netId2;
            return this;
        }

        public Builder setPdn(String pdn2) {
            this.pdn = pdn2;
            return this;
        }

        public Builder setImpi(String impi2) {
            this.impi = impi2;
            return this;
        }

        public Builder setImpu(String impu2) {
            this.impu = impu2;
            return this;
        }

        public Builder setLinkedImpuList(List<String> impuList2) {
            this.impuList = impuList2;
            return this;
        }

        public Builder setPreferredId(String preferredId2) {
            this.preferredId = preferredId2;
            return this;
        }

        public Builder setRemoteUriType(ImsUri.UriType remoteuritype2) {
            this.remoteuritype = remoteuritype2;
            return this;
        }

        public Builder setDomain(String domain2) {
            this.domain = domain2;
            return this;
        }

        public Builder setRegiAlgorithm(String registeralgo2) {
            this.registeralgo = registeralgo2;
            return this;
        }

        public Builder setPassword(String password2) {
            this.password = password2;
            return this;
        }

        public Builder setOutboundSip(boolean issipoutbound2) {
            this.issipoutbound = issipoutbound2;
            return this;
        }

        public Builder setQparam(int qparam2) {
            this.qparam = qparam2;
            return this;
        }

        public Builder setControlDscp(int controlDscp2) {
            this.controlDscp = controlDscp2;
            return this;
        }

        public Builder setTransportType(String transtype2) {
            this.transtype = transtype2;
            return this;
        }

        public Builder setUseTls(boolean usetls) {
            this.isenabletlsforsip = usetls;
            return this;
        }

        public Builder setIsIpSec(boolean isipsec2) {
            this.isipsec = isipsec2;
            return this;
        }

        public Builder setWifiPreConditionEnabled(boolean mIsWifiPreConditionEnabled2) {
            this.mIsWifiPreConditionEnabled = mIsWifiPreConditionEnabled2;
            return this;
        }

        public Builder setIsServerHeaderEnabled(boolean mIsServerHeaderEnabled2) {
            this.mIsServerHeaderEnabled = mIsServerHeaderEnabled2;
            return this;
        }

        public Builder setUseCompactHeader(boolean useCompactHeader) {
            this.mUseCompactHeader = useCompactHeader;
            return this;
        }

        public Builder setAuthAlg(String authalg2) {
            this.authalg = authalg2;
            return this;
        }

        public Builder setEncrAlg(String encralg2) {
            this.encralg = encralg2;
            return this;
        }

        public Builder setHostname(String hostname2) {
            this.hostname = hostname2;
            return this;
        }

        public Builder setPcscfIp(String pcscfIp2) {
            this.pcscfIp = pcscfIp2;
            return this;
        }

        public Builder setPcscfPort(int pcscfPort2) {
            this.pcscfPort = pcscfPort2;
            return this;
        }

        public Builder setEmergencyProfile(boolean emergency) {
            this.isemergencysupport = emergency;
            return this;
        }

        public Builder setServiceList(Set<String> serviceList2) {
            this.serviceList = serviceList2;
            return this;
        }

        public Builder addService(String svc) {
            if (this.serviceList == null) {
                this.serviceList = new HashSet();
            }
            this.serviceList.add(svc);
            return this;
        }

        public Builder setOwnCapabilities(Capabilities cap) {
            this.capabilities = cap;
            return this;
        }

        public Builder setMno(Mno mno2) {
            this.mno = mno2;
            return this;
        }

        public Builder setPrecondEnabled(boolean isPrecondEnabled) {
            this.isprecondenabled = isPrecondEnabled;
            return this;
        }

        public Builder setPrecondInitialSendrecv(boolean isSendrevc) {
            this.ispreconinitialsendrecv = isSendrevc;
            return this;
        }

        public Builder setIsRcsTelephonyFeatureTagRequired(boolean isRcsTelephonyRequired) {
            this.isRcsTelephonyFeatureTagRequired = isRcsTelephonyRequired;
            return this;
        }

        public Builder setIsFullCodecOfferRequired(boolean isFullOffer) {
            this.isFullCodecOfferRequired = isFullOffer;
            return this;
        }

        public Builder setSessionExpires(int sessionExpires) {
            this.sessionexpires = sessionExpires;
            return this;
        }

        public Builder setMinSe(int minSe2) {
            this.minSe = minSe2;
            return this;
        }

        public Builder setSessionRefresher(String sessionRefresher) {
            this.sessionrefresher = sessionRefresher;
            return this;
        }

        public Builder setRegExpires(int regExpires2) {
            this.regExpires = regExpires2;
            return this;
        }

        public Builder setUserAgent(String userAgent2) {
            this.userAgent = userAgent2;
            return this;
        }

        public Builder setDisplayName(String displayName2) {
            this.displayName = displayName2;
            return this;
        }

        public Builder setContactDisplayName(String contactDisplayName2) {
            this.contactDisplayName = contactDisplayName2;
            return this;
        }

        public Builder setUuid(String uuid2) {
            this.uuid = uuid2;
            return this;
        }

        public Builder setInstanceId(String instanceId2) {
            this.instanceId = instanceId2;
            return this;
        }

        public Builder setRealm(String realm2) {
            this.realm = realm2;
            return this;
        }

        public Builder setImMsgTech(String msgtech) {
            this.imMsgTech = msgtech;
            return this;
        }

        public Builder setCallProfile(CallProfile callProfile2) {
            this.callProfile = callProfile2;
            return this;
        }

        public Builder setTimer1(int timer12) {
            this.timer1 = timer12;
            return this;
        }

        public Builder setTimer2(int timer22) {
            this.timer2 = timer22;
            return this;
        }

        public Builder setTimer4(int timer42) {
            this.timer4 = timer42;
            return this;
        }

        public Builder setTimerA(int timerA2) {
            this.timerA = timerA2;
            return this;
        }

        public Builder setTimerB(int timerB2) {
            this.timerB = timerB2;
            return this;
        }

        public Builder setTimerC(int timerC2) {
            this.timerC = timerC2;
            return this;
        }

        public Builder setTimerD(int timerD2) {
            this.timerD = timerD2;
            return this;
        }

        public Builder setTimerE(int timerE2) {
            this.timerE = timerE2;
            return this;
        }

        public Builder setTimerF(int timerF2) {
            this.timerF = timerF2;
            return this;
        }

        public Builder setTimerG(int timerG2) {
            this.timerG = timerG2;
            return this;
        }

        public Builder setTimerH(int timerH2) {
            this.timerH = timerH2;
            return this;
        }

        public Builder setTimerI(int timerI2) {
            this.timerI = timerI2;
            return this;
        }

        public Builder setTimerJ(int timerJ2) {
            this.timerJ = timerJ2;
            return this;
        }

        public Builder setTimerK(int timerK2) {
            this.timerK = timerK2;
            return this;
        }

        public Builder setTimerTS(int timerTS2) {
            this.timerTS = timerTS2;
            return this;
        }

        public Builder setIsSoftphoneEnabled(boolean enabled) {
            this.isSoftphoneEnabled = enabled;
            return this;
        }

        public Builder setIsCdmalessEnabled(boolean enabled) {
            this.isCdmalessEnabled = enabled;
            return this;
        }

        public Builder setMssSize(int mssSize2) {
            this.mssSize = mssSize2;
            return this;
        }

        public Builder setRingbackTimer(int ringbackTimer2) {
            this.ringbackTimer = ringbackTimer2;
            return this;
        }

        public Builder setRingingTimer(int ringingTimer2) {
            this.ringingTimer = ringingTimer2;
            return this;
        }

        public Builder setSipMobility(int sipMobility2) {
            this.sipMobility = sipMobility2;
            return this;
        }

        public Builder setIsEnableGruu(boolean isEnableGruu2) {
            this.isEnableGruu = isEnableGruu2;
            return this;
        }

        public Builder setIsEnableSessionId(boolean isEnableSessionId2) {
            this.isEnableSessionId = isEnableSessionId2;
            return this;
        }

        public Builder setAudioEngineType(int audioEngineType2) {
            this.audioEngineType = audioEngineType2;
            return this;
        }

        public Builder setCurPani(String curPani2) {
            this.curPani = curPani2;
            return this;
        }

        public Builder setVceConfigEnabled(boolean enabled) {
            this.isVceConfigEnabled = enabled;
            return this;
        }

        public Builder setGcfConfigEnabled(boolean enabled) {
            this.isGcfConfigEnabled = enabled;
            return this;
        }

        public Builder setNsdsServiceEnabled(boolean enabled) {
            this.isNsdsServiceEnabled = enabled;
            return this;
        }

        public Builder setMsrpBearerUsed(boolean used) {
            this.isMsrpBearerUsed = used;
            return this;
        }

        public Builder setSubscriberTimer(int subscriberTimer2) {
            this.subscriberTimer = subscriberTimer2;
            return this;
        }

        public Builder setSubscribeReg(boolean enabled) {
            this.isSubscribeReg = enabled;
            return this;
        }

        public Builder setAccessToken(String accessToken2) {
            this.accessToken = accessToken2;
            return this;
        }

        public Builder setAuthServerUrl(String serverUrl) {
            this.authServerUrl = serverUrl;
            return this;
        }

        public Builder setUseKeepAlive(boolean enabled) {
            this.useKeepAlive = enabled;
            return this;
        }

        public Builder setSelfPort(int selfPort2) {
            this.selfPort = selfPort2;
            return this;
        }

        public Builder setScmVersion(int scmVersion2) {
            this.scmVersion = scmVersion2;
            return this;
        }

        public Builder setMsrpTransType(String msrpTransType2) {
            this.msrpTransType = msrpTransType2;
            return this;
        }

        public Builder setIsXqEnabled(boolean enabled) {
            this.isXqEnabled = enabled;
            return this;
        }

        public Builder setTextMode(int textMode2) {
            this.textMode = textMode2;
            return this;
        }

        public Builder setRcsProfile(int rcsProfile2) {
            this.rcsProfile = rcsProfile2;
            return this;
        }

        public Builder setIsTransportNeeded(boolean isTransportNeeded2) {
            this.isTransportNeeded = isTransportNeeded2;
            return this;
        }

        public Builder setRat(int rat2) {
            this.rat = rat2;
            return this;
        }

        public Builder setDbrTimer(int dbrTimer2) {
            this.dbrTimer = dbrTimer2;
            return this;
        }

        public Builder setIsTcpGracefulShutdownEnabled(boolean isTcpGracefulShutdownEnabled2) {
            this.isTcpGracefulShutdownEnabled = isTcpGracefulShutdownEnabled2;
            return this;
        }

        public Builder setTcpRstUacErrorcode(int tcpRstUacErrorcode2) {
            this.tcpRstUacErrorcode = tcpRstUacErrorcode2;
            return this;
        }

        public Builder setTcpRstUasErrorcode(int tcpRstUasErrorcode2) {
            this.tcpRstUasErrorcode = tcpRstUasErrorcode2;
            return this;
        }

        public Builder setPrivacyHeaderRestricted(String privacyHeaderRestricted2) {
            this.privacyHeaderRestricted = privacyHeaderRestricted2;
            return this;
        }

        public Builder setUsePemHeader(boolean usePemHeader2) {
            this.usePemHeader = usePemHeader2;
            return this;
        }

        public Builder setPhoneId(int phoneId2) {
            this.phoneId = phoneId2;
            return this;
        }

        public Builder setSupportEct(boolean supportEct2) {
            this.supportEct = supportEct2;
            return this;
        }

        public Builder setEarlyMediaRtpTimeoutTimer(int earlyMediaRtpTimeoutTimer2) {
            this.earlyMediaRtpTimeoutTimer = earlyMediaRtpTimeoutTimer2;
            return this;
        }

        public Builder setAddHistinfo(boolean addHistinfo2) {
            this.addHistinfo = addHistinfo2;
            return this;
        }

        public Builder setSupportedGeolocationPhase(int supportedGeolocationPhase2) {
            this.supportedGeolocationPhase = supportedGeolocationPhase2;
            return this;
        }

        public Builder setNeedPidfSipMsg(int needPidfSipMsg2) {
            this.needPidfSipMsg = needPidfSipMsg2;
            return this;
        }

        public Builder setUseSubcontactWhenResub(boolean useSubcontactWhenResub2) {
            this.useSubcontactWhenResub = useSubcontactWhenResub2;
            return this;
        }

        public Builder setUseProvisionalResponse100rel(boolean useProvisionalResponse100rel2) {
            this.useProvisionalResponse100rel = useProvisionalResponse100rel2;
            return this;
        }

        public Builder setUse183OnProgressIncoming(boolean use183OnProgressIncoming2) {
            this.use183OnProgressIncoming = use183OnProgressIncoming2;
            return this;
        }

        public Builder setUseQ850causeOn480(boolean useQ850causeOn4802) {
            this.useQ850causeOn480 = useQ850causeOn4802;
            return this;
        }

        public Builder setSupport183ForIr92v9Precondition(boolean support183ForIr92v9Precondition2) {
            this.support183ForIr92v9Precondition = support183ForIr92v9Precondition2;
            return this;
        }

        public Builder setSupportImsNotAvailable(boolean supportImsNotAvailable2) {
            this.supportImsNotAvailable = supportImsNotAvailable2;
            return this;
        }

        public Builder setSupportLtePreferred(boolean supportLtePreferred2) {
            this.supportLtePreferred = supportLtePreferred2;
            return this;
        }

        public Builder setSupportUpgradePrecondition(boolean supportUpgradePrecondition2) {
            this.supportUpgradePrecondition = supportUpgradePrecondition2;
            return this;
        }

        public Builder setSupportReplaceMerge(boolean supportReplaceMerge2) {
            this.supportReplaceMerge = supportReplaceMerge2;
            return this;
        }

        public Builder setSupportAccessType(boolean supportAccessType2) {
            this.supportAccessType = supportAccessType2;
            return this;
        }

        public Builder setLastPaniHeader(String lastPaniHeader2) {
            this.lastPaniHeader = lastPaniHeader2;
            return this;
        }

        public Builder setSelectTransportAfterTcpReset(String selectTransportAfterTcpReset2) {
            this.selectTransportAfterTcpReset = selectTransportAfterTcpReset2;
            return this;
        }

        public Builder setSrvccVersion(int srvccVersion2) {
            this.srvccVersion = srvccVersion2;
            return this;
        }

        public Builder setSubscribeDialogEvent(Boolean useScribeDialogEvent) {
            this.supportScribeDialogEvent = useScribeDialogEvent.booleanValue();
            return this;
        }

        public Builder setIsSimMobility(Boolean simMo) {
            this.isSimMobility = simMo.booleanValue();
            return this;
        }

        public Builder setCmcType(int cmcType2) {
            this.cmcType = cmcType2;
            return this;
        }

        public Builder setCmcRelayType(String cmcRelayType2) {
            this.cmcRelayType = cmcRelayType2;
            return this;
        }

        public Builder setVideoCrbtSupportType(int videoCrbtSupportType2) {
            this.videoCrbtSupportType = videoCrbtSupportType2;
            return this;
        }

        public Builder setRetryInviteOnTcpReset(boolean retryInviteOnTcpReset2) {
            this.retryInviteOnTcpReset = retryInviteOnTcpReset2;
            return this;
        }

        public Builder setEanbleVerstat(boolean enableVerstat2) {
            this.enableVerstat = enableVerstat2;
            return this;
        }

        public Builder setRegRetryBaseTime(int time) {
            this.regRetryBaseTime = time;
            return this;
        }

        public Builder setRegRetryMaxTime(int time) {
            this.regRetryMaxTime = time;
            return this;
        }

        public Builder setSupportDualRcs(boolean supportDualRcs2) {
            this.supportDualRcs = supportDualRcs2;
            return this;
        }

        public Builder setTryReregisterFromKeepalive(boolean tryReregisterFromKeepalive2) {
            this.tryReregisterFromKeepalive = tryReregisterFromKeepalive2;
            return this;
        }

        public Builder setPttSupported(boolean pttSupported) {
            this.isPttSupported = pttSupported;
            return this;
        }

        public Builder setSslType(int sslType2) {
            this.sslType = sslType2;
            return this;
        }

        public Builder setSupport199ProvisionalResponse(boolean support199ProvisionalResponse2) {
            this.support199ProvisionalResponse = support199ProvisionalResponse2;
            return this;
        }

        public Builder setAcb(List<String> acb2) {
            this.acb = acb2;
            return this;
        }

        public Builder setIgnoreDisplayName(boolean ignoreDisplayName2) {
            this.ignoreDisplayName = ignoreDisplayName2;
            return this;
        }

        public Builder setSupportNetworkInitUssi(boolean supportNetworkInitUssi2) {
            this.supportNetworkInitUssi = supportNetworkInitUssi2;
            return this;
        }

        public Builder setSendByeForUssi(boolean sendByeForUssi2) {
            this.sendByeForUssi = sendByeForUssi2;
            return this;
        }

        public Builder setSupportRfc6337ForDelayedOffer(boolean supportRfc6337ForDelayedOffer2) {
            this.supportRfc6337ForDelayedOffer = supportRfc6337ForDelayedOffer2;
            return this;
        }

        public Builder setHashAlgoType(int hashAlgoType2) {
            this.hashAlgoType = hashAlgoType2;
            return this;
        }
    }
}
