package com.sec.internal.ims.entitlement.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class SimSwapNSDSConfigHelper extends NSDSConfigHelper {
    public static final String KEY_NATIVE_MSISDN = "NATIVE_MSISDN";
    private static final String LOG_TAG = SimSwapNSDSConfigHelper.class.getSimpleName();
    private static Map<String, String> sDataMap = new HashMap();

    public static synchronized String getConfigValue(Context context, String key) {
        String str;
        synchronized (SimSwapNSDSConfigHelper.class) {
            if (sDataMap.isEmpty()) {
                Map<String, String> dbDataMap = loadConfigFromDb(context);
                if (!dbDataMap.isEmpty()) {
                    sDataMap.putAll(dbDataMap);
                    addDerivedConfigToMap();
                }
            }
            str = sDataMap.get(key);
        }
        return str;
    }

    public static synchronized void clear() {
        synchronized (SimSwapNSDSConfigHelper.class) {
            sDataMap.clear();
        }
    }

    private static void addDerivedConfigToMap() {
        String entitlmentServerUrl = sDataMap.get(NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER);
        if (entitlmentServerUrl != null && !entitlmentServerUrl.endsWith("generic_devices")) {
            Map<String, String> map = sDataMap;
            map.put(NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER, entitlmentServerUrl + "/generic_devices");
        }
    }

    public static Map<String, String> loadConfigFromDb(Context context) {
        Cursor cursor;
        Map<String, String> dataMap = new HashMap<>();
        try {
            cursor = context.getContentResolver().query(NSDSContractExt.SimSwapNsdsConfigs.CONTENT_URI, new String[]{NSDSContractExt.NsdsConfigColumns.PNAME, NSDSContractExt.NsdsConfigColumns.PVALUE}, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String key = cursor.getString(0);
                    String value = cursor.getString(1);
                    dataMap.put(key, value);
                    String str = LOG_TAG;
                    IMSLog.s(str, "Key:" + key + " Value:" + value);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqe) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "!!!Could not load nsds config from db" + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return dataMap;
        throw th;
    }
}
