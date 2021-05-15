package com.sec.internal.constants.ims.settings;

import android.net.Uri;

public class GlobalSettingsConstants {
    public static final Uri CONTENT_URI = Uri.parse("content://com.sec.ims.settings/global");
    public static final String LOG_TAG = "GlobalSettingsConstants";
    public static final String NAME = "mnoname";

    public static class Call {
        public static final String ALLOW_RELEASE_WFC_BEFORE_HO = "allow_release_wfc_before_ho";
        public static final String ALL_CSFB_ERROR_CODE_LIST = "all_csfb_error_code_list";
        public static final String ALL_VOLTE_RETRY_ERROR_CODE_LIST = "all_volte_retry_error_code_list";
        public static final String E911_CSFB_ERROR_CODE_LIST = "e911_csfb_error_code_list";
        public static final String E911_PDN_SELECTION_VOWIFI = "e911_pdn_selection_vowifi";
        public static final String ECC_CATEGORY_LIST = "ecc_category_list";
        public static final String ECC_CATEGORY_LIST_CDMA = "ecc_category_list_cdma";
        public static final String EMERGENCY_CALL_DOMAIN = "emergency_domain_setting";
        public static final String EMERGENCY_CALL_DOMAIN_WITHOUT_SIM = "no_sim_emergency_domain_setting";
        public static final String IGNORE_DISPLAY_NAME = "ignore_display_name";
        public static final String IS_SERVER_HEADER_ENABLED = "is_server_header_enabled";
        public static final String SRVCC_VERSION = "srvcc_version";
        public static final String T_LOCATION_ACQUIRE_FAIL = "t_location_acquire_fail";
        public static final String T_LTE_911_FAIL = "t_lte_911_fail";
        public static final String T_VALID_LOCATION_TIME = "t_valid_location_time";
        public static final String USSD_DOMAIN = "ussd_domain_setting";
        public static final String VIDEO_CSFB_ERROR_CODE_LIST = "video_csfb_error_code_list";
        public static final String VIDEO_VOLTE_RETRY_ERROR_CODE_LIST = "video_volte_retry_error_code_list";
        public static final String VOICE_CSFB_ERROR_CODE_LIST = "voice_csfb_error_code_list";
        public static final String VOWIFI_NEED_LOCATION_MENU = "vowifi_need_location_menu";
    }

    public static class Entitlement {
        public static final String CHANNEL_NAME = "channel_name";
        public static final String DEVICE_GROUP = "device_group";
        public static final String SUPPORT_CONFIGSERVER = "support_configserver";
        public static final String WFC_AUTO_ON = "wfc_auto_on";
    }

