package com.sec.internal.constants.ims.entitilement;

public class NSDSNamespaces {

    public static final class AkaAuthResultType {
        public static final String AKA_IN_PROGRESS = "InProgress";
        public static final String AKA_NOT_SUPPORTED = "NotSupported";
    }

    public static final class NSDSActions {
        public static final String ACTION_CONFIRM_PUSH_MSG_DELIVERY = "com.samsung.nsds.action.ACTION_CONFIRM_PUSH_MSG_DELIVERY";
        public static final String ACTION_REFRESH_DEVICE_CONFIG = "com.samsung.nsds.action.REFRESH_DEVICE_CONFIG";
        public static final String ACTION_REFRESH_GCM_TOKEN = "com.samsung.nsds.action.ACTION_REFRESH_GCM_TOKEN";
        public static final String ACTION_SIM_DEVICE_ACTIVATION = "com.samsung.nsds.action.ACTION_SIM_DEVICE_ACTIVATION";
        public static final String DEVICE_CONFIG_UPDATED = "com.samsung.nsds.action.DEVICE_CONFIG_UPDATED";
        public static final String DEVICE_PUSH_TOKEN_READY = "com.samsung.nsds.action.DEVICE_PUSH_TOKEN_READY";
        public static final String DEVICE_READY_AFTER_BOOTUP = "com.sec.internal.ims.entitlement.DEVICE_READY_AFTER_BOOTUP";
        public static final String E911_AID_INFO_RECEIVED = "com.samsung.nsds.action.E911_AID_INFO_RECEIVED";
        public static final String ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED = "com.samsung.nsds.action.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED";
        public static final String ENTITLEMENT_CHECK_COMPLETED = "com.samsung.nsds.action.ENTITLEMENT_CHECK_COMPLETED";
        public static final String IS_PRIMARY_ACTIVATED = "com.samsung.nsds.action.IS_PRIMARY_ACTIVATED";
        public static final String LINES_READY_STATUS_UPDATED = "com.samsung.nsds.action.LINES_READY_STATUS_UPDATED";
        public static final String RECEIVED_GCM_EVENT_NOTIFICATION = "com.samsung.nsds.action.RECEIVED_GCM_EVENT_NOTIFICATION";
        public static final String RECEIVED_PUSH_NOTIFICATION = "com.samsung.nsds.action.RECEIVED_PUSH_NOTIFICATION";
        public static final String SIM_DEVICE_ACTIVATED = "com.samsung.nsds.action.SIM_DEVICE_ACTIVATED";
        public static final String SIM_DEVICE_DEACTIVATED = "com.samsung.nsds.action.SIM_DEVICE_DEACTIVATED";
        public static final String SIM_SWAP_COMPLETED = "com.samsung.nsds.action.SIM_SWAP_COMPLETED";
        public static final String SNT_MODE_LOCATIONANDTC_OPEN_WEBSHEET = "com.sec.vsim.ericssonnsds.action.SNT_MODE_LOCATIONANDTC_OPEN_WEBSHEET";
        public static final String UNIFIED_WFC_LOCATIONANDTC_OPEN_WEBSHEET = "com.sec.vsim.ericssonnsds.action.UNIFIED_WFC_LOCATIONANDTC_OPEN_WEBSHEET";
    }

    public static final class NSDSApiVersion {
        public static final String MDSP30 = "3.0";
        public static final String NSDS20 = "1.0";
    }

