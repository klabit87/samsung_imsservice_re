package com.sec.internal.ims.servicemodules.gls;

public interface GlsIntent {
    public static final String CATEGORY_ACTION = "com.samsung.rcs.framework.geolocationshare.category.ACTION";
    public static final String CATEGORY_NOTIFICATION = "com.samsung.rcs.framework.geolocationshare.category.NOTIFICATION";

    public interface Actions {

        public interface RequestIntents {
            public static final String ACCEPT_SHARE_LOCATION_INCALL = "com.samsung.rcs.framework.geolocationshare.action.ACCEPT_SHARE_LOCATION_INCALL";
            public static final String CREATE_SHARE_LOCATION_INCALL = "com.samsung.rcs.framework.geolocationshare.action.CREATE_SHARE_LOCATION_INCALL";
            public static final String CREATE_SHARE_LOCATION_INCALL_GC = "com.samsung.rcs.framework.geolocationshare.action.CREATE_SHARE_LOCATION_INCALL_GC";
            public static final String REJECT_SHARE_LOCATION_INCALL = "com.samsung.rcs.framework.geolocationshare.action.REJECT_SHARE_LOCATION_INCALL";
            public static final String SHARE_LOCATION_IN_CHAT = "com.samsung.rcs.framework.geolocationshare.action.SHARE_LOCATION_IN_CHAT";
            public static final String SHARE_LOCATION_IN_CHAT_GC = "com.samsung.rcs.framework.geolocationshare.action.SHARE_LOCATION_IN_CHAT_GC";
            public static final String START_SHARE_LOCATION_INCALL = "com.samsung.rcs.framework.geolocationshare.action.START_SHARE_LOCATION_INCALL";
        }

        public interface ResponseIntents {
            public static final String ACCEPT_LOCATION_SHARE_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.ACCEPT_LOCATION_SHARE_INCALL_RESPONSE";
            public static final String CANCEL_LOCATION_SHARE_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.CANCEL_LOCATION_SHARE_INCALL_RESPONSE";
            public static final String CREATE_SHARE_LOCATION_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.CREATE_SHARE_LOCATION_INCALL_RESPONSE";
            public static final String DELETE_ALL_LOCATION_SHARE_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.DELETE_ALL_LOCATION_SHARE_INCALL_RESPONSE";
            public static final String INCOMING_LOCATION_SHARE_INCALL_INVITATION = "com.samsung.rcs.framework.geolocationshare.action.INCOMING_LOCATION_SHARE_INCALL_INVITATION";
            public static final String RECEIVE_LOCATION_NOTIFICATION_STATUS = "com.samsung.rcs.framework.geolocationshare.action.RECEIVE_LOCATION_NOTIFICATION_STATUS";
            public static final String RECEIVE_LOCATION_SHARE_MESSAGE = "com.samsung.rcs.framework.geolocationshare.action.RECEIVE_LOCATION_SHARE_MESSAGE";
            public static final String RECEIVE_SHARE_LOCATION_IN_CHAT_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.RECEIVE_SHARE_LOCATION_IN_CHAT_RESPONSE";
            public static final String REJECT_LOCATION_SHARE_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.REJECT_LOCATION_SHARE_INCALL_RESPONSE";
            public static final String SENT_LOCATION_SHARE_MESSAGE = "com.samsung.rcs.framework.geolocationshare.action.SENT_LOCATION_SHARE_MESSAGE";
            public static final String SHARE_LOCATION_IN_CHAT_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.SHARE_LOCATION_IN_CHAT_RESPONSE";
            public static final String START_SHARE_LOCATION_INCALL_RESPONSE = "com.samsung.rcs.framework.geolocationshare.action.START_SHARE_LOCATION_INCALL_RESPONSE";
        }
    }

    public interface Extras {
        public static final String EXTRA_CHAT_ID = "chat_id";
        public static final String EXTRA_CONTACT_URI = "contactUri";
        public static final String EXTRA_DISPOSITION_NOTIFICATION = "disposition_notification";
        public static final String EXTRA_IS_GROUP_CHAT = "is_group_chat";
        public static final String EXTRA_LABEL = "label";
        public static final String EXTRA_LOCATION = "location";
        public static final String EXTRA_LOCATION_LINK = "locationLink";
        public static final String EXTRA_LOCATION_TYPE = "location_type";
        public static final String EXTRA_MAAP_TRAFFIC_TYPE = "maap_traffic_type";
        public static final String EXTRA_MESSAGE_ID = "message_id";
        public static final String EXTRA_MESSAGE_NOTIFICATION_STATUS = "message_notification_status";
        public static final String EXTRA_REQUEST_MESSAGE_ID = "request_message_id";
        public static final String EXTRA_REQUEST_SUCCESS = "response_status";
        public static final String EXTRA_SESSION_DIRECTION = "sessionDirection";
        public static final String EXTRA_SESSION_ID = "sessionId";
        public static final String IS_PUBLICACCOUNT_MSG = "is_publicAccountMsg";
        public static final String SIM_SLOT_ID = "sim_slot_id";
    }
}
