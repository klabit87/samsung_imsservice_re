package com.sec.internal.ims.diagnosis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class ImsLogAgentUtil {
    private static final String AUTHORITY = "content://com.sec.imsservice.log";
    private static final String LOG_TAG = "ImsLogAgentUtil";
    private static final Map<Integer, ContentValues> sCommonHeader = new HashMap<Integer, ContentValues>() {
        {
            put(0, new ContentValues());
            put(1, new ContentValues());
        }
    };

    public static void updateCommonHeader(Context context, int phoneId, String omcnwCode, String newMnoName, String plmn) {
        Context context2 = context;
        int i = phoneId;
        String str = newMnoName;
        ContentValues commonHeader = new ContentValues();
        commonHeader.put(DiagnosisConstants.COMMON_KEY_SIM_SLOT, Integer.valueOf(phoneId));
        commonHeader.put(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE, omcnwCode);
        commonHeader.put(DiagnosisConstants.COMMON_KEY_MNO_NAME, str);
        String newMccMnc = String.format("%-6s", new Object[]{plmn}).replace(' ', '#');
        commonHeader.put(DiagnosisConstants.COMMON_KEY_PLMN, newMccMnc);
        if (context2 != null && sCommonHeader.get(Integer.valueOf(phoneId)) != null) {
            synchronized (sCommonHeader) {
                SharedPreferences prevCommonHeader = ImsSharedPrefHelper.getSharedPref(i, context2, ImsSharedPrefHelper.PRE_COMMON_HEADER, 0, false);
                String oldMccMnc = prevCommonHeader.getString(DiagnosisConstants.COMMON_KEY_PLMN, "");
                String oldMnoName = prevCommonHeader.getString(DiagnosisConstants.COMMON_KEY_MNO_NAME, "");
                int oldSREV = prevCommonHeader.getInt(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 1);
                IMSLog.d(LOG_TAG, i, "setCommonHeader: oldMccMnc [" + oldMccMnc + "], oldMnoName [" + oldMnoName + "], oldSREV [" + oldSREV + "] ==> newMccMnc [" + newMccMnc + "], newMnoName [" + str + "], newSREV [" + 16 + "]");
                if (!TextUtils.equals(oldMccMnc, newMccMnc) || !TextUtils.equals(oldMnoName, str) || 16 != oldSREV) {
                    if (sCommonHeader.get(Integer.valueOf(phoneId)).size() == 0) {
                        ImsSharedPrefHelper.getSharedPref(i, context2, "DRPT", 0, false).edit().clear().apply();
                        ImsSharedPrefHelper.getSharedPref(i, context2, DiagnosisConstants.FEATURE_DRCS, 0, false).edit().clear().apply();
                        IMSLog.i(LOG_TAG, i, "Discard stored DRPT/DRCS due to change of common header");
                    } else {
                        IMSLog.i(LOG_TAG, i, "SIM howswap; DRPT/DRCS might be sent already when ABSENT");
                    }
                }
                commonHeader.put(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 16);
                sCommonHeader.get(Integer.valueOf(phoneId)).putAll(commonHeader);
                SharedPreferences.Editor editor = prevCommonHeader.edit();
                for (String key : commonHeader.keySet()) {
                    Object obj = commonHeader.get(key);
                    if (obj == null) {
                        int oldSREV2 = oldSREV;
                        Log.e(LOG_TAG, "setCommonHeader: [" + key + "] is null!");
                        Context context3 = context;
                        oldSREV = oldSREV2;
                    } else {
                        int oldSREV3 = oldSREV;
                        if ((obj instanceof Integer) != 0) {
                            editor.putInt(key, ((Integer) obj).intValue());
                        } else if (obj instanceof String) {
                            editor.putString(key, (String) obj);
                        } else if (obj instanceof Long) {
                            editor.putLong(key, ((Long) obj).longValue());
                        } else {
                            Log.e(LOG_TAG, "setCommonHeader: [" + key + "] has wrong data type!");
                        }
                        Context context4 = context;
                        oldSREV = oldSREV3;
                    }
                }
                editor.apply();
            }
        }
    }

    public static ContentValues getCommonHeader(Context context, int phoneId) {
        ContentValues cv = sCommonHeader.get(Integer.valueOf(phoneId));
        if (CollectionUtils.isNullOrEmpty(cv)) {
            return new ContentValues();
        }
        for (Map.Entry<String, ?> entry : ImsSharedPrefHelper.getSharedPref(phoneId, context, ImsSharedPrefHelper.PRE_COMMON_HEADER, 0, false).getAll().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                cv.put(key, Integer.valueOf(((Integer) value).intValue()));
            } else if (value instanceof String) {
                cv.put(key, (String) value);
            } else if (value instanceof Long) {
                cv.put(key, Long.valueOf(((Long) value).longValue()));
            }
        }
        if (!cv.containsKey(DiagnosisConstants.COMMON_KEY_SPEC_REVISION)) {
            cv.put(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 16);
        }
        if (!cv.containsKey(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE)) {
            cv.put(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE, OmcCode.getNWCode(phoneId));
        }
        if (!cv.containsKey(DiagnosisConstants.COMMON_KEY_SIM_SLOT)) {
            cv.put(DiagnosisConstants.COMMON_KEY_SIM_SLOT, Integer.valueOf(phoneId));
        }
        return cv;
    }

    private static void passEventLog(Context ctx, String exception) {
        try {
            ctx.getContentResolver().call(Uri.parse(AUTHORITY), DiagnosisConstants.CALL_METHOD_LOGANDADD, exception, (Bundle) null);
        } catch (Exception e) {
            Log.e(LOG_TAG, "passEventLog: exception occurred: " + e);
        }
    }

    public static void sendLogToAgent(int phoneId, Context ctx, String feature, ContentValues log) {
        Integer sendMode = log.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        if (sendMode == null || sendMode.intValue() != 0) {
            log.put(DiagnosisConstants.KEY_SEND_MODE, 0);
        }
        log.put(DiagnosisConstants.KEY_FEATURE, feature);
        try {
            ctx.getContentResolver().insert(UriUtil.buildUri(AUTHORITY, phoneId), log);
        } catch (Exception e) {
            passEventLog(ctx, e.toString());
        }
    }

    public static void storeLogToAgent(int phoneId, Context ctx, String feature, ContentValues log) {
        Integer sendMode = log.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        if (sendMode == null || sendMode.intValue() == 0) {
            log.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        }
        log.put(DiagnosisConstants.KEY_FEATURE, feature);
        try {
            ctx.getContentResolver().insert(UriUtil.buildUri(AUTHORITY, phoneId), log);
        } catch (Exception e) {
            passEventLog(ctx, e.toString());
        }
    }

    public static void requestToSendStoredLog(int phoneId, Context ctx, String feature) {
        try {
            ContentResolver contentResolver = ctx.getContentResolver();
            contentResolver.update(UriUtil.buildUri("content://com.sec.imsservice.log/send/" + feature, phoneId), (ContentValues) null, (String) null, (String[]) null);
        } catch (Exception e) {
            passEventLog(ctx, e.toString());
        }
    }
}