    public static final class NSDSBaseOperations {
        public static final int BULK_ENTITLEMENT_CHECK = 2;
        public static final int CHECK_LOC_AND_TC = 3;
        public static final int CHECK_LOC_AND_TC_AUTO_ON = 16;
        public static final int FAIL_ENTITLEMENT_AUTO_ON = 20;
        public static final int FORCE_CONFIG_UPDATE = 14;
        public static final int OPEN_E911_ADDRESS_UPDATE_WEBSHEET = 13;
        public static final int OPEN_LOC_AND_TC_WEBSHEET = 8;
        public static final int REFRESH_DEVICE_INFO = 9;
        public static final int REGISTER_PUSH_TOKEN = 4;
        public static final int REMOVE_PUSH_TOKEN = 5;
        public static final int REMOVE_PUSH_TOKEN_AUTO_ON = 18;
        public static final int REMOVE_PUSH_TOKEN_AUTO_ON_RETRY = 19;
        public static final int RETRIEVE_AKA_TOKEN = 15;
        public static final int RETRIEVE_DEVICE_CONFIG = 10;
        public static final int RETRY_ENTITLEMENT_AUTO_ON = 17;
        public static final int SIM_DEVICE_ACTIVATION = 1;
        public static final int UPDATE_DEVICE_CONFIG = 11;
    }

    public static final class NSDSDataMapKey {
        public static final String E911_AID_EXP = "e911_aid_exp";
        public static final String HTTP_RESP_CODE = "http_resp_code";
        public static final String HTTP_RESP_REASON = "http_resp_reason";
        public static final String LOC_AND_TC_SERVER_DATA = "loc_and_tc_server_data";
        public static final String LOC_AND_TC_SERVER_URL = "loc_and_tc_server_url";
        public static final String LOC_AND_TC_STATUS = "loc_and_tc_status";
        public static final String SVC_PROV_STATUS = "svc_prov_status";
    }

    public static final class NSDSDeactivationCause {
        public static final int INVALID_FINGERPRINT = 1;
        public static final int NONE = 0;
    }

    public static final class NSDSDefinedResponseCode {
        public static final int AKA_AUTH_FAILED = 1006;
        public static final int FORCE_TOGGLE_OFF_ERROR_CODE = 2303;
        public static final int HTTP_TRANSACTION_ERROR_CODE = 2600;
        public static final int INVALID_SIM_STATUS = 2401;
        public static final int LOCATIONANDTC_UPDATE_CANCEL_CODE = 2500;
        public static final int LOCATIONANDTC_UPDATE_NOT_REQUIRED = 2502;
        public static final int LOCATIONANDTC_UPDATE_SUCCESS_CODE = 2501;
        public static final int MANAGE_CONNECTIVITY_ACTIVATE_GEN_FAILURE = 1300;
        public static final int MANAGE_CONNECTIVITY_ACTIVATE_INVALID_DEVICE_GROUP = 1301;
        public static final int MANAGE_CONNECTIVITY_CONFIGURE_GEN_FAILURE = 1400;
        public static final int MANAGE_CONNECTIVITY_DEACTIVATION_SUCCESS_FOR_INVALID_FINGERPRINT = 1305;
        public static final int MANAGE_CONNECTIVITY_NEW_CONFIG_UPDATED = 1302;
        public static final int MANAGE_LOCATION_AND_TC_GEN_FAILURE = 1800;
        public static final int MANAGE_PUSH_TOKEN_GEN_FAILURE = 1900;
        public static final int MANAGE_SERVICE_PROVISION_GEN_FAILURE = 1500;
        public static final int MANAGE_SERVICE_PROVISION_INVALID_OWNER_ID = 1501;
        public static final int MANAGE_SERVICE_PROVISION_MAX_SVC_INST_REACHED = 1502;
        public static final int MANAGE_SERVICE_PROVISION_SVC_NOT_ENTITLED = 1503;
        public static final int MANAGE_SERVICE_REMOVE_GEN_FAILURE = 1700;
        public static final int MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS = 1701;
        public static final int MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID = 1702;
        public static final int MANAGE_SERVICE_RENEW_GEN_FAILURE = 1600;
        public static final int REGISTERED_DEVICES_GEN_FAILURE = 2000;
        public static final int REGISTERED_MSISDN_GEN_FAILURE = 1100;
        public static final int REQUEST_3GPP_AUTH_GEN_FAILURE = 1001;
        public static final int REQUEST_3GPP_AUTH_SERVER_ERROR = 1002;
        public static final int SVC_NOT_PROVISIONED_ERROR_CODE = 2301;
        public static final int SVC_PROVISION_COMPLETED_SUCCESS_CODE = 2302;
        public static final int SVC_PROVISION_PENDING_ERROR_CODE = 2300;
        public static final int UNKNOWN = -1;
        public static final int VOID_WEBSHEET_TRANSACTION = 2304;
    }

