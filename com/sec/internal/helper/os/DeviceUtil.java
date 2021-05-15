package com.sec.internal.helper.os;

import android.content.Context;
import android.os.SemSystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.internal.util.ArrayUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class DeviceUtil {
    private static final String LOG_TAG = DeviceUtil.class.getSimpleName();
    private static final String OMC_DATA_FILE = "omc_data.json";
    private static final String OMC_PATH_PRISM = "/prism/etc/";
    private static final String[][] REPRESENTATIVE_COUNTRY_ISO = {new String[]{"SE", "NO", "DK", "FI", "IS", "GL"}, new String[]{"LU", "BE"}, new String[]{"LV", "LT", "EE"}, new String[]{"RS", "HR", "AL"}};

    public static boolean isTablet() {
        return SemSystemProperties.get("ro.build.characteristics", "").contains("tablet");
    }

    public static boolean isUSOpenDevice() {
        return SemSystemProperties.get("ro.simbased.changetype", "").contains("SED");
    }

    public static boolean isUSMvnoDevice() {
        return ArrayUtils.contains(new String[]{"TFN", "TFV", "TFA", "TFO", "XAG", "XAR"}, OmcCode.get());
    }

    public static boolean isOtpAuthorized() {
        try {
            byte[] data = Files.readAllBytes(Paths.get(ImsConstants.SystemPath.EFS, new String[]{".otp_auth"}));
            if (data == null || !Arrays.equals(data, CloudMessageProviderContract.JsonData.TRUE.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isUserUnlocked(Context context) {
        UserManager userManager;
        if (context != null && (userManager = (UserManager) context.getSystemService(UserManager.class)) != null) {
            return userManager.isUserUnlocked();
        }
        IMSLog.d(LOG_TAG, "temp log : User is lock");
        return false;
    }

    public static String getModemBoardName() {
        return SemSystemProperties.get("ril.modem.board", "").trim();
    }

    public static boolean getGcfMode() {
        return "1".equals(SemSystemProperties.get(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, "0"));
    }

    public static void setGcfMode(boolean mode) {
        String str = "";
        SemSystemProperties.set(Mno.MOCK_MNO_PROPERTY, mode ? Mno.GCF_OPERATOR_CODE : str);
        String str2 = Mno.MOCK_MNONAME_PROPERTY;
        if (mode) {
            str = Mno.GCF_OPERATOR_NAME;
        }
        SemSystemProperties.set(str2, str);
        String str3 = "1";
        SemSystemProperties.set(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, mode ? str3 : "0");
        if (!mode) {
            str3 = "0";
        }
        SemSystemProperties.set(ImsConstants.SystemProperties.GCF_MODE_PROPERTY_P_OS, str3);
    }

    public static int getWifiStatus(Context context, int defaultValue) {
        int wifiStatus;
        try {
            wifiStatus = Settings.Global.getInt(context.getContentResolver(), "wifi_on");
        } catch (Settings.SettingNotFoundException e) {
            wifiStatus = defaultValue;
        }
        String str = LOG_TAG;
        IMSLog.d(str, "getWifiStatus: " + wifiStatus);
        return wifiStatus;
    }

    public static boolean isSupport5G(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TelephonyManager.class);
        if (telephonyManager == null || (524288 & telephonyManager.getSupportedRadioAccessFamily()) <= 0) {
            IMSLog.d(LOG_TAG, "Support5G() : false");
            return false;
        }
        IMSLog.d(LOG_TAG, "Support5G() : true");
        return true;
    }

    public static boolean isUnifiedSalesCodeInTSS() {
        String unified = SemSystemProperties.get("mdc.unified", ConfigConstants.VALUE.INFO_COMPLETED);
        String str = LOG_TAG;
        IMSLog.d(str, "UnifiedSalesCodeInTSS() : " + unified);
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(unified);
    }

    private static String representativeCountryISO(String countryIso) {
        String repISO = countryIso;
        String[][] strArr = REPRESENTATIVE_COUNTRY_ISO;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String[] representativeIso = strArr[i];
            if (ArrayUtils.contains(representativeIso, countryIso)) {
                repISO = representativeIso[0];
                break;
            }
            i++;
        }
        IMSLog.i(LOG_TAG, "representativeCountryISO " + countryIso + " ==> " + repISO);
        return repISO;
    }

    public static boolean includedSimByTSS(String mnoname) {
        return includedSimByTSS(mnoname, "/prism/etc//omc_data.json");
    }

    public static boolean includedSimByTSS(String mnoname, String omcDataPath) {
        JsonReader reader;
        Throwable th;
        boolean included = false;
        String countryIso = representativeCountryISO(Mno.getCountryFromMnomap(mnoname).getCountryIso());
        File file = new File(omcDataPath);
        if (!file.exists() || file.length() <= 0) {
            IMSLog.e(LOG_TAG, "omc_data.json not found.");
        } else {
            try {
                reader = new JsonReader(new BufferedReader(new FileReader(file)));
                JsonElement omc_data = new JsonParser().parse(reader);
                if (!omc_data.isJsonNull() && omc_data.isJsonObject() && omc_data.getAsJsonObject().has("unified_sales_code_list")) {
                    JsonElement list = omc_data.getAsJsonObject().getAsJsonObject("unified_sales_code_list");
                    String code = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, "");
                    if (!list.isJsonNull() && list.isJsonObject() && list.getAsJsonObject().has(code)) {
                        JsonElement je = list.getAsJsonObject().get(code);
                        if (!je.isJsonNull() && je.isJsonArray()) {
                            Iterator it = je.getAsJsonArray().iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (((JsonElement) it.next()).getAsString().equalsIgnoreCase(countryIso)) {
                                        included = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                reader.close();
            } catch (JsonParseException | IOException e) {
                String str = LOG_TAG;
                IMSLog.e(str, "omc_data.json parsing failed by " + e);
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "includedSimByTSS " + included + " in Unified Sales Code (TSS2.0)");
        return included;
        throw th;
    }
}
