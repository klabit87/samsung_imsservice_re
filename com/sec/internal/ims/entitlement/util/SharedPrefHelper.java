package com.sec.internal.ims.entitlement.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class SharedPrefHelper {
    private static final String LOG_TAG = SharedPrefHelper.class.getSimpleName();
    private final String mSharedPrefName;

    public SharedPrefHelper(String sharedPrefName) {
        this.mSharedPrefName = sharedPrefName;
    }

    public String get(Context context, String key) {
        return context.getSharedPreferences(this.mSharedPrefName, 0).getString(key, (String) null);
    }

    public long getLong(Context context, String key, long defValue) {
        return context.getSharedPreferences(this.mSharedPrefName, 0).getLong(key, defValue);
    }

    public void save(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(this.mSharedPrefName, 0).edit();
        editor.putString(key, value);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.s(str, "saved preference with key:" + key + " Value:" + value);
    }

    public void save(Context context, String key, long value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(this.mSharedPrefName, 0).edit();
        editor.putLong(key, value);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.s(str, "saved preference with key:" + key + " Value:" + value);
    }

    public void save(Context context, Map<String, String> mp) {
        SharedPreferences.Editor editor = context.getSharedPreferences(this.mSharedPrefName, 0).edit();
        for (Map.Entry<String, String> pair : mp.entrySet()) {
            editor.putString(pair.getKey(), pair.getValue());
        }
        editor.commit();
    }

    public void remove(Context context, String... keys) {
        SharedPreferences.Editor editor = context.getSharedPreferences(this.mSharedPrefName, 0).edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }
}
