package com.sec.internal.ims.servicemodules.euc.test;

public interface EucTestIntent {
    public static final String CATEGORY_ACTION = "com.sec.internal.ims.servicemodules.euc.test.category.ACTION";

    public interface Action {
        public static final String INCOMING_ACKNOWLEDGEMENT_EUCR = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_ACKNOWLEDGEMENT_EUCR";
        public static final String INCOMING_NOTIFICATION_EUCR = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_NOTIFICATION_EUCR";
        public static final String INCOMING_PERSISTENT_EUCR = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_PERSISTENT_EUCR";
        public static final String INCOMING_SYSTEM_EUCR = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_SYSTEM_EUCR";
        public static final String INCOMING_USER_CONSENT = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_USER_CONSENT";
        public static final String INCOMING_VOLATILE_EUCR = "com.sec.internal.ims.servicemodules.euc.test.action.INCOMING_VOLATILE_EUCR";
        public static final String SEND_EUCR_RESPONSE = "com.sec.internal.ims.servicemodules.euc.test.action.SEND_EUCR_RESPONSE";
    }

    public interface Extras {
        public static final String ACCEPT_BUTTON_LANG_LIST = "accept_button_lang_list";
        public static final String ACCEPT_BUTTON_LIST = "accept_button_list";
        public static final String ACK_STATUS = "ack_status";
        public static final String ACK_STATUS_ERROR = "error";
        public static final String ACK_STATUS_OK = "ok";
        public static final String EXTERNAL_EUCR = "external_eucr";
        public static final String HANDLE = "handle";
        public static final String ID = "id";
        public static final String MESSAGE = "message";
        public static final String OK_BUTTON_LANG_LIST = "ok_button_lang_list";
        public static final String OK_BUTTON_LIST = "ok_button_list";
        public static final String PIN_INDICATION = "pin_indication";
        public static final String REJECT_BUTTON_LANG_LIST = "reject_button_lang_list";
        public static final String REJECT_BUTTON_LIST = "reject_button_list";
        public static final String REMOTE_URI = "remote_uri";
        public static final String SUBJECT_LANG_LIST = "subject_lang_list";
        public static final String SUBJECT_LIST = "subject_list";
        public static final String SUBSCRIBER_IDENTITY = "subscriber_identity";
        public static final String SYSTEM_DATA = "system_data";
        public static final String SYSTEM_TYPE = "system_type";
        public static final String TEXT_LANG_LIST = "text_lang_list";
        public static final String TEXT_LIST = "text_list";
        public static final String TIMEOUT = "timeout";
        public static final String TIMESTAMP = "timestamp";
        public static final String TITLE = "title";
        public static final String USER_ACCEPT = "user_accept";
        public static final String USER_PIN = "user_pin";
    }
}
