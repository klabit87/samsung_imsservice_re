package com.sec.internal.helper.os;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.ims.ImsConstants;
import java.util.List;

public class PackageUtils {
    private static final String ONETALK_API_SERVICE_PACKAGE = "com.samsung.vzwapiservice";

    public static String getMsgAppPkgName(Context context) {
        String packageName = SemFloatingFeature.getInstance().getString(ImsConstants.SecFloatingFeatures.CONFIG_PACKAGE_NAME, "com.android.mms");
        if ("com.android.mms".equals(packageName)) {
            return "com.android.mms";
        }
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return packageName;
        } catch (PackageManager.NameNotFoundException e) {
            return "com.android.mms";
        }
    }

    public static boolean isProcessRunning(Context context, String processName) {
        List<ActivityManager.RunningAppProcessInfo> appList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (appList == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app != null && TextUtils.equals(processName, app.processName)) {
                return true;
            }
        }
        return false;
    }

    public static String getProcessNameById(Context context, int pId) {
        List<ActivityManager.RunningAppProcessInfo> appList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (appList == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app != null && app.pid == pId) {
                return app.processName;
            }
        }
        return "";
    }

    public static boolean isOneTalkFeatureEnabled(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(ONETALK_API_SERVICE_PACKAGE, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
