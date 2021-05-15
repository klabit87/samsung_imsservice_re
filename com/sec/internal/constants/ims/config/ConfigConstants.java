package com.sec.internal.constants.ims.config;

import android.net.Uri;
import android.os.Build;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import java.util.Map;
import java.util.TreeMap;

public class ConfigConstants {
    public static final Map<String, String> APPID_MAP = new TreeMap<String, String>() {
        {
            put("ap2001", "0");
            put("ap2002", "1");
            put(AECNamespace.ApplicationId.APP_ID_VOLTE, "2");
            put(AECNamespace.ApplicationId.APP_ID_VOWIFI, DiagnosisConstants.RCSM_ORST_REGI);
            put(AECNamespace.ApplicationId.APP_ID_SMSOIP, DiagnosisConstants.RCSM_ORST_HTTP);
            put("urn:oma:mo:ext-3gpp-ims:1.0", "0");
            put("urn:oma:mo:ext-3gpp-nas-config:1.0", DiagnosisConstants.RCSM_ORST_ITER);
        }
    };
    public static final String AUTHORITY = "com.samsung.rcs.autoconfigurationprovider";
    public static final String CONFIG_URI = "content://com.samsung.rcs.autoconfigurationprovider/";
    public static final Uri CONTENT_URI = Uri.parse(CONFIG_URI);

    public static class ATCMD {
        public static final String OMADM_VALUE = "OMADM_VALUE";
        public static final String SMS_SETTING = "SMS_SETTING";
    }

    public static class BUILD {
        public static final String TERMINAL_MODEL = Build.MODEL;
        public static final String TERMINAL_OS_VERSION = Build.VERSION.RELEASE;
        public static final String TERMINAL_SW_VERSION = Build.VERSION.INCREMENTAL;
        public static final String TERMINAL_VENDOR = Build.MANUFACTURER;
    }

    public static class CONFIGTYPE {
        public static final String PARSEDXML_DATA = "parsedxml_data";
        public static final String STORAGE_DATA = "storage_data";
    }

