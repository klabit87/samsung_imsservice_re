package com.sec.internal.constants.ims;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal;

public class ImsConstants {
    public static String DOWNLOAD_CONFIG = "downloadconfig";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsConstants.class.getSimpleName();

    public static class CmcInfo {
        public static final String CMC_DUMMY_TEL_NUMBER = "99991111222";
    }

    public static class DeRegistrationCause {
        public static final int AUTOCONFIG_SMS_PUSH = 143;
        public static final int COUNTRY_CHANGED = 802;
        public static final int DATA_DEACTIVATED_DEFAULT = 3;
        public static final int DATA_DEACTIVATED_IMS = 4;
        public static final int DCN = 807;
        public static final int DEVICE_SHUT_DOWN = 13;
        public static final int ENTITLEMENT_FAILED = 144;
        public static final int EPDG_NOT_AVAILABLE = 124;
        public static final int FLIGHT_MODE_ON = 12;
        public static final int MOCK_LOCATION_UPDATED = 41;
        public static final int MULTI_USER_SWITCHED = 1000;
        public static final int NETWORK_MODE_CHANGED = 5;
        public static final int PHONE_CRASH = 6;
        public static final int SIM_NOT_AVAILABLE = 1;
        public static final int SIM_REFRESH_TIMEOUT = 42;
    }

    public static class EmergencyPdnPolicy {
        public static final int EPDN = 0;
        public static final int IMSPDN_IF_IPC_RAT_EPDG = 1;
    }

    public static class FtDlParams {
        public static final String FT_DL_CONV_ID = "ci";
        public static final String FT_DL_ID = "id";
        public static final String FT_DL_OTHER_PARTY = "op";
        public static final String FT_DL_URL = "url";
    }