    public static class RCS {
        public static final String AGGR_IMDN_SUPPORTED = "aggr_imdn_supported";
        public static final String ALT_PROVISIONING_VERSION = "alt_provisioning_versions";
        public static final String APPLICATION_SERVER = "rcs_application_server";
        public static final String AUTO_ACCEPT_GROUP_CHAT = "auto_accept_group_chat";
        public static final String AUTO_CONFIG_PDN = "rcs_auto_config_pdn";
        public static final String BOT_SERVICE_ID_PREFIX_LIST = "bot_service_id_prefix_list";
        public static final String CAPABILITY_DISCOVERY_MECH = "capdisc_mech";
        public static final String CHAT_AUTH = "chat_auth";
        public static final String CONF_SUBSCRIBE_ENABLED = "conf_subscribe_enabled";
        public static final String CONTACT_SYNC_IN_SWITCH_OFF = "rcs_contact_sync_in_switch_off";
        public static final String CUSTOM_CONFIG_SERVER_URL = "rcs_custom_config_server_url";
        public static final String ENABLE_DEFAULT_SMS_FALLBACK = "enable_default_sms_fallback";
        public static final String ENABLE_FT_AUTO_RESUMABLE = "enable_ft_auto_resumable";
        public static final String ENABLE_GROUP_CHAT_LIST_RETRIEVE = "enable_group_chat_list_retrieve";
        public static final String ENABLE_RCS_EXTENSIONS = "enable_rcs_extensions";
        public static final String FTHTTP_OVER_DEFAULT_PDN = "fthttp_over_default_pdn";
        public static final String FTHTTP_TRUST_ALL_CERTS = "fthttp_trust_all_certs";
        public static final String FT_CANCEL_MEMORY_FULL = "ft_cancel_memory_full";
        public static final String FT_CHUNK_SIZE = "rcs_ft_chunk_size";
        public static final String FT_DEFAULT_MECH = "ft_default_mech";
        public static final String FT_FALLBACK_ALL_FAIL = "ft_fallback_all_fail";
        public static final String FULL_SF_GROUP_CHAT = "full_sf_group_chat";
        public static final String GEOPUSH_AUTH = "geopush_auth";
        public static final String GROUP_CHAT_AUTH = "group_chat_auth";
        public static final String IM_SESSION_TIMER = "im_session_timer";
        public static final String ISH_CHUNK_SIZE = "rcs_ish_chunk_size";
        public static final String LEGACY_LATCHING = "legacy_latching";
        public static final String LOCAL_CONFIG_SERVER = "rcs_local_config_server";
        public static final String MAX_1TO_MANY_RECIPIENTS = "max_1to_many_recipients";
        public static final String MAX_ADHOC_GROUP_SIZE = "max_adhoc_group_size";
        public static final String MESSAGING_UX = "messaging_ux";
        public static final String MODEL_NAME = "rcs_model_name";
        public static final String MSRP_CEMA = "msrp_cema";
        public static final String MSRP_DISCARD_PORT = "msrp_discard_port";
        public static final String NETWORK_TYPE = "rcs_network_type";
        public static final String OTP_SMS_PORT = "rcs_otp_sms_port";
        public static final String OTP_SMS_TYPE = "rcs_otp_sms_type";
        public static final String PAGER_MODE_SIZE_LIMIT = "pager_mode_size_limit";
        public static final String PRE_CONSENT = "rcs_pre_consent";
        public static final String RCS_APP_LIST = "rcs_app_list";
        public static final String RCS_CARRIER_LIST = "rcs_carrier_list";
        public static final String RCS_CLIENT_VERSION = "rcs_client_version";
        public static final String RCS_DEFAULT_DISABLE_INITIAL_SCAN = "rcs_default_disable_initial_scan";
        public static final String RCS_DEFAULT_ENABLED = "rcs_default_enabled";
        public static final String RCS_FORCE_DISABLE_INITIAL_SCAN = "rcs_force_disable_initial_scan";
        public static final String RCS_INITIAL_CONTACT_SYNC_BEFORE_REGI = "rcs_initial_contact_sync_before_regi";
        public static final String RCS_OPEN_LIST_FOR_EUR = "rcs_open_list_for_eur";
        public static final String RCS_OPEN_SWITCH_FOR_EUR = "rcs_open_switch_for_eur";
        public static final String RCS_PHASE_VERSION = "rcs_phase_version";
        public static final String RCS_PROVISIONING_VERSION = "rcs_provisioning_version";
        public static final String RCS_SUPPORT_EXPONENTIAL_CAPINFOEXPIRY = "rcs_support_exponential_capinfoexpiry";
        public static final String RCS_SUPPORT_EXPONENTIAL_RETRY_ACS = "rcs_support_exponential_retry_acs";
        public static final String RCS_SUPPORT_MVS_AUTH = "rcs_support_mvs_auth";
        public static final String RCS_VERSION = "rcs_version";
        public static final String RECONNECT_GUARD_TIMER = "reconnect_guard_timer";
        public static final String REGEX_TO_HIDE = "rcs_regex_to_hide";
        public static final String SHOW_MAIN_SWITCH = "rcs_show_main_switch";
        public static final String SHOW_REGI_ICON = "show_rcs_regi_icon";
        public static final String SMS_DEST_PORT = "sms_dest_port";
        public static final String STANDALONE_MSG_AUTH = "standalone_msg_auth";
        public static final String SUPPORT_CHAT_ON_DEFAULT_MMS_APP = "support_chat_on_default_mms_app";
        public static final String SUPPORT_NOTIFICATION_FOR_TNC = "support_notification_for_TnC";
        public static final String UP_PROFILE = "rcs_up_profile";
        public static final String USER_ALIAS_AUTH = "user_alias_auth";
    }

    public static class Registration {
        public static final String BLOCK_REGI_ON_INVALID_ISIM = "block_regi_on_invalid_isim";
        public static final String DEFAULT_RCS_VOLTE_REGISTRATION = "default_rcs_volte_registration";
        public static final String EXTENDED_SERVICES = "services";
        public static final String IMS_ENABLED = "ims_enabled";
        public static final String IWLAN_PANI_FORMAT = "iwlan_pani_format";
        public static final String KEEP_MSISDN_VALIDATION = "keep_msisdn_validation";
        public static final String PDN_FAIL_REASON_LIST = "pdn_fail_reason_list";
        public static final String REMOVE_ICON_NOSVC = "remove_icon_nosvc";
        public static final String SHOW_REGI_INFO_IN_SEC_SETTINGS = "show_regi_info_in_sec_settings";
        public static final String SHOW_VOLTE_REGI_ICON = "show_volte_regi_icon";
        public static final String SHOW_VOWIFI_REGI_ICON = "show_vowifi_regi_icon";
        public static final String SMS_OVER_IP_INDICATION = "sms_over_ip_indication";
        public static final String SUPPORT_VOWIFI = "support_vowifi";
        public static final String USER_AGENT = "user_agent";
        public static final String USE_USIM_ON_INVALID_ISIM = "use_usim_on_invalid_isim";
        public static final String VIDEO_DEFAULT_ENABLED = "vt_default_enabled";
        public static final String VOICE_DOMAIN_PREF_EUTRAN = "voice_domain_pref_eutran";
        public static final String VOLTE_DOMESTIC_DEFAULT_ENABLED = "volte_domestic_default_enabled";
        public static final String VOLTE_ICON = "volte_regi_icon_id";
        public static final String VOLTE_INTERNATIONAL_DEFAULT_ENABLED = "volte_international_default_enabled";
        public static final String VOWIFI_ICON = "vowifi_regi_icon_id";
        public static final String VOWIFI_OPERATOR_LABEL = "vowifi_operator_label";
        public static final String VOWIFI_OPERATOR_LABEL_ONGOING = "vowifi_operator_label_ongoing";
        public static final String VOWIFI_SUBTEXT_ON_LOCKSCREEN = "vowifi_subtext_on_lockscreen";
    }