    public static final class NSDSDeviceState {
        public static final String ACTIVATED = "activated";
        public static final String ACTIVATION_IN_PROGRESS = "activation_in_progress";
        public static final String DEACTIVATED = "deactivated";
        public static final String DEVICECONFIG_IN_PROGRESS = "deviceconfig_in_progress";
        public static final String ENTITLMENT_IN_PROGRESS = "entitlement_in_progress";
        public static final String SERVICE_PROVISIONED = "service_provisioned";
    }

    public static final class NSDSEvents {
        public static final int EVT_ACTIVATE_SIM_DEVICE = 3;
        public static final int EVT_AUTHORIZE_MSISDN = 209;
        public static final int EVT_BIND_SERVICE = 5;
        public static final int EVT_BOOT_UP_INIT = 20;
        public static final int EVT_CANCEL_MSISDN_AUTH = 11;
        public static final int EVT_CANCEL_MSISDN_AUTHORIZATION = 211;
        public static final int EVT_DEACTIVATE_SIM_DEVICE = 4;
        public static final int EVT_ENABLE_OR_DISABLE_SERVICE = 42;
        public static final int EVT_ENTITLMENT_CHECK = 15;
        public static final int EVT_FLIGHT_MODE_CHANGED = 18;
        public static final int EVT_IMS_DEREG = 1;
        public static final int EVT_IMS_REG = 0;
        public static final int EVT_PARSE_CONFIG = 2;
        public static final int EVT_PDN_CONNECTED = 22;
        public static final int EVT_PDN_CONNECTION_FAIL = 24;
        public static final int EVT_PERFORM_BOOTUP_PRCEDURES = 45;
        public static final int EVT_PERFORM_PROC_ON_CONFIG_REFRESH_COMPLETE = 41;
        public static final int EVT_REFRESH_DEVICE_CONFIG = 14;
        public static final int EVT_REFRESH_DEVICE_INFO = 13;
        public static final int EVT_REFRESH_ENTITLEMENT_AND_911_AID = 44;
        public static final int EVT_REFRESH_ENTITLEMENT_AND_911_AID_AUTO_ON = 51;
        public static final int EVT_REGISTER_EVENT_MESSENGER = 223;
        public static final int EVT_REMOVE_PUSH_TOKEN = 17;
        public static final int EVT_RETRIEVE_AKA_TOKEN = 49;
        public static final int EVT_SCHEDULE_GCM_REG_TOKEN = 43;
        public static final int EVT_SIM_SWAP = 40;
        public static final int EVT_START_PDN_CONNECTIVITY = 46;
        public static final int EVT_STOP_PDN_CONNECTIVITY = 50;
        public static final int EVT_UNREGISTER_EVENT_MESSENGER = 224;
        public static final int EVT_UPDATE_DEVICE_NAME = 6;
        public static final int EVT_UPDATE_E911_ADDRESS = 19;
        public static final int EVT_UPDATE_ENTITLEMENT_URL = 212;
        public static final int EVT_UPDATE_PUSH_TOKEN_IN_SES = 21;
        public static final int EVT_VOWIFI_TOGGLE_OFF = 221;
        public static final int EVT_VOWIFI_TOGGLE_ON = 220;
    }