    public static class Intents {
        public static final String ACTION_AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
        public static final String ACTION_AKA_CHALLENGE_COMPLETE = "com.sec.imsservice.AKA_CHALLENGE_COMPLETE";
        public static final String ACTION_AKA_CHALLENGE_FAILED = "com.sec.imsservice.AKA_CHALLENGE_FAILED";
        public static final String ACTION_CALL_STATE_CHANGED = "com.samsung.rcs.CALL_STATE_CHANGED";
        public static final String ACTION_DATAUSAGE_REACH_TO_LIMIT = "com.android.intent.action.DATAUSAGE_REACH_TO_LIMIT";
        public static final String ACTION_DCN_TRIGGERED = "com.samsung.intent.action.UPDATE_IMS_REGISTRATION";
        public static final String ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
        public static final String ACTION_DEVICE_STORAGE_FULL = "android.intent.action.DEVICE_STORAGE_FULL";
        public static final String ACTION_DEVICE_STORAGE_NOT_FULL = "android.intent.action.DEVICE_STORAGE_NOT_FULL";
        public static final String ACTION_DM_CHANGED = "com.samsung.ims.dm.DM_CHANGED";
        public static final String ACTION_DSAC_MODE_SWITCH = "android.ims.hvolte.MODE_SWITCH";
        public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
        public static final String ACTION_EMM_ERROR = "com.samsung.intent.action.EMM_ERROR";
        public static final String ACTION_FLIGHT_MODE = "com.sec.android.internal.ims.FLIGHT_MODE";
        public static final String ACTION_FLIGHT_MODE_BY_POWEROFF = "powerofftriggered";
        public static final String ACTION_IMS_ON_SIMLOADED = "com.samsung.ims.action.onsimloaded";
        public static final String ACTION_IMS_STATE = "com.samsung.ims.action.IMS_REGISTRATION";
        public static final String ACTION_PCO_INFO = "com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE";
        public static final String ACTION_PERIODIC_POLLING_TIMEOUT = "com.sec.internal.ims.imsservice.dm_polling_timeout";
        public static final String ACTION_REQUEST_AKA_CHALLENGE = "com.sec.imsservice.REQUEST_AKA_CHALLENGE";
        public static final String ACTION_RESET_NETWORK_SETTINGS = "com.samsung.intent.action.SETTINGS_NETWORK_RESET";
        public static final String ACTION_RESET_SETTINGS = "com.samsung.intent.action.SETTINGS_SOFT_RESET";
        public static final String ACTION_RETRYTIME_EXPIRED = "android.intent.action.retryTimeExpired";
        public static final String ACTION_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
        public static final String ACTION_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
        public static final String ACTION_SIM_ICCID_CHANGED = "com.samsung.action.SIM_ICCID_CHANGED";
        public static final String ACTION_SIM_ISIM_LOADED = "android.intent.action.ISIM_LOADED";
        public static final String ACTION_SIM_REFRESH = "com.android.intent.isim_refresh";
        public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
        public static final String ACTION_SMS_CALLBACK_MODE_CHANGED_INTERNAL = "com.samsung.intent.action.SMS_CALLBACK_MODE_CHANGED_INTERNAL";
        public static final String ACTION_SOFT_RESET = "com.samsung.intent.action.SETTINGS_SOFT_RESET";
        public static final String ACTION_T3396_EXPIRED = "android.intent.action.retrySetupData";
        public static final String ACTION_WFC_SWITCH_PROFILE = "action_wfc_switch_profile_broadcast";
        public static final String EXTRA_AIRPLANE_KEY = "state";
        public static final String EXTRA_ANDORID_PHONE_ID = "android:phone_id";
        public static final String EXTRA_APN_TYPE_KEY = "apnType";
        public static final String EXTRA_CALL_EVENT = "EXTRA_CALL_EVENT";
        public static final String EXTRA_CALL_IMAGE = "EXTRA_CALL_IMAGE";
        public static final String EXTRA_CALL_IMPORTANCE = "EXTRA_CALL_IMPORTANCE";
        public static final String EXTRA_CALL_LATITUDE = "EXTRA_CALL_LATITUDE";
        public static final String EXTRA_CALL_LONGITUDE = "EXTRA_CALL_LONGITUDE";
        public static final String EXTRA_CALL_RADIUS = "EXTRA_CALL_RADIUS";
        public static final String EXTRA_CALL_SUBJECT = "EXTRA_CALL_SUBJECT";
        public static final String EXTRA_CAUSE_KEY = "CAUSE";
        public static final String EXTRA_DCN_PHONE_ID = "phoneId";
        public static final String EXTRA_DSAC_MODE = "modeType";
        public static final String EXTRA_IS_CMC_CALL = "EXTRA_IS_CMC_CALL";
        public static final String EXTRA_IS_CMC_CONNECTED = "EXTRA_IS_CMC_CONNECTED";
        public static final String EXTRA_IS_INCOMING = "EXTRA_IS_INCOMING";
        public static final String EXTRA_LIMIT_POLICY = "policyData";
        public static final String EXTRA_PCO_VALUE_KEY = "pcoValue";
        public static final String EXTRA_PHONE_ID = "EXTRA_PHONE_ID";
        public static final String EXTRA_POWEROFF_TRIGGERED = "powerofftriggered";
        public static final String EXTRA_REGISTERED = "REGISTERED";
        public static final String EXTRA_REGISTERED_SERVICES = "SERVICE";
        public static final String EXTRA_REGI_PHONE_ID = "PHONE_ID";
        public static final String EXTRA_RESET_NETWORK_SUBID = "subId";
        public static final String EXTRA_SIMMOBILITY = "SIMMO";
        public static final String EXTRA_SIP_ERROR_CODE = "SIP_ERROR";
        public static final String EXTRA_SIP_ERROR_REASON = "ERROR_REASON";
        public static final String EXTRA_TEL_NUMBER = "EXTRA_TEL_NUMBER";
        public static final String EXTRA_UICC_MOBILITY_SPEC_VER = "IMS_CONFIG_UICC_MOBILITY_SPEC_VER";
        public static final String EXTRA_UPDATED_ITEM = "item";
        public static final String EXTRA_UPDATED_PHONE_ID = "phoneId";
        public static final String EXTRA_UPDATED_VALUE = "value";
        public static final String EXTRA_VOWIFI = "VOWIFI";
        public static final String EXTRA_WFC_REQUEST = "oem_request";
        public static final String INTENT_ACTION_LTE_REJECT = "android.intent.action.LTE_REJECT";
        public static final String INTENT_ACTION_REGIST_REJECT = "com.android.server.status.regist_reject";
    }