    public static class SS {
        public static final String APN_SELECTION = "apn_selection";
        public static final String AUTH_PROXY_IP = "auth_proxy_ip";
        public static final String AUTH_PROXY_PORT = "auth_proxy_port";
        public static final String BSF_IP = "bsf_ip";
        public static final String BSF_PORT = "bsf_port";
        public static final String CALLBARRING_BY_NETWORK = "ss_callbarring_by_network";
        public static final String CALLWAITING_BY_NETWORK = "ss_callwaiting_by_network";
        public static final String CB_SELECT_MODE = "ss_cb_select_mode";
        public static final String CF_BUSY_RULEID = "ss_cf_busy_ruleid";
        public static final String CF_NOT_LOGGED_IN_RULEID = "ss_cf_not_logged_in_ruleid";
        public static final String CF_NOT_REACHABLE_RULEID = "ss_cf_not_reachable_ruleid";
        public static final String CF_NO_ANSWER_RULEID = "ss_cf_no_answer_ruleId";
        public static final String CF_SET_ALL_MEDIA = "ss_cf_set_all_media";
        public static final String CF_UNCONDITIONAL_RULEID = "ss_cf_unconditional_ruleid";
        public static final String CF_URI_TYPE = "ss_cf_uri_type";
        public static final String CLIP_CLIR_BY_NETWORK = "ss_clip_clir_by_network";
        public static final String CONTROL_PREF = "ss_control_pref";
        public static final String CSFB_WITH_IMSERROR = "ss_csfb_with_imserror";
        public static final String DELAY_DISCONNECT_XCAP_PDN = "ss_delay_disconnect_xcap_pdn";
        public static final String DISCONNECT_XCAP_PDN = "ss_disconnect_xcap_pdn";
        public static final String DOMAIN = "ss_domain_setting";
        public static final String ENABLE_GBA = "enable_gba";
        public static final String ENABLE_IN_ROAMING = "ss_enable_in_roaming";
        public static final String ERROR403_CSFB_UNTIL_REBOOT = "ss_403_csfb_until_reboot";
        public static final String ERROR_MSG_DISPLAY = "ss_error_msg_display";
        public static final String HTTP_PASSWORD = "http_password";
        public static final String HTTP_USERNAME = "http_username";
        public static final String ICB_ANONYMOUS_RULEID = "ss_icb_anonymous_ruleid";
        public static final String ICB_ROAMING_RULEID = "ss_icb_roaming_ruleid";
        public static final String ICB_UNCONDITIONAL_RULEID = "ss_icb_unconditional_ruleid";
        public static final String INSERT_NEW_RULE = "ss_insert_new_rule";
        public static final String IS_NEED_GET_FIRST = "ss_is_need_get_first";
        public static final String MEDIA_TYPE = "ss_support_media_type";
        public static final String NEED_SEPERATE_CFA = "ss_need_seperate_CFA";
        public static final String NEED_SEPERATE_CFNL = "ss_need_seperate_CFNL";
        public static final String NEED_SEPERATE_CFNRY = "ss_need_seperate_CFNRy";
        public static final String NO_MEDIA_FOR_CB = "ss_no_media_for_CB";
        public static final String SELECT_IP_VERSION = "ss_select_ip_version";
        public static final String SELECT_MODE = "ss_select_mode";
        public static final String SUPPORT_ALTERNATIVE_MEDIA_FOR_CB = "ss_support_alternative_media_for_cb";
        public static final String SUPPORT_SIMSERVS_RETRY = "ss_support_simservs_retry";
        public static final String SUPPORT_SS_ELEMENT = "ss_support_ss_element";
        public static final String SUPPORT_TLS = "ss_support_tls";
        public static final String XCAP_ROOT_URI = "xcap_root_uri";
        public static final String XCAP_ROOT_URI_PREF = "xcap_root_uri_pref";
        public static final String XDM_USER_AGENT = "xdm_user_agent";
        public static final String XDM_USER_ID_DOMAIN = "xdm_user_id_domain";
    }
}
