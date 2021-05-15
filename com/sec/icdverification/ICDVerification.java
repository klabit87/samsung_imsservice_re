package com.sec.icdverification;

import android.os.SystemProperties;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;

public final class ICDVerification {
    private static native int getTamperflag(String str, String str2, String str3);

    static {
        try {
            System.loadLibrary("get.icd.samsung");
        } catch (UnsatisfiedLinkError e) {
            Log.e("ICDVerification", "get_icd load Fail");
        }
    }

    public static final int check() {
        return getTamperflag("multi", SystemProperties.get("ro.product.cpu.abi"), ConfigConstants.VALUE.INFO_COMPLETED);
    }
}