    public static final class ConfigPath {
        public static final String APPAUTH_CHARACTERISTIC_PATH = "root/application/0/appauth/";
        public static final String APPAUTH_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/ext/gsma/";
        public static final String APPLICATION_CHARACTERISTIC_PATH = "root/application/0/";
        public static final String APPLICATION_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/";
        public static final String CAPDISCOVERY_ALLOWED_PREFIXES_PATH = "root/application/1/capdiscovery/capdiscoverywhitelist/capdiscoveryallowedprefixes/prefix";
        public static final String CAPDISCOVERY_CHARACTERISTIC_PATH = "root/application/1/capdiscovery/";
        public static final String CAPDISCOVERY_EXT_JOYN_PATH = "root/application/1/capdiscovery/ext/joyn/";
        public static final String CHATBOT_CHARACTERISTIC_PATH = "root/application/1/messaging/chatbot/";
        public static final String CHAT_CHARACTERISTIC_PATH = "root/application/1/messaging/chat/";
        public static final String CLIENT_CONTROL_CHARACTERISTIC_PATH = "root/application/1/clientcontrol/";
        public static final String CONFIG_VERSION = "root/vers/version";
        public static final String CPM_CHARACTERISTIC_PATH = "root/application/1/cpm/";
        public static final String CPM_MESSAGESTORE_CHARACTERISTIC_PATH = "root/application/1/cpm/messagestore/";
        public static final String ENRICHED_CALLING_CHARACTERISTIC_PATH = "root/application/4/";
        public static final String EXT_CHARACTERISTIC_PATH = "root/application/0/ext/";
        public static final String EXT_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/ext/gsma/";
        public static final String FILETRANSFER_CHARACTERISTIC_PATH = "root/application/1/messaging/filetransfer/";
        public static final String IM_CHARACTERISTIC_PATH = "root/application/1/im/";
        public static final String IM_EXT_CHARACTERISTIC_PATH = "root/application/1/im/ext/";
        public static final String JOYN_MESSAGING_CHARACTERISTIC_PATH = "root/application/1/serviceproviderext/joyn/messaging/";
        public static final String JOYN_UX_MESSAGING_UX = "root/application/1/serviceproviderext/joyn/ux/messagingUX";
        public static final String JOYN_UX_PATH = "root/application/1/serviceproviderext/joyn/ux/";
        public static final String LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH = "root/application/0/lbo_p-cscf_address/0/lbo_p-cscf_addresses/";
        public static final String LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/lbo_p-cscf_addresses/";
        public static final String LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH = "root/application/0/lbo_p-cscf_address/";
        public static final String LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/lbo_p-cscf_address/";
        public static final String MESSAGESTORE_CHARACTERISTIC_PATH = "root/application/1/messaging/messagestore/";
        public static final String MESSAGING_CHARACTERISTIC_PATH = "root/application/1/messaging/";
        public static final String MSISDN_PATH = "info/msisdn/";
        public static final String OMADM_AVAIL_CACHE_EXP = "omadm/./3GPP_IMS/AVAIL_CACHE_EXP";
        public static final String OMADM_CAP_CACHE_EXP = "omadm/./3GPP_IMS/CAP_CACHE_EXP";
        public static final String OMADM_CAP_DISCOVERY = "omadm/./3GPP_IMS/CAP_DISCOVERY";
        public static final String OMADM_CAP_POLL_INTERVAL = "omadm/./3GPP_IMS/CAP_POLL_INTERVAL";
        public static final String OMADM_DCN_NUMBER = "omadm/./3GPP_IMS/DCN_NUMBER";
        public static final String OMADM_EAB_SETTING = "omadm/./3GPP_IMS/EAB_SETTING";
        public static final String OMADM_HOME_NETWORK_DOMAIN_NAME = "omadm/./3GPP_IMS/Home_network_domain_name";
        public static final String OMADM_LVC_ENABLED = "omadm/./3GPP_IMS/LVC_ENABLED";
        public static final String OMADM_PATH = "omadm/./3GPP_IMS/";
        public static final String OMADM_POLL_LIST_SUB_EXP = "omadm/./3GPP_IMS/POLL_LIST_SUB_EXP";
        public static final String OMADM_PRIVATE_USER_IDENTITY = "omadm/./3GPP_IMS/Private_user_identity";
        public static final String OMADM_PUBLIC_USER_ID = "omadm/./3GPP_IMS/Public_user_identity_List/Public_user_identity_List_1/Public_user_identity";
        public static final String OMADM_PUBLISH_ERR_RETRY_TIMER = "omadm/./3GPP_IMS/PUBLISH_ERR_RETRY_TIMER";
        public static final String OMADM_PUBLISH_TIMER = "omadm/./3GPP_IMS/PUBLISH_TIMER";
        public static final String OMADM_PUBLISH_TIMER_EXTEND = "omadm/./3GPP_IMS/PUBLISH_TIMER_EXTEND";
        public static final String OMADM_P_CSCF_ADDRESS = "omadm/./3GPP_IMS/P-CSCF_Address";
        public static final String OMADM_SPR_VOLTE_UI_DEFAULT = "omadm/./3GPP_IMS/SPR_VOLTE_UI_DEFAULT";
        public static final String OMADM_SRC_THROTTLE_PUBLISH = "omadm/./3GPP_IMS/SRC_THROTTLE_PUBLISH";
        public static final String OMADM_SUBSCRIBE_MAX_ENTRY = "omadm/./3GPP_IMS/SUBSCRIBE_MAX_ENTRY";
        public static final String OMADM_TWLAN_911_CALLFAIL_TIMER = "omadm/./3GPP_IMS/TWLAN_911_CALLFAIL_TIMER";
        public static final String OMADM_T_LTE_911_FAIL = "omadm/./3GPP_IMS/T_LTE_911_FAIL";
        public static final String OMADM_VOICE_DOMAIN_PREF_EUTRAN = "omadm/./3GPP_IMS/VOICE_DOMAIN_PREF_EUTRAN";
        public static final String OMADM_VOLTE_ENABLED = "omadm/./3GPP_IMS/VOLTE_ENABLED";
        public static final String OMADM_VOLTE_USER_SETTING = "omadm/./3GPP_IMS/VOLTE_USER_SETTING";
        public static final String OMADM_VWF_ENABLED = "omadm/./3GPP_IMS/VWF_ENABLED";
        public static final String OMADM_VZW_TIMS_TIMER = "omadm/./3GPP_IMS/VZW_TIMS_TIMER";
        public static final String OTHER_CHARACTERISTIC_PATH = "root/application/1/other/";
        public static final String PLUGINS_CHARACTERISTIC_PATH = "root/application/1/messaging/plugins/";
        public static final String PRESENCE_CHARACTERISTIC_PATH = "root/application/1/presence/";
        public static final String PRESENCE_LOCATION_PATH = "root/application/1/presence/location/";
        public static final String PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH = "root/application/0/public_user_identity_list/";
        public static final String PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH = "root/application/0/public_user_identity_list/0/public_user_identities/";
        public static final String PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/public_user_identity_list/";
        public static final String SERVICEPROVIDEREXT_CHARACTERISTIC_PATH = "root/application/1/serviceproviderext/";
        public static final String SERVICEPROVIDEREXT_CHATBOT_PATH = "root/application/1/serviceproviderext/chatbot/";
        public static final String SERVICE_CHARACTERISTIC_PATH = "root/application/1/services/";
        public static final String SERVICE_EXT_DATAOFF_PATH = "root/application/1/services/ext/dataoff/";
        public static final String SERVICE_PATH = "root/services/";
        public static final String SERVICE_PROVIDER_EXT_PATH = "root/serviceproviderext/";
        public static final String SLM_CHARACTERISTIC_PATH = "root/application/1/cpm/standalonemsg/";
        public static final String STANDALONEMSG_CHARACTERISTIC_PATH = "root/application/1/messaging/standalonemsg/";
        public static final String TOKEN_PATH = "root/token/";
        public static final String TRANSPORT_PROTO_CHARACTERISTIC_PATH = "root/application/1/other/transportproto/";
        public static final String TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH = "root/application/0/ext/transportproto/";
        public static final String TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH = "root/application/0/3gpp_ims/ext/gsma/transportproto/";
        public static final String UX_CHARACTERISTIC_PATH = "root/application/1/ux/";
        public static final String VERS_PATH = "root/vers/";
        public static final String XDMS_CHARACTERISTIC_PATH = "root/application/1/xdms/";
    }

