package com.sec.internal.helper.os;

import android.content.Context;
import android.os.SemSystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.util.ArrayUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class DeviceUtil {
    private static final String LOG_TAG = DeviceUtil.class.getSimpleName();
    private static final String OMC_DATA_FILE = "omc_data.json";
    private static final String OMC_PATH_PRISM = "/prism/etc/";

    public static boolean isTablet() {
        String deviceType = SemSystemProperties.get("ro.build.characteristics");
        return deviceType != null && deviceType.contains("tablet");
    }

    public static boolean isUSOpenDevice() {
        if (SemSystemProperties.get("ro.simbased.changetype", "").contains("SED")) {
            return true;
        }
        return false;
    }

    public static boolean isUSMvnoDevice() {
        String salesCode = OmcCode.get();
        return TextUtils.equals(salesCode, "TFN") || TextUtils.equals(salesCode, "TFV") || TextUtils.equals(salesCode, "TFA") || TextUtils.equals(salesCode, "TFO") || TextUtils.equals(salesCode, "XAG") || TextUtils.equals(salesCode, "XAR");
    }

    public static boolean isOtpAuthorized() {
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        int length = 0;
        try {
            FileInputStream fis2 = new FileInputStream("/efs/sec_efs/.otp_auth");
            length = fis2.read(buffer);
            try {
                fis2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (length > 0) {
            return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(new String(buffer).trim());
        }
        return false;
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

    public static Boolean getGcfMode() {
        return Boolean.valueOf("1".equals(SemSystemProperties.get(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, "0")));
    }

    public static void setGcfMode(Boolean mode) {
        String str = "";
        SemSystemProperties.set(Mno.MOCK_MNO_PROPERTY, mode.booleanValue() ? Mno.GCF_OPERATOR_CODE : str);
        String str2 = Mno.MOCK_MNONAME_PROPERTY;
        if (mode.booleanValue()) {
            str = Mno.GCF_OPERATOR_NAME;
        }
        SemSystemProperties.set(str2, str);
        String str3 = "1";
        SemSystemProperties.set(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, mode.booleanValue() ? str3 : "0");
        if (!mode.booleanValue()) {
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
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
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
        String[] NEE = {"SE", "NO", "DK", "FI", "IS", "GL"};
        String[] LUX = {"LU", "BE"};
        String[] SEB = {"LV", "LT", "EE"};
        String[] SEE = {"RS", "HR", "AL"};
        if (ArrayUtils.contains(NEE, countryIso)) {
            repISO = NEE[0];
        } else if (ArrayUtils.contains(LUX, countryIso)) {
            repISO = LUX[0];
        } else if (ArrayUtils.contains(SEB, countryIso)) {
            repISO = SEB[0];
        } else if (ArrayUtils.contains(SEE, countryIso)) {
            repISO = SEE[0];
        }
        String str = LOG_TAG;
        IMSLog.i(str, "representativeCountryISO = " + repISO);
        return repISO;
    }

    public static boolean includedSimByTSS(String mnoname) {
        JsonReader reader;
        Throwable th;
        boolean included = false;
        String countryIso = representativeCountryISO(Mno.getCountryFromMnomap(mnoname).getCountryIso());
        File file = new File("/prism/etc//omc_data.json");
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
                e.printStackTrace();
                IMSLog.e(LOG_TAG, "omc_data.json parsing fail.");
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        String str = LOG_TAG;
        IMSLog.i(str, "includedSimByTSS " + included + " in Unified Sales Code (TSS2.0)");
        return included;
        throw th;
    }
}
