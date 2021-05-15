package com.sec.internal.ims.servicemodules.options;

import android.content.ContentValues;
import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CapabilityUtil {
    private static final String LOG_TAG = "CapabilityUtil";
    static final int[] exponentialCapInfoExpiry = {Id.REQUEST_SIP_DIALOG_SEND_SIP, 3600, 7200, 14400, 28800};
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private IPresenceModule mPresenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();

    CapabilityUtil(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    /* access modifiers changed from: package-private */
    public boolean isCheckRcsSwitch(Context context) {
        boolean rcsSwitchOn = false;
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            boolean z = true;
            if (DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS_SWITCH, sm.getSimSlotIndex()) != 1) {
                z = false;
            }
            rcsSwitchOn |= z;
        }
        return rcsSwitchOn;
    }

    /* access modifiers changed from: package-private */
    public boolean isCapabilityDiscoveryDisabled(Context context, int phoneId) {
        boolean isOptionsEnabled = DmConfigHelper.getImsSwitchValue(context, "options", phoneId) == 1;
        boolean isPresenceEnabled = DmConfigHelper.getImsSwitchValue(context, "presence", phoneId) == 1;
        if (isOptionsEnabled || isPresenceEnabled) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public String extractMsisdnFromUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            Log.e(LOG_TAG, "extractMsisdnFromUri uri is empty");
            return "";
        }
        int prefixIndex = uri.indexOf(":");
        String removedPrefix = uri;
        if (prefixIndex >= 0) {
            removedPrefix = uri.substring(prefixIndex + 1);
        }
        String trimmed = removedPrefix;
        int domainIndex = removedPrefix.indexOf("@");
        if (domainIndex >= 0) {
            return removedPrefix.substring(0, domainIndex);
        }
        return trimmed;
    }

    /* access modifiers changed from: package-private */
    public int getCapInfoExpiry(Capabilities capex, int phoneId) {
        int expCapInfoExpiry;
        CapabilityConfig mCapabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(phoneId);
        if (capex != null && capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            return mCapabilityConfig.getNonRCScapInfoExpiry();
        }
        int capInfoExpiry = mCapabilityConfig.getCapInfoExpiry();
        boolean isSupportExpCapInfoExpiry = mCapabilityConfig.getIsSupportExpCapInfoExpiry();
        if (capex == null || !isSupportExpCapInfoExpiry || (expCapInfoExpiry = capex.getExpCapInfoExpiry()) >= capInfoExpiry) {
            return capInfoExpiry;
        }
        if (expCapInfoExpiry > 0) {
            return expCapInfoExpiry;
        }
        return exponentialCapInfoExpiry[0];
    }

    /* access modifiers changed from: package-private */
    public int updateExpCapInfoExpiry(Capabilities capex, long features, int phoneId) {
        long j = features;
        int i = phoneId;
        int expCapInfoExpiry = capex.getExpCapInfoExpiry();
        CapabilitiesCache mCapabilitiesCache = this.mCapabilityDiscovery.getCapabilitiesCache(i);
        boolean needUpdate = false;
        boolean isCapChanged = mCapabilitiesCache.isAvailable(j) != mCapabilitiesCache.isAvailable(capex.getFeature()) || (mCapabilitiesCache.isAvailable(j) && j != capex.getFeature());
        if (expCapInfoExpiry == 0 || isCapChanged) {
            IMSLog.i(LOG_TAG, i, "updateExpCapInfoExpiry: initiates expCapInfoExpiry");
            return exponentialCapInfoExpiry[0];
        }
        if (new Date().getTime() - capex.getTimestamp().getTime() >= ((long) expCapInfoExpiry) * 1000) {
            needUpdate = true;
        }
        IMSLog.i(LOG_TAG, i, "updateExpCapInfoExpiry: expCapInfoExpiry = " + expCapInfoExpiry + ", needUpdate = " + needUpdate);
        if (needUpdate) {
            int i2 = 0;
            while (true) {
                int[] iArr = exponentialCapInfoExpiry;
                if (i2 >= iArr.length - 1) {
                    break;
                } else if (expCapInfoExpiry == iArr[i2]) {
                    IMSLog.i(LOG_TAG, i, "updateExpCapInfoExpiry: increase to " + exponentialCapInfoExpiry[i2 + 1]);
                    return exponentialCapInfoExpiry[i2 + 1];
                } else {
                    i2++;
                }
            }
        }
        return expCapInfoExpiry;
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedPrefixesUri(ImsUri uri, int phoneId) {
        CapabilityConfig mCapabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(phoneId);
        if (mCapabilityConfig == null) {
            return false;
        }
        if (uri != null && uri.getUriType() == ImsUri.UriType.SIP_URI && UriUtil.getMsisdnNumber(uri) == null) {
            return true;
        }
        Set<Pattern> prefixPatterns = mCapabilityConfig.getCapAllowedPrefixes();
        if (prefixPatterns.isEmpty()) {
            return true;
        }
        String msdn = UriUtil.getMsisdnNumber(uri);
        if (msdn == null) {
            return false;
        }
        for (Pattern prefix : prefixPatterns) {
            if (prefix.matcher(msdn).find()) {
                IMSLog.s(LOG_TAG, phoneId, "isAllowedPrefixesUri: prefix = " + prefix.pattern() + ", msdn = " + msdn);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public long getDelayTimeToPoll(long lastListSubscribeStamp, int phoneId) {
        if (lastListSubscribeStamp == -1) {
            return 0;
        }
        long delay = (RcsPolicyManager.getRcsStrategy(phoneId).getThrottledDelay((long) this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollListSubExpiry()) * 1000) - (new Date().getTime() - lastListSubscribeStamp);
        IMSLog.i(LOG_TAG, phoneId, "getDelayTimeToPoll: delay = " + delay + ", lastListSubscribeStamp = " + lastListSubscribeStamp);
        if (delay > 0) {
            return delay;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isRegistrationSupported(ImsRegistration regiInfo) {
        if (!regiInfo.hasService("presence") && !regiInfo.hasService("options")) {
            Log.e(LOG_TAG, "isRegistrationSupported: no presence and options in service list");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(regiInfo.getPhoneId()) == null) {
            Log.e(LOG_TAG, "isRegistrationSupported: getRcsStrategy is null");
            return false;
        } else if (!RcsPolicyManager.getRcsStrategy(regiInfo.getPhoneId()).checkImsiBasedRegi(regiInfo)) {
            return true;
        } else {
            Log.e(LOG_TAG, "isRegistrationSupported: isImsiBasedRegi is true");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public ImsUri getNetworkPreferredUri(ImsUri uri) {
        Capabilities caps = null;
        CapabilitiesCache cacheList = this.mCapabilityDiscovery.getCapabilitiesCache();
        if (cacheList != null) {
            caps = cacheList.get(uri);
        }
        if (caps == null) {
            return null;
        }
        String domain = null;
        Iterator<ImsUri> it = caps.getPAssertedId().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ImsUri remoteUri = it.next();
            if (remoteUri.getUriType() == ImsUri.UriType.SIP_URI) {
                domain = remoteUri.getHost();
                break;
            }
        }
        return this.mCapabilityDiscovery.getUriGenerator().getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri, domain);
    }

    private boolean isCapabilityCacheEmpty(int phoneId) {
        for (Capabilities capex : this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).getCapabilities()) {
            if (capex.getContactId() != null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void changeParalysed(boolean mode, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "changeParalysed");
        if (this.mPresenceModule.getParalysed(phoneId) != mode) {
            this.mPresenceModule.setParalysed(mode, phoneId);
            CapabilityConfig mCapabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(phoneId);
            if (mode && mCapabilityConfig != null && mCapabilityConfig.usePresence()) {
                Log.i(LOG_TAG, "call unpublish");
                this.mPresenceModule.unpublish(phoneId);
            }
            if (!mode && isCapabilityCacheEmpty(phoneId)) {
                this.mCapabilityDiscovery.onContactChanged(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRemovedNumbers(int phoneId) {
        List<String> removedNumbers = this.mCapabilityDiscovery.getPhonebook().getAndFlushRemovedNumbers();
        IMSLog.s(LOG_TAG, phoneId, "handleRemovedNumbers: removed numbers " + removedNumbers);
        List<ImsUri> removedUriList = new ArrayList<>();
        for (String number : removedNumbers) {
            ImsUri uri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(number, true);
            if (uri != null) {
                removedUriList.add(uri);
                if (this.mCapabilityDiscovery.updatePollList(uri, false, phoneId)) {
                    IMSLog.s(LOG_TAG, phoneId, "handleRemovedNumbers: updatePollList, removed uri = " + uri);
                }
            }
        }
        if (removedUriList.size() > 0) {
            this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).remove(removedUriList);
            CapabilityConfig mCapabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(phoneId);
            if (mCapabilityConfig != null && mCapabilityConfig.usePresence()) {
                this.mPresenceModule.removePresenceCache(removedUriList, phoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long filterFeaturesWithService(long features, Set<String> services, int networkType) {
        if (services == null) {
            return features;
        }
        IMSLog.s(LOG_TAG, "filterFeaturesWithService: features=" + Long.toHexString(features) + ", services=" + services + ", networkType=" + networkType);
        return features & (0 | checkChatFeatures(services) | checkCshFeatures(services) | checkRcsFeatures(services, networkType));
    }

    public static boolean hasFeature(long ownFeature, long feature) {
        return (ownFeature & feature) == feature;
    }

    private long checkChatFeatures(Set<String> services) {
        long maskChatServices = 0;
        if (services.contains(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
            maskChatServices = 0 | Capabilities.FEATURE_CHATBOT_CHAT_SESSION | Capabilities.FEATURE_CHATBOT_STANDALONE_MSG | Capabilities.FEATURE_CHATBOT_EXTENDED_MSG;
        }
        if (services.contains("ft_http")) {
            maskChatServices |= (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_FT_VIA_SMS);
        }
        if (services.contains("slm")) {
            maskChatServices |= ((long) Capabilities.FEATURE_STANDALONE_MSG) | Capabilities.FEATURE_PUBLIC_MSG;
        }
        if (services.contains("im")) {
            maskChatServices |= (long) (Capabilities.FEATURE_CHAT_CPM | Capabilities.FEATURE_CHAT_SIMPLE_IM | Capabilities.FEATURE_INTEGRATED_MSG | Capabilities.FEATURE_SF_GROUP_CHAT | Capabilities.FEATURE_STICKER);
        }
        if (services.contains("ft")) {
            return maskChatServices | ((long) (Capabilities.FEATURE_FT | Capabilities.FEATURE_FT_STORE | Capabilities.FEATURE_FT_THUMBNAIL | Capabilities.FEATURE_FT_VIA_SMS));
        }
        return maskChatServices;
    }

    private long checkCshFeatures(Set<String> services) {
        long maskCshServices = 0;
        if (services.contains("is")) {
            maskCshServices = 0 | ((long) Capabilities.FEATURE_ISH);
        }
        if (services.contains("vs")) {
            maskCshServices |= (long) Capabilities.FEATURE_VSH;
        }
        if (services.contains("gls")) {
            maskCshServices |= (long) (Capabilities.FEATURE_GEOLOCATION_PULL | Capabilities.FEATURE_GEOLOCATION_PULL_FT | Capabilities.FEATURE_GEOLOCATION_PUSH | Capabilities.FEATURE_GEO_VIA_SMS);
        }
        if (services.contains("ec")) {
            return maskCshServices | Capabilities.FEATURE_ENRICHED_CALL_COMPOSER | Capabilities.FEATURE_ENRICHED_SHARED_MAP | Capabilities.FEATURE_ENRICHED_SHARED_SKETCH | Capabilities.FEATURE_ENRICHED_POST_CALL;
        }
        return maskCshServices;
    }

    private long checkRcsFeatures(Set<String> services, int networkType) {
        long maskRcsServices = 0;
        if (services.contains("plug-in")) {
            maskRcsServices = 0 | Capabilities.FEATURE_PLUG_IN;
        }
        if (services.contains("presence")) {
            maskRcsServices |= (long) (Capabilities.FEATURE_PRESENCE_DISCOVERY | Capabilities.FEATURE_SOCIAL_PRESENCE);
        }
        if (services.contains("lastseen")) {
            maskRcsServices |= Capabilities.FEATURE_LAST_SEEN_ACTIVE;
        }
        if (services.contains("mmtel") && isServiceAvailable(networkType)) {
            maskRcsServices |= (long) (Capabilities.FEATURE_MMTEL | Capabilities.FEATURE_IPCALL);
        }
        ISimManager sm = SimManagerFactory.getSimManager();
        if (services.contains("mmtel-video") && (sm.getSimMno().isKor() || isServiceAvailable(networkType))) {
            maskRcsServices |= (long) (Capabilities.FEATURE_MMTEL_VIDEO | Capabilities.FEATURE_IPCALL_VIDEO | Capabilities.FEATURE_IPCALL_VIDEO_ONLY);
        }
        if (!isServiceAvailable(networkType) || !services.contains("mmtel") || !services.contains("mmtel-call-composer")) {
            return maskRcsServices;
        }
        return maskRcsServices | Capabilities.FEATURE_MMTEL_CALL_COMPOSER;
    }

    /* access modifiers changed from: package-private */
    public long filterFeaturesWithCallState(long features, boolean isInCall, String callNumber) {
        if (isInCall && callNumber != null) {
            return features;
        }
        Log.i(LOG_TAG, "filterFeaturesWithCallState: disable ISH, VSH, ShareMap and ShareSketch");
        return ((long) (~Capabilities.FEATURE_VSH)) & features & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH);
    }

    /* access modifiers changed from: package-private */
    public long filterEnrichedCallFeatures(long features) {
        Log.i(LOG_TAG, "filterEnrichedCallFeatures: disable CallComposer, PostCall, ISH, VSH, ShareMap and ShareSketch");
        return ((long) (~Capabilities.FEATURE_VSH)) & features & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH) & (~Capabilities.FEATURE_ENRICHED_CALL_COMPOSER) & (~Capabilities.FEATURE_ENRICHED_POST_CALL);
    }

    /* access modifiers changed from: package-private */
    public long filterInCallFeatures(long features, ImsUri requestUri, String callNumber) {
        if (requestUri == null) {
            Log.i(LOG_TAG, "Request URI is null, return existing availFeatures");
            return features;
        }
        String requestMsisdn = requestUri.getMsisdn();
        Log.i(LOG_TAG, "request uri[" + IMSLog.checker(requestMsisdn) + "] callNumber[" + IMSLog.checker(callNumber) + "]");
        if (callNumber == null) {
            Log.i(LOG_TAG, "we're not in call with " + IMSLog.checker(requestMsisdn) + ", remove incall features");
            return ((long) (~Capabilities.FEATURE_VSH)) & features & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH);
        }
        ImsUri normalizedReqUri = null;
        ImsUri normalizedCallNumber = null;
        UriGenerator mUriGenerator = this.mCapabilityDiscovery.getUriGenerator();
        if (mUriGenerator != null) {
            normalizedReqUri = mUriGenerator.getNormalizedUri(requestMsisdn, true);
            normalizedCallNumber = mUriGenerator.getNormalizedUri(callNumber, true);
        }
        ImsUri normalizedReqUri2 = normalizedReqUri != null ? normalizedReqUri : requestUri;
        Log.i(LOG_TAG, "normalizedReqUri[" + IMSLog.checker(normalizedReqUri2) + "] normalizedCallNumber[" + IMSLog.checker(normalizedCallNumber) + "]");
        if (!normalizedReqUri2.equals(normalizedCallNumber)) {
            Log.i(LOG_TAG, "we're not in call with " + IMSLog.checker(requestMsisdn) + ", remove incall features");
            return ((long) (~Capabilities.FEATURE_VSH)) & features & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH);
        }
        Log.i(LOG_TAG, "we're in call with " + IMSLog.checker(requestMsisdn) + ", don't change incall features");
        return features;
    }

    /* access modifiers changed from: package-private */
    public Set<String> filterServicesWithReg(Map<Integer, ImsRegistration> imsRegInfoList, IRegistrationManager regMan, int networkType, int phoneId) {
        if (!imsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
            return null;
        }
        ImsProfile imsProfile = imsRegInfoList.get(Integer.valueOf(phoneId)).getImsProfile();
        int imsHandle = imsRegInfoList.get(Integer.valueOf(phoneId)).getHandle();
        Set<String> regiServices = imsRegInfoList.get(Integer.valueOf(phoneId)).getServices();
        int network = regMan.getCurrentNetwork(imsHandle);
        if (ConfigUtil.isRcsEur(SimUtil.getSimMno(phoneId))) {
            network = networkType;
        }
        Set<String> profileServices = regMan.getServiceForNetwork(imsProfile, network, false, phoneId);
        Set<String> filteredServices = new HashSet<>();
        if (profileServices != null) {
            for (String svc : regiServices) {
                if (profileServices.contains(svc)) {
                    filteredServices.add(svc);
                }
            }
        }
        return filteredServices;
    }

    private boolean isServiceAvailable(int networkType) {
        ISimManager sm = SimManagerFactory.getSimManager();
        Mno mno = sm.getSimMno();
        if (mno == Mno.ATT) {
            if (networkType == 13 || networkType == 18 || networkType == 20 || sm.hasVsim()) {
                return true;
            }
            return false;
        } else if (!mno.isKor() || networkType == 13 || networkType == 20) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPhoneLockState(Context context) {
        String cryptoType = SystemProperties.get("ro.crypto.type", "");
        String voldDecrypt = SystemProperties.get("vold.decrypt", "");
        if ("block".equals(cryptoType) && !"trigger_restart_framework".equals(voldDecrypt)) {
            Log.i(LOG_TAG, "isPhoneLockState: not required sync contact in lock state");
            IMSLog.c(LogClass.CDM_BOOT_COMP, "N,LOCKED");
            return true;
        } else if (isCheckRcsSwitch(context)) {
            return false;
        } else {
            Log.i(LOG_TAG, "isPhoneLockState : rcs switch is disabled");
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void sendGateMessage(ImsUri uri, long availFeatures, int phoneId) {
        try {
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.i(LOG_TAG, phoneId, "sendGateMessage");
                PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber phoneNumber = util.parse(UriUtil.getMsisdnNumber(uri), "");
                String nationalNumber = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL).replace(" ", "");
                String countryCode = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(phoneNumber.getCountryCode())});
                String capOnOff = "OFF";
                if (hasFeature(availFeatures, (long) Capabilities.FEATURE_CHAT_CPM) || hasFeature(availFeatures, (long) Capabilities.FEATURE_FT_SERVICE)) {
                    capOnOff = "ON";
                }
                IMSLog.g("GATE", "<GATE-M>IPME_CAPABILITY_" + capOnOff + "_+" + countryCode + nationalNumber + "</GATE-M>");
            }
        } catch (NumberParseException e) {
            IMSLog.s(LOG_TAG, "Failed to parse uri : " + uri);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean sendRCSLInfoToHQM(Context context, boolean isLatching, int phoneId) {
        if (phoneId < 0) {
            Log.e(LOG_TAG, "sendRCSLInfoToHQM : phoneId is invaild " + phoneId);
            phoneId = 0;
        }
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        rcsmKeys.put(DiagnosisConstants.RCSL_KEY_LTCH, String.valueOf(isLatching ^ true ? 1 : 0));
        return RcsHqmAgent.sendRCSInfoToHQM(context, DiagnosisConstants.FEATURE_RCSL, phoneId, rcsmKeys);
    }

    /* access modifiers changed from: package-private */
    public void sendRCSCInfoToHQM(int phoneId) {
        if (phoneId < 0) {
            Log.e(LOG_TAG, "sendRCSCInfoToHQM : phoneId is invaild " + phoneId);
            phoneId = 0;
        }
        this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).sendRCSCInfoToHQM();
    }

    /* access modifiers changed from: package-private */
    public void onImsSettingsUpdate(Context context, int phoneId) {
        this.mCapabilityDiscovery.removeMessages(7);
        if (this.mCapabilityDiscovery.getCapabilityControl(phoneId) != null && this.mCapabilityDiscovery.getCapabilityControl(phoneId) == this.mCapabilityDiscovery.getPresenceModule()) {
            if (!DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING).booleanValue()) {
                this.mCapabilityDiscovery.getCapabilityControl(phoneId).reset(phoneId);
                this.mCapabilityDiscovery.clearCapabilitiesCache(phoneId);
                this.mCapabilityDiscovery.changeParalysed(true, phoneId);
                IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
                if (mnoStrategy != null) {
                    mnoStrategy.updateOmaDmNodes(phoneId);
                    return;
                }
                return;
            } else if (!this.mCapabilityDiscovery.getPresenceModule().getBadEventProgress(phoneId)) {
                this.mCapabilityDiscovery.changeParalysed(false, phoneId);
            }
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null || this.mCapabilityDiscovery.getCapabilityControl(phoneId) == null) {
            Log.i(LOG_TAG, "onImsSettingsUpdate: not ready");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "onImsSettingsUpdate: refresh configuration");
        this.mCapabilityDiscovery.getCapabilityControl(phoneId).readConfig(phoneId);
        IMnoStrategy mnoStrategy2 = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mnoStrategy2 != null) {
            mnoStrategy2.updateOmaDmNodes(phoneId);
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isPollingPeriodUpdated() && this.mCapabilityDiscovery.getCapabilityControl(phoneId).isReadyToRequest(phoneId)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(3, Integer.valueOf(phoneId)));
        }
    }

    /* access modifiers changed from: package-private */
    public void onNetworkChanged(Context context, NetworkEvent event, int phoneId, int mAvailablePhoneId, Map<Integer, ImsRegistration> mImsRegInfoList, NetworkEvent mNetworkEvent, int mNetworkClass) {
        IMSLog.s(LOG_TAG, phoneId, "onNetworkChanged: " + event);
        if (RcsUtils.DualRcs.isDualRcsReg() && ConfigUtil.isRcsEur(phoneId)) {
            IMSLog.i(LOG_TAG, phoneId, "onNetworkChanged: dual rcs is true.");
        } else if (mAvailablePhoneId != phoneId) {
            IMSLog.s(LOG_TAG, phoneId, "onNetworkChanged: mAvailablePhoneId = ! phoneId");
            return;
        }
        int networkclass = TelephonyManagerExt.getNetworkClass(event.network);
        boolean doUpdate = true;
        if (event.network == 0 && !event.isWifiConnected) {
            return;
        }
        if (mNetworkEvent == null || mNetworkEvent.network != event.network) {
            if (mNetworkClass == networkclass) {
                doUpdate = false;
            }
            if (!event.isWifiConnected || (SimUtil.getSimMno(phoneId).isRjil() && event.network != 0)) {
                this.mCapabilityDiscovery.setNetworkType(event.network);
            } else {
                this.mCapabilityDiscovery.setNetworkType(18);
            }
            this.mCapabilityDiscovery.setNetworkEvent(event);
            this.mCapabilityDiscovery.setNetworkClass(networkclass);
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno == Mno.ATT && doUpdate) {
                this.mCapabilityDiscovery.setOwnCapabilities(phoneId, true);
            } else if (ConfigUtil.isRcsEur(mno)) {
                Log.i(LOG_TAG, "onNetworkChanged: setOwnCapabilities(false) is called");
                if (RcsUtils.DualRcs.isDualRcsReg()) {
                    for (int i = 0; i < 2; i++) {
                        if (RcsUtils.UiUtils.isRcsEnabledinSettings(context, i) && mImsRegInfoList.containsKey(Integer.valueOf(i))) {
                            this.mCapabilityDiscovery.updateOwnCapabilities(i);
                            this.mCapabilityDiscovery.setOwnCapabilities(i, false);
                        }
                    }
                } else if (mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
                    this.mCapabilityDiscovery.updateOwnCapabilities(phoneId);
                    this.mCapabilityDiscovery.setOwnCapabilities(phoneId, false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean blockOptionsToOwnUri(ImsUri uri, int phoneId) {
        if (uri == null || this.mCapabilityDiscovery.getCapabilityControl(phoneId) == null || this.mCapabilityDiscovery.getCapabilityControl(phoneId) != this.mCapabilityDiscovery.getOptionsModule()) {
            return false;
        }
        for (Capabilities ownCap : this.mCapabilityDiscovery.getOwnList().values()) {
            if (ownCap.isAvailable() && ownCap.getUri() != null && uri.equals(ownCap.getUri())) {
                if (!RcsUtils.DualRcs.isDualRcsReg() || phoneId == ownCap.getPhoneId()) {
                    IMSLog.s(LOG_TAG, "blockOptionsToOwnUri: Block for sending OPTIONS to own number " + ownCap.getUri());
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkModuleReady(int phoneId) {
        if (!this.mCapabilityDiscovery.isRunning()) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: module is disabled");
            return false;
        } else if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: uriGenerator is null");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: MnoStrategy is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: config is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isAvailable()) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, phoneId, "checkModuleReady: mConfig.isAvailable == false");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onServiceSwitched(int phoneId, ContentValues switchStatus, Map<Integer, Boolean> mPresenceSwitchOnList, Map<Integer, Boolean> mOptionsSwitchOnList, boolean mCapabilityModuleOn) {
        IMSLog.d(LOG_TAG, phoneId, "onServiceSwitched: ");
        boolean isChanged = false;
        boolean isPresenceOn = false;
        boolean isOptionsOn = false;
        if (switchStatus != null) {
            isPresenceOn = ((Integer) switchStatus.get("presence")).intValue() == 1;
            isOptionsOn = ((Integer) switchStatus.get("options")).intValue() == 1;
        }
        if (mPresenceSwitchOnList.get(Integer.valueOf(phoneId)).booleanValue() != isPresenceOn) {
            this.mCapabilityDiscovery.setPresenceSwitch(phoneId, isPresenceOn);
            isChanged = true;
            IMSLog.d(LOG_TAG, phoneId, "onServiceSwitched: presence changed: " + isPresenceOn);
        }
        if (mOptionsSwitchOnList.get(Integer.valueOf(phoneId)).booleanValue() != isOptionsOn) {
            this.mCapabilityDiscovery.settOptionsSwitch(phoneId, isOptionsOn);
            isChanged = true;
            IMSLog.d(LOG_TAG, phoneId, "onServiceSwitched: options changed: " + isOptionsOn);
        }
        if (!isChanged) {
            return;
        }
        if (!mPresenceSwitchOnList.containsValue(true) && !mOptionsSwitchOnList.containsValue(true)) {
            this.mCapabilityDiscovery.setCapabilityModuleOn(false);
            this.mCapabilityDiscovery.stop();
        } else if (!mCapabilityModuleOn) {
            this.mCapabilityDiscovery.setCapabilityModuleOn(true);
            this.mCapabilityDiscovery.start();
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserSwitched() {
        int userId = Extensions.ActivityManager.getCurrentUser();
        Log.d(LOG_TAG, "onUserSwitched: userId = " + userId);
        for (Integer phoneId : this.mCapabilityDiscovery.getUrisToRequest().keySet()) {
            List<ImsUri> urisToRequest = this.mCapabilityDiscovery.getUrisToRequest().get(phoneId);
            synchronized (urisToRequest) {
                urisToRequest.clear();
            }
            this.mCapabilityDiscovery.putUrisToRequestList(phoneId.intValue(), urisToRequest);
        }
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        if (capabilityDiscoveryModule.getCapabilityConfig(capabilityDiscoveryModule.getDefaultPhoneId()) != null) {
            CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
            if (!capabilityDiscoveryModule2.getCapabilityConfig(capabilityDiscoveryModule2.getDefaultPhoneId()).isDisableInitialScan()) {
                Log.d(LOG_TAG, "onUserSwitched: start ContactCache");
                this.mCapabilityDiscovery.getPhonebook().stop();
                this.mCapabilityDiscovery.getPhonebook().start();
                this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long getRandomizedDelayForPeriodicPolling(int phoneId, long delay) {
        IMSLog.d(LOG_TAG, phoneId, "getRandomizedDelayForPeriodicPolling: delay: " + (1000 * delay));
        return (long) (((0.2d * Math.random()) + 0.9d) * ((double) delay));
    }
}