    public static final class ConfigTable {
        public static final String APPAUTH_REALM = "realm";
        public static final String APPAUTH_USER_NAME = "UserName";
        public static final String APPAUTH_USER_PWD = "UserPwd";
        public static final String CAPDISCOVERY_ALLOWED_PREFIXES = "capdiscoveryallowedprefixes";
        public static final String CAPDISCOVERY_CAPINFO_EXPIRY = "capinfoexpiry";
        public static final String CAPDISCOVERY_CAP_DISC_COMMON_STACK = "capDiscCommonStack";
        public static final String CAPDISCOVERY_DEFAULT_DISC = "defaultdisc";
        public static final String CAPDISCOVERY_DISABLE_INITIAL_SCAN = "disableInitialAddressBookScan";
        public static final String CAPDISCOVERY_JOYN_LASTSEENACTIVE = "lastseenactive";
        public static final String CAPDISCOVERY_JOYN_MSGCAPVALIDITY = "msgcapvalidity";
        public static final String CAPDISCOVERY_MAX_ENTRIES_IN_LIST = "maxentriesinlist";
        public static final String CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY = "nonRCScapInfoExpiry";
        public static final String CAPDISCOVERY_POLLING_PERIOD = "pollingperiod";
        public static final String CAPDISCOVERY_POLLING_RATE = "pollingrate";
        public static final String CAPDISCOVERY_POLLING_RATE_PERIOD = "pollingrateperiod";
        public static final String CHATBOT_BOTINFOFQDNROOT = "BotinfoFQDNRoot";
        public static final String CHATBOT_CHATBOTBLACKLIST = "ChatbotBlacklist";
        public static final String CHATBOT_CHATBOTDIRECTORY = "ChatbotDirectory";
        public static final String CHATBOT_CHATBOT_MSG_TECH = "ChatbotMsgTech";
        public static final String CHATBOT_IDENTITY_IN_ENRICHED_SEARCH = "IdentityInEnrichedSearch";
        public static final String CHATBOT_MSGHISTORYSELECTABLE = "MsgHistorySelectable";
        public static final String CHATBOT_PRIVACY_DISABLE = "PrivacyDisable";
        public static final String CHATBOT_SPECIFIC_CHATBOTS_LIST = "SpecificChatbotsList";
        public static final String CLIENTCONTROL_CFS_TRIGGER = "cfsTrigger";
        public static final String CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH = "displayNotificationSwitch";
        public static final String CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON = "ftHTTPCapAlwaysOn";
        public static final String CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS = "ftMax1ToManyRecipients";
        public static final String CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS = "max1toManyRecipients";
        public static final String CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH = "1toManySelectedTech";
        public static final String CLIENTCONTROL_RECONNECT_GUARD_TIMER = "reconnectGuardTimer";
        public static final String CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY = "serviceAvailabilityInfoExpiry";
        public static final String CPM_MESSAGE_STORE_AUTH_ARCHIVE = "AuthArchive";
        public static final String CPM_MESSAGE_STORE_AUTH_PROT = "AuthProt";
        public static final String CPM_MESSAGE_STORE_DATA_CONNECTION_SYNC_TIMER = "DataConnectionSyncTimer";
        public static final String CPM_MESSAGE_STORE_EVENT_RPTING = "EventRpting";
        public static final String CPM_MESSAGE_STORE_MMS_STORE = "MMSStore";
        public static final String CPM_MESSAGE_STORE_SMS_STORE = "SMSStore";
        public static final String CPM_MESSAGE_STORE_SYNC_TIMER = "SyncTimer";
        public static final String CPM_MESSAGE_STORE_URL = "Url";
        public static final String CPM_MESSAGE_STORE_USER_NAME = "messagestore/UserName";
        public static final String CPM_MESSAGE_STORE_USER_PWD = "messagestore/UserPwd";
        public static final String CPM_SLM_MAX_MSG_SIZE = "MaxSizeStandalone";
        public static final String DATAOFF_CONTENT_SHARE = "contentShareDataOff";
        public static final String DATAOFF_FILE_TRANSFER = "fileTransferDataOff";
        public static final String DATAOFF_IP_VIDEO = "IPVideoCallDataOff";
        public static final String DATAOFF_MMS = "mmsDataOff";
        public static final String DATAOFF_PRE_AND_POST_CALL = "preAndPostCallDataOff";
        public static final String DATAOFF_PROVISIONING = "provisioningDataOff";
        public static final String DATAOFF_RCS_MESSAGING = "rcsMessagingDataOff";
        public static final String DATAOFF_SMSOIP = "smsoIPDataOff";
        public static final String DATAOFF_SYNC = "syncDataOff";
        public static final String DATAOFF_VOLTE = "volteDataOff";
        public static final String EXT_END_USER_CONF_REQID = "endUserConfReqId";
        public static final String EXT_INT_URL_FORMAT = "inturlfmt";
        public static final String EXT_MAX_SIZE_IMAGE_SHARE = "maxsizeimageshare";
        public static final String EXT_MAX_TIME_VIDEO_SHARE = "maxtimevideoshare";
        public static final String EXT_NAT_URL_FORMAT = "naturlfmt";
        public static final String EXT_Q_VALUE = "q-value";
        public static final String EXT_RCS_VOLTE_SINGLE_REGISTRATION = "rcsVolteSingleRegistration";
        public static final String EXT_UUID_VALUE = "uuid_Value";
        public static final String HOME_NETWORK_DOMAIN_NAME = "home_network_domain_name";
        public static final String IM_AUT_ACCEPT = "autaccept";
        public static final String IM_AUT_ACCEPT_GROUP_CHAT = "autacceptgroupchat";
        public static final String IM_CHAT_REVOKE_TIMER = "ChatRevokeTimer";
        public static final String IM_CONF_FCTY_URI = "conf-fcty-uri";
        public static final String IM_DEFERRED_MSG_FUNC_URI = "deferred-msg-func-uri";
        public static final String IM_EXPLODER_URI = "exploder-uri";
        public static final String IM_EXT_CB_FT_HTTP_CS_URI = "cbftHTTPCSURI";
        public static final String IM_EXT_FT_HTTP_EXTRA_CS_URI = "ftHTTPExtraCSURI";
        public static final String IM_EXT_MAX_IMDN_AGGREGATION = "MaxIMDNAggregation";
        public static final String IM_EXT_MAX_SIZE_EXTRA_FILE_TR = "MaxSizeExtraFileTr";
        public static final String IM_FIRST_MSG_INVITE = "firstMessageInvite";
        public static final String IM_FT_AUT_ACCEPT = "ftautaccept";
        public static final String IM_FT_CAP_ALWAYS_ON = "ftCapalwaysON";
        public static final String IM_FT_DEFAULT_MECH = "ftDefaultMech";
        public static final String IM_FT_HTTP_CS_PWD = "ftHTTPCSPwd";
        public static final String IM_FT_HTTP_CS_URI = "ftHTTPCSURI";
        public static final String IM_FT_HTTP_CS_USER = "ftHTTPCSUser";
        public static final String IM_FT_HTTP_DL_URI = "ftHTTPDLURI";
        public static final String IM_FT_HTTP_FALLBACK = "ftHTTPFallback";
        public static final String IM_FT_HTTP_FT_WARN_SIZE = "ftHTTPftWarnSize";
        public static final String IM_FT_HTTP_MAX_SIZE_FILE_TR = "ftHTTPMaxSizeFileTr";
        public static final String IM_FT_HTTP_MAX_SIZE_FILE_TR_INCOMING = "ftHTTPMaxSizeFileTrIncoming";
        public static final String IM_FT_ST_AND_FW_ENABLED = "ftStAndFwEnabled";
        public static final String IM_FT_THUMB = "ftThumb";
        public static final String IM_FT_WARN_SIZE = "ftWarnSize";
        public static final String IM_GROUP_CHAT_FULL_STAND_FWD = "GroupChatFullStandFwd";
        public static final String IM_GROUP_CHAT_ONLY_F_STAND_FWD = "GroupChatOnlyFStandFwd";
        public static final String IM_IM_CAP_ALWAYS_ON = "imcapalwayson";
        public static final String IM_IM_CAP_NON_RCS = "imCapNonRCS";
        public static final String IM_IM_MSG_TECH = "imMsgTech";
        public static final String IM_IM_SESSION_START = "imsessionstart";
        public static final String IM_IM_WARN_IW = "imwarniw";
        public static final String IM_IM_WARN_SF = "imWarnSF";
        public static final String IM_MASS_FCTY_URI = "mass-fcty-uri";
        public static final String IM_MAX_ADHOC_GROUP_SIZE = "max_adhoc_group_size";
        public static final String IM_MAX_ADHOC_OPEN_GROUP_SIZE = "max_adhoc_open_group_size";
        public static final String IM_MAX_CONCURRENT_SESSION = "maxConcurrentSession";
        public static final String IM_MAX_SIZE = "MaxSize";
        public static final String IM_MAX_SIZE_1_TO_1 = "maxsize1to1";
        public static final String IM_MAX_SIZE_1_TO_M = "maxsize1tom";
        public static final String IM_MAX_SIZE_FILE_TR = "MaxSizeFileTr";
        public static final String IM_MAX_SIZE_FILE_TR_INCOMING = "MaxSizeFileTrIncoming";
        public static final String IM_MULTIMEDIA_CHAT = "multiMediaChat";
        public static final String IM_PRES_SRV_CAP = "pres-srv-cap";
        public static final String IM_SMS_FALLBACK_AUTH = "smsfallbackauth";
        public static final String IM_TIMER_IDLE = "TimerIdle";
        public static final String KEEP_ALIVE_ENABLED = "keep_alive_enabled";
        public static final String LBO_PCSCF_ADDRESS = "address";
        public static final String LBO_PCSCF_ADDRESS_TYPE = "addresstype";
        public static final String MESSAGESTORE_MSG_STORE_AUTH = "MsgStoreAuth";
        public static final String MESSAGESTORE_MSG_STORE_NOTIF_URL = "MsgStoreNotifUrl";
        public static final String MESSAGESTORE_MSG_STORE_URL = "MsgStoreUrl";
        public static final String MESSAGESTORE_MSG_STORE_USER_NAME = "MsgStoreUserName";
        public static final String MESSAGESTORE_MSG_STORE_USER_PWD = "MsgStoreUserPwd";
        public static final String MSISDN_MSGUI_DISPLAY = "msgui_display";
        public static final String MSISDN_SKIP_COUNT = "skip_count";
        public static final String NC_URL = "NC_URL";
        public static final String NMS_URL = "NMS_URL";
        public static final String OTHER_CALL_COMPOSER_TIMER_IDLE = "callComposerTimerIdle";
        public static final String OTHER_EXTENSIONS_MAX_MSRP_SIZE = "extensionsMaxMSRPSize";
        public static final String OTHER_MAX_TIME_VIDEO_SHARE = "maxtimevideoshare";
        public static final String OTHER_UUID_VALUE_JOYN = "uuid_Value";
        public static final String OTHER_WARN_SIZE_IMAGE_SHARE = "warnsizeimageshare";
        public static final String PERSONAL_PROFILE_ADDR = "profile/addr";
        public static final String PERSONAL_PROFILE_ADDRTYPE = "profile/addrtype";
        public static final String PLUGINS_CATALOGURI = "catalogURI";
        public static final String PRESENCE_CLIENT_OBJ_DATALIMIT = "client-obj-datalimit";
        public static final String PRESENCE_LOC_INFO_MAX_VALID_TIME = "locinfomaxvalidtime";
        public static final String PRESENCE_MAX_SUBSCRIPTION_LIST = "max-number-ofsubscriptions-inpresence-list";
        public static final String PRESENCE_PUBLISH_TIMER = "PublishTimer";
        public static final String PRESENCE_RLS_URI = "RLS-URI";
        public static final String PRESENCE_THROTTLE_PUBLISH = "source-throttlepublish";
        public static final String PRIVATE_USER_IDENTITY = "private_user_identity";
        public static final String PUBLIC_ACCOUNT_ADDR = "publicaccount/Addr";
        public static final String PUBLIC_ACCOUNT_ADDRTYPE = "publicaccount/AddrType";
        public static final String PUBLIC_USER_IDENTITY = "public_user_identity";
        public static final String REG_RETRY_BASE_TIME = "RegRetryBaseTime";
        public static final String REG_RETRY_MAX_TIME = "RegRetryMaxTime";
        public static final String SERVICEPROVIDEREXT_CHATBOT_PASSWORD = "chatbot/Password";
        public static final String SERVICEPROVIDEREXT_CHATBOT_USER_NAME = "chatbot/Username";
        public static final String SERVICEPROVIDEREXT_FTHTTPGROUPCHAT = "fthttpGroupChat";
        public static final String SERVICES_ALLOW_RCS_EXTENSIONS = "allowRCSExtensions";
        public static final String SERVICES_CHAT_AUTH = "ChatAuth";
        public static final String SERVICES_COMPOSER_AUTH = "composerAuth";
        public static final String SERVICES_FT_AUTH = "ftAuth";
        public static final String SERVICES_GEOPULL_AUTH = "geolocPullAuth";
        public static final String SERVICES_GEOPUSH_AUTH = "geolocPushAuth";
        public static final String SERVICES_GROUP_CHAT_AUTH = "GroupChatAuth";
        public static final String SERVICES_IR94_VIDEO_AUTH = "IR94VideoAuth";
        public static final String SERVICES_IS_AUTH = "isAuth";
        public static final String SERVICES_POST_CALL_AUTH = "postCallAuth";
        public static final String SERVICES_PRESENCE_PRFL = "presencePrfl";
        public static final String SERVICES_RCS_DISABLED_STATE = "rcsDisabledState";
        public static final String SERVICES_RCS_IPVIDEOCALL_AUTH = "rcsIPVideoCallAuth";
        public static final String SERVICES_RCS_IPVOICECALL_AUTH = "rcsIPVoiceCallAuth";
        public static final String SERVICES_RCS_STATE = "rcsState";
        public static final String SERVICES_SHARED_MAP_AUTH = "sharedMapAuth";
        public static final String SERVICES_SHARED_SKETCH_AUTH = "sharedSketchAuth";
        public static final String SERVICES_SLM_AUTH = "standaloneMsgAuth";
        public static final String SERVICES_SUPPORTED_RCS_PROFILE_VERSIONS = "SupportedRCSProfileVersions";
        public static final String SERVICES_SUPPORTED_RCS_VERSIONS = "SupportedRCSVersions";
        public static final String SERVICES_VS_AUTH = "vsAuth";
        public static final String SLM_MAX_MSG_SIZE = "MaxSize";
        public static final String SLM_SWITCH_OVER_SIZE = "SwitchoverSize";
        public static final String SPG_PARAMS_URL = "params-url";
        public static final String SPG_URL = "spgUrl";
        public static final String TIMER_T1 = "Timer_T1";
        public static final String TIMER_T2 = "Timer_T2";
        public static final String TIMER_T4 = "Timer_T4";
        public static final String TOKEN = "token";
        public static final String TRANSPORTPROTO_PS_MEDIA = "psmedia";
        public static final String TRANSPORTPROTO_PS_MEDIA_ROAMING = "psmediaroaming";
        public static final String TRANSPORTPROTO_PS_RT_MEDIA = "psrtmedia";
        public static final String TRANSPORTPROTO_PS_RT_MEDIA_ROAMING = "psrtmediaroaming";
        public static final String TRANSPORTPROTO_PS_SIGNALLING = "pssignalling";
        public static final String TRANSPORTPROTO_PS_SIGNALLING_ROAMING = "pssignallingroaming";
        public static final String TRANSPORTPROTO_WIFI_MEDIA = "wifimedia";
        public static final String TRANSPORTPROTO_WIFI_RT_MEDIA = "wifirtmedia";
        public static final String TRANSPORTPROTO_WIFI_SIGNALLING = "wifisignalling";
        public static final String UX_ALLOW_ENRICHED_CHATBOT_SEARCH_DEFAULT = "allowEnrichedChatbotSearchDefault";
        public static final String UX_CALL_LOG_BEARER_DIFFER = "callLogsBearerDiffer";
        public static final String UX_FT_FB_DEFAULT = "ftFBDefault";
        public static final String UX_IR51_SWITCH_UX = "IR51SwitchUx";
        public static final String UX_MESSAGING_UX = "messagingUX";
        public static final String UX_MSG_FB_DEFAULT = "msgFBDefault";
        public static final String UX_SPAM_NOTIFICATION_TEXT = "spamNotificationText";
        public static final String UX_TOKEN_LINK_NOTIFICATION_TEXT = "tokenLinkNotificationText";
        public static final String UX_UNAVAILABLE_ENDPOINT_TEXT = "unavailableEndpointText";
        public static final String UX_USER_ALIAS_AUTH = "userAliasAuth";
        public static final String UX_VIDEO_AND_ENCALL_UX = "videoAndEnCallUX";
        public static final String VALIDITY = "validity";
        public static final String VERSION = "version";
        public static final String XDMS_XCAP_ROOT_URI = "xcaprooturi";
    }