    public static class Packages {
        public static final String CLASS_NSDS = "com.sec.internal.vsim.ericssonnsds.NSDSService";
        public static final String CLASS_SIMMOBILITY_KIT_UPDATE = "com.samsung.ims.smk.DeviceUpdateService";
        public static final String PACKAGE_BIKE_MODE = "com.samsung.android.app.bikemode";
        public static final String PACKAGE_DM_CLIENT = "com.ims.dm";
        public static final String PACKAGE_MY_TMOBILE = "com.tmobile.pr.mytmobile";
        public static final String PACKAGE_QUALITY_DATALOG = "com.vzw.qualitydatalog";
        public static final String PACKAGE_SDM = "com.samsung.sdm";
        public static final String PACKAGE_SEC_MSG = "com.samsung.android.messaging";
        public static final String PACKAGE_SIMMOBILITY_KIT = "com.samsung.ims.smk";
        public static final String PACKAGE_VSIMSERVICE = "com.sec.vsimservice";
        public static final String SMK_PRELOADED_VERSION = "1.3.20";
    }

    public static class Phone {
        public static int SLOT_1 = 0;
        public static int SLOT_2 = 1;
    }

    public static class RCS_AS {
        public static final String CARRIER = "";
        public static final String INTEROP = "interop";
        public static final String JIBE = "jibe";
        public static final String SEC = "sec";
    }

    public static class SecFloatingFeatures {
        public static String CDMALESS = "SEC_FLOATING_FEATURE_COMMON_CDMALESS";
        public static String CONFIG_BRAND_NAME = "SEC_FLOATING_FEATURE_SETTINGS_CONFIG_BRAND_NAME";
        public static String CONFIG_DUAL_IMS = "SEC_FLOATING_FEATURE_COMMON_CONFIG_DUAL_IMS";
        public static String CONFIG_OMC_VERSION = "SEC_FLOATING_FEATURE_COMMON_CONFIG_OMC_VERSION";
        public static String CONFIG_PACKAGE_NAME = "SEC_FLOATING_FEATURE_MESSAGE_CONFIG_PACKAGE_NAME";
        public static String SIM_MOBILITY_ENABLED = "SEC_FLOATING_FEATURE_IMS_SUPPORT_SIM_MOBILITY";
        public static String SUPPORT_PTT = "SEC_FLOATING_FEATURE_COMMON_SUPPORT_PTT";
    }

    public static class ServiceType {
        public static final int RCS = 2;
        public static final int VIDEO = 1;
        public static final int VOICE = 0;
        public static final int VOWIFI = 3;
    }

    public static class SimMobilityKitTimer {
        public static final long BASIC_INTERVAL = 604800000;
        public static final String RETRY_INTERVAL = "com.samsung.ims.smk.retry_interval";
        public static final String START_TIMER = "com.samsung.ims.smk.smk_timer_start";
        public static final String TIMER_EXIST = "smk_timer_exist";
    }

    public static class SystemPath {
        public static final String EFS = "/efs/sec_efs/";
    }

    public static class SystemProperties {
        public static final String CARRIERFEATURE_FORCE_USE = "persist.ims.carrierfeature_force_use";
        public static final String CURRENT_PLMN = "ril.currentplmn";
        public static final String FIRST_API_VERSION = "ro.product.first_api_level";
        public static final String GCF_MODE_PROPERTY = "persist.ims.gcfmode";
        public static final String GCF_MODE_PROPERTY_P_OS = "persist.radio.gcfmode";
        public static final String IMSSWITCH_POLICY = "persist.ims.imsswitch";
        public static final String IMS_TEST_MODE_PROP = "persist.sys.ims_test_mode";
        public static final String LTE_VOICE_STATUS = "ril.lte.voice.status";
        public static final String PS_INDICATOR = "ril.ims.ltevoicesupport";
        public static final String SIMMOBILITY_ENABLE = "persist.ims.simmobility";
        public static final String SIM_STATE = "gsm.sim.state";
    }

