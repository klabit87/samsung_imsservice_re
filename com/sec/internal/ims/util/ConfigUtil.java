package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ConfigUtil {
    private static final String ALGORITHM = "AES";
    public static final String LOCAL_CONFIG_FILE = "localconfig";
    private static final String LOG_TAG = ConfigUtil.class.getSimpleName();
    public static final String SDCARD_CONFIG_FILE = "/localconfig/config-local.xml";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String UTF8 = "UTF-8";
    private static final byte[] mAesIvBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] mAesKeyBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6};

    public static String getAcsServerType(Context context, int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
    }

    public static String getAcsCustomServerUrl(Context context, int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
    }

    public static String getNetworkType(Context context, int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.NETWORK_TYPE, "ims,internet,wifi");
    }

    public static String getModelName(int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.MODEL_NAME, "");
    }

    public static String getSmsType(int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.OTP_SMS_TYPE, "");
    }

    public static String getSmsPort(int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.RCS.OTP_SMS_PORT, "");
    }

    public static String getSetting(String key, int phoneId) {
        return ImsRegistry.getString(phoneId, key, "");
    }

    public static boolean isRcsChatEnabled(Context ctx, int phoneId, ISimManager sm, boolean defaultValue) {
        boolean result = defaultValue;
        if (sm == null) {
            return result;
        }
        ContentValues mnoInfo = sm.getMnoInfo();
        boolean z = false;
        if (CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false) && CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false) && CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false)) {
            z = true;
        }
        boolean result2 = z;
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "isRcsChatEnabled: " + result2);
        return result2;
    }

    public static String getRcsProfileWithFeature(Context ctx, int phoneId, ImsProfile imsProfile) {
        if (imsProfile == null) {
            IMSLog.e(LOG_TAG, phoneId, "getRcsProfileWithFeature: imsProfile: empty");
            return "";
        }
        String rcsProfile = imsProfile.getRcsProfile();
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getRcsProfileWithFeature: rcsProfile from imsProfile: " + rcsProfile);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        Mno mno = sm != null ? sm.getNetMno() : Mno.DEFAULT;
        String rcsAs = getAcsServerType(ctx, phoneId);
        if (TextUtils.isEmpty(rcsProfile) || ImsConstants.RCS_AS.JIBE.equals(rcsAs) || mno != Mno.VZW) {
            return rcsProfile;
        }
        if (!isRcsChatEnabled(ctx, phoneId, sm, rcsProfile.startsWith("UP"))) {
            IMSLog.d(LOG_TAG, phoneId, "getRcsProfileWithFeature: use default rcsProfile");
            return "";
        }
        String str2 = LOG_TAG;
        IMSLog.d(str2, phoneId, "getRcsProfileWithFeature: use " + rcsProfile + " rcsProfile");
        return rcsProfile;
    }

    public static String getRcsProfileLoaderInternalWithFeature(Context ctx, String mnoName, int simSlot) {
        String rcsProfile;
        Mno mno = Mno.DEFAULT;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(simSlot);
        if (sm != null) {
            rcsProfile = ImsProfileLoaderInternal.getRcsProfile(ctx, sm.getSimMnoName(), simSlot);
            mno = sm.getNetMno();
        } else {
            rcsProfile = ImsProfileLoaderInternal.getRcsProfile(ctx, mnoName, simSlot);
        }
        String str = LOG_TAG;
        IMSLog.d(str, simSlot, "getRcsProfileLoaderInternalWithFeature: rcsProfile: " + rcsProfile);
        String rcsAs = getAcsServerType(ctx, simSlot);
        if (TextUtils.isEmpty(rcsProfile) || ImsConstants.RCS_AS.JIBE.equals(rcsAs) || mno != Mno.VZW) {
            return rcsProfile;
        }
        if (!isRcsChatEnabled(ctx, simSlot, sm, rcsProfile.startsWith("UP"))) {
            IMSLog.d(LOG_TAG, simSlot, "getRcsProfileLoaderInternalWithFeature: use default rcsProfile");
            return "";
        }
        String str2 = LOG_TAG;
        IMSLog.d(str2, simSlot, "getRcsProfileLoaderInternalWithFeature: rcsProfile: " + rcsProfile);
        return rcsProfile;
    }

    public static int getAutoconfigSourceWithFeature(Context ctx, int phoneId, int defval) {
        int result = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.RCS.LOCAL_CONFIG_SERVER, defval);
        String rcsAs = getAcsServerType(ctx, phoneId);
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getAutoconfigSourceWithFeature: " + result + " from globalSettings");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        Mno mno = sm != null ? sm.getNetMno() : Mno.DEFAULT;
        if (ImsConstants.RCS_AS.JIBE.equals(rcsAs) || mno != Mno.VZW || result != 0) {
            return result;
        }
        int result2 = !isRcsChatEnabled(ctx, phoneId, sm, true) ? 2 : result;
        String str2 = LOG_TAG;
        IMSLog.d(str2, phoneId, "getAutoconfigSourceWithFeature: use " + result2);
        return result2;
    }

    public static boolean hasAcsProfile(Context context, int phoneId, ISimManager sm) {
        IMSLog.d(LOG_TAG, phoneId, "hasAcsProfile:");
        if (!isRcsAvailable(context, phoneId, sm)) {
            return false;
        }
        IRegistrationManager rm = ImsRegistry.getRegistrationManager();
        if (CollectionUtils.isNullOrEmpty((Object[]) rm.getProfileList(phoneId))) {
            IMSLog.e(LOG_TAG, phoneId, "no profile found");
            return false;
        } else if (sm.getSimMno() == Mno.DEFAULT) {
            IMSLog.e(LOG_TAG, phoneId, "no SIM loaded");
            return false;
        } else if (!isSimMobilityRCS(phoneId, sm, rm)) {
            IMSLog.e(LOG_TAG, phoneId, "This is a other country SIM, RCS disabled in SIM mobility");
            return false;
        } else {
            for (ImsProfile profile : rm.getProfileList(phoneId)) {
                if (profile.getNeedAutoconfig()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isRcsAvailable(Context context, int phoneId, ISimManager sm) {
        IMSLog.d(LOG_TAG, phoneId, "isRcsAvailable:");
        if (sm == null || (sm != null && sm.hasNoSim())) {
            return false;
        }
        if (!RcsUtils.DualRcs.isRegAllowed(context, phoneId)) {
            IMSLog.d(LOG_TAG, phoneId, "DDS set to other SIM");
            return false;
        }
        boolean isRcsUserSettingEnabled = true;
        if (!(sm.getSimMno() == Mno.ATT || sm.getSimMno() == Mno.VZW)) {
            int rcsUserSetting = ImsConstants.SystemSettings.getRcsUserSetting(context, -1, phoneId);
            isRcsUserSettingEnabled = rcsUserSetting == 1 || rcsUserSetting == 2;
        }
        if (!ImsRegistry.getConfigModule().isRcsEnabled(phoneId) || !isRcsUserSettingEnabled) {
            return false;
        }
        return true;
    }

    public static boolean hasChatbotService(int phoneId, IRegistrationManager rm) {
        boolean hasChatBot = false;
        for (ImsProfile profile : rm.getProfileList(phoneId)) {
            hasChatBot = profile.hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
            if (hasChatBot) {
                break;
            }
        }
        return hasChatBot;
    }

    public static boolean isRcsOnly(ImsProfile profile) {
        return !profile.hasService("mmtel") && !profile.hasService("mmtel-video") && !profile.hasService("smsip");
    }

    public static boolean isRcsEur(int phoneId) {
        return isRcsEur(SimUtil.getSimMno(phoneId));
    }

    public static boolean isRcsEur(Mno mno) {
        return mno.isEur() || mno.isSea() || mno.isMea() || mno.isSwa();
    }

    public static boolean isRcsEurNonRjil(Mno mno) {
        return isRcsEur(mno) && !mno.isRjil();
    }

    public static boolean isSimMobilityRCS(int phoneId, ISimManager sm, IRegistrationManager rm) {
        if (!ImsRegistry.getConfigModule().isSimMoActivatedAndRcsEurSupported(phoneId, sm, rm)) {
            IMSLog.d(LOG_TAG, phoneId, "isSimMobilityRCS: no need to check about SimMobility");
            return true;
        }
        boolean mobilityRCS = false;
        if (CollectionUtils.isNullOrEmpty((Object[]) rm.getProfileList(phoneId))) {
            IMSLog.d(LOG_TAG, phoneId, "isSimMobilityRCS: no profile found");
        } else {
            ImsProfile[] profileList = rm.getProfileList(phoneId);
            int length = profileList.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (profileList[i].getEnableRcs()) {
                    IMSLog.d(LOG_TAG, phoneId, "isSimMobilityRCS: RCS is enabled in SimMobility");
                    mobilityRCS = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        if (!OmcCode.isKorOpenOmcCode() || !sm.getSimMno().isKor()) {
            return mobilityRCS;
        }
        return true;
    }

    public static boolean checkSupportSimMobilityForRcsEur(int phoneId, ISimManager sm, IRegistrationManager rm) {
        List<IRegisterTask> rtl = rm.getPendingRegistration(phoneId);
        if (rtl == null) {
            return true;
        }
        for (IRegisterTask task : rtl) {
            if (task.isRcsOnly() && isRcsEurNonRjil(sm.getSimMno())) {
                IMSLog.d(LOG_TAG, phoneId, "checkSupportSimMobilityForRcsEur: is not support sim mobility for RCS");
                return false;
            }
        }
        return true;
    }

    public static boolean isGcForEur(Context context, int phoneId) {
        return isRcsEur(phoneId) && ImsConstants.RCS_AS.JIBE.equals(getAcsServerType(context, phoneId)) && isRcsPreConsent(context, phoneId);
    }

    public static boolean isRcsPreConsent(Context context, int phoneId) {
        return ImsRegistry.getInt(phoneId, GlobalSettingsConstants.RCS.PRE_CONSENT, 0) == 1;
    }

    public static boolean checkMdmRcsStatus(Context context, int phoneId) {
        Cursor cr;
        String result;
        String simSlot = String.valueOf(phoneId);
        Uri uri = Uri.parse("content://com.sec.knox.provider2/PhoneRestrictionPolicy");
        String[] selectionArgs = {"1", ConfigConstants.VALUE.INFO_COMPLETED, simSlot};
        boolean isAllowed = true;
        if (context == null) {
            return true;
        }
        try {
            cr = context.getContentResolver().query(uri, (String[]) null, "isRCSEnabled", selectionArgs, (String) null);
            if (cr != null) {
                if (cr.moveToFirst() && (result = cr.getString(cr.getColumnIndex("isRCSEnabled"))) != null && result.equals(ConfigConstants.VALUE.INFO_COMPLETED)) {
                    IMSLog.d(LOG_TAG, phoneId, "checkMdmRcsStatus: Disabled RCS");
                    isAllowed = false;
                }
            }
            if (cr != null) {
                cr.close();
            }
        } catch (IllegalArgumentException e) {
            IMSLog.e(LOG_TAG, phoneId, "checkMdmRcsStatus: isAllowed = true due to IllegalArgumentException");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return isAllowed;
        throw th;
    }

    public static boolean shallUsePreviousCookie(int lastErrorCode, Mno mno) {
        return mno == Mno.SWISSCOM && lastErrorCode >= 500 && lastErrorCode != 511;
    }

    public static boolean doesUpRcsProfileMatchProvisioningVersion(String rcsProfile, String rcsProvisioningVersion) {
        if (rcsProfile == null) {
            return false;
        }
        if (rcsProfile.startsWith("UP_1.0") || rcsProfile.startsWith("UP_T")) {
            return "2.0".equals(rcsProvisioningVersion);
        }
        if (rcsProfile.startsWith("UP_2.0") || rcsProfile.startsWith("UP_2.2")) {
            return ConfigConstants.PVALUE.PROVISIONING_VERSION_4_0.equals(rcsProvisioningVersion);
        }
        if (rcsProfile.startsWith("UP_2.3") || rcsProfile.startsWith("UP_2.4")) {
            return ConfigConstants.PVALUE.PROVISIONING_VERSION_5_0.equals(rcsProvisioningVersion);
        }
        return false;
    }

    public static int getConfigId(Context context, String fileName) {
        try {
            return context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getResourcesFromFile(Context context, int phoneId, String fileName, String charset) {
        InputStream inputStream;
        InputStream is;
        String xml = null;
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getResourcesFromFile: fileName: " + fileName);
        try {
            if (SDCARD_CONFIG_FILE.equals(fileName)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + SDCARD_CONFIG_FILE);
            } else {
                inputStream = context.getResources().openRawResource(getConfigId(context, LOCAL_CONFIG_FILE));
            }
            is = inputStream;
            byte[] buffer = new byte[is.available()];
            if (is.read(buffer) < 0) {
                IMSLog.e(LOG_TAG, phoneId, "fail to read buffer");
            }
            xml = new String(buffer, charset);
            if (is != null) {
                is.close();
            }
            return xml;
        } catch (IOException e) {
            e.printStackTrace();
            return xml;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static String encryptParam(String input) {
        try {
            SecretKeySpec key = new SecretKeySpec(mAesKeyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec(mAesIvBytes);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(1, key, iv);
            return new String(Base64.encode(cipher.doFinal(input.getBytes("UTF-8")), 0), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void encryptParams(Map<String, String> params, String... keys) {
        for (String key : keys) {
            String value = params.get(key.toLowerCase(Locale.US));
            if (value != null) {
                params.put(key.toLowerCase(Locale.US), new String(Base64.encode(value.getBytes(), 0)));
            }
        }
    }

    public static String decryptParam(String input, String defValue) {
        String result = null;
        if (input == null || "".equals(input)) {
            return defValue;
        }
        try {
            byte[] data = Base64.decode(input.getBytes("UTF-8"), 0);
            if (data != null) {
                SecretKeySpec key = new SecretKeySpec(mAesKeyBytes, "AES");
                IvParameterSpec iv = new IvParameterSpec(mAesIvBytes);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(2, key, iv);
                result = new String(cipher.doFinal(data), "UTF-8");
            }
            return result != null ? result : defValue;
        } catch (Exception e) {
            e.printStackTrace();
            return defValue;
        }
    }

    public static String getFormattedUserAgent(Mno mno, String model, String version, String clientVersion) {
        if (mno.isKor()) {
            String omcCode = "";
            if (OmcCode.isSKTOmcCode()) {
                omcCode = "SKT";
            } else if (OmcCode.isKTTOmcCode()) {
                omcCode = "KT";
            } else if (OmcCode.isLGTOmcCode()) {
                omcCode = "LGU";
            } else if (OmcCode.isKorOpenOmcCode()) {
                omcCode = "OMD";
            }
            return String.format(ConfigConstants.TEMPLATE.USER_AGENT_KOR, new Object[]{model, version, omcCode});
        }
        return String.format(ConfigConstants.TEMPLATE.USER_AGENT, new Object[]{model, version, clientVersion});
    }

    public static String buildIdentity(Context context, int phoneId) {
        int subId = SimUtil.getSubId(phoneId);
        String identity = "";
        ITelephonyManager telephonyManagerWrapper = TelephonyManagerWrapper.getInstance(context);
        String imsi = telephonyManagerWrapper.getSubscriberId(subId);
        String msisdn = telephonyManagerWrapper.getMsisdn(subId);
        String imei = telephonyManagerWrapper.getDeviceId(phoneId);
        if (!TextUtils.isEmpty(imsi)) {
            identity = "IMSI_" + imsi;
        } else if (!TextUtils.isEmpty(msisdn)) {
            identity = "MSISDN_" + msisdn;
        } else if (!TextUtils.isEmpty(imei)) {
            identity = "IMEI_" + imei;
        } else {
            IMSLog.e(LOG_TAG, phoneId, "identity error");
        }
        String identity2 = identity.replaceAll("[\\W]", "");
        IMSLog.d(LOG_TAG, phoneId, "buildIdentity: " + subId + ", + identity : " + IMSLog.checker(identity2));
        return identity2;
    }
}
