package com.sec.internal.constants.ims.entitilement;

import android.content.ContentUris;
import android.net.Uri;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;

public class NSDSContractExt {
    public static final int ACCOUNTS = 9;
    public static final int ACCOUNT_ID = 47;
    public static final int ACTIVATE_SIM_DEVICE = 30;
    public static final int ACTIVE_ACCOUNT = 7;
    public static final int ACTIVE_LINES = 8;
    public static final int ACTIVE_LINES_WITH_SERVICES = 29;
    public static final int ALL_LINES = 45;
    public static final int ALL_LINES_INTERNAL = 77;
    public static final int ALL_LINES_IN_ACTIVE_ACCOUNT = 44;
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.samsung.ims.nsds.provider");
    public static final int BINDING_SERVICE = 82;
    public static final int DEACTIVATE_SIM_DEVICE = 31;
    public static final int DEVICES = 2;
    public static final int DEVICE_CONFIG = 39;
    public static final int DEVICE_CONFIG_ELEMENT = 62;
    public static final int DEVICE_ID_LINE_ID_SERVICES = 6;
    public static final int DEVICE_ID_SERVICES = 42;
    public static final int DEVICE_ID_SET_PRIMARY = 26;
    public static final int DEVICE_NAME = 78;
    public static final int DEVICE_OWN_ACTIVATION_STATUS = 28;
    public static final int DEVICE_OWN_LOGIN_STATUS = 41;
    public static final int DEVICE_OWN_NSDS_SERVICE_STATUS = 61;
    public static final int DEVICE_OWN_READY_STATUS = 60;
    public static final int DEVICE_PROVISIONED = 79;
    public static final int DEVICE_PUSH_TOKEN = 67;
    public static final int DISABLE_ACTIVE_ACCOUNT = 48;
    public static final int DISABLE_CAB = 50;
    public static final int DOWNLOAD_ALL_CONTACTS = 24;
    public static final int ENABLE_CAB = 49;
    public static final int ENTITLEMENT_URL = 73;
    public static final int GCM_TOKENS = 74;
    public static final int INACTIVE_LINES_WITH_SERVICES = 70;
    public static final int LINES = 0;
    public static final int LINE_ID_ACTIVATE_SERVICES = 19;
    public static final int LINE_ID_ADD_SERVICES = 17;
    public static final int LINE_ID_DEACTIVATE_SERVICES = 20;
    public static final int LINE_ID_DEVICES = 43;
    public static final int LINE_ID_REMOVE_SERVICES = 18;
    public static final int NSDS_CONFIGS = 40;
    public static final String PROVIDER_NAME = "com.samsung.ims.nsds.provider";
    public static final int RECONNECT_DB = 81;
    public static final int RETRIEVE_AKA_TOKEN = 80;
    public static final int SERVICES = 63;
    public static final int SIM_SWAP_NSDS_CONFIGS = 71;
    public static final int SIM_SWAP_SERVICES = 72;
    public static final int UPDATE_E911_ADDRESS = 46;
    public static final int UPLOAD_ALL_CONTACTS = 23;
    public static final int UPLOAD_UPDATED_CONTACT = 25;
    public static final int VOWIFI_TOGGLE_OFF = 33;
    public static final int VOWIFI_TOGGLE_ON = 32;

    interface AccountColumns {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String ACCOUNT_ID = "account_id";
        public static final String DEVICE_UID = "device_uid";
        public static final String EMAIL = "email";
        public static final String IS_ACTIVE = "is_active";
        public static final String IS_TEMPORARY = "is_temporary";
    }

    interface CommonColumns {
        public static final String ID = "_id";
    }

    interface ConnectivityParamsColumns {
        public static final String CERTIFICATE = "certificate";
        public static final String EPDG_ADDRESSES = "epdg_addresses";
    }

    interface ConnectivityServicesColumns {
        public static final String APPSTORE_URL = "appstore_url";
        public static final String CLIENT_ID = "client_id";
        public static final String CONNECTIVITY_ID = "connectivity_id";
        public static final String PACKAGE_NAME = "package_name";
        public static final String SERVICE_NAME = "service_name";
    }

    interface DeviceColumns {
        public static final String ACCOUNT_ID = "device_account_id";
        public static final String DEVICE_IS_LOCAL = "is_local";
        public static final String DEVICE_IS_PRIMARY = "is_primary";
        public static final String DEVICE_NAME = "device_name";
        public static final String DEVICE_TYPE = "device_type";
        public static final String DEVICE_UID = "device_uid";
    }

    interface DeviceConfigColumns {
        public static final String DEVICE_CONFIG = "device_config";
        public static final String DEVICE_ID = "device_id";
        public static final String VERSION = "version";
    }

