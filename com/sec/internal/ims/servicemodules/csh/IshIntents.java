package com.sec.internal.ims.servicemodules.csh;

public class IshIntents {

    public interface IshIntent {
        public static final String ACTION_SHARE_ACCEPT = "com.sec.rcs.mediatransfer.ish.action.SHARE_ACCEPT";
        public static final String ACTION_SHARE_CANCEL = "com.sec.rcs.mediatransfer.ish.action.SHARE_CANCEL";
        public static final String ACTION_SHARE_CONTENT = "com.sec.rcs.mediatransfer.ish.action.SHARE_CONTENT";
    }

    public interface IshNotificationIntent {
        public static final String CATEGORY_NOTIFICATION = "com.sec.rcs.mediatransfer.ish.category.NOTIFICATION";
        public static final String NOTIFICATION_CSH_SERVICE_NOT_READY = "com.sec.rcs.mediatransfer.ish.notification.CSH_SERVICE_NOT_READY";
        public static final String NOTIFICATION_FILE_PATH_ERROR = "com.sec.rcs.mediatransfer.ish.notification.SHARE_FILE_PATH_ERROR";
        public static final String NOTIFICATION_SHARE_CANCELED = "com.sec.rcs.mediatransfer.ish.notification.SHARE_CANCELED";
        public static final String NOTIFICATION_SHARE_COMMUNICATION_ERROR = "com.sec.rcs.mediatransfer.ish.notification.SHARE_COMMUNICATION_ERROR";
        public static final String NOTIFICATION_SHARE_COMPLETED = "com.sec.rcs.mediatransfer.ish.notification.SHARE_COMPLETED";
        public static final String NOTIFICATION_SHARE_CONNECTED = "com.sec.rcs.mediatransfer.ish.notification.SHARE_CONNECTED";
        public static final String NOTIFICATION_SHARE_CREATED = "com.sec.rcs.mediatransfer.ish.notification.SHARE_CREATED";
        public static final String NOTIFICATION_SHARE_INCOMING = "com.sec.rcs.mediatransfer.ish.notification.SHARE_INCOMING";
        public static final String NOTIFICATION_SHARE_LIMIT_EXCEEDED = "com.sec.rcs.mediatransfer.ish.notification.SHARE_LIMIT_EXCEEDED";
        public static final String NOTIFICATION_SHARE_PROGRESS = "com.sec.rcs.mediatransfer.ish.notification.SHARE_PROGRESS";
        public static final String NOTIFICATION_SHARE_SERVICE_NOT_READY = "com.sec.rcs.mediatransfer.ish.notification.SHARE_SERVICE_NOT_READY";
        public static final String NOTIFICATION_SHARE_SERVICE_READY = "com.sec.rcs.mediatransfer.ish.notification.SHARE_SERVICE_READY";
    }
}
