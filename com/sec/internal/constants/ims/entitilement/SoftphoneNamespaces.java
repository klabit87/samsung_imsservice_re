package com.sec.internal.constants.ims.entitilement;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;

public class SoftphoneNamespaces {
    public static final String ERROR = "error";
    public static final long[] mTimeoutType1 = {UtStateMachine.HTTP_READ_TIMEOUT_GCF, 5000, 20000};
    public static final long[] mTimeoutType2 = {3000, 7000, 25000};
    public static final long[] mTimeoutType3 = {7000, 15000, 35000};
    public static final long[] mTimeoutType4 = {5000, 15000, 45000};

    public static final class Intent {
        public static final String CATEGORY_ACTION = "com.samsung.softphone.category.ACTION";

        public static final class Action {
            public static final String ACCOUNT_DEREGISTERED = "com.samsung.softphone.action.ACCOUNT_DEREGISTERED";
            public static final String ACCOUNT_IDENTITY_RELEASED = "com.samsung.softphone.action.ACCOUNT_IDENTITY_RELEASED";
            public static final String ACCOUNT_IN_INTERNATIONAL = "com.samsung.softphone.action.ACCOUNT_IN_INTERNATIONAL";
            public static final String ACCOUNT_LOCATION_UNKNOWN = "com.samsung.softphone.action.ACCOUNT_LOCATION_UNKNOWN";
            public static final String ACCOUNT_LOGIN_COMPLETED = "com.samsung.softphone.action.ACCOUNT_LOGIN_COMPLETED";
            public static final String ACCOUNT_MISSING_E911 = "com.samsung.softphone.action.ACCOUNT_MISSING_E911";
            public static final String ACCOUNT_REGISTERED = "com.samsung.softphone.action.ACCOUNT_REGISTERED";
            public static final String ACCOUNT_REQUEST_LOGOUT = "com.samsung.softphone.action.ACCOUNT_REQUEST_LOGOUT";
        }

        public static final class Extras {
            public static final String ACCOUNT_ID = "account_id";
            public static final String MSISDN = "msisdn";
        }
    }

    public static final class SoftphoneAlarm {
        public static final String ACTION_REFRESH_IDENTITY = "refresh_identity";
        public static final String ACTION_REFRESH_TOKEN = "refresh_token";
        public static final String ACTION_RESEND_SMS = "resend_sms";
    }

    public static final class SoftphoneEvents {
        public static final int EVENT_ADD_E911_ADDRESS_DONE = 107;
        public static final int EVENT_AIRPLANE_MODE_ON = 1031;
        public static final int EVENT_GET_CALL_FORWARDING_INFO_DONE = 109;
        public static final int EVENT_GET_CALL_WAITING_INFO_DONE = 108;
        public static final int EVENT_IMS_DEREGISTERED = 1017;
        public static final int EVENT_IMS_REGISTERED = 1016;
        public static final int EVENT_LABEL_UPDATED = 1019;
        public static final int EVENT_LOGOUT = 1018;
        public static final int EVENT_NETWORK_CONNECTED = 1032;
        public static final int EVENT_OBTAIN_ACCESS_TOKEN_DONE = 100;
        public static final int EVENT_OBTAIN_IMS_IDENTIFIERS_DONE = 104;
        public static final int EVENT_OBTAIN_PD_COOKIES = 1020;
        public static final int EVENT_OBTAIN_PD_COOKIES_DONE = 1021;
        public static final int EVENT_OBTAIN_TERMS_CONDITIONS_DONE = 102;
        public static final int EVENT_OUT_OF_SERVICE = 1033;
        public static final int EVENT_PROVISION_ACCOUNT_DONE = 103;
        public static final int EVENT_REFRESH_TOKEN_DONE = 1015;
        public static final int EVENT_RELEASE_FOR_RELOGIN = 1029;
        public static final int EVENT_RELEASE_IMS_IDENTIFIERS_DONE = 105;
        public static final int EVENT_RELOGIN = 1028;
        public static final int EVENT_REQUEST_AKA_CHALLENGE_DONE = 1034;
        public static final int EVENT_RESERVE_IMS_IDENTIFIERS_DONE = 101;
        public static final int EVENT_RETRY_OBTAIN_ACCESS_TOKEN = 1027;
        public static final int EVENT_REVOKE_ACCESS_TOKEN_DONE = 1012;
        public static final int EVENT_REVOKE_REFRESH_TOKEN_DONE = 1013;
        public static final int EVENT_SEND_MESSAGE = 1022;
        public static final int EVENT_SEND_MESSAGE_DONE = 1023;
        public static final int EVENT_SET_CALL_FORWARDING_INFO_DONE = 1011;
        public static final int EVENT_SET_CALL_WAITING_INFO_DONE = 1010;
        public static final int EVENT_SHUTDOWN = 1024;
        public static final int EVENT_START_RELOGIN = 1030;
        public static final int EVENT_TRANSITION_TO_ACTIVATEDSTATE = 1038;
        public static final int EVENT_TRANSITION_TO_INITSTATE = 1036;
        public static final int EVENT_TRANSITION_TO_REDISTATE = 1035;
        public static final int EVENT_TRANSITION_TO_REFRESHSTATE = 1037;
        public static final int EVENT_TRY_REGISTER_FAIL = 1014;
        public static final int EVENT_USER_SWITCH = 1025;
        public static final int EVENT_USER_SWITCH_BACK = 1026;
        public static final int EVENT_VALIDATE_E911_ADDRESS_DONE = 106;
    }

