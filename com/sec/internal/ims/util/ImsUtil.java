package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ImsUtil {
    public static final String IPME_STATUS = "ipme_status";
    private static final String LOG_TAG = ImsUtil.class.getSimpleName();

    public static boolean isRttModeOnFromCallSettings(Context context, int phoneId) {
        String rttSettingDb = "preferred_rtt_mode";
        if (phoneId > 0) {
            rttSettingDb = rttSettingDb + phoneId;
        }
        int isEnabled = Settings.Secure.getInt(context.getContentResolver(), rttSettingDb, 0);
        IMSLog.d(LOG_TAG, phoneId, rttSettingDb + " : " + isEnabled);
        if (isEnabled != 0) {
            return true;
        }
        return false;
    }

    static boolean isCdmalessModel() {
        return !"LRA".equalsIgnoreCase(OmcCode.get()) && SemFloatingFeature.getInstance().getBoolean(ImsConstants.SecFloatingFeatures.CDMALESS);
    }

    public static boolean isCdmalessEnabled(int phoneId) {
        return isCdmalessEnabled(phoneId, isSimMobilityActivated(phoneId));
    }

    public static boolean isCdmalessEnabled(int phoneId, boolean isSimMobility) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm != null) {
            return (!DeviceUtil.isTablet() && isSimMobility) || isCdmalessModel() || isVzwInboundWithCdmaLess(sm.getSimMno(), sm.getSimMnoName());
        }
        Log.d(LOG_TAG, "isCdmalessEnabled, SIM not ready");
        return isCdmalessModel();
    }

    public static boolean isVzwInboundWithCdmaLess(Mno simMno, String mnoName) {
        if (simMno != Mno.VZW) {
            return false;
        }
        String salesCode = OmcCode.get();
        for (String mvno : Mno.VZW.getAllSalesCodes()) {
            if (TextUtils.equals(mvno, salesCode)) {
                return false;
            }
        }
        if (!"VZW_US:TFN".equalsIgnoreCase(mnoName) || !"TFN".equalsIgnoreCase(salesCode)) {
            return true;
        }
        return false;
    }

    public static String getPathWithPhoneId(String path, int phoneId) {
        return path + "#" + ImsConstants.Uris.FRAGMENT_SIM_SLOT + phoneId;
    }

    public static boolean isSimMobilityActivated(int phoneId) {
        return SlotBasedConfig.getInstance(phoneId).isSimMobilityActivated();
    }

    public static boolean isMatchedService(Set<String> registeredService, String filter) {
        String[] mask;
        if ("volte".equals(filter)) {
            mask = ImsProfile.getVoLteServiceList();
        } else if (DeviceConfigManager.RCS.equals(filter)) {
            mask = ImsProfile.getRcsServiceList();
        } else if (ChatServiceImpl.SUBJECT.equals(filter)) {
            mask = ImsProfile.getChatServiceList();
        } else {
            Log.d(LOG_TAG, "invalid service type : " + filter);
            return false;
        }
        for (String service : mask) {
            if (registeredService.contains(service)) {
                return true;
            }
        }
        return false;
    }

    public static int getHandle(long handle) {
        return (int) handle;
    }

    public static void updateSsDomain(Context context, int phoneId, String ssDomain) {
        String str = LOG_TAG;
        Log.d(str, "update SS domain : " + ssDomain);
        ContentValues domain = new ContentValues();
        domain.put(GlobalSettingsConstants.SS.DOMAIN, ssDomain);
        context.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), phoneId), domain, (String) null, (String[]) null);
        ImsRegistry.getServiceModuleManager().getUtServiceModule().updateCapabilities(phoneId);
    }

    public static void updateEmergencyCallDomain(Context context, int phoneId, ImsProfile emergencyProfile, ISimManager sm, String eDomain) {
        boolean isPsTargetDomain;
        IMSLog.d(LOG_TAG, phoneId, "updateEmergencyCallDomain:");
        if (sm != null) {
            String str = LOG_TAG;
            IMSLog.d(str, phoneId, "emergencyCallDomain: " + eDomain);
            boolean isPsDomainInSettings = false;
            String targetDomain = "PS";
            if (targetDomain.equalsIgnoreCase(eDomain) || "IMS".equalsIgnoreCase(eDomain)) {
                isPsDomainInSettings = true;
            }
            String str2 = LOG_TAG;
            IMSLog.d(str2, phoneId, "emergencyCallDomain: isPsDomainInSettings-" + isPsDomainInSettings + ", SIM absent-" + sm.hasNoSim());
            if (!sm.hasNoSim()) {
                String simEDomain = ImsSharedPrefHelper.getString(phoneId, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", eDomain);
                if ((emergencyProfile != null && emergencyProfile.getSimMobility()) || DeviceUtil.getGcfMode().booleanValue()) {
                    simEDomain = "PS";
                }
                String str3 = LOG_TAG;
                IMSLog.d(str3, phoneId, "emergencyCallDomain: targetDomain from globalsetting-" + simEDomain);
                isPsTargetDomain = targetDomain.equalsIgnoreCase(simEDomain);
            } else {
                isPsTargetDomain = updateEmergencyCallDomainForNoSim(phoneId, sm, emergencyProfile, false);
                if (TextUtils.isEmpty(ImsSharedPrefHelper.getString(phoneId, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", ""))) {
                    ImsSharedPrefHelper.save(phoneId, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", eDomain);
                }
            }
            Mno mno = sm.getNetMno();
            if (mno.isAus() || isPsDomainInSettings != isPsTargetDomain) {
                if (mno.isAus()) {
                    isPsTargetDomain = true;
                } else if (isPsTargetDomain) {
                    boolean hasEmergencyProfile = emergencyProfile != null;
                    String str4 = LOG_TAG;
                    IMSLog.d(str4, phoneId, "hasEmergencyProfile: " + hasEmergencyProfile);
                    if (!hasEmergencyProfile) {
                        IMSLog.d(LOG_TAG, phoneId, "emergencyCallDomain: no E911 profile keep e-domain as CS");
                        return;
                    }
                }
                if (!isPsTargetDomain) {
                    targetDomain = "CS";
                }
                String str5 = LOG_TAG;
                IMSLog.d(str5, phoneId, "update emergency Domain: " + eDomain + " => " + targetDomain);
                ContentValues domain = new ContentValues();
                domain.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, targetDomain);
                context.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), phoneId), domain, (String) null, (String[]) null);
                return;
            }
            String str6 = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("emergencyCallDomain: already ");
            if (!isPsTargetDomain) {
                targetDomain = "CS";
            }
            sb.append(targetDomain);
            Log.d(str6, sb.toString());
        }
    }

    private static boolean updateEmergencyCallDomainForNoSim(int phoneId, ISimManager sm, ImsProfile emergencyProfile, boolean isPsTargetDomain) {
        boolean supportNoSimPsE911 = false;
        Mno salesCodeMno = Mno.fromSalesCode(OmcCode.get());
        Mno simMno = sm.getSimMno();
        if (salesCodeMno != simMno && !salesCodeMno.isChn() && !salesCodeMno.isHkMo()) {
            supportNoSimPsE911 = true;
        } else if ("PS".equalsIgnoreCase(ImsRegistry.getString(phoneId, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN_WITHOUT_SIM, "CS"))) {
            supportNoSimPsE911 = true;
        }
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "supportNoSimPsE911: " + supportNoSimPsE911 + ", simMno: " + simMno.getName() + ", salesCodeMno: " + salesCodeMno.getName());
        if (supportNoSimPsE911) {
            isPsTargetDomain = true;
        }
        if ((OmcCode.isChinaOmcCode() || OmcCode.isJPNOmcCode()) && DeviceUtil.getGcfMode().booleanValue()) {
            isPsTargetDomain = true;
        }
        if (OmcCode.isDCMOmcCode()) {
            isPsTargetDomain = true;
        }
        if (sm.getNetMno() == Mno.KDDI) {
            return true;
        }
        return isPsTargetDomain;
    }

    public static boolean isPttSupported() {
        boolean supported = SemFloatingFeature.getInstance().getBoolean(ImsConstants.SecFloatingFeatures.SUPPORT_PTT);
        String str = LOG_TAG;
        Log.d(str, "isPttSupported: " + supported);
        return supported;
    }

    public static String hideInfo(String info, int end) {
        try {
            if (!TextUtils.isEmpty(info)) {
                return info.substring(0, Math.min(end, info.length()));
            }
            return MessageContextValues.none;
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(LOG_TAG, "hideInfo had OutOfBoundeEception");
            return MessageContextValues.none;
        }
    }

    public static void listToDumpFormat(int MainSub, int phoneId, String chatId, List<String> list) {
        try {
            list.add(0, Integer.toString(phoneId));
            list.add(1, hideInfo(chatId, 4));
            IMSLog.c(MainSub, String.join(",", list));
        } catch (Exception e) {
            Log.e(LOG_TAG, "listToDumpFormat has an exception");
        }
    }

    public static void listToDumpFormat(int MainSub, int phoneId, String chatId) {
        listToDumpFormat(MainSub, phoneId, hideInfo(chatId, 4), new ArrayList<>());
    }

    public enum PdnFailReason {
        PDN_MAX_TIMEOUT(-22),
        PDN_THROTTLED(-8),
        INSUFFICIENT_RESOURCES(26),
        MISSING_UNKNOWN_APN(27),
        ACTIVATION_REJECT_GGSN(30),
        SERVICE_OPTION_NOT_SUPPORTED(32),
        SERVICE_OPTION_NOT_SUBSCRIBED(33),
        MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED(55),
        PROTOCOL_ERRORS(111),
        NO_IMS_APN(-1),
        NOT_DEFINED(-1);
        
        private final int mFailCause;

        private PdnFailReason(int failCause) {
            this.mFailCause = failCause;
        }

        public static PdnFailReason valueOf(int failCause) {
            PdnFailReason result = NOT_DEFINED;
            for (PdnFailReason pdnFail : values()) {
                if (pdnFail.mFailCause == failCause) {
                    return pdnFail;
                }
            }
            return result;
        }

        public int getCause() {
            return this.mFailCause;
        }
    }

    public static String getPublicId(int phoneId) {
        ImsUri impuUri;
        String impu = null;
        if (ImsServiceStub.getInstance() == null) {
            IMSLog.d(LOG_TAG, phoneId, "getImModule: getInstance is null");
            return null;
        }
        ImsRegistration reg = ((ImModule) ImsServiceStub.getInstance().getServiceModuleManager().getImModule()).getImsRegistration(phoneId);
        if (reg == null || (impuUri = reg.getPreferredImpu().getUri()) == null) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
            if (sm != null) {
                impu = sm.getImpuFromIsim(0);
                if (TextUtils.isEmpty(impu)) {
                    impu = sm.getDerivedImpu();
                }
            }
            if (!TextUtils.isEmpty(impu)) {
                return impu;
            }
            IMSLog.e(LOG_TAG, phoneId, "There is no impu");
            return "";
        }
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getPublicId: registered IMPU=" + IMSLog.checker(impuUri.toString()));
        return impuUri.toString();
    }

    /* renamed from: com.sec.internal.ims.util.ImsUtil$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason;

        static {
            int[] iArr = new int[PdnFailReason.values().length];
            $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason = iArr;
            try {
                iArr[PdnFailReason.PDN_THROTTLED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason[PdnFailReason.SERVICE_OPTION_NOT_SUPPORTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason[PdnFailReason.SERVICE_OPTION_NOT_SUBSCRIBED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason[PdnFailReason.MISSING_UNKNOWN_APN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason[PdnFailReason.PROTOCOL_ERRORS.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static boolean isPermanentPdnFailureReason(String reason) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$util$ImsUtil$PdnFailReason[PdnFailReason.valueOf(reason).ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4 || i == 5) {
            return true;
        }
        return false;
    }

    public static String getSystemProperty(String property, int phoneId, String defaultVal) {
        String propVal = null;
        String prop = SemSystemProperties.get(property);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                propVal = values[phoneId];
            }
        }
        return propVal == null ? defaultVal : propVal;
    }
}