    public static final class NSDSExtras {
        public static final String CONFIRMATION_URL = "confirmation_url";
        public static final String DATE = "date";
        public static final String DEVICE_EVENT_TYPE = "device_event_type";
        public static final String DEVICE_ID = "device_id";
        public static final String DEVICE_NAME = "device_name";
        public static final String DEVICE_PUSH_TOKEN = "device_push_token";
        public static final String E911_AID = "e911_aid";
        public static final String E911_AID_EXPIRATION = "e911_aid_expiration";
        public static final String ERROR_CODES = "error_codes";
        public static final String EVENT_LIST = "event_list";
        public static final String FORCED_CONFIG_UPDATE = "forced_config_udpate";
        public static final String FROM_REG_STATUS = "from_reg_status";
        public static final String GCM_PROTOCOL_TO_SERVER = "gcm_protocol_to_server";
        public static final String GCM_SENDER_ID = "gcm_sender_id";
        public static final String IMSI = "imsi";
        public static final String IS_PRIMARY_DEVICE = "is_primary_device";
        public static final String LOCATIONANDTC_CLIENT_NAME = "com.sec.vsim.ericssonnsds.LOCATIONANDTC_CLIENT_NAME";
        public static final String LOCATIONANDTC_DATA = "com.sec.vsim.ericssonnsds.LOCATIONANDTC_DATA";
        public static final String LOCATIONANDTC_RESULT_CODE = "com.sec.vsim.ericssonnsds.LOCATIONANDTC_RESULT_CODE";
        public static final String LOCATIONANDTC_TITLE = "com.sec.vsim.ericssonnsds.LOCATIONANDTC_TITLE";
        public static final String LOCATIONANDTC_URL = "com.sec.vsim.ericssonnsds.LOCATIONANDTC_URL";
        public static final String LOCATION_AND_TC_MESSENGER = "com.sec.vsim.ericssonnsds.LOCATION_AND_TC_MESSENGER";
        public static final String LOC_AND_TC_WEBSHEET_RESULT_MESSAGE = "com.sec.vsim.ericssonnsds.LOC_AND_TC_WEBSHEET_RESULT_MESSAGE";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String MSISDN = "msisdn";
        public static final String MSISDN_LIST = "msisdn_list";
        public static final String NOTIFCATION_CONTENT = "notification_content";
        public static final String NOTIFCATION_TITLE = "notification_title";
        public static final String ORIG_ERROR_CODE = "orig_error_code";
        public static final String ORIG_PUSH_MESSAGE = "orig_push_message";
        public static final String PNS_SUBTYPE = "pns_subtype";
        public static final String PNS_TYPE = "pns_type";
        public static final String POLL_INTERVAL = "poll_interval";
        public static final String REQUEST_STATUS = "request_status";
        public static final String REQ_TOGGLE_OFF_OP = "req_toggle_off_op";
        public static final String RETRY_COUNT = "retry_count";
        public static final String SERVICE_VOLTE = "service_volte";
        public static final String SERVICE_VOWIFI = "service_vowifi";
        public static final String SIM_ABSENT = "com.sec.vsim.ericssonnsds.SIM_ABSENT";
        public static final String SIM_DEACTIVATION_CAUSE = "sim_deactivation_cause";
        public static final String SIM_SLOT_IDX = "com.sec.vsim.ericssonnsds.SIM_SLOT_IDX";
        public static final String SIM_SWAPPED = "com.sec.vsim.ericssonnsds.SIM_SWAPPED";
        public static final String TO_REG_STATUS = "to_reg_status";
    }

    public static final class NSDSGcmEventType {
        public static final String E911_ADDR_UPDATE = "E911_ADDR_UPDATE";
        public static final String ENTMT_UPDATE = "ENTMT_UPDATE";
    }

    public static final class NSDSHttpResponseCode {
        public static final int BUSY_HERE = 486;
        public static final int REQUEST_TIMEOUT = 408;
        public static final int SERVER_INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int TEMPORARILY_UNAVAILABLE = 480;
    }

    public static final class NSDSLocAndTcWebSheetCallback {
        public static final int LOCATIONANDTC_RESULT_CANCEL = 2;
        public static final int LOCATIONANDTC_RESULT_PENDING = 0;
        public static final int LOCATIONANDTC_RESULT_SUCCESS = 1;
        public static final int LOCATIONANDTC_RESULT_VOID = 3;
    }

