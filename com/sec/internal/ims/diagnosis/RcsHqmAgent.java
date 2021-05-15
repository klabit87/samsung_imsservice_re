package com.sec.internal.ims.diagnosis;

import android.content.Context;
import android.os.SemHqmManager;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import java.util.Map;

public class RcsHqmAgent {
    private static final String LOG_TAG = RcsHqmAgent.class.getSimpleName();

    public static String getPLMN(int slot) {
        if (slot < 0) {
            String str = LOG_TAG;
            Log.e(str, "getPLMN : invalid slot " + slot);
            slot = 0;
        }
        String[] numericList = SemSystemProperties.get("gsm.operator.numeric", "00101#").trim().split(",");
        if (numericList.length <= slot) {
            return "00101#";
        }
        return String.format("%-6s", new Object[]{numericList[slot]}).replace(' ', '#');
    }

    public static boolean sendRCSInfoToHQM(Context context, String feature, int slot, Map<String, String> keys) {
        String plmn = getPLMN(slot);
        String omcnw = OmcCode.getNWCode(slot);
        Mno mno = SimUtil.getSimMno(slot);
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        sb.append(DiagnosisConstants.COMMON_KEY_SPEC_REVISION);
        sb.append("\":\"");
        sb.append(16);
        sb.append("\",\"");
        sb.append(DiagnosisConstants.COMMON_KEY_MNO_NAME);
        sb.append("\":\"");
        sb.append(mno);
        sb.append("\",\"");
        sb.append(DiagnosisConstants.COMMON_KEY_PLMN);
        sb.append("\":\"");
        sb.append(plmn);
        sb.append("\",\"");
        sb.append(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE);
        sb.append("\":\"");
        sb.append(omcnw);
        sb.append("\",\"");
        sb.append(DiagnosisConstants.COMMON_KEY_SIM_SLOT);
        sb.append("\":\"");
        sb.append(slot);
        sb.append("\"");
        for (Map.Entry<String, String> key : keys.entrySet()) {
            sb.append(",\"");
            sb.append(key.getKey());
            sb.append("\":\"");
            sb.append(key.getValue());
            sb.append("\"");
        }
        return sendHWParamToHQM(context, feature, sb.toString());
    }

    protected static boolean sendHWParamToHQM(Context context, String feature, String custom_dataset) {
        String str = LOG_TAG;
        Log.d(str, "sendHWParamToHQM: feature - " + feature + ", custom_dataset - " + custom_dataset);
        try {
            if (((SemHqmManager) context.getSystemService("HqmManagerService")).sendHWParamToHQM(0, DiagnosisConstants.COMPONENT_ID, feature, "sm", "0.0", ImsConstants.RCS_AS.SEC, "", custom_dataset, "")) {
                return true;
            }
            Log.e(LOG_TAG, "sendHWParamToHQM: return false.");
            return false;
        } catch (NullPointerException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "sendHWParamToHQM: NullPointerException. " + e);
            return false;
        } catch (ClassCastException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "sendHWParamToHQM: ClassCastException. " + e2);
            return false;
        }
    }
}