    interface GcmTokensColumns {
        public static final String DEVICE_UID = "device_uid";
        public static final String GCM_TOKEN = "gcm_token";
        public static final String PROTOCOL_TO_SERVER = "protocol_to_server";
        public static final String SENDER_ID = "sender_id";
    }

    interface LineColumns {
        public static final String ACCOUNT_ID = "account_id";
        public static final String CAB_STATUS = "cab_status";
        public static final String COLOR = "color";
        public static final String E911_ADDRESS_ID = "e911_address_id";
        public static final String E911_AID_EXPIRATION = "e911_aid_expiration";
        public static final String E911_SERVER_DATA = "e911_server_data";
        public static final String E911_SERVER_URL = "e911_server_url";
        public static final String FRIENDLY_NAME = "friendly_name";
        public static final String ICON = "icon";
        public static final String IS_DEVICE_DEFAULT = "is_device_default";
        public static final String IS_NATIVE = "is_native";
        public static final String IS_OWNER = "is_owner";
        public static final String LINE_RES_PACKAGE = "line_res_package";
        public static final String LOCATION_STATUS = "location_status";
        public static final String MSISDN = "msisdn";
        public static final String REG_STATUS = "reg_status";
        public static final String RING_TONE = "ring_tone";
        public static final String SERVICE_ATTRIBUTES = "service_attributes";
        public static final String STATUS = "status";
        public static final String TC_STATUS = "tc_status";
        public static final String TYPE = "type";
    }

    interface NsdsConfigColumns {
        public static final String IMSI = "imsi";
        public static final String PNAME = "pname";
        public static final String PVALUE = "pvalue";
    }

    interface ProvisioningParametersColumns {
        public static final String APN = "apn";
        public static final String IMPU = "impu";
        public static final String PCSCF_ADDRESS = "pcscf_address";
        public static final String SIP_PASSWORD = "sip_password";
        public static final String SIP_URI = "sip_uri";
        public static final String SIP_USERNAME = "sip_username";
    }

    public interface QueryParams {
        public static final String DEVICE_UID = "device_uid";
        public static final String IMSI = "imsi";
        public static final String SLOT_ID = "slot";
    }

    interface ServiceColumns {
        public static final String CONFIG_PARAMETERS = "config_parameters";
        public static final String DEVICE_ID = "device_id";
        public static final String EXPIRATION_TIME = "expiration_time";
        public static final String IS_NATIVE = "is_native";
        public static final String IS_OWNER = "is_owner";
        public static final String LINE_ID = "line_id";
        public static final String PROVISIONING_PARAMETERS_ID = "provisioning_params_id";
        public static final String SERVICE_FINGERPRINT = "service_fingerprint";
        public static final String SERVICE_INSTANCE_ID = "service_instance_id";
        public static final String SERVICE_INSTANCE_TOKEN = "service_instance_token";
        public static final String SERVICE_MSISDN = "service_msisdn";
        public static final String SERVICE_MSISDN_FRIENDLY_NAME = "msisdn_friendly_name";
        public static final String SERVICE_NAME = "service_name";
        public static final String SERVICE_STATUS = "service_status";
        public static final String SERVICE_TOKEN_EXPIRE_TIME = "service_token_expire_time";
    }

