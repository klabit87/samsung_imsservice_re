package com.sec.internal.constants.ims;

import android.text.TextUtils;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.servicemodules.ss.UtError;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;
import java.util.Locale;

public class SipErrorBase extends SipError {
    public static final SipError ACCEPTED = new SipError(202, "Accepted");
    public static final SipError ADDRESS_INCOMPLETE = new SipError(484, "Address Incomplete");
    public static final SipError ALTERNATIVE_SERVICE = new SipError(380, "Alternative Service");
    public static final SipError ALTERNATIVE_SERVICE_EMERGENCY = new SipError(381, "Alternative Service Emergency");
    public static final SipError ALTERNATIVE_SERVICE_EMERGENCY_CSFB = new SipError(382, "Alternative Service Emergency CSFB");
    public static final SipError AMBIGUOUS = new SipError(485, "Ambiguous");
    public static final SipError ANONYMITY_DISALLOWED = new SipError(433, "Anonymity Disallowed");
    public static final SipError BAD_EVENT = new SipError(489, "Bad Event");
    public static final SipError BAD_EXTENSION = new SipError(420, "Bad Extension");
    public static final SipError BAD_GATEWAY = new SipError(502, "Bad Gateway");
    public static final SipError BAD_IDENTITY_INFO = new SipError(436, "Bad Identity-Info");
    public static final SipError BAD_INFO_PACKAGE = new SipError(469, "Bad Info Package");
    public static final SipError BAD_LOCATION_INFORMATION = new SipError(424, "Bad Location Information");
    public static final SipError BAD_REQUEST = new SipError(400, "Bad Request");
    public static final SipError BUSY_EVERYWHERE = new SipError(600, "Busy Everywhere");
    public static final SipError BUSY_HERE = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Busy Here");
    public static final SipError CALL_DOES_NOT_EXIST = new SipError(481, "Call/Transaction Does Not Exist");
    public static final SipError CALL_IS_BEING_FORWARDED = new SipError(MNO.VODAFONE_NZ, "Call Is Being Forwarded");
    public static final SipError CONDITIONAL_REQUEST_FAILED = new SipError(UtError.PRECONDITION_FAILED, "Conditional Request Failed");
    public static final SipError CONSENT_NEEDED = new SipError(470, "Consent Needed");
    public static final SipError DECLINE = new SipError(Id.REQUEST_UPDATE_TIME_IN_PLANI, "Decline");
    public static final SipError DOES_NOT_EXIST_ANYWHERE = new SipError(604, "Does Not Exist Anywhere");
    public static final SipError E911_NOT_ALLOWED_ON_SD = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "E911 Not Allowed on Secondary");
    public static final SipError EARLY_DIALOG_TERMINATED = new SipError(MNO.SKY_GB, "Early Dialog Terminated");
    public static final SipError EMPTY_PCSCF = new SipError(701, "EMPTY P-CSCF");
    public static final SipError EXTENSION_REQUIRED = new SipError(421, "Extension Required");
    public static final SipError FIRST_HOP_LACKS_OUTBOUND_SUPPORT = new SipError(439, "First Hop Lacks Outbound Support");
    public static final SipError FLOW_FAILED = new SipError(430, "Flow Failed");
    public static final SipError FORBIDDEN = new SipError(403, "Forbidden");
    public static final SipError FORBIDDEN_SERVICE_NOT_AUTHORISED = new SipError(403, "Forbidden Service Not Authorised");
    public static final SipError GONE = new SipError(410, "Gone");
    public static final SipError INTERVAL_TOO_BRIEF = new SipError(423, "Interval Too Brief");
    public static final SipError INVALID_IDENTITY_HEADER = new SipError(438, "Invalid Identity Header");
    public static final SipError LOOP_DETECTED = new SipError(482, "Loop Detected");
    public static final SipError MAX_BREADTH_EXCEEDED = new SipError(440, "Max-Breadth Exceeded");
    public static final SipError MEDIA_BEARER_OR_QOS_LOST = new SipError(500, "Media bearer or QoS lost");
    public static final SipError MESSAGE_TOO_LARGE = new SipError(513, "Message Too Large");
    public static final SipError METHOD_NOT_ALLOWED = new SipError(AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED, "Method Not Allowed");
    public static final SipError MISSING_P_ASSOCIATED_URI = new SipError(790, "Missing P-Associated URI header.");
    public static final SipError MOVED_PERMANENTLY = new SipError(CallStateMachine.ON_TIMER_VZW_EXPIRED, "Moved Permanently");
    public static final SipError MOVED_TEMPORARILY = new SipError(CallStateMachine.ON_REINVITE_TIMER_EXPIRED, "Moved Temporarily");
    public static final SipError MULTIPLE_CHOICES = new SipError(300, "Multiple Choices");
    public static final SipError NOTIFY_TERMINATED_DEACTIVATED = new SipError(Id.REQUEST_PRESENCE_SUBSCRIBE, "Notify terminated deactivated");
    public static final SipError NOTIFY_TERMINATED_REJECTED = new SipError(Id.REQUEST_PRESENCE_UNSUBSCRIBE, "Notify terminated rejected");
    public static final SipError NOTIFY_TERMINATED_UNREGISTERED = new SipError(714, "Notify terminated unregistered");
    public static final SipError NOT_ACCEPTABLE = new SipError(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF, "Not Acceptable");
    public static final SipError NOT_ACCEPTABLE_GLOBALLY = new SipError(606, "Not Acceptable");
    public static final SipError NOT_ACCEPTABLE_HERE = new SipError(488, "Not Acceptable Here");
    public static final SipError NOT_FOUND = new SipError(404, "Not Found");
    public static final SipError NOT_IMPLEMENTED = new SipError(501, "Not Implemented");
    public static final SipError NO_DNS = new SipError(703);
    public static final SipError NO_NOTIFICATION = new SipError(204, "No Notification");
    public static final SipError OK = new SipError(200, "OK");
    public static final SipError OK_SMC = new SipError(200, "OK - SAR succesful and registrar saved");
    public static final SipError PAYMENT_REQUIRED = new SipError(402, "Payment Required");
    public static final SipError PRECONDITION_FAILURE = new SipError(580, "Precondition Failure");
    public static final SipError PROVIDE_REFERRER_IDENTITY = new SipError(OMAGlobalVariables.HTTP_TOO_MANY_REQUEST, "Provide Referrer Identity");
    public static final SipError PROXY_AUTHENTICATION_REQUIRED = new SipError(RegistrationEvents.EVENT_CHECK_UNPROCESSED_OMADM_CONFIG, "Proxy Authentication Required");
    public static final SipError REQUEST_ENTITY_TOO_LARGE = new SipError(413, "Request Entity Too Large");
    public static final SipError REQUEST_PENDING = new SipError(491, "Request Pending");
    public static final SipError REQUEST_TERMINATED = new SipError(487, "Request Terminated");
    public static final SipError REQUEST_TIMEOUT = new SipError(408, "Request Timeout");
    public static final SipError REQUEST_URI_TOO_LONG = new SipError(414, "Request-URI Too Long");
    public static final SipError RINGING = new SipError(MNO.EVR_ESN, "Ringing");
    public static final SipError SECURITY_AGREEMENT_REQUIRED = new SipError(494, "Security Agreement Required");
    public static final SipError SERVER_INTERNAL_ERROR = new SipError(500, "Server Internal Error");
    public static final SipError SERVER_TIMEOUT = new SipError(Id.REQUEST_IM_SENDMSG, "Server Time-out");
    public static final SipError SERVICE_UNAVAILABLE = new SipError(503, "Service Unavailable");
    public static final SipError SESSION_INTERVAL_TOO_SMALL = new SipError(422, "Session Interval Too Small");
    public static final SipError SESSION_PROGRESS = new SipError(MNO.MOVISTAR_MEXICO, "Session Progress");
    public static final SipError SIP_INVITE_TIMEOUT = new SipError(709);
    public static final SipError SIP_TIMEOUT = new SipError(708);
    public static final SipError TEMPORARILY_UNAVAIABLE = new SipError(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE, "Temporarily Unavailable");
    public static final SipError TOO_MANY_HOPS = new SipError(483, "Too Many Hops");
    public static final SipError TRYING = new SipError(100, "Trying");
    public static final SipError UNAUTHORIZED = new SipError(401, "Unauthorized");
    public static final SipError UNDECIPHERABLE = new SipError(493, "Undecipherable");
    public static final SipError UNKNOWN_LOCAL_ERROR = new SipError(700, "Unknown Local Error");
    public static final SipError UNKNOWN_RESOURCE_PRIORITY = new SipError(417, "Unknown Resource-Priority");
    public static final SipError UNSUPPORTED_CERTIFICATE = new SipError(437, "Unsupported Certificate");
    public static final SipError UNSUPPORTED_MEDIA_TYPE = new SipError(AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
    public static final SipError UNSUPPORTED_URI_SCHEME = new SipError(416, "Unsupported URI Scheme");
    public static final SipError UNWANTED = new SipError(607, "Unwanted");
    public static final SipError USER_NOT_REGISTERED = new SipError(403, "Forbidden: User Not Registered");
    public static final SipError USER_NOT_REGISTERED2 = new SipError(403, "Forbidden - User Not Registered");
    public static final SipError USE_IDENTITY_HEADER = new SipError(428, "Use Identity Header");
    public static final SipError USE_PROXY = new SipError(CallStateMachine.ON_DUMMY_DNS_TIMER_EXPIRED, "Use Proxy");
    public static final SipError VERSION_NOT_SUPPORTED = new SipError(Id.REQUEST_IM_SEND_COMPOSING_STATUS, "Version Not Supported");
    protected SipError mDefaultRejectReason = BUSY_HERE;

    public static class END_REASON {
        public static final int BEARER_LOST = 11;
        public static final int BEARER_NOT_ESTABLISHED = 29;
        public static final int CALL_BUSY = 16;
        public static final int EPDN_SETUP_FAIL = 22;
        public static final int FAKE_MDMN_CONNECTED = 19;
        public static final int IMS_DEREGISTERED = 14;
        public static final int INVITE_FLUSH = 25;
        public static final int LOST_EPDN_CONNECTION = 15;
        public static final int LOST_LTE_WIFI_CONNECTION = 12;
        public static final int LOST_WIFI_CONNECTION = 21;
        public static final int LOW_BATTERY = 6;
        public static final int MDMN_END_BY_REGULAR_CALL_RELEASE = 26;
        public static final int MDMN_PULL_BY_PRIMARY = 20;
        public static final int MDMN_PULL_BY_SECONDARY = 27;
        public static final int NETWORK_COVERAGE_LOST = 24;
        public static final int NORMAL = 5;
        public static final int NWAY_CONFERENCE = 7;
        public static final int NW_HANDOVER = 4;
        public static final int OUT_OF_BATTERY = 10;
        public static final int PS_BARRING = 13;
        public static final int RRC_CONNECTION_REJECT = 23;
        public static final int SESSIONPROGRESS_TIMER = 17;
        public static final int SIP_RESPONSE_TIMEOUT = 28;
        public static final int SRVCC_HANDOVER = 8;
        public static final int VOPS_DISABLED = 9;
    }

    public static class REJECT_REASON {
        public static final int CALL_BUSY = 11;
        public static final int E911_NOT_ALLOWED_ON_SD = 15;
        public static final int LOW_BATTERY = 6;
        public static final int NO_ANSWER = 13;
        public static final int NW_HANDOVER = 4;
        public static final int OUT_OF_BATTERY = 10;
        public static final int SRVCC_HANDOVER = 8;
        public static final int TEMP_NOT_ACCEPTABLE = 9;
        public static final int THIRD_WAITING_CALL = 14;
        public static final int UESR_UNWANTED = 16;
        public static final int USER_BUSY = 2;
        public static final int USER_BUSY_CS_CALL = 7;
        public static final int USER_CALL_BLOCK = 12;
        public static final int USER_DECLINE = 3;
    }

    public enum SipErrorType {
        SUCCESS(2),
        ERROR_4XX(4),
        ERROR_5XX(5),
        ERROR_6XX(6);
        
        private int mType;

        private SipErrorType(int type) {
            this.mType = type;
        }

        public boolean equals(SipError sipError) {
            return equals(sipError.getCode());
        }

        public boolean equals(int errorCode) {
            return this.mType == errorCode / 100;
        }
    }

    public SipError getFromRejectReason(int reason) {
        if (reason != 2) {
            if (reason == 3) {
                return BUSY_HERE;
            }
            if (reason != 6) {
                if (reason != 7) {
                    if (reason == 9) {
                        return TEMPORARILY_UNAVAIABLE;
                    }
                    if (reason != 10) {
                        if (reason != 16) {
                            return this.mDefaultRejectReason;
                        }
                        return UNWANTED;
                    }
                }
            }
            return TEMPORARILY_UNAVAIABLE;
        }
        return BUSY_HERE;
    }

    public static boolean isImsForbiddenError(SipError error) {
        if (error != null && FORBIDDEN.getCode() == error.getCode() && !TextUtils.isEmpty(error.getReason()) && error.getReason().toLowerCase(Locale.US).contains("Forbidden".toLowerCase()) && !error.getReason().toLowerCase(Locale.US).contains(RegistrationConstants.REASON_REGISTERED.toLowerCase())) {
            return true;
        }
        return false;
    }

    public static boolean isImsOutageError(SipError error) {
        if (error != null && SipErrorVzw.IMS_OUTAGE.getCode() == error.getCode() && !TextUtils.isEmpty(error.getReason()) && error.getReason().toLowerCase(Locale.US).contains("Outage".toLowerCase())) {
            return true;
        }
        return false;
    }
}
