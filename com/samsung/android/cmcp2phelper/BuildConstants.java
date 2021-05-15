package com.samsung.android.cmcp2phelper;

import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;

public class BuildConstants {
    private static boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private static boolean USER_BINARY = "user".equals(SemSystemProperties.get("ro.build.type", "user"));

    public static boolean isShipBuild() {
        return SHIP_BUILD;
    }

    public static boolean isUserBinary() {
        return USER_BINARY;
    }
}