    public static final class NSDSManageConnectivityOperation {
        public static final int ACTIVATE = 0;
        public static final int CONFIGURE = 3;
        public static final int DELETE = 2;
        public static final int RENEW = 1;
    }

    public static final class NSDSManagePushTokenOperation {
        public static final int REGISTER = 0;
        public static final int REMOVE = 1;
    }

    public static final class NSDSManageServiceOperation {
        public static final int ASSIGN_NAME = 7;
        public static final int INSTANCE_TOKEN = 5;
        public static final int PROVISION = 0;
        public static final int REMOVE = 2;
        public static final int RENEW = 1;
    }

    public static final class NSDSMethodNamespace {
        public static final String GET_MSISDN = "getMSISDN";
        public static final String GET_TOKEN = "getToken";
        public static final String MANAGE_CONNECTIVITY = "manageConnectivity";
        public static final String MANAGE_LOC_AND_TC = "manageLocationAndTC";
        public static final String MANAGE_PUSH_TOKEN = "managePushToken";
        public static final String MANAGE_SERVICE = "manageService";
        public static final String REGISTERED_DEVICES = "registeredDevices";
        public static final String REGISTERED_MSISDN = "registeredMSISDN";
        public static final String REQ_3GPP_AUTH = "3gppAuthentication";
        public static final String SERVICE_ENTITLEMENT_STATUS = "serviceEntitlementStatus";
    }

    public static final class NSDSMigration {
        public static final String DEFAULT_KEY = "000";
        public static final String MIGRATED = "migrated";
        public static final String MIGRATING = "migrating";
    }

    public static final class NSDSOSType {
        public static final int ANDROID = 0;
    }

    public static final class NSDSRegisteredMSISDNOperation {
        public static final int AVAILABLE = 0;
    }

    public static final class NSDSRequestServices {
        public static final String REQ_SERVICE_VOLTE = "VoLTE";
        public static final String REQ_SERVICE_VOWIFI = "VoWiFi";
    }

    public static final class NSDSResponseCode {
        public static final int ERROR_CERTIFICATE_GENERATION_FAILURE = 1022;
        public static final int ERROR_CREATION_FAILURE = 1061;
        public static final int ERROR_INVALID_CSR = 1025;
        public static final int ERROR_INVALID_DEVICE_GROUP = 1054;
        public static final int ERROR_INVALID_DEVICE_STATUS = 1029;
        public static final int ERROR_INVALID_FINGERPRINT = 1041;
        public static final int ERROR_INVALID_OWNERID = 1024;
        public static final int ERROR_INVALID_REQUEST = 1004;
        public static final int ERROR_INVALID_SERVICE_INSTANCEID = 1053;
        public static final int ERROR_INVALID_SERVICE_NAME = 1046;
        public static final int ERROR_MAX_SERVICE_INSTANCE_REACHED = 1044;
        public static final int ERROR_MAX_SERVICE_REACHED = 1040;
        public static final int ERROR_SERVER_ERROR = 1111;
        public static final int ERROR_SERVER_SUSPENDED = 1063;
        public static final int ERROR_SERVICE_NOT_ENTITLED = 1048;
        public static final int REQUEST_AKA_CHALLENGE = 1003;
        public static final int REQUEST_SUCCESSFUL = 1000;
    }

    public static final class NSDSResponseMessage {
        public static final int RESPONSE_ACTIVATE_SIM_DEVICE = 103;
        public static final int RESPONSE_CHECK_LOC_AND_TC = 104;
        public static final int RESPONSE_DEACTIVATE_DEVICE = 111;
        public static final int RESPONSE_ENTITLMENT_CHECK = 101;
        public static final int RESPONSE_REGISTER_PUSH_TOKEN = 112;
        public static final int RESPONSE_REMOVE_PUSH_TOKEN = 113;
        public static final int RESPONSE_RETRIEVE_AKA_TOKEN = 118;
        public static final int RESPONSE_RETRIEVE_DEVICE_CONFIG = 102;
        public static final int RESPONSE_UPDATE_DEVICE_CONFIG = 109;
    }

