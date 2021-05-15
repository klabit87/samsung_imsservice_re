package com.sec.internal.helper.os;

import com.samsung.android.emergencymode.SemEmergencyManager;

public class SystemUtil {
    public static boolean checkUltraPowerSavingMode(SemEmergencyManager emergencyManager) {
        return emergencyManager.checkModeType(512) || emergencyManager.checkModeType(1024);
    }
}
