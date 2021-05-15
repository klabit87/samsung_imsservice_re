package com.sec.internal.constants.ims.entitilement;

public class DeviceEventType {

    public static final class Internal {
        public static final int AKA_TOKEN_RETRIEVAL = 19;
        public static final int BOOTUP_CONFIG_REFRESH = 15;
        public static final int BOOTUP_CONFIG_RETRIEVAL = 14;
        public static final int BOOTUP_DEVICE_INFO_REFRESH = 13;
        public static final int BOOTUP_ENTITLEMENT_CHECK = 1;
        public static final int BOOTUP_OR_APP_INITIATED_SIM_DEVICE_ACTIVATION = 11;
        public static final int DAILY_24_HOUR = 4;
        public static final int DEVICE_CONFIG_FORCE_REFRESH = 18;
        public static final int E911_AID_VALIDATION = 6;
        public static final int E911_PUSH_NOTIFICATION_RECEIVED = 8;
        public static final int LOC_AND_TC_CHECK_AFTER_WEBSHEET = 12;
        public static final int NONE = 0;
        public static final int SVC_PROVISION_CHECK_AFTER_WEBSHEET = 7;
        public static final int SVC_PROVISION_CHECK_RETRY = 10;
        public static final int SVC_PUSH_NOTIFICATION_RECEIVED = 9;
        public static final int USER_INITIATED_TOGGLE_OFF = 3;
        public static final int USER_INITIATED_TOGGLE_ON = 2;
        public static final int USER_INITIATED_UPDATED_E911_ADDRESS = 5;
    }

    public static final class Main {
        public static final int ENTITLEMENT_CHECK_COMPLETED = 3;
        public static final int LOC_AND_TC_UPDATED = 2;
        public static final int LOC_AND_TC_WEBSHEET_COMPLETED = 5;
        public static final int LOC_AND_TC_WEBSHEET_LAUNCHED = 4;
        public static final int SIM_DEVICE_ACTIVATED = 0;
        public static final int SIM_DEVICE_DEACTIVATED = 1;
    }
}