    public static class Uris {
        public static final String AUTHORITY = "com.sec.ims.settings";
        public static final String CONFIG_URI = "content://com.sec.ims.settings";
        public static final String FRAGMENT_SIM_SLOT = "simslot";
        public static final Uri LINES_CONTENT_URI = Uri.parse("content://com.samsung.ims.nsds.provider/lines");
        public static final Uri MMS_PREFERENCE_PROVIDER_DATASAVER_URI = Uri.parse("content://com.android.mms.csc.PreferenceProvider/data_saver");
        public static final Uri MMS_PREFERENCE_PROVIDER_KEY_URI = Uri.parse("content://com.android.mms.csc.PreferenceProvider/key");
        public static final Uri RCS_PREFERENCE_PROVIDER_SUPPORT_DUAL_RCS = Uri.parse("content://com.sec.ims.android.rcs/support_dual_rcs");
        public static final Uri SETTINGS_PROVIDER_CARRIER_FEATURE_URI = Uri.parse("content://com.sec.ims.settings/carrier_feature_updated");
        public static final Uri SETTINGS_PROVIDER_DOWNLOAD_CONFIG_RESET_URI = Uri.parse("content://com.sec.ims.settings/resetconfig");
        public static final Uri SETTINGS_PROVIDER_DOWNLOAD_CONFIG_URI = Uri.parse("content://com.sec.ims.settings/downloadconfig");
        public static final Uri SETTINGS_PROVIDER_PROFILE_URI = Uri.parse("content://com.sec.ims.settings/profile");
        public static final Uri SETTINGS_PROVIDER_SIMMOBILITY_URI = Uri.parse("content://com.sec.ims.settings/simmobility");
        public static final Uri SMS_DEFAULT_APPLICATION_URI = Settings.Secure.getUriFor("sms_default_application");
        public static final Uri SMS_SETTING = Uri.parse("content://com.sec.ims.settings/sms_setting");
    }

    public static class VoiceDomainPrefEutran {
        public static final int CS_VOICE_ONLY = 1;
        public static final int CS_VOICE_PREFERRED = 2;
        public static final int IMS_PS_VOICE_ONLY = 4;
        public static final int IMS_PS_VOICE_PREFERRED = 3;
    }

