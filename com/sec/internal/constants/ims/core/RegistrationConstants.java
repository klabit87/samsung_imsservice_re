package com.sec.internal.constants.ims.core;

import com.sec.internal.log.IMSLog;

public class RegistrationConstants {
    public static final String IKE_AUTH_ERROR = "IKE_AUTH_ERROR";
    public static final String REASON_AIRPLANE_MODE_ON = "AirplaneModeOn";
    public static final String REASON_IMS_NOT_AVAILABLE = "ImsNotAvailable";
    public static final String REASON_IMS_PDN_REQUEST = "ImsPdnRequst";
    public static final String REASON_INTERNET_PDN_REQUEST = "InternetPdnRequest";
    public static final String REASON_MANUAL_DEREGI = "ManualDeregi";
    public static final String REASON_PLMN_CHANGED = "PlmnChanged";
    public static final String REASON_REGISTERED = "Registered";
    public static final String REASON_REGISTRATION_ERROR = "RegistrationError";
    public static final String REASON_SIM_REFRESH = "SimRefresh";
    public static final String REASON_TIMS_EXPIRED = "TimsExpired";
    public static final String REASON_TIMS_REFRESHING = "TimsRefreshing";
    public static final String REASON_VOPS_CHANGED = "VopsChanged";

    public static final class DeregiReason {
        public static final int NW_INITIATED = 1;
        public static final int PERMANENT_BLOCKED = 33;
        public static final int PERMANENT_FAILURE = 32;
        public static final int TEMP_FAILURE = 16;
        public static final int UE_TRIGGER = 2;
    }

    public static final class DetailedDeregiReason {
        public static final int ACTION_SHUTDOWN = 26;
        public static final int APP_INVOKE = 37;
        public static final int AUTOCONFIG_CHANGED = 32;
        public static final int CALL_STATE_CHANGED = 7;
        public static final int COUNTRY_CHANGED = 50;
        public static final int DCN = 51;
        public static final int DDS_CHANGED = 35;
        public static final int DEFAULT_NW_CHANGED = 12;
        public static final int EPDG_REQUEST = 27;
        public static final int EPDN_DISCONNECTED = 3;
        public static final int EXTERNAL_IMPU_CHANGED = 28;
        public static final int FLIGHT_MODE_ON = 23;
        public static final int FORCE_SMS_PUSH = 9;
        public static final int LOCAL_IP_CHANGED = 5;
        public static final int LOWER_PRIORITY = 46;
        public static final int MANUAL_DEREGI = 22;
        public static final int MANUAL_EMERGENCY_DEREGI = 30;
        public static final int MOBILE_DATA_CHANGED = 34;
        public static final int MOVE_NEXT_PCSCF = 11;
        public static final int MSGAPP_CHANGED = 36;
        public static final int NETWORK_MODE_CHANGED = 31;
        public static final int NW_INTIATED = 1;
        public static final int NW_INTIATED_END = 20;
        public static final int NW_REJECTED = 10;
        public static final int NW_TYPE_CHANGED = 4;
        public static final int OUT_OF_BATTERY = 33;
        public static final int PCSCF_UPDATED = 8;
        public static final int PDN_DISCONNECTED = 2;
        public static final int PERMANENT_BLOCKED = 81;
        public static final int PERMANENT_FAILURE = 71;
        public static final int PERMANENT_FAILURE_END = 80;
        public static final int PHONE_CRASH = 52;
        public static final int PROFILE_UPDATED = 29;
        public static final int PUBLISH_ERROR = 45;
        public static final int REGI_ERROR = 42;
        public static final int REQUEST_PDN_TIMEOUT = 13;
        public static final int RIL_INVOKE = 24;
        public static final int SERVICE_NOT_AVAILABLE = 72;
        public static final int SIM_REFRESH_TIMEOUT = 25;
        public static final int SIP_ERROR = 43;
        public static final int SIP_ERROR_AFTER_HANGUP = 47;
        public static final int SSAC_BARRED = 76;
        public static final int SUBSCRIBE_ERROR = 44;
        public static final int TEMPORARY_FAILURE = 41;
        public static final int TEMPORARY_FAILURE_END = 70;
        public static final int TTY_ENABLED = 75;
        public static final int UNKNOWN = 41;
        public static final int USER_TRIGGER = 21;
        public static final int USER_TRIGGER_END = 40;
        public static final int VOLTE_OFF = 73;
        public static final int VOLTE_ROAMING_DISABLED = 74;
        public static final int VZW_TIMER_EXPIRED = 49;
    }

    public static final class NotAvailableReason {
        public static final int TIMS_EXPIRED = 1;
        public static final int UNKNOWN = 0;
    }

    public static final class RecoveryReason {
        public static final String NO_USER_AGENT = "NoUserAgent";
        public static final String POSTPONED_RECOVERY = "PostponedRecovery";
        public static final String UA_CREATION_FAILED = "UACreateFailed";
        public static final String UA_STATE_MISMATCH = "UaStateMismatch";
    }

    public enum RegisterTaskState {
        IDLE,
        CONFIGURING,
        CONFIGURED,
        CONNECTING,
        CONNECTED,
        RESOLVING,
        RESOLVED,
        REGISTERING,
        REGISTERED,
        DEREGISTERING,
        EMERGENCY
    }

    private RegistrationConstants() {
    }

    public enum RegistrationType {
        IMS_PROFILE_BASED_REG(-1),
        DUAL_REG(0),
        SINGLE_REG(1),
        DUAL_WHEN_ROAMING_REG(2);
        
        private final int mValue;

        private RegistrationType(int value) {
            this.mValue = value;
        }

        public final int getValue() {
            return this.mValue;
        }

        public static RegistrationType valueOf(int i) {
            for (RegistrationType r : values()) {
                if (r.mValue == i) {
                    return r;
                }
            }
            IMSLog.e(RegistrationConstants.class.getSimpleName(), "Invalid RegistrationType: " + i);
            return IMS_PROFILE_BASED_REG;
        }
    }
}
