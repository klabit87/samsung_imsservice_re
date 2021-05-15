package com.sec.internal.ims.servicemodules.ss;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.log.IMSLog;

public class UtLog {
    public static String extractLogFromUtProfile(UtProfile profile) {
        StringBuilder logBuilder = new StringBuilder("ImsUt[" + profile.requestId + "]> ");
        switch (profile.type) {
            case 100:
                logBuilder.append("queryCallForward");
                logBuilder.append(" cfType = ");
                logBuilder.append(getStringCfType(profile.condition));
                break;
            case 101:
                logBuilder.append("updateCallForward");
                logBuilder.append(" cfType = ");
                logBuilder.append(getStringCfType(profile.condition));
                logBuilder.append(" action = ");
                logBuilder.append(getStringAction(profile.action));
                logBuilder.append(" ssClass = ");
                logBuilder.append(getStringSsClass(profile.serviceClass));
                logBuilder.append(" noReplyTimer = ");
                logBuilder.append(profile.timeSeconds);
                logBuilder.append(" number = ");
                logBuilder.append(IMSLog.checker(profile.number));
                break;
            case 102:
            case 104:
                logBuilder.append("queryCallBarring");
                logBuilder.append(" cbType = ");
                logBuilder.append(getStringCbType(profile.condition));
                break;
            case 103:
            case 105:
                logBuilder.append("updateCallBarring");
                logBuilder.append(" cbType = ");
                logBuilder.append(getStringCbType(profile.condition));
                logBuilder.append(" action = ");
                logBuilder.append(getStringAction(profile.action));
                logBuilder.append(" ssClass = ");
                logBuilder.append(getStringSsClass(profile.serviceClass));
                logBuilder.append(" password = ");
                logBuilder.append(IMSLog.checker(profile.password));
                break;
            case 106:
                logBuilder.append("queryCLIP");
                break;
            case 107:
                logBuilder.append("updateCLIP");
                logBuilder.append(" enable = ");
                logBuilder.append(profile.enable);
                break;
            case 108:
                logBuilder.append("queryCLIR");
                break;
            case 109:
                logBuilder.append("updateCLIR");
                logBuilder.append(" clirMode = ");
                logBuilder.append(getStringClirMode(profile.condition));
                break;
            case 110:
                logBuilder.append("queryCOLP");
                break;
            case 111:
                logBuilder.append("updateCOLP");
                logBuilder.append(" enable = ");
                logBuilder.append(profile.enable);
                break;
            case 112:
                logBuilder.append("queryCOLR");
                break;
            case 113:
                logBuilder.append("updateCOLR");
                logBuilder.append(" presentation = ");
                logBuilder.append(profile.condition);
                break;
            case 114:
                logBuilder.append("queryCallWaiting");
                break;
            case 115:
                logBuilder.append("updateCallWaiting");
                logBuilder.append(" enable = ");
                logBuilder.append(getStringStatus(profile.enable));
                logBuilder.append(" ssClass = ");
                logBuilder.append(getStringSsClass(profile.serviceClass));
                break;
            case 116:
                logBuilder.append("querySimServDoc");
                break;
        }
        return logBuilder.toString();
    }

