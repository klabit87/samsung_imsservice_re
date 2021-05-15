package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.SharedPreferences;
import com.sec.internal.ims.rcs.util.RcsUtils;

public class ImUserPreference {
    private static final String FT_AUTO_ACCEPT_SIM1 = "FT_AUTO_ACCEPT";
    private static final String FT_AUTO_ACCEPT_SIM2 = "FT_AUTO_ACCEPT_SIM2";
    private static final String SHARED_PREFS_NAME = "im_user_prefs";
    private static final String USER_ALIAS = "USER_ALIAS";
    private static ImUserPreference sInstance;

    private ImUserPreference() {
    }

    public static ImUserPreference getInstance() {
        if (sInstance == null) {
            sInstance = new ImUserPreference();
        }
        return sInstance;
    }

    public void setFtAutAccept(Context context, int phoneId, int accept) {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            persist(context, getFtAutAcceptPrefByPhoneId(phoneId), accept);
        } else {
            persist(context, getFtAutAcceptPrefByPhoneId(0), accept);
        }
    }

    public int getFtAutAccept(Context context) {
        return getInt(context, getFtAutAcceptPrefByPhoneId(0), -1);
    }

    public int getFtAutAccept(Context context, int phoneId) {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            return getInt(context, getFtAutAcceptPrefByPhoneId(phoneId), -1);
        }
        return getInt(context, getFtAutAcceptPrefByPhoneId(0), -1);
    }

    public void setUserAlias(Context context, String userAlias) {
        persist(context, USER_ALIAS, userAlias);
    }

    public String getUserAlias(Context context) {
        return getString(context, USER_ALIAS, "");
    }

    private void persist(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private void persist(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private int getInt(Context context, String key, int def) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, 0).getInt(key, def);
    }

    private String getString(Context context, String key, String def) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, 0).getString(key, def);
    }

    private String getFtAutAcceptPrefByPhoneId(int phoneId) {
        return phoneId == 1 ? FT_AUTO_ACCEPT_SIM2 : FT_AUTO_ACCEPT_SIM1;
    }
}