    public static class SystemSettings {
        public static final SettingsItem AIRPLANE_MODE = new SettingsItem("settings", GLOBAL, "airplane_mode_on");
        public static int AIRPLANE_MODE_ON = 1;
        public static final String AUTHOTIRY_RCS_DM_CONFIG = "com.samsung.rcs.dmconfigurationprovider";
        public static final SettingsItem CARRIER_FEATURE_UPDATED = new SettingsItem(Uris.AUTHORITY, "", "carrier_feature_updated");
        public static final SettingsItem DATA_ROAMING = new SettingsItem("settings", GLOBAL, "data_roaming");
        public static int DATA_ROAMING_UNKNOWN = -1;
        public static final SettingsItem DEFAULT_SMS_APP = new SettingsItem("settings", SECURE, "sms_default_application");
        /* access modifiers changed from: private */
        public static String GLOBAL = "global";
        public static final SettingsItem IMS_DM_CONFIG = new SettingsItem(AUTHOTIRY_RCS_DM_CONFIG, IMS_OMADM, "*");
        public static final SettingsItem IMS_DOWNLOAD_CONFIG = new SettingsItem(Uris.AUTHORITY, "", "downloadconfig");
        public static final SettingsItem IMS_GLOBAL = new SettingsItem(Uris.AUTHORITY, GLOBAL, "");
        private static String IMS_NV = "nvstorage";
        public static final SettingsItem IMS_NV_STORAGE = new SettingsItem(Uris.AUTHORITY, IMS_NV, "*");
        private static String IMS_OMADM = "omadm/./3GPP_IMS";
        private static String IMS_PROFILE = "profile";
        public static final SettingsItem IMS_PROFILES = new SettingsItem(Uris.AUTHORITY, IMS_PROFILE, "*");
        public static final SettingsItem IMS_RESET_DOWNLOAD_CONFIG = new SettingsItem(Uris.AUTHORITY, "", "resetconfig");
        public static final SettingsItem IMS_SIM_MOBILITY = new SettingsItem(Uris.AUTHORITY, SIM_MOBILITY, "");
        private static String IMS_SWITCH = "imsswitch";
        public static final SettingsItem IMS_SWITCHES = new SettingsItem(Uris.AUTHORITY, IMS_SWITCH, "");
        private static String IMS_USER = "userconfig";
        public static final SettingsItem LOCATION_MODE = new SettingsItem("settings", SECURE, "location_providers_allowed");
        public static final SettingsItem LTE_DATA_MODE = new SettingsItem("settings", SECURE, IVolteServiceModuleInternal.LTE_DATA_NETWORK_MODE);
        public static int LTE_DATA_NETWORK_MODE_ENABLED = 1;
        public static final SettingsItem LTE_DATA_ROAMING = new SettingsItem("settings", SYSTEM, "lte_roaming_mode_on");
        public static int LTE_DATA_ROAMING_DISABLED = 0;
        public static final SettingsItem MNOMAP_UPDATED = new SettingsItem(Uris.AUTHORITY, "", "mnomap_updated");
        public static final SettingsItem MOBILE_DATA = new SettingsItem("settings", GLOBAL, "mobile_data");
        public static final SettingsItem MOBILE_DATA_PRESSED = new SettingsItem("settings", GLOBAL, "mobile_data_pressed");
        public static final SettingsItem PREFFERED_NETWORK_MODE = new SettingsItem("settings", GLOBAL, "preferred_network_mode");
        public static final SettingsItem PREFFERED_VOICE_CALL = new SettingsItem("settings", SYSTEM, "prefered_voice_call");
        public static final SettingsItem RCS_ALLOWED_URI = new SettingsItem("com.sec.knox.provider2", "PhoneRestrictionPolicy", "isRCSEnabled");
        public static final int RCS_DISABLED = 0;
        public static final int RCS_DISABLED_BY_NETWORK = -2;
        public static final int RCS_ENABLED = 1;
        public static final int RCS_ENABLED_BY_NETWORK = 3;
        public static final int RCS_NOTSET = -1;
        public static final SettingsItem RCS_ROAMING_PREF = new SettingsItem(Uris.AUTHORITY, IMS_USER, "");
        public static final int RCS_SETTING_NOT_FOUND = -3;
        public static final int RCS_TURNING_OFF = 2;
        public static final SettingsItem RCS_USER_SETTING1 = new SettingsItem("settings", SYSTEM, "rcs_user_setting");
        public static final SettingsItem RCS_USER_SETTING2 = new SettingsItem("settings", SYSTEM, "rcs_user_setting2");
        public static int ROAMING_DATA_ENABLED = 1;
        /* access modifiers changed from: private */
        public static String SECURE = "secure";
        public static final SettingsItem SETUP_WIZARD = new SettingsItem("settings", SECURE, "user_setup_complete");
        private static String SIM_MOBILITY = "simmobility";
        /* access modifiers changed from: private */
        public static String SYSTEM = "system";
        public static final SettingsItem USER_TOGGLED_VOLTE_SLOT1 = new SettingsItem("settings", SYSTEM, "voicecall_type_user_action");
        public static final SettingsItem USER_TOGGLED_VOLTE_SLOT2 = new SettingsItem("settings", SYSTEM, "voicecall_type_user_action2");
        public static final int VIDEO_DISABLED = 1;
        public static final int VIDEO_ENABLED = 0;
        public static final int VIDEO_UNKNOWN = -1;
        public static final SettingsItem VILTE_SLOT1 = new SettingsItem("settings", SYSTEM, "videocall_type");
        public static final SettingsItem VILTE_SLOT2 = new SettingsItem("settings", SYSTEM, "videocall_type2");
        public static final int VOICE_CS = 1;
        public static final int VOICE_UNKNOWN = -1;
        public static final int VOICE_VOLTE = 0;
        public static final SettingsItem VOLTE_PROVISIONING = new SettingsItem("settings", SYSTEM, "allow_volte_provisioning");
        public static final int VOLTE_PROVISIONING_DISABLED = 0;
        public static final int VOLTE_PROVISIONING_ENABLED = 1;
        public static final SettingsItem VOLTE_ROAMING = new SettingsItem("settings", GLOBAL, "hd_voice_roaming_enabled");
        public static int VOLTE_ROAMING_DISABLED = 0;
        public static int VOLTE_ROAMING_ENABLED = 1;
        public static int VOLTE_ROAMING_UNKNOWN = -1;
        public static final SettingsItem VOLTE_SLOT1 = new SettingsItem("settings", "system", "voicecall_type");
        public static final SettingsItem VOLTE_SLOT2 = new SettingsItem("settings", SYSTEM, "voicecall_type2");
        public static final SettingsItem WIFI_CALL_ENABLE1 = new SettingsItem("settings", SYSTEM, "wifi_call_enable1");
        public static final SettingsItem WIFI_CALL_ENABLE2 = new SettingsItem("settings", SYSTEM, "wifi_call_enable2");
        public static final SettingsItem WIFI_CALL_PREFERRED1 = new SettingsItem("settings", SYSTEM, "wifi_call_preferred1");
        public static final SettingsItem WIFI_CALL_PREFERRED2 = new SettingsItem("settings", SYSTEM, "wifi_call_preferred2");
        public static final SettingsItem WIFI_CALL_WHEN_ROAMING1 = new SettingsItem("settings", SYSTEM, "wifi_call_when_roaming1");
        public static final SettingsItem WIFI_CALL_WHEN_ROAMING2 = new SettingsItem("settings", SYSTEM, "wifi_call_when_roaming2");
        public static final SettingsItem WIFI_SETTING = new SettingsItem("settings", GLOBAL, "wifi_on");