    public static String extractCrLogFromUtProfile(int phoneId, UtProfile profile) {
        StringBuilder crLog = new StringBuilder(phoneId + "," + profile.requestId + ",>,");
        switch (profile.type) {
            case 100:
                crLog.append("GET_CF,");
                crLog.append(getCrLogCfType(profile.condition));
                break;
            case 101:
                crLog.append("PUT_CF,");
                crLog.append(getCrLogCfType(profile.condition));
                crLog.append(",");
                crLog.append(getCrLogAction(profile.action));
                crLog.append(",");
                crLog.append(getCrLogSsClass(profile.serviceClass));
                crLog.append(",");
                crLog.append(profile.timeSeconds);
                crLog.append(",");
                crLog.append(IMSLog.checker(profile.number));
                break;
            case 102:
            case 104:
                crLog.append("GET_CB,");
                crLog.append(getCrLogCbType(profile.condition));
                break;
            case 103:
            case 105:
                crLog.append("PUT_CB,");
                crLog.append(getCrLogCbType(profile.condition));
                crLog.append(",");
                crLog.append(getCrLogAction(profile.action));
                crLog.append(",");
                crLog.append(getCrLogSsClass(profile.serviceClass));
                crLog.append(",");
                crLog.append(IMSLog.checker(profile.password));
                break;
            case 106:
                crLog.append("GET_CLIP");
                break;
            case 107:
                crLog.append("PUT_CLIP,");
                crLog.append(profile.enable);
                break;
            case 108:
                crLog.append("GET_CLIR");
                break;
            case 109:
                crLog.append("PUT_CLIR,");
                crLog.append(profile.condition);
                break;
            case 110:
                crLog.append("GET_COLP");
                break;
            case 111:
                crLog.append("PUT_COLP,");
                crLog.append(profile.enable);
                break;
            case 112:
                crLog.append("GET_COLR");
                break;
            case 113:
                crLog.append("PUT_COLR,");
                crLog.append(profile.enable);
                break;
            case 114:
                crLog.append("GET_CW");
                break;
            case 115:
                crLog.append("PUT_CW,");
                crLog.append(getCrLogStatus(profile.enable));
                break;
            case 116:
                crLog.append("GET_SD");
                break;
        }
        return crLog.toString();
    }

    public static String extractLogFromResponse(int requestType, Bundle[] response) {
        int i = requestType;
        Bundle[] bundleArr = response;
        StringBuilder logBuilder = new StringBuilder();
        int i2 = 1;
        if (i == 108) {
            int[] queryClir = bundleArr[0].getIntArray("queryClir");
            logBuilder.append("queryCLIR {");
            logBuilder.append(queryClir[0]);
            logBuilder.append(",");
            logBuilder.append(queryClir[1]);
            logBuilder.append("}");
        } else if (i == 114) {
            logBuilder.append("queryCallWaiting");
            logBuilder.append(" {status: ");
            logBuilder.append(getStringStatus(bundleArr[0].getBoolean("status", false)));
            logBuilder.append("}");
        } else if (i != 115) {
            switch (i) {
                case 100:
                    logBuilder.append("queryCallForward");
                    int i3 = 0;
                    while (i3 < bundleArr.length) {
                        int serviceClass = bundleArr[i3].getInt("serviceClass", i2);
                        int condition = bundleArr[i3].getInt("condition", 0);
                        String cfUri = bundleArr[i3].getString("number");
                        if (TextUtils.isEmpty(cfUri)) {
                            cfUri = "";
                        }
                        logBuilder.append(" {cfType: ");
                        logBuilder.append(getStringCfType(condition));
                        logBuilder.append(",");
                        logBuilder.append(" status: ");
                        logBuilder.append(getStringStatus(bundleArr[i3].getInt("status", 0)));
                        logBuilder.append(",");
                        logBuilder.append(" number: ");
                        logBuilder.append(IMSLog.checker(cfUri));
                        logBuilder.append(",");
                        if (condition == 2) {
                            logBuilder.append(" NoReplyTimer: ");
                            logBuilder.append(bundleArr[i3].getInt(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER, 0));
                            logBuilder.append(",");
                        }
                        logBuilder.append(" serviceClass: ");
                        logBuilder.append(getStringSsClass(serviceClass));
                        logBuilder.append("}");
                        i3++;
                        i2 = 1;
                        int i4 = requestType;
                    }
                    break;
                case 101:
                    logBuilder.append("updateCallForward {Success}");
                    break;
                case 102:
                case 104:
                    logBuilder.append("queryCallBarring");
                    for (int i5 = 0; i5 < bundleArr.length; i5++) {
                        logBuilder.append(" {cbType: ");
                        logBuilder.append(getStringCbType(bundleArr[i5].getInt("condition", 0)));
                        logBuilder.append(",");
                        logBuilder.append(" status: ");
                        logBuilder.append(getStringStatus(bundleArr[i5].getInt("status", 0)));
                        logBuilder.append(",");
                        logBuilder.append(" serviceClass: ");
                        logBuilder.append(getStringSsClass(bundleArr[i5].getInt("serviceClass", 1)));
                        logBuilder.append("}");
                    }
                    break;
                case 103:
                case 105:
                    logBuilder.append("updateCallBarring {Success}");
                    break;
                default:
                    logBuilder.append("requestType[");
                    logBuilder.append(getStringRequestType(requestType));
                    logBuilder.append("] {Success}");
                    break;
            }
        } else {
            logBuilder.append("updateCallWaiting {Success}");
        }
        return logBuilder.toString();
    }

