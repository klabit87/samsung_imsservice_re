package com.sec.internal.log;

import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;

public class AECLog {
    private static final String LOG_LEVEL_PROP_LOW = "0x4f4c";
    private static final String LOG_TAG = AECLog.class.getSimpleName();
    private static final boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private static final String sysLoglevel = SemSystemProperties.get("ro.boot.debug_level", LOG_LEVEL_PROP_LOW);

    public static void d(String tag, String msg) {
        String str = LOG_TAG;
        Log.d(str, tag + ": " + msg);
    }

    public static void d(String tag, String msg, int phoneId) {
        String str = LOG_TAG;
        Log.d(str, tag + "<" + phoneId + ">: " + msg);
    }

    public static void e(String tag, String msg) {
        String str = LOG_TAG;
        Log.e(str, tag + ": " + msg);
    }

    public static void e(String tag, String msg, int phoneId) {
        String str = LOG_TAG;
        Log.e(str, tag + "<" + phoneId + ">: " + msg);
    }

    public static void i(String tag, String msg) {
        String str = LOG_TAG;
        Log.i(str, tag + ": " + msg);
    }

    public static void i(String tag, String msg, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, tag + "<" + phoneId + ">: " + msg);
    }

    public static void s(String tag, String msg) {
        if (!SHIP_BUILD && !sysLoglevel.equalsIgnoreCase(LOG_LEVEL_PROP_LOW)) {
            d(tag, msg);
        }
    }

    public static void s(String tag, String msg, int phoneId) {
        if (!SHIP_BUILD && !sysLoglevel.equalsIgnoreCase(LOG_LEVEL_PROP_LOW)) {
            d(tag, msg, phoneId);
        }
    }
}
