package com.sec.internal.helper;

import android.os.SemSystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimUtil {
    private static final String LOG_TAG = "SimUtil";
    private static int sDefaultPhoneId = 0;
    private static Map<Integer, Mno> sMnoMap = new ConcurrentHashMap();
    private static int sPhoneCount = 0;
    private static SubscriptionManager sSubMgr = null;

    public static int getSimSlotPriority() {
        SubscriptionManager subscriptionManager = sSubMgr;
        if (subscriptionManager == null) {
            Log.d(LOG_TAG, "getSimSlotPriority: SubscriptionManager is not created. Return 0..");
            return 0;
        }
        int simSlot = Extensions.SubscriptionManager.getDefaultDataPhoneId(subscriptionManager);
        if (isValidSimSlot(simSlot)) {
            return simSlot;
        }
        Log.d(LOG_TAG, "getSimSlotPriority: Invalid DDS slot: " + simSlot + ", phoneCount: " + sPhoneCount);
        int slot = 0;
        while (slot < sPhoneCount) {
            SubscriptionInfo subinfo = sSubMgr.getActiveSubscriptionInfoForSimSlotIndex(slot);
            if (subinfo == null || subinfo.getSubscriptionId() == -1) {
                slot++;
            } else {
                Log.d(LOG_TAG, "subInfo is valid on slot#" + slot);
                return slot;
            }
        }
        return getDefaultPhoneId();
    }

    public static boolean isValidSimSlot(int slot) {
        return slot >= 0 && slot < sPhoneCount;
    }

    public static void setSubMgr(SubscriptionManager subMgr) {
        sSubMgr = subMgr;
    }

    public static int getPhoneCount() {
        return sPhoneCount;
    }

    public static void setPhoneCount(int phoneCount) {
        sPhoneCount = phoneCount;
    }

    public static int getDefaultPhoneId() {
        return sDefaultPhoneId;
    }

    public static void setDefaultPhoneId(int phoneId) {
        sDefaultPhoneId = phoneId;
    }

    public static String getConfigDualIMS() {
        if (sPhoneCount < 2) {
            return SimConstants.DSDS_SI_DDS;
        }
        String configDualImsProp = SemSystemProperties.get("persist.ril.config.dualims", "");
        if (!TextUtils.isEmpty(configDualImsProp)) {
            return configDualImsProp;
        }
        String configDualIMS = SemFloatingFeature.getInstance().getString(ImsConstants.SecFloatingFeatures.CONFIG_DUAL_IMS);
        if (TextUtils.isEmpty(configDualIMS)) {
            configDualIMS = SimConstants.DSDS_SI_DDS;
        }
        if (!SemCscFeature.getInstance().getBoolean("CscFeature_Common_SupportDualIMS", true)) {
            return SimConstants.DSDS_SI_DDS;
        }
        return configDualIMS;
    }

    public static boolean isDualIMS() {
        String config = getConfigDualIMS();
        return SimConstants.DSDS_DI.equals(config) || SimConstants.DSDA_DI.equals(config);
    }

    public static boolean isDdsSimSlot(int phoneId) {
        return phoneId == Extensions.SubscriptionManager.getDefaultDataPhoneId(sSubMgr);
    }

    public static int getSubId(int phoneId) {
        int[] subIdArray = Extensions.SubscriptionManager.getSubId(phoneId);
        if (subIdArray != null) {
            return subIdArray[0];
        }
        IMSLog.e(LOG_TAG, phoneId, "subIdArray is null");
        return -1;
    }

    public static boolean isMultiSimSupported() {
        String sp = SemSystemProperties.get("persist.radio.multisim.config");
        return "dsds".equals(sp) || "dsda".equals(sp);
    }

    public static boolean isSimMobilityFeatureEnabled() {
        int isSimMoEnabledForTest = SemSystemProperties.getInt(ImsConstants.SystemProperties.SIMMOBILITY_ENABLE, -1);
        if (isSimMoEnabledForTest == 1) {
            Log.i(LOG_TAG, "SimMobility Enabled for test");
            return true;
        } else if (isSimMoEnabledForTest == 0) {
            Log.i(LOG_TAG, "SimMobility disabled by manual");
            return false;
        } else if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 28) {
            return true;
        } else {
            return SemFloatingFeature.getInstance().getBoolean(ImsConstants.SecFloatingFeatures.SIM_MOBILITY_ENABLED);
        }
    }

    public static boolean isSoftphoneEnabled() {
        String mdmnTypes = SemCscFeature.getInstance().getString(CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGMDMNTYPE).toUpperCase();
        if (TextUtils.isEmpty(mdmnTypes) || !mdmnTypes.contains("Softphone".toUpperCase())) {
            return false;
        }
        return true;
    }

    public static void setSimMno(int phoneId, Mno mno) {
        IMSLog.i(LOG_TAG, phoneId, "setSimMno : " + mno);
        sMnoMap.put(Integer.valueOf(phoneId), mno);
    }

    public static Mno getMno() {
        return getMno(sDefaultPhoneId, true);
    }

    public static Mno getMno(int phoneId) {
        return getMno(phoneId, true);
    }

    public static Mno getSimMno(int phoneId) {
        return getMno(phoneId, false);
    }

    private static Mno getMno(int phoneId, boolean referSalesCode) {
        if (Mno.getMockMno() != null) {
            return Mno.getMockMno();
        }
        if (sMnoMap.get(Integer.valueOf(phoneId)) != null) {
            return sMnoMap.get(Integer.valueOf(phoneId));
        }
        if (!referSalesCode) {
            return Mno.DEFAULT;
        }
        IMSLog.e(LOG_TAG, phoneId, "fail to get mno from map");
        return Mno.fromSalesCode(OmcCode.getNWCode(phoneId));
    }

    public static int getOppositeSimSlot(int currentSimSlot) {
        return currentSimSlot == ImsConstants.Phone.SLOT_1 ? ImsConstants.Phone.SLOT_2 : ImsConstants.Phone.SLOT_1;
    }

    public static boolean isSupportCarrierVersion(int phoneId) {
        String clientId = SemCscFeature.getInstance().getString(phoneId, "CscFeature_GMS_SetClientIDBaseMs");
        return !clientId.isEmpty() && !clientId.contains("samsung");
    }
}
