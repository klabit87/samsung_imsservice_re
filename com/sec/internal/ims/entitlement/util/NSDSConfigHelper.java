package com.sec.internal.ims.entitlement.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.UserManager;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class NSDSConfigHelper {
    public static final String KEY_CONFIG_REFRESH_ON_POWERUP = "configRefreshOnPowerUp";
    public static final String KEY_GCM_EVT_LST_MSG_SENDER_ID = "GCM_Sender_ID_Event_List";
    public static final String KEY_GCM_PUSH_MSG_SENDER_ID = "GCM_Sender_ID";
    public static final String KEY_URL_ENTITLEMENT_SERVER = "entitlement_server_FQDN";
    private static final String LOG_TAG = NSDSConfigHelper.class.getSimpleName();
    private static Map<String, String> sDataMap = new HashMap();

    public static synchronized void clear() {
        synchronized (NSDSConfigHelper.class) {
            sDataMap.clear();
        }
    }

    public static synchronized String getConfigValue(Context context, String imsi, String key) {
        String str;
        synchronized (NSDSConfigHelper.class) {
            if (sDataMap.isEmpty()) {
                Map<String, String> dbDataMap = loadConfigFromDb(context, imsi);
                if (!dbDataMap.isEmpty()) {
                    sDataMap.putAll(dbDataMap);
                    addDerivedConfigToMap();
                }
            }
            str = sDataMap.get(key);
        }
        return str;
    }

    public static synchronized String getConfigValue(Context context, String imsi, String key, String defValue) {
        String retValue;
        synchronized (NSDSConfigHelper.class) {
            retValue = getConfigValue(context, imsi, key);
            if (retValue == null) {
                retValue = defValue;
            }
        }
        return retValue;
    }

    private static void addDerivedConfigToMap() {
        String entitlmentServerUrl = sDataMap.get(KEY_URL_ENTITLEMENT_SERVER);
        if (entitlmentServerUrl != null && !entitlmentServerUrl.endsWith("generic_devices")) {
            Map<String, String> map = sDataMap;
            map.put(KEY_URL_ENTITLEMENT_SERVER, entitlmentServerUrl + "/generic_devices");
        }
    }

    protected static Map<String, String> loadConfigFromDb(Context context, String imsi) {
        Cursor cursor;
        Map<String, String> dataMap = new HashMap<>();
        try {
            cursor = context.getContentResolver().query(NSDSContractExt.NsdsConfigs.CONTENT_URI, new String[]{NSDSContractExt.NsdsConfigColumns.PNAME, NSDSContractExt.NsdsConfigColumns.PVALUE}, "imsi = ?", new String[]{imsi}, (String) null);
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

    public static boolean getConfigRefreshOnPowerUp(Context context, String imsi) {
        return getBooleanValue(getConfigValue(context, imsi, KEY_CONFIG_REFRESH_ON_POWERUP, "0"));
    }

    private static boolean getBooleanValue(String value) {
        try {
            if (!TextUtils.isEmpty(value)) {
                return value.equals("1");
            }
            return false;
        } catch (NumberFormatException nfe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Invalid confifg value:" + value + nfe.getMessage());
            return false;
        }
    }

    public static boolean isUserUnlocked(Context context) {
        if (context == null) {
            IMSLog.s(LOG_TAG, "context is null");
        } else if (((UserManager) context.getSystemService(UserManager.class)).isUserUnlocked()) {
            return true;
        }
        IMSLog.i(LOG_TAG, "User is lock");
        return false;
    }

    public static String getConfigServer(int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.Entitlement.SUPPORT_CONFIGSERVER, "");
    }

    public static boolean isWFCAutoOnEnabled(int phoneId) {
        return ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.Entitlement.WFC_AUTO_ON, false);
    }
}
