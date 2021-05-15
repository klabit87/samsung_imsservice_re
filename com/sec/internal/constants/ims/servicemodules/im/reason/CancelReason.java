package com.sec.internal.constants.ims.servicemodules.im.reason;

public enum CancelReason {
    UNKNOWN(0),
    CANCELED_BY_USER(1),
    CANCELED_BY_REMOTE(2),
    CANCELED_BY_SYSTEM(3),
    REJECTED_BY_USER(4),
    REJECTED_BY_REMOTE(5),
    TIME_OUT(6),
    LOW_MEMORY(7),
    TOO_LARGE(8),
    NOT_AUTHORIZED(9),
    REMOTE_BLOCKED(10),
    VALIDITY_EXPIRED(11),
    REMOTE_TEMPORARILY_UNAVAILABLE(12),
    ERROR(13),
    INVALID_REQUEST(14),
    REMOTE_USER_INVALID(15),
    FORBIDDEN_NO_RETRY_FALLBACK(16),
    CONTENT_REACHED_DOWNSIZE(17),
    NO_RESPONSE(18),
    LOCALLY_ABORTED(19),
    CONNECTION_RELEASED(20),
    DEVICE_UNREGISTERED(21),
    DEDICATED_BEARER_UNAVAILABLE_TIMEOUT(22),
    MSRP_SESSION_ERROR_NO_RESUME(23),
    WIFI_DISCONNECTED(24),
    CONNECTION_LOST(25),
    FORBIDDEN_FT_HTTP(26),
    INVALID_FT_FILE_SIZE(27),
    INVALID_URL_TEMPLATE(28);
    
    private final int mId;

    private CancelReason(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public static CancelReason valueOf(int id) {
        for (CancelReason r : values()) {
            if (r.mId == id) {
                return r;
            }
        }
        return null;
    }
}