    public static final class NSDSSIMDeviceType {
        public static final int HANDHELD = 0;
    }

    public static final class NSDSServices {
        public static final String SERVICE_CONNECTIVITY_MANAGER = "conn-mgr";
        public static final String SERVICE_VOLTE = "volte";
        public static final String SERVICE_VOWIFI = "vowifi";
        public static final String SERVICE_VOWIFI_AND_VVM = "vowifi+vvm";
    }

    public static final class NSDSServicesType {
        public static final int SERVICE_VOLTE = 2;
        public static final int SERVICE_VOWIFI = 1;
    }

    public static final class NSDSSettings {
        public static final String CHANNEL_NAME_DISH = "BOOST";
        public static final String CHANNEL_NAME_SE_DEVICE = "Nonstock";
        public static final String CHANNEL_NAME_TMK = "METRO";
        public static final String CHANNEL_NAME_TMO = "TMO";
        public static final String DEVICE_GROUP = "Samsung-NSDS";
        public static final String DEVICE_GROUP_20 = "Samsung-NSDS-CM-2.0";
        public static final boolean IS_SIM_DEVICE = true;
        public static final String OS = "Android";
        public static final int RETRY_COUNT_FOR_AUTO_ON = 1;
        public static final int RETRY_COUNT_FOR_INVALID_FINGERPRINT = 1;
        public static final int RETRY_COUNT_FOR_SERVER_ERROR = 4;
        public static final boolean USE_AKA_AUTH = true;
    }

    public static final class NSDSSharedPref {
        public static final String NAME_SHARED_PREF = "ericsson.nsds";
        public static final String NAME_SHARED_PREF_CONFIG = "entitlement.config";
        public static final String PREF_ACCESS_TOKEN = "access_token";
        public static final String PREF_ACCESS_TOKEN_EXPIRY = "access_token_expiry";
        public static final String PREF_ACCESS_TOKEN_TYPE = "access_token_type";
        public static final String PREF_AKA_TOKEN = "aka_token";
        public static final String PREF_AUTO_ACTIVATE_AFTER_OOS = "activate_after_oos";
        public static final String PREF_DEVICECONIFG_STATE = "device_config_state";
        public static final String PREF_DEVICE_GROUP_NAME = "device_group_name";
        public static final String PREF_DEVICE_ID = "device_id";
        public static final String PREF_DEVICE_STATE = "device_state";
        public static final String PREF_ENTITLEMENT_COMPLETED = "entitlement_completed";
        public static final String PREF_ENTITLEMENT_SERVER_URL = "entitlement_server_url";
        public static final String PREF_ENTITLEMENT_STATE = "entitlement_state";
        public static final String PREF_GCM_SENDER_ID = "gcm_sender_id";
        public static final String PREF_IMSI = "imsi";
        public static final String PREF_IMSI_EAP = "imsi_eap";
        public static final String PREF_MIGRATE_DB_TO_CE = "migrate_to_ce";
        public static final String PREF_PEDNING_SIM_SWAP = "pending_sim_swap";
        public static final String PREF_PREV_IMSI = "prev_imsi";
        public static final String PREF_PUSH_TOKEN = "push_token";
        public static final String PREF_SENT_TOKEN_TO_SERVER = "sent_token_to_server";
        public static final String PREF_SVC_PROV_STATE = "service_provision_state";
    }

    public static final class NSDSSimAuthType {
        public static final String ISIM = "isim";
        public static final String UNKNOWN = "unknown";
        public static final String USIM = "usim";
    }

    public static final class VowifiAutoOnOperation {
        public static final String AUTOON_COMPLETED = "completed";
        public static final String AUTOON_IN_PROGRESS = "inprogress";
        public static final String AUTOON_RETRY = "retry";
    }
}
