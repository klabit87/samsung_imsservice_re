package com.sec.internal.ims.config;

import android.os.Build;
import com.sec.internal.constants.ims.config.ConfigConstants;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfigContract {
    public static final Map<String, String> PATH_TABLE;
    public static final Map<String, String> STORAGE_DEFAULT;

    public static class BUILD {
        public static String getTerminalModel() {
            return Build.MODEL;
        }

        public static String getTerminalSwVersion() {
            return Build.VERSION.INCREMENTAL;
        }
    }

    static {
        HashMap hashMap = new HashMap();
        STORAGE_DEFAULT = hashMap;
        hashMap.put("root/vers/version", "0");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.VERS_VERSION_BACKUP, "0");
        STORAGE_DEFAULT.put("root/vers/validity", "0");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.INFO_COMPLETED, ConfigConstants.VALUE.INFO_COMPLETED);
        STORAGE_DEFAULT.put(ConfigConstants.PATH.TC_POPUP_USER_ACCEPT, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.START_MSISDN_TIMER, "0");
        STORAGE_DEFAULT.put("root/token/token", "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.POLICY_SMS_PORT, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.EAP_AKA, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.EAP_AKA_CHALLENGE, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.SPG_URL, "");
        STORAGE_DEFAULT.put(ConfigConstants.PATH.SPG_PARAMS_URL, "");
        HashMap hashMap2 = new HashMap();
        PATH_TABLE = hashMap2;
        hashMap2.putAll(getPathTable(ConfigConstants.ConfigPath.VERS_PATH, "version"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.VERS_PATH, "validity"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TOKEN_PATH, "token"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MSISDN_PATH, ConfigConstants.ConfigTable.MSISDN_SKIP_COUNT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MSISDN_PATH, ConfigConstants.ConfigTable.MSISDN_MSGUI_DISPLAY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_MAX_SIZE_IMAGE_SHARE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, "maxtimevideoshare"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_Q_VALUE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_INT_URL_FORMAT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_NAT_URL_FORMAT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_RCS_VOLTE_SINGLE_REGISTRATION));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, "uuid_Value"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH, "UserName"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH, "UserPwd"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH, "realm"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_VERSIONS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_PROFILE_VERSIONS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_STATE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_PRESENCE_PRFL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_FT_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_SLM_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_GEOPULL_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_VS_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_IS_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_IPVOICECALL_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_IPVIDEOCALL_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_ALLOW_RCS_EXTENSIONS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_RCS_MESSAGING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_FILE_TRANSFER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_SMSOIP));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_MMS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_VOLTE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_IP_VIDEO));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_PROVISIONING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, ConfigConstants.ConfigTable.DATAOFF_SYNC));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE_PERIOD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_CAP_DISC_COMMON_STACK));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_MAX_ENTRIES_IN_LIST));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH, ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PRESENCE_PUBLISH_TIMER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PRESENCE_RLS_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH, ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH, ConfigConstants.ConfigTable.PRESENCE_CLIENT_OBJ_DATALIMIT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_MSG_TECH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_CAP_ALWAYS_ON));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_WARN_SF));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_GROUP_CHAT_FULL_STAND_FWD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_GROUP_CHAT_ONLY_F_STAND_FWD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_SMS_FALLBACK_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_CAP_NON_RCS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_WARN_IW));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_AUT_ACCEPT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_IM_SESSION_START));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FIRST_MSG_INVITE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_TIMER_IDLE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_CONCURRENT_SESSION));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MULTIMEDIA_CHAT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, "MaxSize"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_1));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_M));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_WARN_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR_INCOMING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_THUMB));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_ST_AND_FW_ENABLED));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_CAP_ALWAYS_ON));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_PRES_SRV_CAP));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_DEFERRED_MSG_FUNC_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, "max_adhoc_group_size"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_CONF_FCTY_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_EXPLODER_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MASS_FCTY_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_FT_WARN_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR_INCOMING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_MAX_ADHOC_OPEN_GROUP_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.IM_EXT_MAX_IMDN_AGGREGATION));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SLM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH, "MaxSize"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, "EventRpting"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, "SyncTimer"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, "DataConnectionSyncTimer"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, "SMSStore"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH, "MMSStore"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.OTHER_WARN_SIZE_IMAGE_SHARE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, "maxtimevideoshare"));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.OTHER_EXTENSIONS_MAX_MSRP_SIZE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.XDMS_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.XDMS_XCAP_ROOT_URI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_MEDIA));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_SIGNALLING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING_ROAMING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA_ROAMING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA_ROAMING));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_RT_MEDIA));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_MESSAGING_UX));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_USER_ALIAS_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_SPAM_NOTIFICATION_TEXT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_TOKEN_LINK_NOTIFICATION_TEXT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_UNAVAILABLE_ENDPOINT_TEXT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_VIDEO_AND_ENCALL_UX));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_IR51_SWITCH_UX));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_FT_FB_DEFAULT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_CALL_LOG_BEARER_DIFFER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.UX_ALLOW_ENRICHED_CHATBOT_SEARCH_DEFAULT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_CHATBOTDIRECTORY));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_BOTINFOFQDNROOT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_CHATBOTBLACKLIST));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_MSGHISTORYSELECTABLE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_SPECIFIC_CHATBOTS_LIST));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_IDENTITY_IN_ENRICHED_SEARCH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_URL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_NOTIF_URL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_AUTH));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_NAME));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_PWD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.PLUGINS_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.PLUGINS_CATALOGURI));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_FTHTTPGROUPCHAT));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_USER_NAME));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH, ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_PASSWORD));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_STATE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_PATH, ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH, ConfigConstants.ConfigTable.SPG_URL));
        PATH_TABLE.putAll(getPathTable(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH, ConfigConstants.ConfigTable.SPG_PARAMS_URL));
    }

    private static Map<String, String> getPathTable(String path, String param) {
        Map<String, String> ret = new HashMap<>();
        ret.put(param.toLowerCase(Locale.US), (path + param).toLowerCase(Locale.US));
        return ret;
    }
}