    public static final class SoftphoneModels {
        public static final String A4S = "SM-T307U";
        public static final String A8 = "SM-T387AA";
        public static final String CHAGALL = "SAMSUNG-SM-T807A";
        public static final String DAVINCI = "SAMSUNG-SM-T817A";
        public static final String KLIMT = "SAMSUNG-SM-T707A";
        public static final String RENOIR = "SAMSUNG-SM-T377A";
        public static final String S2 = "SAMSUNG-SM-T818A";
        public static final String S4 = "SM-T837A";
        public static final String VIEW2 = "SM-T927A";
    }

    public static final class SoftphoneSettings {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String AKA_AUTH_PATH = "/softphone/v1/challengeKeys";
        public static final String ATTEMPT = "attempt";
        public static final int ATTEMPT_LIMIT = 6;
        public static final String CALL_FORWARDING_PATH = "/callHandlingFeatures/v1/communication-diversion";
        public static final String CALL_WAITING_PATH = "/callHandlingFeatures/v1/communication-waiting";
        public static final String CONFIRMED = "confirmed";
        public static final String E911ADDRESS_PATH = "/softphone/v1/locations";
        public static final String E911ADDRESS_VALIDATION_PATH = "/emergencyServices/v1/e911Locations";
        public static final String EMERGENCY_PATH = "/emergencyServices/v1/";
        public static final String ENCRYPTION_ALGORITHM = "AES";
        public static final String FEATURE_PATH = "/callHandlingFeatures/v1/";
        public static final long LONG_BACKOFF = 60000;
        public static final String MSIP_CLIENTID_PREFIX = "SCH_SCC_";
        public static final String MSIP_CLIENT_VERSION = "1.0";
        public static final String MSIP_ERROR_URL = "http://error.com";
        public static final String MSIP_MESSAGE_PATH = "/messaging/v0/outbound";
        public static final String MSIP_PROD_DOMAIN_NAME = "messagessd.att.net";
        public static final String MSIP_PROD_MESSAGE_HOST = "messagessd.att.net";
        public static final String MSIP_PROD_TOKEN_HOST = "tprodsmsx.att.net";
        public static final String MSIP_REDICRECT_URL = "http://good.com";
        public static final String MSIP_STAGE_DOMAIN_NAME = "messagessd.stage.att.net";
        public static final String MSIP_STAGE_MESSAGE_HOST = "messagessd.stage.att.net";
        public static final String MSIP_STAGE_TOKEN_HOST = "tstagesms.stage.att.net";
        public static final String MSIP_TOKEN_PATH = "/commonLogin/nxsEDAM/controller.do";
        public static final String OBTAIN_IDENTIFIERS_PATH = "/softphone/v1/identities?SoftphoneType:sip";
        public static final String PATH = "/softphone/v1/";
        public static final String PROD_APP_KEY_A4S = "dyp77kwaauqxx6aalgpjjq3ctoq1dzwk";
        public static final String PROD_APP_KEY_A8 = "oxqybwzqkuke37qbfxhemdphjn8fc4wr";
        public static final String PROD_APP_KEY_CHAGALL = "b3wq9r826cwqsr2pptuha65rsovyvogb";
        public static final String PROD_APP_KEY_DAVINCI = "x1o8hkhkc9a4s0j3wctd9vpvy4fsbopr";
        public static final String PROD_APP_KEY_KLIMT = "gbyqrqhf78wrpaf82qer2lnpivumunoc";
        public static final String PROD_APP_KEY_RENOIR = "up3fpnsojlca8uggywdwgkyaqgmnsb3h";
        public static final String PROD_APP_KEY_S2 = "ejmp2k1chenktmtoe0cg97rkwg8vlogm";
        public static final String PROD_APP_KEY_S4 = "mjoy9og9bd8bixjhxr1nannwgbalxudu";
        public static final String PROD_APP_KEY_VIEW2 = "nuri2epiz6mhrlidwqomhgxcywlqunzr";
        public static final String PROD_APP_SECRET_A4S = "tuqlgat1pdra8x1mjxdnawt7psgxzsgo";
        public static final String PROD_APP_SECRET_A8 = "hekgi2kbc21bvjpnkorin95iiog6c9ol";
        public static final String PROD_APP_SECRET_CHAGALL = "iu0rqlat1tzdpb2b39txmopketfooofo";
        public static final String PROD_APP_SECRET_DAVINCI = "rao5cogniww5it7mufx57xfbfrhsutmn";
        public static final String PROD_APP_SECRET_KLIMT = "ifnskvditiaasgwgfpaukrvx3eaqvh1r";
        public static final String PROD_APP_SECRET_RENOIR = "05wib7o909lwzlgjkvwad6kyr9photnb";
        public static final String PROD_APP_SECRET_S2 = "t9yzb6hlaxkersol9us5lamrf1iiru6a";
        public static final String PROD_APP_SECRET_S4 = "szyqu8oecuoikomhaooihdgbxrcjzfku";
        public static final String PROD_APP_SECRET_VIEW2 = "i6w0p4bjaf56pwuytlvwehrpajpgh4v8";
        public static final String PROD_HOST = "api.att.com";
        public static final String PROVISION_ACCOUNT_PATH = "/softphone/v1/account";
        public static final int REFRESH_TIMER_PERCENTAGE = 900;
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String RELEASE_IDENTIFIERS_PATH = "/softphone/v1/identities";
        public static final String RETRY_COUNT = "retry_count";
        public static final int RETRY_LIMIT = 3;
        public static final String REVOKE_TOKEN_PATH = "/oauth/v4/revoke";
        public static final String SCHEME = "https://";
        public static final String SCOPE = "SOFTPHONE CALLHANDLINGFEATURES EMERGENCYSERVICES";
        public static final int SHORT_ATTEMPT_LIMIT = 3;
        public static final String STAGE_APP_KEY = "inestcjvum7fuv9ssvy9phlq1kwgzsjg";
        public static final String STAGE_APP_SECRET = "mkitwyspqpeszb34moy6ai7brzjvz7tm";
        public static final String STAGE_HOST = "api-stage-numbersync.bf.sl.attcompute.com";
        public static final String TERMS_AND_CONDITIONS_PATH = "/softphone/v1/termsAndConditions?tcType=Url";
        public static final String TGUARD_MSIP_OPERATION = "SessionGen";
        public static final String TOKEN_PATH = "/oauth/v4/token";
    }

