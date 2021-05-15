package com.sec.internal.ims.servicemodules.csh.event;

import android.net.Uri;

public interface ICshConstants {

    public interface ExtraInformation {
        public static final String EXTRA_BYTES_DONE = "com.sec.rcs.mediatransfer.csh.extra.BYTES_DONE";
        public static final String EXTRA_BYTES_TOTAL = "com.sec.rcs.mediatransfer.csh.extra.BYTES_TOTAL";
        public static final String EXTRA_CONTACT_URI = "com.sec.rcs.mediatransfer.csh.extra.CONTACT_URI";
        public static final String EXTRA_FILE_PATH = "com.sec.rcs.mediatransfer.csh.extra.FILE_PATH";
        public static final String EXTRA_REASON = "com.sec.rcs.mediatransfer.csh.extra.REASON";
        public static final String EXTRA_REMAINING_TIME = "com.sec.rcs.mediatransfer.csh.extra.REMAINING_TIME";
        public static final String EXTRA_SHARE_DIRECTION = "com.sec.rcs.mediatransfer.csh.extra.SHARE_DIRECTION";
        public static final String EXTRA_SHARE_ID = "com.sec.rcs.mediatransfer.csh.extra.SHARE_ID";
        public static final String EXTRA_SHARE_TYPE = "com.sec.rcs.mediatransfer.csh.extra.SHARE_TYPE";
        public static final String EXTRA_SURFACE_ORIENTATION = "com.sec.rcs.mediatransfer.csh.extra.SURFACE_ORIENTATION";
    }

    public interface NotificationReason {
        public static final int BY_REMOTE = 2;
        public static final int BY_SERVER = 12;
        public static final int BY_SYSTEM = 3;
        public static final int CANCELED = 10;
        public static final int DISALLOWED = 13;
        public static final int NONE = 9;
        public static final int REJECTED = 4;
        public static final int TIME_OUT = 6;
    }

    public interface ShareDatabase {
        public static final int ACTIVE_SESSIONS = 5;
        public static final Uri ACTIVE_SESSIONS_URI = Uri.parse("content://com.samsung.rcs.cs/active_sessions");
        public static final String KEY_FILE_NAME = "path";
        public static final String KEY_FILE_SIZE = "size";
        public static final String KEY_PROGRESS = "progress";
        public static final String KEY_RESOLUTION_HEIGHT = "res_height";
        public static final String KEY_RESOLUTION_WIDTH = "res_width";
        public static final String KEY_SHARE_DIRECTION = "dir";
        public static final String KEY_SHARE_ID = "id";
        public static final String KEY_SHARE_STATE = "state";
        public static final String KEY_SHARE_TYPE = "type";
        public static final String KEY_TARGET_CONTACT = "contact";
        public static final String PROVIDER_NAME = "com.samsung.rcs.cs";
    }

    public interface ShareDirection {
        public static final int CSH_DIRECTION_INCOMING = 0;
        public static final int CSH_DIRECTION_OUTGOING = 1;
    }

    public interface ShareSource {
        public static final int CSH_CAMERA_SOURCE = 1;
    }

    public interface ShareState {
        public static final int ACCEPTED = 11;
        public static final int CANCELED = 6;
        public static final int COMPLETED = 4;
        public static final int DISPLAYING_PROGRESS = 17;
        public static final int FAILED = 12;
        public static final int FINISHED = 13;
        public static final int INITIALIZED = 1;
        public static final int INTERRUPTED = 5;
        public static final int IN_PROGRESS = 3;
        public static final int PENDING = 2;
        public static final int PENDING_ACCEPT = 18;
        public static final int PENDING_TERMINATION_LOCAL = 15;
        public static final int PENDING_TERMINATION_REMOTE = 16;
        public static final int PRESENTING_MEDIA = 10;
        public static final int SILENTLY_REFUSED = 9;
        public static final int TERMINATED = 14;
        public static final int TIMED_OUT = 7;
    }

    public interface ShareType {
        public static final int CSH_IMAGE_SHARE = 1;
        public static final int CSH_VIDEO_SHARE = 2;
    }
}
