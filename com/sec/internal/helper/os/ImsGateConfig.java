package com.sec.internal.helper.os;

import com.samsung.android.feature.SemGateConfig;

public class ImsGateConfig {
    public static boolean isGateEnabled() {
        return SemGateConfig.isGateEnabled();
    }
}
