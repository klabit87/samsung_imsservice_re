package com.sec.internal.constants.ims;

import android.content.Context;
import android.os.Handler;
import com.sec.internal.constants.ims.ImsConstants;

public class VowifiConfig extends Handler {
    private static final String LOG_TAG = "VowifiConfig";
    public static final int UNKNOWN = -1;
    public static final String WIFI_CALL_ENABLE = "wifi_call_enable";
    public static final String WIFI_CALL_PREFERRED = "wifi_call_preferred";
    public static final String WIFI_CALL_WHEN_ROAMING = "wifi_call_when_roaming";

    public static final class HOME_PREF {
        public static final int CELLULAR = 2;
        public static final int NEVER_USE_CS = 3;
        public static final int WIFI = 1;
    }

    public static final class ROAM_PREF {
        public static final int CELLULAR = 0;
        public static final int WIFI = 1;
    }

    public static final class STATUS {
        public static final int OFF = 0;
        public static final int ON = 1;
    }

    public static boolean isEnabled(Context context, int phoneId) {
        return ImsConstants.SystemSettings.getWiFiCallEnabled(context, 0, phoneId) == 1;
    }

    public static int getPrefMode(Context context, int defaultValue, int phoneId) {
        return ImsConstants.SystemSettings.getWiFiCallPreferred(context, defaultValue, phoneId);
    }

    public static int getPrefMode(Context context, int defaultValue) {
        return getPrefMode(context, defaultValue, ImsConstants.Phone.SLOT_1);
    }

    public static int getRoamPrefMode(Context context, int defaultValue, int phoneId) {
        return ImsConstants.SystemSettings.getWiFiCallWhenRoaming(context, defaultValue, phoneId);
    }

    public static void setEnabled(Context context, int vowifiStatus, int phoneId) {
        ImsConstants.SystemSettings.setWiFiCallEnabled(context, phoneId, vowifiStatus);
    }

    public static void setPrefMode(Context context, int pref, int phoneId) {
        ImsConstants.SystemSettings.setWiFiCallPreferred(context, phoneId, pref);
    }

    public static void setRoamPrefMode(Context context, int pref, int phoneId) {
        ImsConstants.SystemSettings.setWiFiCallWhenRoaming(context, phoneId, pref);
    }
}
