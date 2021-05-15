package com.sec.internal.constants.tapi;

import android.net.Uri;
import android.provider.BaseColumns;

public class UserConsentProviderContract {
    public static final String AUTHORITY = "com.sec.internal.userconsentprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://com.sec.internal.userconsentprovider");
    public static final int EUCR_ACKNOWLEDGEMENT_ID = 3;
    public static final String EUCR_ACKNOWLEDGEMENT_LABEL = "ACKNOWLEDGEMENT";
    public static final int EUCR_NOTIFICATION_ID = 2;
    public static final String EUCR_NOTIFICATION_LABEL = "NOTIFICATION";
    public static final int EUCR_PERSISTENT_ID = 0;
    public static final String EUCR_PERSISTENT_LABEL = "PERSISTENT";
    public static final int EUCR_VOLATILE_ID = 1;
    public static final String EUCR_VOLATILE_LABEL = "VOLATILE";
    public static final int EULA_ID = 4;
    public static final String EULA_LABEL = "EULA";
    public static final int USER_CONSENT_STATE_ACCEPTED_ID = 0;
    public static final int USER_CONSENT_STATE_REJECTED_ID = 1;

    private UserConsentProviderContract() {
    }

    public static final class UserConsentList implements BaseColumns {
        static final String ACCEPT_BUTTON_LABEL = "ACCEPT_BUTTON";
        public static final Uri CONTENT_URI = UserConsentProviderContract.CONTENT_URI;
        public static final String ID = "ID";
        protected static final String[] PROJECTION_ALL = {ROWID, ID, TIMESTAMP, STATE, "TYPE", SUBJECT_LABEL, TEXT_LABEL, ACCEPT_BUTTON_LABEL, REJECT_BUTTON_LABEL, REMOTE_URI, SUBSCRIBER_IDENTITY};
        static final String REJECT_BUTTON_LABEL = "REJECT_BUTTON";
        public static final String REMOTE_URI = "REMOTE_URI";
        public static final String ROWID = "ROWID";
        public static final String SORT_ORDER_DEFAULT = "TIMESTAMP DESC";
        public static final String STATE = "STATE";
        static final String SUBJECT_LABEL = "SUBJECT";
        public static final String SUBSCRIBER_IDENTITY = "SUBSCRIBER_IDENTITY";
        static final String TEXT_LABEL = "TEXT";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String TYPE = "TYPE";

        private UserConsentList() {
        }
    }
}
