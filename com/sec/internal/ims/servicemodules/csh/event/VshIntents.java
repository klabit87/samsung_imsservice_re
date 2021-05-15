package com.sec.internal.ims.servicemodules.csh.event;

public interface VshIntents {
    public static final String LIVE_VIDEO_CONTENTPATH = "com.sec.rcs.videosharing.LIVE_VIDEO_CONTENTPATH";

    public interface VshIntent {
        public static final String ACTION_CHANGE_SURFACE_ORIENTATION = "com.sec.rcs.mediatransfer.vsh.action.CHANGE_VIDEO_ORIENTATION";
        public static final String ACTION_SHARE_ACCEPT = "com.sec.rcs.mediatransfer.vsh.action.SHARE_ACCEPT";
        public static final String ACTION_SHARE_CANCEL = "com.sec.rcs.mediatransfer.vsh.action.SHARE_CANCEL";
        public static final String ACTION_SHARE_CONTENT = "com.sec.rcs.mediatransfer.vsh.action.SHARE_CONTENT";
        public static final String ACTION_TOGGLE_CAMERA = "com.sec.rcs.mediatransfer.vsh.action.TOGGLE_CAMERA";
    }

    public interface VshNotificationIntent {
        public static final String CATEGORY_NOTIFICATION = "com.sec.rcs.mediatransfer.vsh.category.NOTIFICATION";
        public static final String NOTIFICATION_APPROCHING_VS_MAX_DURATION = "com.sec.rcs.mediatransfer.vsh.notification.APPROCHING_VS_MAX_DURATION";
        public static final String NOTIFICATION_CSH_CAM_ERROR = "com.sec.rcs.mediatransfer.vsh.notification.CSH_CAM_ERROR";
        public static final String NOTIFICATION_CSH_SERVICE_NOT_READY = "com.sec.rcs.mediatransfer.vsh.notification.CSH_SERVICE_NOT_READY";
        public static final String NOTIFICATION_SHARE_CANCELED = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_CANCELED";
        public static final String NOTIFICATION_SHARE_COMMUNICATION_ERROR = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_LCOMMUNICATION_ERROR";
        public static final String NOTIFICATION_SHARE_CONNECTED = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_CONNECTED";
        public static final String NOTIFICATION_SHARE_INCOMING = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_INCOMING";
        public static final String NOTIFICATION_SHARE_SERVICE_NOT_READY = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_SERVICE_NOT_READY";
        public static final String NOTIFICATION_SHARE_SERVICE_READY = "com.sec.rcs.mediatransfer.vsh.notification.SHARE_SERVICE_READY";
    }
}
