package com.sec.internal.constants.ims.entitilement;

public class EntitlementNamespaces {

    public static final class EntitlementActions {
        public static final String ACTION_REFRESH_DEVICE_CONFIG = "com.sec.vsim.ericssonnsds.REFRESH_DEVICE_CONFIG";
        public static final String ACTION_RETRY_DEVICE_CONFIG = "com.sec.vsim.ericssonnsds.RETRY_DEVICE_CONFIG";
    }

    public static final class EntitlementEvents {
        public static final int EVENT_AKA_TOKEN_RETRIEVAL = 200;
        public static final int EVENT_FORCE_CONFIG_UPDATE = 108;
        public static final int EVENT_INITIALIZE = 106;
        public static final int EVENT_REFRESH_DEVICE_CONFIG = 107;
        public static final int EVENT_UPDATE_ENTITLEMENT_URL = 201;
    }
}