    public static final class Lines implements CommonColumns, LineColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "lines");
        public static final String QUERY_PARAM_LINE_NAME = "lineName";
        public static final String QUERY_PARAM_MSISDN = "msisdn";
        public static final String QUERY_PARAM_SERVICE_IDS = "service_ids";
        public static final String QUERY_PARAM_SERVICE_NAMES = "service_names";
        public static final int STATUS_ACTIVE = 1;
        public static final int STATUS_INACTIVE = 0;
        public static final int STATUS_NOT_REGISTERED = 0;
        public static final int STATUS_READY_FOR_USE = 2;

        private Lines() {
        }

        public static final Uri buildLinesUri(String deviceUid) {
            Uri.Builder builder = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "lines").buildUpon();
            builder.appendQueryParameter("device_uid", deviceUid);
            return builder.build();
        }

        public static final Uri buildAllLinesInternalUri() {
            return Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "all_lines_internal");
        }

        public static final Uri buildActiveLinesWithServicveUri(String deviceUid) {
            Uri.Builder builder = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "active_lines_with_services").buildUpon();
            builder.appendQueryParameter("device_uid", deviceUid);
            return builder.build();
        }

        public static final Uri buildActiveLinesWithServicveUri() {
            return Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "active_lines_with_services").buildUpon().build();
        }

        public static final Uri buildLineUri(long lineId) {
            return ContentUris.withAppendedId(CONTENT_URI, lineId);
        }

        public static final Uri buildServicesUri(long deviceId, long lineId) {
            Uri deviceIdUri = ContentUris.withAppendedId(Devices.CONTENT_URI, deviceId);
            return Uri.withAppendedPath(Uri.withAppendedPath(deviceIdUri, "lines/" + lineId), GlobalSettingsConstants.Registration.EXTENDED_SERVICES);
        }

        public static final Uri buildRefreshSitUri(String msisdn) {
            Uri.Builder builder = Uri.withAppendedPath(CONTENT_URI, "refresh_sit").buildUpon();
            builder.appendQueryParameter("msisdn", msisdn);
            return builder.build();
        }
    }

    public static final class Devices implements CommonColumns, DeviceColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "devices");
        public static final String DEVICE_PUSH_TOKEN = "device_push_token";
        public static final String OWN_ACTIVATION_STATUS = "activation_status";
        public static final String OWN_READY_STATUS = "ready_status";
        public static final String QUERY_PARAM_DEVICE_NAME = "deviceName";
        public static final String QUERY_PARAM_IS_PRIMARY = "is_primary";

        private Devices() {
        }

        public static final Uri buildDeviceUri(long deviceId) {
            return ContentUris.withAppendedId(CONTENT_URI, deviceId);
        }

        public static final Uri buildUpdateDeviceNameUri(long deviceRowId) {
            return Uri.withAppendedPath(ContentUris.withAppendedId(CONTENT_URI, deviceRowId), "update_device_name");
        }
    }

    public static final class Accounts implements CommonColumns, AccountColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "accounts");

        private Accounts() {
        }

        public static final Uri buildAccountUri(long accountId) {
            return ContentUris.withAppendedId(CONTENT_URI, accountId);
        }
    }

    public static final class ProvisioningParameters implements CommonColumns, ProvisioningParametersColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "provisioning_params");

        private ProvisioningParameters() {
        }

        public static final Uri buildProvisioningParametersUri(long provisionParamId) {
            return ContentUris.withAppendedId(CONTENT_URI, provisionParamId);
        }
    }

    public static final class Services implements CommonColumns, ServiceColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, GlobalSettingsConstants.Registration.EXTENDED_SERVICES);

        private Services() {
        }

        public static final Uri buildServiceUri(long serviceId) {
            return ContentUris.withAppendedId(CONTENT_URI, serviceId);
        }
    }

    public static final class ConnectivityParameters implements CommonColumns, ConnectivityParamsColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "connectivity_params");

        private ConnectivityParameters() {
        }

        public static final Uri buildConnectivityParamsUri(long connectivityParamId) {
            return ContentUris.withAppendedId(CONTENT_URI, connectivityParamId);
        }
    }

    public static final class DeviceConfig implements CommonColumns, DeviceConfigColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "device_config");
        public static final String ELEMENT_NAME = "element_name";
        public static final String ELEMENT_VLAUE = "element_value";
        public static final String QUERY_PARAM_TAG_NAME = "tag_name";

        private DeviceConfig() {
        }

        public static final Uri buildDeviceConfigUri(long rowId) {
            return ContentUris.withAppendedId(CONTENT_URI, rowId);
        }
    }

    public static final class ConnectivityParameterServiceNames implements CommonColumns, ConnectivityServicesColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "connectivity_service_names");

        private ConnectivityParameterServiceNames() {
        }

        public static final Uri buildServiceNamesUri(long serviceNameRowId) {
            return ContentUris.withAppendedId(CONTENT_URI, serviceNameRowId);
        }
    }

    public static final class GcmTokens implements CommonColumns, GcmTokensColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "gcm_tokens");

        private GcmTokens() {
        }

        public static Uri buildGcmTokensUri(long gcmTokensRowId) {
            return ContentUris.withAppendedId(CONTENT_URI, gcmTokensRowId);
        }
    }

    public static final class NsdsConfigs implements CommonColumns, NsdsConfigColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "nsds_configs");
        public static final String QUERY_PARAM_ENTITLEMENT_URI = "entitlement_url";

        private NsdsConfigs() {
        }

        public static final Uri buildNsdsConfigUri(long rowId) {
            return ContentUris.withAppendedId(CONTENT_URI, rowId);
        }
    }

    public static final class SimSwapNsdsConfigs implements CommonColumns, NsdsConfigColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "sim_swap_nsds_configs");

        private SimSwapNsdsConfigs() {
        }

        public static final Uri buildNsdsConfigUri(long rowId) {
            return ContentUris.withAppendedId(CONTENT_URI, rowId);
        }
    }
}