    public static class KEY {
        public static final String INTERNAL_CONFIG_PROXY_AUTHORITY = "89148ec7-de3f-42de-b2c5-b33298e1f4c6";
    }

    public static final class NetworkType {
        public static final int IMS = 2;
        public static final int MOBILE = 1;
        public static final int NONE = 0;
        public static final int WIFI = 3;
    }

    public static class PATH {
        public static final String ADDITIONAL_SERVER = "root/access-control/server";
        public static final String DEFAULT_SERVER = "root/access-control/default";
        public static final String EAP_AKA = "root/eap_aka";
        public static final String EAP_AKA_CHALLENGE = "root/eap_aka/eap_aka_challenge";
        public static final String IM_MAX_SIZE = "root/application/1/im/maxsize";
        public static final String IM_MAX_SIZE_1_TO_1 = "root/application/1/im/maxsize1to1";
        public static final String INFO_COMPLETED = "info/completed";
        public static final String INFO_LAST_ERROR_CODE = "info/last_error_code";
        public static final String MSG = "root/msg";
        public static final String MSG_ACCEPT_BUTTON = "root/msg/accept_btn";
        public static final String MSG_MESSAGE = "root/msg/message";
        public static final String MSG_REJECT_BUTTON = "root/msg/reject_btn";
        public static final String MSG_TITLE = "root/msg/title";
        public static final String MSISDN_MSGUI_DISPLAY = "info/msisdn/msgui_display";
        public static final String MSISDN_SKIP_COUNT = "info/msisdn/skip_count";
        public static final String NEXT_AUTOCONFIG_TIME = "info/next_autoconfig_time";
        public static final String OMADM_PREFIX = "omadm/./3GPP_IMS/";
        public static final String POLICY_SMS_PORT = "root/policy/sms_port";
        public static final String RAW_CONFIG_XML_FILE = "info/raw_config_xml_file";
        public static final String RCS_DISABLED_STATE = "root/application/1/services/rcsdisabledstate";
        public static final String RCS_DISABLED_STATE_FOR_VZW = "root/services/rcsdisabledstate";
        public static final String RCS_STATE = "root/application/1/services/rcsstate";
        public static final String RCS_STATE_FOR_VZW = "root/services/rcsstate";
        public static final String SERVER_SUPPORTED_VESIONS = "info/server_supported_vesions";
        public static final String SPG_PARAMS_URL = "root/serviceproviderext/params-url";
        public static final String SPG_URL = "root/serviceproviderext/spgurl";
        public static final String START_MSISDN_TIMER = "info/start_msisdn_timer";
        public static final String TC_POPUP_USER_ACCEPT = "info/tc_popup_user_accept";
        public static final String TOKEN_TOKEN = "root/token/token";
        public static final String USERPWD = "root/application/0/appauth/userpwd";
        public static final String USERPWD_UP20 = "root/application/0/3gpp_ims/ext/gsma/userpwd";
        public static final String VERS_VALIDITY = "root/vers/validity";
        public static final String VERS_VERSION = "root/vers/version";
        public static final String VERS_VERSION_BACKUP = "backup/vers/version";
        public static final String VERS_VERSION_FROM_SERVER = "root/vers/version_from_server";
    }

