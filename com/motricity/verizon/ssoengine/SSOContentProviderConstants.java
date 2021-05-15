package com.motricity.verizon.ssoengine;

public class SSOContentProviderConstants {
    public static final String APPTOKEN_PATH = "token";
    public static final String[] AUTHORITIES = {AUTHORITY, AUTHORITY_SVS};
    public static final String AUTHORITY = "com.verizon.loginclient";
    public static final String AUTHORITY_SVS = "com.verizon.services.loginclient";
    public static final String AUTHTOKEN_PATH = "authtoken";
    public static final String AUTHTOKEN_URI = "content://com.verizon.loginclient/authtoken";
    public static final String AUTHTOKEN_URI_SILENT = "content://com.verizon.loginclient/authtoken/silent";
    public static final String AUTHTOKEN_URI_SVCS = "content://com.verizon.services.loginclient/authtoken";
    public static final String AUTHTOKEN_URI_SVCS_SILENT = "content://com.verizon.services.loginclient/authtoken/silent";
    public static final String CONTENT_URI = "content://com.verizon.loginclient/token";
    public static final String CONTENT_URI_SILENT = "content://com.verizon.loginclient/token/silent";
    public static final String CONTENT_URI_SVCS = "content://com.verizon.services.loginclient/token";
    public static final String CONTENT_URI_SVCS_SILENT = "content://com.verizon.services.loginclient/token/silent";
    public static final String IDENTITY_URI = "content://com.verizon.loginclient/identity";
    public static final String IDENTITY_URI_SILENT = "content://com.verizon.loginclient/identity/silent";
    public static final String IDENTITY_URI_SVCS = "content://com.verizon.services.loginclient/identity";
    public static final String IDENTITY_URI_SVCS_SILENT = "content://com.verizon.services.loginclient/identity/silent";
    public static final String IDEN_PATH = "identity";
    public static final String[] LTE_FEATURES = {"com.verizon.hardware.telephony.lte", "com.verizon.hardware.telephony.ehrpd", "com.vzw.telephony.lte", "com.vzw.telephony.ehrpd"};
    public static final String[] OFFICIAL_PACKAGES = {"com.motricity.verizon.ssodownloadable", "com.motricity.verizon.ssoengine", "com.verizon.mips.services", "com.verizon.loginengine.unbranded"};
    public static final String QUERY_SCHEME = "content://";
    public static final String SILENT_PATH_SUFFIX = "/silent";
    public static final String TOKEN = "token";

    public static class ResultFields {
        public static final String IMEI = "imei";
        public static final String IMSI = "imsi";
        public static final String MDN = "mdn";
        public static final String SIGNATURE = "signature";
        public static final String SIGNATURE_CREATE_TIME = "signatureCreate";
        public static final String SIGNATURE_EXPIRE_TIME = "signatureExpire";
        public static final String SUBSCRIPTION_ID = "subscriptionId";
        public static final String TID = "tid";
        public static final String TOKEN = "token";
    }

    public static class SelectParams {
        public static final String ALWAYS_RETURN_SUBID = "alwaysReturnSubscriptionId";
        public static final String DELETE_ALL = "deleteAll";
        public static final String SUBSCRIPTION_ID = "subscriptionId";
    }
}
