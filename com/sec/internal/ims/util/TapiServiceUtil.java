package com.sec.internal.ims.util;

import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;

public class TapiServiceUtil {
    public static boolean isSupportTapi() {
        int phoneCount = SimUtil.getPhoneCount();
        boolean rcsEnabled = false;
        for (int phoneId = 0; phoneId < phoneCount; phoneId++) {
            boolean z = true;
            if (DmConfigHelper.getImsSwitchValue(ImsRegistry.getContext(), DeviceConfigManager.RCS, phoneId) != 1) {
                z = false;
            }
            rcsEnabled |= z;
        }
        return rcsEnabled;
    }
}