    public static String extractCrLogFromResponse(int requestType, Bundle[] response) {
        StringBuilder crLog = new StringBuilder();
        if (requestType == 100) {
            for (int i = 0; i < response.length; i++) {
                int serviceClass = response[i].getInt("serviceClass", 1);
                int condition = response[i].getInt("condition", 0);
                String cfUri = response[i].getString("number");
                if (TextUtils.isEmpty(cfUri)) {
                    cfUri = "";
                }
                crLog.append(",{");
                crLog.append(getCrLogStatus(response[i].getInt("status", 0)));
                crLog.append(",");
                crLog.append(IMSLog.checker(cfUri));
                crLog.append(",");
                if (condition == 2) {
                    crLog.append(response[i].getInt(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER, 0));
                    crLog.append(",");
                }
                crLog.append(getCrLogSsClass(serviceClass));
                crLog.append("}");
            }
        } else if (requestType == 102 || requestType == 104) {
            for (int i2 = 0; i2 < response.length; i2++) {
                crLog.append(",{");
                crLog.append(getCrLogStatus(response[i2].getInt("status", 0)));
                crLog.append(",");
                crLog.append(getCrLogSsClass(response[i2].getInt("serviceClass", 1)));
                crLog.append("}");
            }
        } else if (requestType == 114) {
            crLog.append(",");
            crLog.append(getCrLogStatus(response[0].getBoolean("status", false)));
        }
        return crLog.toString();
    }

    public static String extractLogFromError(int requestType, Bundle error) {
        StringBuilder logBuilder = new StringBuilder();
        if (requestType == 108) {
            logBuilder.append("queryCLIR");
        } else if (requestType == 114) {
            logBuilder.append("queryCallWaiting");
        } else if (requestType != 115) {
            switch (requestType) {
                case 100:
                    logBuilder.append("queryCallForward");
                    break;
                case 101:
                    logBuilder.append("updateCallForward");
                    break;
                case 102:
                case 104:
                    logBuilder.append("queryCallBarring");
                    break;
                case 103:
                case 105:
                    logBuilder.append("updateCallBarring");
                    break;
            }
        } else {
            logBuilder.append("updateCallWaiting");
        }
        logBuilder.append(" {requestType: ");
        logBuilder.append(getStringRequestType(requestType));
        logBuilder.append(",");
        logBuilder.append(" error: ");
        logBuilder.append(error.getInt("originErrorCode", 0));
        logBuilder.append(",");
        logBuilder.append(" converted error: ");
        logBuilder.append(error.getInt("errorCode", 0));
        logBuilder.append("}");
        return logBuilder.toString();
    }

    private static String getStringStatus(int status) {
        if (status == 0) {
            return "Deactivated";
        }
        return "Activated";
    }

    private static String getCrLogStatus(int status) {
        if (status == 0) {
            return "X";
        }
        return "O";
    }

    private static String getStringStatus(boolean status) {
        if (status) {
            return "Activated";
        }
        return "Deactivated";
    }

    private static String getCrLogStatus(boolean status) {
        if (status) {
            return "O";
        }
        return "X";
    }

