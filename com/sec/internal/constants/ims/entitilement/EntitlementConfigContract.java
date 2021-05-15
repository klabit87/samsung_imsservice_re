package com.sec.internal.constants.ims.entitilement;

import android.content.ContentUris;
import android.net.Uri;

public class EntitlementConfigContract {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.samsung.ims.entitlementconfig.provider");
    public static final int BINDING_SERVICE = 8;
    public static final int DEVICE_CONFIG = 1;
    public static final int ENTITLEMENT_URL = 6;
    public static final int FORCE_UPDATE = 5;
    public static final int NSDS_JANKSY_CONFIG = 2;
    public static final int NSDS_XPATH_EXPR = 3;
    public static final String PROVIDER_NAME = "com.samsung.ims.entitlementconfig.provider";
    public static final int RCS_CONFIG = 4;
    public static final int RECONNECT_DB = 7;

    interface CommonColumns {
        public static final String ID = "_id";
    }

    interface DeviceConfigColumns {
        public static final String BACKUP_VERSION = "backup_version";
        public static final String COMPLETED = "completed";
        public static final String DEVICE_CONFIG = "device_config";
        public static final String IMSI = "imsi";
        public static final String NEXT_AUTO_CONFIG_TIME = "next_config_time";
        public static final String TC_POPUP_USER_ACCEPT = "tc_popup_user_accept";
        public static final String TOKEN = "token";
        public static final String VALIDITY = "validity";
        public static final String VERSION = "version";
    }

    public static final class DeviceConfig implements CommonColumns, DeviceConfigColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(EntitlementConfigContract.AUTHORITY_URI, "config");
        public static final String ELEMENT_NAME = "element_name";
        public static final String ELEMENT_VLAUE = "element_value";
        public static final String QUERY_PARAM_ENTITLEMENT_URI = "entitlement_url";
        public static final String QUERY_PARAM_TAG_NAME = "tag_name";
        public static final String XML_CONFIG = "xml_config";

        private DeviceConfig() {
        }

        public static final Uri buildDeviceConfigUri(long rowId) {
            return ContentUris.withAppendedId(CONTENT_URI, rowId);
        }

        public static final Uri buildXPathExprUri(String xPathSelection) {
            Uri.Builder builder = Uri.withAppendedPath(CONTENT_URI, "xpath").buildUpon();
            builder.appendQueryParameter("tag_name", xPathSelection);
            return builder.build();
        }
    }
}
