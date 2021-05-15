package com.sec.internal.ims.core.sim;

import android.content.ContentValues;
import android.content.Context;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.util.CscParserConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimManagerUtils {
    private static final String LOG_TAG = "SimManager-Utils";

    SimManagerUtils() {
    }

    static boolean needImsUpOnUnknownState(Context ctx, int phoneId) {
        if (Settings.System.getInt(ctx.getContentResolver(), new String[]{"phone1_on", "phone2_on"}[phoneId], 1) == 1 || !SemSystemProperties.get("ro.boot.hardware").contains("qcom")) {
            return false;
        }
        return true;
    }

    static String extractMnoFromImpi(String impi) {
        if (impi == null) {
            Log.e(LOG_TAG, "IMPI is null");
            return null;
        }
        Matcher m = Pattern.compile("\\d+@ims\\.mnc\\d+\\.mcc\\d+\\.3gppnetwork\\.org").matcher(impi);
        if (TextUtils.isEmpty(impi)) {
            Log.e(LOG_TAG, "IMPI is empty");
            return "";
        } else if (m.matches()) {
            int start = impi.indexOf("mcc") + 3;
            String mccMnc = impi.substring(start, impi.indexOf(".", start));
            int start2 = impi.indexOf("mnc") + 3;
            int start3 = impi.indexOf(".", start2);
            return mccMnc + impi.substring(start2, start3);
        } else if (impi.indexOf("@") == 14) {
            return impi.substring(0, 5);
        } else {
            return impi.substring(0, 6);
        }
    }

    static String extractImsiFromImpi(String impi, String imsiFromTm) {
        if (impi == null) {
            Log.e(LOG_TAG, "IMPI is null");
            return imsiFromTm;
        } else if (TextUtils.isEmpty(impi)) {
            Log.e(LOG_TAG, "IMPI is empty");
            return imsiFromTm;
        } else {
            int end = impi.indexOf("@");
            if (end >= 14) {
                return impi.substring(0, end);
            }
            Log.e(LOG_TAG, "@ not found, IMPI is invalid");
            return imsiFromTm;
        }
    }

    static int[] parseMccMnc(int phoneId, String mccMnc) {
        if (!isValidSimOperator(phoneId, mccMnc)) {
            IMSLog.e(LOG_TAG, phoneId, "parseMccMnc: mccMnc is invalid");
            return null;
        }
        try {
            return new int[]{Integer.parseInt(mccMnc.substring(0, 3)), Integer.parseInt(mccMnc.substring(3))};
        } catch (NumberFormatException e) {
            IMSLog.e(LOG_TAG, phoneId, "parseMccMnc: operator is invalid : " + mccMnc);
            return null;
        }
    }

    static boolean isValidSimOperator(int phoneId, String operator) {
        if (!TextUtils.isEmpty(operator) && operator.length() >= 3) {
            return true;
        }
        IMSLog.e(LOG_TAG, phoneId, "isValidSimOperator: operator is invalid");
        return false;
    }

    static boolean isISimAppPresent(int phoneId, ITelephonyManager tm) {
        if (tm.getPhoneCount() == 1) {
            return true ^ TextUtils.isEmpty(tm.getAidForAppType(5));
        }
        return "1".equals(tm.getTelephonyProperty(phoneId, "ril.hasisim", "0"));
    }

    static String readSimStateProperty(int phoneId, ITelephonyManager tm) {
        if (tm == null) {
            return "UNKNOWN";
        }
        String state = tm.getTelephonyProperty(phoneId, ImsConstants.SystemProperties.SIM_STATE, "UNKNOWN");
        char c = 65535;
        int hashCode = state.hashCode();
        if (hashCode != -2044189691) {
            if (hashCode != 0) {
                if (hashCode != 77848963) {
                    if (hashCode != 433141802) {
                        if (hashCode != 1034051831) {
                            if (hashCode == 1924388665 && state.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
                                c = 0;
                            }
                        } else if (state.equals("NOT_READY")) {
                            c = 2;
                        }
                    } else if (state.equals("UNKNOWN")) {
                        c = 5;
                    }
                } else if (state.equals("READY")) {
                    c = 1;
                }
            } else if (state.equals("")) {
                c = 4;
            }
        } else if (state.equals(IccCardConstants.INTENT_VALUE_ICC_LOADED)) {
            c = 3;
        }
        if (c == 0 || c == 1 || c == 2 || c == 3) {
            return state;
        }
        if (c == 4 || c == 5) {
            return "UNKNOWN";
        }
        return IccCardConstants.INTENT_VALUE_ICC_LOCKED;
    }

    static String getOmcNetworkCode(int phoneId, String defaultValue) {
        String targetProperty = OmcCode.OMCNW_CODE_PROPERTY;
        if (OmcCode.getOmcVersion() >= 5.0d) {
            targetProperty = phoneId == ImsConstants.Phone.SLOT_1 ? OmcCode.OMCNW_CODE_PROPERTY : OmcCode.OMCNW_CODE_PROPERTY2;
        }
        return SemSystemProperties.get(targetProperty, defaultValue);
    }

    static String getEhplmn(SubscriptionInfo subInfo) {
        try {
            return (String) ((List) Optional.ofNullable(subInfo.getEhplmns()).orElse(Collections.emptyList())).stream().filter($$Lambda$SimManagerUtils$CfbNCm4plA8nnLFICz58e_nFfy8.INSTANCE).findFirst().orElse("");
        } catch (NoSuchMethodError e) {
            Log.e(LOG_TAG, "getEhplmn: " + e);
            return "";
        }
    }

    static /* synthetic */ boolean lambda$getEhplmn$0(String e) {
        return e.length() >= 5;
    }

    static String getMvnoName(String name) {
        int delimiterPos = name.indexOf(Mno.MVNO_DELIMITER);
        if (delimiterPos != -1) {
            return name.substring(delimiterPos + 1);
        }
        return "";
    }

    static ContentValues getSimMobilityRcsSettings(int phoneId, List<ImsProfile> profiles) {
        ContentValues rcsSettings = new ContentValues();
        boolean isEnableRcs = false;
        boolean isEnableRcsChat = false;
        Iterator<ImsProfile> it = profiles.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ImsProfile p = it.next();
            if (p.getEnableRcs()) {
                isEnableRcs = true;
                if (p.getEnableRcsChat()) {
                    isEnableRcsChat = true;
                }
            }
        }
        if (isEnableRcs) {
            IMSLog.i(LOG_TAG, phoneId, "getSimMobilityRcsSettings: isEnableRcs true");
            rcsSettings.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, true);
        }
        if (isEnableRcsChat) {
            IMSLog.i(LOG_TAG, phoneId, "getSimMobilityRcsSettings: isEnableRcsChat true");
            rcsSettings.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, true);
        }
        return rcsSettings;
    }

    static String getSimEmergencyDomain(ContentValues mnoInfo) {
        Integer imsSwitchType = mnoInfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
        if (imsSwitchType == null || imsSwitchType.intValue() == 0) {
            return "";
        }
        return mnoInfo.getAsString(CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_EMERGENCY_CALL);
    }

    static String convertMnoInfoToString(ContentValues mnoInfo) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        StringBuilder summary = new StringBuilder();
        summary.append(CollectionUtils.getStringValue(mnoInfo, ISimManager.KEY_NW_NAME, "?"));
        summary.append("|");
        String str6 = "T";
        summary.append(CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.DeviceManagement.ENABLE_IMS, false) ? str6 : "F");
        if (CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.ENABLE_VOLTE, false)) {
            str = str6;
        } else {
            str = "F";
        }
        summary.append(str);
        if (CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.DeviceManagement.SUPPORT_VOWIFI, false)) {
            str2 = str6;
        } else {
            str2 = "F";
        }
        summary.append(str2);
        if (CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.ENABLE_SMS_IP, false)) {
            str3 = str6;
        } else {
            str3 = "F";
        }
        summary.append(str3);
        if (CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.SS_CSFB_IMS_ERROR, false)) {
            str4 = str6;
        } else {
            str4 = "F";
        }
        summary.append(str4);
        if (CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS, false)) {
            str5 = str6;
        } else {
            str5 = "F";
        }
        summary.append(str5);
        if (!CollectionUtils.getBooleanValue(mnoInfo, CscParserConstants.CustomerSettingTable.RCS.ENABLE_RCS_CHAT_SERVICE, false)) {
            str6 = "F";
        }
        summary.append(str6);
        summary.append((String) CscParserConstants.CustomerSettingTable.VoLTE.MAP_DOMAIN_VOICE_EUTRAN.entrySet().stream().filter(new Predicate(mnoInfo) {
            public final /* synthetic */ ContentValues f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return ((String) ((Map.Entry) obj).getKey()).equalsIgnoreCase(CollectionUtils.getStringValue(this.f$0, CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_VOICE_EUTRAN, ""));
            }
        }).map($$Lambda$etDQhIA8H5hI6BDqsFIFQkLL9Nc.INSTANCE).findFirst().orElse("?"));
        String eDomain = CollectionUtils.getStringValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_EMERGENCY_CALL, "");
        if (TextUtils.isEmpty(eDomain)) {
            eDomain = CollectionUtils.getStringValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_EMERGENCY_CALL_FIX_TYPO, "");
        }
        summary.append((String) Optional.ofNullable(CscParserConstants.CustomerSettingTable.VoLTE.MAP_DOMAIN_EMERGENCY_CALL.get(eDomain)).orElse("?"));
        summary.append((String) Optional.ofNullable(CscParserConstants.CustomerSettingTable.VoLTE.MAP_DOMAINS_PSCS.get(CollectionUtils.getStringValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_SS, ""))).orElse("?"));
        summary.append((String) Optional.ofNullable(CscParserConstants.CustomerSettingTable.VoLTE.MAP_DOMAINS_PSCS.get(CollectionUtils.getStringValue(mnoInfo, CscParserConstants.CustomerSettingTable.VoLTE.DOMAIN_USSD, ""))).orElse("?"));
        return summary.toString();
    }
}
