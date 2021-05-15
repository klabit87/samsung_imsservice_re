package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorVzw extends SipErrorBase {
    public static final SipError BUSY_ALREADY_IN_TWO_CALLS = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Already On Two Calls");
    public static final SipError BUSY_ESTABLISHING_ANOTHER_CALL = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Establishing Another Call");
    public static final SipError CALL_REJECTED_BY_NOANSWER = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "No Answer");
    public static final SipError CALL_REJECTED_BY_USER = new SipError(600, "Call Rejected By User");
    public static final SipError FORBIDDEN_ORIG_USER_NOT_REGISTERED = new SipError(403, "Forbidden - Originating user not registered");
    public static final SipError FORBIDDEN_ORIG_USER_NOT_REGISTERED2 = new SipError(403, "Forbidden: Originating User Not Registered");
    public static final SipError FORBIDDEN_USER_NOT_AUTHORIZED_FOR_PRESENCE = new SipError(403, "Forbidden - Not authorized for Presence");
    public static final SipError FORBIDDEN_USER_NOT_AUTHORIZED_FOR_SERVICE = new SipError(403, "Forbidden. Not Authorized for Service");
    public static final SipError FORBIDDEN_USER_NOT_REGISTERED = new SipError(403, "Forbidden - User Not Registered");
    public static final SipError IMS_OUTAGE = new SipError(503, "Service Unavailable: IMS Outage");
    public static final SipError NOT_ACCEPTABLE_1X_CALL_SETUP = new SipError(488, "On 1X call setup");
    public static final SipError NOT_ACCEPTABLE_ACTIVE_1X_CALL = new SipError(488, "On active 1X call");
    public static final SipError NOT_ACCEPTABLE_CODEC_NOT_SUPPORTED = new SipError(488, "Codec not supported");
    public static final SipError NOT_ACCEPTABLE_MOVED_TO_EHRPD = new SipError(488, "Moved to eHRPD");
    public static final SipError NOT_ACCEPTABLE_NO_PROVISIONING = new SipError(488, "Subscriber not provisioned for VoLTE");
    public static final SipError NOT_ACCEPTABLE_NO_VOPS = new SipError(488, "VOPS OFF");
    public static final SipError NOT_ACCEPTABLE_ON_EHRPD = new SipError(488, "On eHRPD");
    public static final SipError NOT_ACCEPTABLE_SSAC_ON = new SipError(488, "SSAC ON");
    public static final SipError NOT_ACCEPTABLE_VOLTE_OFF = new SipError(488, "VoLTE setting Off");
    public static final SipError TTY_ON = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "TTY On");
    public static final SipError VIDEO_UPGRADE_REQUEST_IN_PROGRESS = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Video Upgrade Request In Progress");
    public static final SipError VOWIFI_OFF = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "VoWiFi OFF");

    public SipError getFromRejectReason(int reason) {
        if (reason < 0) {
            reason = 3;
        }
        if (reason == 3) {
            return CALL_REJECTED_BY_USER;
        }
        if (reason == 4) {
            return NOT_ACCEPTABLE_MOVED_TO_EHRPD;
        }
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return CALL_REJECTED_BY_NOANSWER;
    }
}
