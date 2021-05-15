package com.sec.internal.helper;

import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;

public class OmcCode {
    private static final String LOG_TAG = "OmcCode";
    public static final String OMCNW_CODE_PROPERTY = "ro.csc.omcnw_code";
    public static final String OMCNW_CODE_PROPERTY2 = "ro.csc.omcnw_code2";
    public static final String OMC_CODE_PROPERTY = "ro.csc.sales_code";
    public static final String PERSIST_OMC_CODE_PROPERTY = "persist.omc.sales_code";

    public static String get() {
        String salesCode = SemSystemProperties.get(PERSIST_OMC_CODE_PROPERTY, "");
        if (TextUtils.isEmpty(salesCode)) {
            return SemSystemProperties.get(OMC_CODE_PROPERTY, "");
        }
        return salesCode;
    }

    public static String getNWCode(int simslot) {
        return getNWCode(simslot, true);
    }

    public static String getNWCode(int simslot, boolean fallback) {
        String str = "";
        if (getOmcVersion() < 5.0d || !"dsds".equals(SemSystemProperties.get("persist.radio.multisim.config"))) {
            if (fallback) {
                str = get();
            }
            return SemSystemProperties.get(OMCNW_CODE_PROPERTY, str);
        } else if (simslot == 1) {
            if (fallback) {
                str = get();
            }
            return SemSystemProperties.get(OMCNW_CODE_PROPERTY2, str);
        } else {
            if (fallback) {
                str = get();
            }
            return SemSystemProperties.get(OMCNW_CODE_PROPERTY, str);
        }
    }

    public static boolean isTmpSimSwap(int simslot) {
        return !get().equals(getNWCode(simslot));
    }

    public static String getPath() {
        return SemSystemProperties.get("persist.sys.omc_path", "/system/csc");
    }

    public static String getNWPath() {
        return SemSystemProperties.get("persist.sys.omcnw_path", getPath());
    }

    public static String getNWPath(int phoneId) {
        if (phoneId == 1) {
            return SemSystemProperties.get("persist.sys.omcnw_path2", getPath());
        }
        return SemSystemProperties.get("persist.sys.omcnw_path", getPath());
    }

    public static String getEtcPath() {
        return SemSystemProperties.get("persist.sys.omc_etcpath", getNWPath());
    }

    public static boolean isOmcModel() {
        return CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("persist.sys.omc_support", ConfigConstants.VALUE.INFO_COMPLETED));
    }

    public static boolean isSKTOmcCode() {
        return "SKT".equals(get()) || "SKC".equals(get());
    }

    public static boolean isKTTOmcCode() {
        return "KTT".equals(get()) || "KTC".equals(get());
    }

    public static boolean isLGTOmcCode() {
        return "LGT".equals(get()) || "LUC".equals(get());
    }

    public static boolean isKorOpenOmcCode() {
        return "KOO".equals(get()) || get().contains("K0");
    }

    public static boolean isKOROmcCode() {
        return isKorOpenOmcCode() || isSKTOmcCode() || isKTTOmcCode() || isLGTOmcCode();
    }

    public static boolean isChinaOmcCode() {
        String omc = get();
        return "CHC".equals(omc) || "CHM".equals(omc) || "TGY".equals(omc) || "BRI".equals(omc);
    }

    public static boolean isMainlandChinaOmcCode() {
        String omc = get();
        return "CHC".equals(omc) || "CHM".equals(omc);
    }

    public static boolean isJPNOmcCode() {
        return "DCM".equals(get()) || "KDI".equals(get()) || "KDR".equals(get()) || "UQM".equals(get()) || "JCO".equals(get()) || "SJP".equals(get());
    }

    public static boolean isDCMOmcCode() {
        return "DCM".equals(get());
    }

    public static double getOmcVersion() {
        try {
            return Double.parseDouble(SemFloatingFeature.getInstance().getString(ImsConstants.SecFloatingFeatures.CONFIG_OMC_VERSION, "0.0"));
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "NumberFormatException");
            return 0.0d;
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, "NullPointerException");
            return 0.0d;
        }
    }
}