    public static String getStringSsClass(int serviceClass) {
        if (serviceClass == 0) {
            return "ALL";
        }
        if (serviceClass == 1) {
            return "AUDIO";
        }
        if (serviceClass == 8) {
            return "SMS";
        }
        if (serviceClass == 16) {
            return "VIDEO";
        }
        if (serviceClass != 255) {
            return "INVALID CLASS";
        }
        return "ALL";
    }

    public static String getCrLogSsClass(int serviceClass) {
        if (serviceClass == 0) {
            return "ALL";
        }
        if (serviceClass == 1) {
            return "A";
        }
        if (serviceClass == 16) {
            return "V";
        }
        if (serviceClass != 255) {
            return "?";
        }
        return "ALL";
    }

    public static String getStringAction(int action) {
        if (action == 0) {
            return "Deactivation";
        }
        if (action == 1) {
            return "Activation";
        }
        if (action == 3) {
            return "Registration";
        }
        if (action != 4) {
            return null;
        }
        return "Erasure";
    }

    public static String getCrLogAction(int action) {
        if (action == 0) {
            return "D";
        }
        if (action == 1) {
            return "A";
        }
        if (action == 3) {
            return "R";
        }
        if (action != 4) {
            return null;
        }
        return "E";
    }

    public static String getStringRequestType(int requestType) {
        switch (requestType) {
            case 100:
                return "SS_GET_CF";
            case 101:
                return "SS_PUT_CF";
            case 102:
                return "SS_GET_ICB";
            case 103:
                return "SS_PUT_ICB";
            case 104:
                return "SS_GET_OCB";
            case 105:
                return "SS_PUT_OCB";
            case 106:
                return "SS_GET_CLIP";
            case 107:
                return "SS_PUT_CLIP";
            case 108:
                return "SS_GET_CLIR";
            case 109:
                return "SS_PUT_CLIR";
            case 110:
                return "SS_GET_COLP";
            case 111:
                return "SS_PUT_COLP";
            case 112:
                return "SS_GET_COLR";
            case 113:
                return "SS_PUT_COLR";
            case 114:
                return "SS_GET_CW";
            case 115:
                return "SS_PUT_CW";
            case 116:
                return "SS_GET_SD";
            case 118:
                return "SS_GET_ACB";
            case 119:
                return "SS_PUT_ACB";
            default:
                return null;
        }
    }

    public static String getStringCfType(int condition) {
        if (condition == 0) {
            return "Unconditional";
        }
        if (condition == 1) {
            return "Busy";
        }
        if (condition == 2) {
            return "Unanswered";
        }
        if (condition != 3) {
            return null;
        }
        return "Not reachable";
    }

    public static String getCrLogCfType(int condition) {
        if (condition == 0) {
            return "U";
        }
        if (condition == 1) {
            return "B";
        }
        if (condition == 2) {
            return "NRy";
        }
        if (condition != 3) {
            return null;
        }
        return "NRc";
    }

    public static String getStringCbType(int condition) {
        if (condition == 1) {
            return "All incoming";
        }
        if (condition == 2) {
            return "All outgoing";
        }
        if (condition == 3) {
            return "Outgoing international";
        }
        if (condition == 4) {
            return "Outgoing international except home";
        }
        if (condition != 5) {
            return null;
        }
        return "Incoming calls when roaming";
    }

    public static String getCrLogCbType(int condition) {
        if (condition == 1) {
            return "AI";
        }
        if (condition == 2) {
            return "AO";
        }
        if (condition == 3) {
            return "OI";
        }
        if (condition == 4) {
            return "OIEXHC";
        }
        if (condition != 5) {
            return null;
        }
        return "ICWR";
    }

    public static String getStringClirMode(int condition) {
        if (condition == 0) {
            return "Default";
        }
        if (condition == 1) {
            return "Invocation";
        }
        if (condition != 2) {
            return null;
        }
        return "Suppression";
    }
}