    public static class PNAME {
        public static final String APP = "app";
        public static final String CLIENT_VENDOR = "client_vendor";
        public static final String CLIENT_VERSION = "client_version";
        public static final String DEFAULT_SMS_APP = "default_sms_app";
        public static final String EAP_PAYLD = "EAP_PAYLD";
        public static final String IMEI = "IMEI";
        public static final String IMSI = "IMSI";
        public static final String IMSI_EAP = "IMSI_EAP";
        public static final String MSISDN = "msisdn";
        public static final String OTP = "OTP";
        public static final String PROVISIONING_VERSION = "provisioning_version";
        public static final String RCS_PROFILE = "rcs_profile";
        public static final String RCS_STATE = "rcs_state";
        public static final String RCS_VERSION = "rcs_version";
        public static final String RJIL_TOKEN = "rjil_token";
        public static final String SIM_MODE = "sim_mode";
        public static final String SMS_PORT = "SMS_port";
        public static final String TERMINAL_MODEL = "terminal_model";
        public static final String TERMINAL_SW_VERSION = "terminal_sw_version";
        public static final String TERMINAL_VENDOR = "terminal_vendor";
        public static final String TOKEN = "token";
        public static final String VERS = "vers";
    }

    public static class PVALUE {
        public static final String CLIENT_VENDOR = "SEC";
        public static final String CLIENT_VERSION_NAME = "RCSAndr-";
        public static final String CLIENT_VERSION_VALUE = "6.0";
        public static final String DEFAULT_SMS_APP = "1";
        public static final String NONDEFAULT_SMS_APP = "2";
        public static final String PROVISIONING_VERSION = "2.0";
        public static final String PROVISIONING_VERSION_2_0 = "2.0";
        public static final String PROVISIONING_VERSION_4_0 = "4.0";
        public static final String PROVISIONING_VERSION_5_0 = "5.0";
        public static final String RCS_VERSION = "6.0";
        public static final String TERMINAL_MODEL = BUILD.TERMINAL_MODEL;
        public static final String TERMINAL_SW_VERSION = BUILD.TERMINAL_SW_VERSION;
        public static final String TERMINAL_VENDOR = BUILD.TERMINAL_VENDOR;
        public static final String TOKEN = "";
        public static final String VERS = "0";
    }