    public static final class SoftphoneSharedPref {
        public static final String LAST_SMS_TIME = "sms_time";
        public static final String PREF_ENVIRONMENT = "environment";
        public static final String PREF_FQDN = "fqdn";
        public static final String PREF_IMPI = "impi";
        public static final String PREF_IMPU = "impu";
        public static final String PREF_PD_COOKIES = "pd_cookies";
        public static final String PREF_TGUARD_APPID = "tguard_appid";
        public static final String PREF_TGUARD_TOKEN = "tguard_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SHARED_PREF_NAME = "softphone";
    }

    public static final class SoftphoneCallHandling {
        public static final String ACTIONS = "actions";
        public static final String ACTIVE = "active";
        public static final String COMMON_POLICY_NS_PREFIX = "cp";
        public static final String COMMUNICATION_DIVERSION = "communication-diversion";
        public static final String COMMUNICATION_WAITING = "communication-waiting";
        public static final String CONDITIONS = "conditions";
        public static final String DEACTIVATED = "rule-deactivated";
        public static final String FORWARD_TO = "forward-to";
        public static final String ID = "id";
        public static final String NO_REPLY_TIMER = "NoReplyTimer";
        public static final String RULE = "rule";
        public static final String RULESET = "ruleset";
        public static final String SUPPLEMENTARY_SERVICE_NS_PREFIX = "ss";
        public static final String TARGET = "target";
        public static final String XML_CHARSET = "UTF-8";
        public static final String XML_VERSION = "1.0";

        public static String getId(int condition) {
            if (condition == 0) {
                return "call-diversion-unconditional";
            }
            if (condition == 1) {
                return "call-diversion-busy";
            }
            if (condition == 2) {
                return "call-diversion-no-reply";
            }
            if (condition == 3) {
                return "call-diversion-not-reachable";
            }
            if (condition != 8) {
                return NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
            }
            return "call-diversion-not-logged-in";
        }

        public static String getCondition(int condition) {
            if (condition == 0) {
                return "unconditional";
            }
            if (condition == 1) {
                return "busy";
            }
            if (condition == 2) {
                return "no-answer";
            }
            if (condition == 3) {
                return "not-reachable";
            }
            if (condition != 8) {
                return NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
            }
            return "not-registered";
        }
    }
}
