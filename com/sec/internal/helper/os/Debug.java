package com.sec.internal.helper.os;

import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;

public class Debug {
    public static final int DEBUG_LEVEL_HIGH = 3;
    public static final int DEBUG_LEVEL_LOW = 1;
    public static final int DEBUG_LEVEL_MID = 2;
    private static final String DEBUG_LEVEL_PROP = "ro.boot.debug_level";
    private static final String DEBUG_LEVEL_PROP_HIGH = "0x4948";
    private static final String DEBUG_LEVEL_PROP_LOW = "0x4f4c";
    private static final String DEBUG_LEVEL_PROP_MID = "0x494d";
    private static final String DEBUG_LEVEL_SILENT_LOG = "dev.silentlog.on";
    private static final String PRODUCT_SHIP_PROP = "ro.product_ship";
    private static boolean mSilentLogEnabled = false;

    public static boolean isProductShip() {
        return !android.os.Debug.semIsProductDev() || CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(SemSystemProperties.get(PRODUCT_SHIP_PROP, CloudMessageProviderContract.JsonData.TRUE));
    }

    public static int getSystemDebugLevel() {
        String sysDebuglevel = SemSystemProperties.get(DEBUG_LEVEL_PROP, DEBUG_LEVEL_PROP_LOW);
        if (sysDebuglevel.equalsIgnoreCase(DEBUG_LEVEL_PROP_LOW) && !mSilentLogEnabled) {
            return 1;
        }
        if (!sysDebuglevel.equalsIgnoreCase(DEBUG_LEVEL_PROP_MID) && sysDebuglevel.equalsIgnoreCase(DEBUG_LEVEL_PROP_HIGH)) {
            return 3;
        }
        return 2;
    }

    public static void setSilentLogEnabled() {
        mSilentLogEnabled = "On".equals(SemSystemProperties.get(DEBUG_LEVEL_SILENT_LOG, ""));
    }
}