    public static class TEMPLATE {
        public static final String USER_AGENT = "IM-client/OMA1.0 Samsung/%s-%s Samsung-RCS/%s";
        public static final String USER_AGENT_KOR = "TTA-RCS/1.0 %s/%s Device_Type/RCS_Android_Phone %s";
    }

    public static class URL {
        public static final String CONFIG_TEMPLATE = "http://config.rcs.mnc<MNC>.mcc<MCC>.pub.3gppnetwork.org";
        public static final String INTERNAL_CONFIG_PROXY_AUTHORITY = "http://127.0.0.1:1080/test/";
        public static final String INTERNAL_CONFIG_PROXY_TEMPLATE = "http://localhost:1080/cookie/mnc<MNC>/mcc<MCC>/";
        public static final String MCC_PNAME = "MCC";
        public static final String MCC_PVALUE = "<MCC>";
        public static final String MNC_PNAME = "MNC";
        public static final String MNC_PVALUE = "<MNC>";
    }

    public static class VALUE {
        public static final String EAP_AKA = "";
        public static final String EAP_AKA_CHALLENGE = "";
        public static final String IM_MAX_SIZE = "";
        public static final String IM_MAX_SIZE_1_TO_1 = "";
        public static final String INFO_COMPLETED = "false";
        public static final String NEXT_AUTOCONFIG_TIME = "";
        public static final String POLICY_SMS_PORT = "";
        public static final String RCS_DISABLED_STATE = "";
        public static final String RCS_DISABLED_STATE_FOR_VZW = "";
        public static final String RCS_STATE = "";
        public static final String RCS_STATE_FOR_VZW = "";
        public static final String SPG_PARAMS_URL = "";
        public static final String SPG_URL = "";
        public static final String START_MSISDN_TIMER = "0";
        public static final String TC_POPUP_USER_ACCEPT = "";
        public static final String TOKEN_TOKEN = "";
        public static final String VERS_VALIDITY = "0";
        public static final String VERS_VERSION = "0";
        public static final String VERS_VERSION_BACKUP = "0";
    }
}
