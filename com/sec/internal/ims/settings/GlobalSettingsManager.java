package com.sec.internal.ims.settings;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.HashMap;

public class GlobalSettingsManager {
    private static final HashMap<Integer, GlobalSettingsManager> sInstances = new HashMap<>();
    private GlobalSettingsRepo mGlobalSettingsRepo;

    private GlobalSettingsManager(Context context, int phoneId) {
        this.mGlobalSettingsRepo = new GlobalSettingsRepo(context, phoneId);
    }

    public static GlobalSettingsManager getInstance(Context context, int phoneId) {
        synchronized (sInstances) {
            if (sInstances.containsKey(Integer.valueOf(phoneId))) {
                GlobalSettingsManager globalSettingsManager = sInstances.get(Integer.valueOf(phoneId));
                return globalSettingsManager;
            }
            sInstances.put(Integer.valueOf(phoneId), new GlobalSettingsManager(context, phoneId));
            return sInstances.get(Integer.valueOf(phoneId));
        }
    }

    public synchronized GlobalSettingsRepo getGlobalSettings() {
        return this.mGlobalSettingsRepo;
    }

    public boolean getBoolean(String field, boolean defaultVal) {
        boolean z = true;
        Cursor cursor = this.mGlobalSettingsRepo.query(new String[]{field}, (String) null, (String[]) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String value = cursor.getString(0);
                    if (!TextUtils.isEmpty(value)) {
                        if (!CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(value) && !"1".equalsIgnoreCase(value)) {
                            z = false;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return z;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return defaultVal;
        throw th;
    }

    public String getString(String field, String defaultVal) {
        String result = defaultVal;
        Cursor cursor = this.mGlobalSettingsRepo.query(new String[]{field}, (String) null, (String[]) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(0) != null ? cursor.getString(0) : defaultVal;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
        throw th;
    }

    public String[] getStringArray(String field, String[] defaultVal) {
        Cursor cursor = this.mGlobalSettingsRepo.query(new String[]{field}, (String) null, (String[]) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String values = cursor.getString(0);
                    if (!TextUtils.isEmpty(values)) {
                        String[] split = values.replaceAll("\\[|\\]|\"", "").trim().split(",");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return split;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return defaultVal;
        throw th;
    }

    public int getInt(String field, int defaultVal) {
        int result = defaultVal;
        Cursor cursor = this.mGlobalSettingsRepo.query(new String[]{field}, (String) null, (String[]) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && !TextUtils.isEmpty(cursor.getString(0))) {
                    result = Integer.parseInt(cursor.getString(0));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
                return defaultVal;
            } catch (Throwable e2) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th) {
                        e2.addSuppressed(th);
                    }
                }
                throw e2;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }
}
