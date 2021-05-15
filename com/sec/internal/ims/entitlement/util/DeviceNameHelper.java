package com.sec.internal.ims.entitlement.util;

import android.content.Context;
import android.provider.Settings;
import com.sec.ims.extensions.Extensions;

public class DeviceNameHelper {
    public static String getDeviceName(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Extensions.Settings.Global.DEVICE_NAME);
    }
}
