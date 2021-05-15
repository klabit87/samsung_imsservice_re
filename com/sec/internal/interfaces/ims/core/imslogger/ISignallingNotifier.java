package com.sec.internal.interfaces.ims.core.imslogger;

import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.config.ConfigConstants;

public interface ISignallingNotifier {
    public static final String ACTION_SIP_MESSAGE = "com.sec.imsservice.sip.signalling";
    public static final boolean DEBUG;
    public static final boolean ENG = SemSystemProperties.get("ro.build.type", "user").equals("eng");
    public static final String PERMISSION = "com.sec.imsservice.sip.signalling.READ_PERMISSION";
    public static final boolean SHIPBUILD = Boolean.parseBoolean(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));

    public enum PackageStatus {
        NOT_INSTALLED,
        INSTALLED,
        DM_CONNECTED,
        DM_DISCONNECTED,
        EMERGENCY_MODE
    }

    boolean send(Object obj);

    static {
        boolean z = false;
        if (SemSystemProperties.getInt("ro.debuggable", 0) == 1) {
            z = true;
        }
        DEBUG = z;
    }
}
