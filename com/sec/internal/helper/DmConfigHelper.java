package com.sec.internal.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.log.IMSLog;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class DmConfigHelper {
    private static final String LOG_TAG = DmConfigHelper.class.getSimpleName();
    private static Map<String, String> mServiceSwitchDmMap;

    static {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        mServiceSwitchDmMap = concurrentHashMap;
        concurrentHashMap.put("mmtel", ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED);
        mServiceSwitchDmMap.put("mmtel-video", ConfigConstants.ConfigPath.OMADM_LVC_ENABLED);
        mServiceSwitchDmMap.put("presence", ConfigConstants.ConfigPath.OMADM_EAB_SETTING);
    }

    public static Boolean readBool(Context context, String path) {
        return readBool(context, path, false, 0);
    }

    public static Boolean readBool(Context context, String path, Boolean defValue, int phoneId) {
        String boolStr = read(context, path, (String) null, phoneId);
        if (boolStr != null) {
            return Boolean.valueOf("1".equals(boolStr));
        }
        return defValue;
    }

    public static Long readLong(Context context, String path, Long defValue, int phoneId) {
        String longStr = read(context, path, (String) null, phoneId);
        if (TextUtils.isEmpty(longStr)) {
            return defValue;
        }
        try {
            return Long.valueOf(Long.parseLong(longStr));
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static Integer readInt(Context context, String path, Integer defValue) {
        return readInt(context, path, defValue, 0);
    }

    public static Integer readInt(Context context, String path, Integer defValue, int phoneId) {
        String intStr = read(context, path, (String) null, phoneId);
        if (TextUtils.isEmpty(intStr)) {
            return defValue;
        }
        try {
            return Integer.valueOf(Integer.parseInt(intStr));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defValue;
        }
    }

    public static String read(Context context, String path, String defValue, int phoneId) {
        Map<String, String> readData = read(context, path, phoneId);
        if (readData == null) {
            return defValue;
        }
        String ret = readData.get(path.toLowerCase(Locale.US));
        if (TextUtils.isEmpty(ret)) {
            ret = readData.get(("omadm/./3GPP_IMS/" + path).toLowerCase(Locale.US));
        }
        if (!TextUtils.isEmpty(ret)) {
            return ret;
        }
        return defValue;
    }

    public static Map<String, String> read(Context context, String path, int phoneId) {
        Map<String, String> readData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Cursor cursor = context.getContentResolver().query(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/" + path, phoneId), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return readData;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    readData.put(cursor.getString(0), cursor.getString(1));
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return readData;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static boolean readSwitch(Context context, String serviceSwitchName, boolean defValue, int phoneId) {
        String read = null;
        String mappedDm = null;
        if (mServiceSwitchDmMap.containsKey(serviceSwitchName)) {
            mappedDm = mServiceSwitchDmMap.get(serviceSwitchName);
            read = read(context, mappedDm, (String) null, phoneId);
        }
        if (read == null) {
            return defValue;
        }
        String str = LOG_TAG;
        Log.d(str, "readBool(" + serviceSwitchName + ") from " + mappedDm + ": [" + read + "]");
        return "1".equals(read);
    }

    public static void setImsUserSetting(Context context, String name, int value, int phoneId) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put(ImsConstants.Intents.EXTRA_UPDATED_VALUE, Integer.valueOf(value));
        context.getContentResolver().update(UriUtil.buildUri("content://com.sec.ims.settings/imsusersetting", phoneId), values, (String) null, (String[]) null);
    }

    public static int getImsUserSetting(Context context, String projection, int phoneId) {
        ContentValues res = new ContentValues();
        Uri uri = UriUtil.buildUri("content://com.sec.ims.settings/imsusersetting", phoneId);
        Cursor c = context.getContentResolver().query(uri, new String[]{projection}, (String) null, (String[]) null, (String) null);
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    if (c.moveToFirst()) {
                        do {
                            res.put(c.getString(c.getColumnIndexOrThrow("name")), Integer.valueOf(c.getInt(c.getColumnIndexOrThrow(ImsConstants.Intents.EXTRA_UPDATED_VALUE))));
                        } while (c.moveToNext());
                    }
                    if (c != null) {
                        c.close();
                    }
                    Integer ret = -1;
                    if (res.getAsInteger(projection) != null) {
                        ret = res.getAsInteger(projection);
                    }
                    return ret.intValue();
                }
            } catch (IllegalArgumentException e) {
                Log.d(LOG_TAG, "getImsUserSetting: false due to IllegalArgumentException");
            } catch (Throwable th) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        IMSLog.d(LOG_TAG, phoneId, "getImsUserSetting: not found");
        if (c != null) {
            c.close();
        }
        return -1;
    }

    public static void setImsSwitch(Context context, String serviceSwitch, boolean enabled, int phoneId) {
        ContentValues values = new ContentValues();
        values.put("service", serviceSwitch);
        values.put("enabled", Boolean.valueOf(enabled));
        context.getContentResolver().update(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch", phoneId), values, (String) null, (String[]) null);
    }

    public static boolean isImsSwitchEnabled(Context context, String projection, int phoneId) {
        return getImsSwitchValue(context, projection, phoneId) == 1;
    }

    public static int getImsSwitchValue(Context context, String projection, int phoneId) {
        ContentValues cv = getImsSwitchValue(context, new String[]{projection}, phoneId);
        if (cv == null || cv.size() == 0) {
            Log.d(LOG_TAG, "getImsSwitchValue: value is not exist.");
            return 0;
        }
        Integer ret = 0;
        if (cv.getAsInteger(projection) != null) {
            ret = cv.getAsInteger(projection);
        }
        return ret.intValue();
    }

    public static ContentValues getImsSwitchValue(Context context, String[] projection, int phoneId) {
        ContentValues res = new ContentValues();
        Cursor c = context.getContentResolver().query(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch", phoneId), projection, (String) null, (String[]) null, (String) null);
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    if (c.moveToFirst()) {
                        do {
                            res.put(c.getString(c.getColumnIndexOrThrow("name")), Integer.valueOf(c.getInt(c.getColumnIndexOrThrow("enabled"))));
                        } while (c.moveToNext());
                    }
                    if (c != null) {
                        c.close();
                    }
                    return res;
                }
            } catch (IllegalArgumentException e) {
                Log.d(LOG_TAG, "isServiceEnabled: false due to IllegalArgumentException");
            } catch (Throwable th) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        IMSLog.d(LOG_TAG, phoneId, "getImsSwitchValue: not found");
        if (c != null) {
            c.close();
        }
        return res;
    }
}
