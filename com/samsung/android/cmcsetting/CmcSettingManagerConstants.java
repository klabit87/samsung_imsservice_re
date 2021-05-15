package com.samsung.android.cmcsetting;

public class CmcSettingManagerConstants {
    public static final String VERSION = "1.3.3";

    public enum DeviceCategory {
        DEVICE_CATEGORY_PHONE,
        DEVICE_CATEGORY_TABLET,
        DEVICE_CATEGORY_BT_WATCH,
        DEVICE_CATEGORY_SPEAKER,
        DEVICE_CATEGORY_PC,
        DEVICE_CATEGORY_TV,
        DEVICE_CATEGORY_LAPTOP,
        DEVICE_CATEGORY_UNDEFINED
    }

    public enum DeviceType {
        DEVICE_TYPE_PD,
        DEVICE_TYPE_SD,
        DEVICE_TYPE_UNDEFINED
    }

    public enum NetworkMode {
        NETWORK_MODE_USE_MOBILE_NETWORK,
        NETWORK_MODE_WIFI_ONLY,
        NETWORK_MODE_UNDEFINED
    }
}
