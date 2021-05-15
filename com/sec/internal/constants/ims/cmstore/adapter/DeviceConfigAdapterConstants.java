package com.sec.internal.constants.ims.cmstore.adapter;

public class DeviceConfigAdapterConstants {
    public static final String DEVICE_CONFIG = "device_config";
    public static final String FAX_API_VERSION = "faxApiVersion";

    public static class TmoFax {
        public static final String ROOT_URL = "rootURL";
        public static final String SERVICE_NAME = "serviceName";
    }

    public static class TmoMstoreServerValues {
        public static final String AKA_URL = "AKAUrl";
        public static final String API_VERSION = "apiVersion";
        public static final String AUTH_PROT = "AuthProt";
        public static final String DATA_CONNECTION_SYNC_TIMER = "DataConnectionSyncTimer";
        public static final String DISABLE_DIRECTION_HEADER = "disableDirectionHeader";
        public static final String EVENT_RPTING = "EventRpting";
        public static final String FOLDER_ID = "FolderID";
        public static final String MAX_BULK_DELETE = "MaxBulkDelete";
        public static final String MAX_SEARCH = "MaxSearch";
        public static final String MMS_STORE = "MMSStore";
        public static final String PUSH_SYNC_DELAY = "PushSyncDelay";
        public static final String SERVER_ROOT = "serverRoot";
        public static final String SIT_URL = "SiTUrl";
        public static final String SMS_STORE = "SMSStore";
        public static final String STORE_NAME = "storeName";
        public static final String SYNC_FROM_DAYS = "SyncFromDays";
        public static final String SYNC_TIMER = "SyncTimer";
        public static final String USER_NAME = "UserName";
        public static final String USER_PWD = "UserPwd";
        public static final String WSG_URI = "WSG_URI";

        public static class TmoFolderId {
            public static final String CALL_HISTORY = "CallHistory";
            public static final String MEDIA_FAX = "Media/Fax";
            public static final String RCS_MESSAGE_STORE = "RCSMessageStore";
            public static final String VM_GREETINGS = "VV-Mail/Greetings";
            public static final String VM_INBOX = "VV-Mail/Inbox";
        }

        public static class TmoSyncFromDays {
            public static final String CALL_LOG = "CallLog";
            public static final String FAX = "FAX";
            public static final String MESSAGES = "Message";
            public static final String VVM = "VVM";
            public static final String VVM_GREETING = "VVMGreeting";
        }
    }
}