        public static void addUri(UriMatcher matcher, SettingsItem uri, int event) {
            matcher.addURI(uri.getAuthority(), uri.getPath(), event);
        }

        public static int getVoiceCallType(Context context, int defaultValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                return VOLTE_SLOT1.get(context, defaultValue);
            }
            return VOLTE_SLOT2.get(context, defaultValue);
        }

        public static void setVoiceCallType(Context context, int newValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                if (VOLTE_SLOT1.get(context, -1) != newValue || newValue == -1) {
                    VOLTE_SLOT1.set(context, newValue);
                }
            } else if (VOLTE_SLOT2.get(context, -1) != newValue || newValue == -1) {
                VOLTE_SLOT2.set(context, newValue);
            }
        }

        public static void setVoiceCallTypeUserAction(Context context, int newValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                if (USER_TOGGLED_VOLTE_SLOT1.get(context, 0) != newValue) {
                    USER_TOGGLED_VOLTE_SLOT1.set(context, newValue);
                }
            } else if (USER_TOGGLED_VOLTE_SLOT2.get(context, 0) != newValue) {
                USER_TOGGLED_VOLTE_SLOT2.set(context, newValue);
            }
        }

        public static boolean isUserToggledVoiceCallType(Context context, int phoneId) {
            int isToggled;
            if (phoneId == Phone.SLOT_1) {
                isToggled = USER_TOGGLED_VOLTE_SLOT1.get(context, 0);
            } else {
                isToggled = USER_TOGGLED_VOLTE_SLOT2.get(context, 0);
            }
            if (isToggled == 1) {
                return true;
            }
            return false;
        }

        public static int getRcsUserSetting(Context context, int defaultValue, int phoneId) {
            return getSettingsItemByPhoneId(2, phoneId).get(context, defaultValue);
        }

        public static void setRcsUserSetting(Context context, int newValue, int phoneId) {
            SettingsItem si = getSettingsItemByPhoneId(2, phoneId);
            if (si.get(context, -1) != newValue || newValue == -1) {
                si.set(context, newValue);
            }
        }

        public static int getVideoCallType(Context context, int defaultValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                return VILTE_SLOT1.get(context, defaultValue);
            }
            return VILTE_SLOT2.get(context, defaultValue);
        }

        public static void setVideoCallType(Context context, int newValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                if (VILTE_SLOT1.get(context, -1) != newValue || newValue == -1) {
                    VILTE_SLOT1.set(context, newValue);
                }
            } else if (VILTE_SLOT2.get(context, -1) != newValue || newValue == -1) {
                VILTE_SLOT2.set(context, newValue);
            }
        }

        public static boolean isWiFiCallEnabled(Context context) {
            int wifiCallEnabled1 = getWiFiCallEnabled(context, -1, 0);
            int wifiCallEnabled2 = getWiFiCallEnabled(context, -1, 1);
            String access$000 = ImsConstants.LOG_TAG;
            Log.d(access$000, "isWiFiCallEnabled: wifi_call_enable [" + wifiCallEnabled1 + "], wifi_call_enable2 [" + wifiCallEnabled2 + "]");
            if (wifiCallEnabled1 == 1 || wifiCallEnabled2 == 1) {
                return true;
            }
            return false;
        }

        public static int getWiFiCallEnabled(Context context, int defaultValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                return WIFI_CALL_ENABLE1.get(context, defaultValue);
            }
            return WIFI_CALL_ENABLE2.get(context, defaultValue);
        }

        public static int getWiFiCallPreferred(Context context, int defaultValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                return WIFI_CALL_PREFERRED1.get(context, defaultValue);
            }
            return WIFI_CALL_PREFERRED2.get(context, defaultValue);
        }

        public static int getWiFiCallWhenRoaming(Context context, int defaultValue, int phoneId) {
            if (phoneId == Phone.SLOT_1) {
                return WIFI_CALL_WHEN_ROAMING1.get(context, defaultValue);
            }
            return WIFI_CALL_WHEN_ROAMING2.get(context, defaultValue);
        }

        public static void setWiFiCallEnabled(Context context, int phoneId, int value) {
            if (phoneId == Phone.SLOT_1) {
                WIFI_CALL_ENABLE1.set(context, value);
            } else if (phoneId == Phone.SLOT_2) {
                WIFI_CALL_ENABLE2.set(context, value);
            }
        }

        public static void setWiFiCallPreferred(Context context, int phoneId, int value) {
            if (phoneId == Phone.SLOT_1) {
                WIFI_CALL_PREFERRED1.set(context, value);
            } else if (phoneId == Phone.SLOT_2) {
                WIFI_CALL_PREFERRED2.set(context, value);
            }
        }

        public static void setWiFiCallWhenRoaming(Context context, int phoneId, int value) {
            if (phoneId == Phone.SLOT_1) {
                WIFI_CALL_WHEN_ROAMING1.set(context, value);
            } else if (phoneId == Phone.SLOT_2) {
                WIFI_CALL_WHEN_ROAMING2.set(context, value);
            }
        }

        public static SettingsItem getSettingsItemByPhoneId(int item, int phoneId) {
            if (item != 0) {
                if (item != 1) {
                    if (item != 2) {
                        if (item != 3) {
                            return null;
                        }
                        if (phoneId == 0) {
                            return WIFI_CALL_ENABLE1;
                        }
                        return WIFI_CALL_ENABLE2;
                    } else if (phoneId == 0) {
                        return RCS_USER_SETTING1;
                    } else {
                        return RCS_USER_SETTING2;
                    }
                } else if (phoneId == 0) {
                    return VILTE_SLOT1;
                } else {
                    return VILTE_SLOT2;
                }
            } else if (phoneId == 0) {
                return VOLTE_SLOT1;
            } else {
                return VOLTE_SLOT2;
            }
        }

        public static class SettingsItem {
            private String mAuthority = "";
            private String mCategory = "";
            private String mName = "";

            public SettingsItem(String authority, String category, String name) {
                this.mAuthority = authority;
                this.mCategory = category;
                this.mName = name;
            }

            public String getAuthority() {
                return this.mAuthority;
            }

            public String getName() {
                return this.mName;
            }

            public String getPath() {
                if (TextUtils.isEmpty(this.mName)) {
                    return this.mCategory;
                }
                return this.mCategory + "/" + this.mName;
            }

            public Uri getUri() {
                return Uri.parse(SSOContentProviderConstants.QUERY_SCHEME + this.mAuthority + "/" + getPath());
            }

            public int get(Context context, int defaultValue) {
                int value = defaultValue;
                try {
                    if (SystemSettings.SYSTEM.equalsIgnoreCase(this.mCategory)) {
                        value = Settings.System.getInt(context.getContentResolver(), this.mName);
                    } else if (SystemSettings.GLOBAL.equalsIgnoreCase(this.mCategory)) {
                        value = Settings.Global.getInt(context.getContentResolver(), this.mName);
                    } else if (SystemSettings.SECURE.equalsIgnoreCase(this.mCategory)) {
                        value = Settings.Secure.getInt(context.getContentResolver(), this.mName);
                    } else {
                        String access$000 = ImsConstants.LOG_TAG;
                        Log.e(access$000, "Unknown Category : " + this.mCategory);
                    }
                } catch (Settings.SettingNotFoundException e) {
                    String access$0002 = ImsConstants.LOG_TAG;
                    Log.d(access$0002, "SettingNotFound : " + getPath());
                }
                String access$0003 = ImsConstants.LOG_TAG;
                Log.d(access$0003, "getInt(" + getPath() + ") : " + value);
                return value;
            }

            public int getbySubId(Context context, int defaultValue, int subId) {
                int value = defaultValue;
                try {
                    if (SystemSettings.GLOBAL.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver = context.getContentResolver();
                        value = Settings.Global.getInt(contentResolver, this.mName + subId);
                    } else if (SystemSettings.SYSTEM.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver2 = context.getContentResolver();
                        value = Settings.System.getInt(contentResolver2, this.mName + subId);
                    } else if (SystemSettings.SECURE.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver3 = context.getContentResolver();
                        value = Settings.Secure.getInt(contentResolver3, this.mName + subId);
                    } else {
                        String access$000 = ImsConstants.LOG_TAG;
                        Log.e(access$000, "Unknown Category : " + this.mCategory);
                    }
                } catch (Settings.SettingNotFoundException e) {
                    String access$0002 = ImsConstants.LOG_TAG;
                    Log.d(access$0002, "SettingNotFound : " + getPath() + ",subId " + subId);
                }
                String access$0003 = ImsConstants.LOG_TAG;
                Log.d(access$0003, "getIntbySubId(" + getPath() + subId + ") : " + value);
                return value;
            }

            public void set(Context context, int value) {
                String access$000 = ImsConstants.LOG_TAG;
                Log.d(access$000, "setInt(" + getPath() + ") : " + value);
                if (SystemSettings.SYSTEM.equalsIgnoreCase(this.mCategory)) {
                    Settings.System.putInt(context.getContentResolver(), this.mName, value);
                } else if (SystemSettings.GLOBAL.equalsIgnoreCase(this.mCategory)) {
                    Settings.Global.putInt(context.getContentResolver(), this.mName, value);
                } else if (SystemSettings.SECURE.equalsIgnoreCase(this.mCategory)) {
                    Settings.Secure.putInt(context.getContentResolver(), this.mName, value);
                } else {
                    String access$0002 = ImsConstants.LOG_TAG;
                    Log.e(access$0002, "Unknown Category : " + this.mCategory);
                }
            }
        }
    }

    public static class OmaVersion {
        public static final String OMA_2_0 = "OMA2.0";
        public static final String OMA_2_1 = "OMA2.1";
        public static final String OMA_2_2 = "OMA2.2";

        private OmaVersion() {
        }
    }
}
