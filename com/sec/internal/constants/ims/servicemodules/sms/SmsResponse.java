package com.sec.internal.constants.ims.servicemodules.sms;

public class SmsResponse {
    public static final int CDMA_NETWORK_TYPE = 1;
    public static final int ERROR_CAUSE_SPECIFIED = 4;
    public static final int ERROR_IMS_FAILED = 9;
    public static final int ERROR_NONE = 0;
    public static final int ERROR_PERMANENT = 3;
    public static final int ERROR_TEMPORARY = 2;
    public static final int ERROR_UNDEFINED = 255;
    public static final int GSM_NETWORK_TYPE = 2;
    public static final int RESULT_CANCELLED = 23;
    public static final int RESULT_ENCODING_ERROR = 18;
    public static final int RESULT_ERROR_FDN_CHECK_FAILURE = 6;
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    public static final int RESULT_ERROR_NONE = 0;
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    public static final int RESULT_ERROR_NULL_PDU = 3;
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    public static final int RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED = 8;
    public static final int RESULT_ERROR_SHORT_CODE_NOT_ALLOWED = 7;
    public static final int RESULT_INTERNAL_ERROR = 21;
    public static final int RESULT_INVALID_ARGUMENTS = 11;
    public static final int RESULT_INVALID_SMSC_ADDRESS = 19;
    public static final int RESULT_INVALID_SMS_FORMAT = 14;
    public static final int RESULT_INVALID_STATE = 12;
    public static final int RESULT_MODEM_ERROR = 16;
    public static final int RESULT_NETWORK_ERROR = 17;
    public static final int RESULT_NETWORK_REJECT = 10;
    public static final int RESULT_NO_MEMORY = 13;
    public static final int RESULT_NO_RESOURCES = 22;
    public static final int RESULT_OPERATION_NOT_ALLOWED = 20;
    public static final int RESULT_RADIO_NOT_AVAILABLE = 9;
    public static final int RESULT_REQUEST_NOT_SUPPORTED = 24;
    public static final int RESULT_SYSTEM_ERROR = 15;
    public static final int SIP_ACCEPTED = 202;
    public static final int SIP_OK = 200;
    public static final int SMS_FALLBACK_FOR_IMS = 19;
    public static final int STATUS_ACCEPTED = 0;
    public static final int STATUS_BLOCKED_DESTINATION = 7;
    public static final int STATUS_CANCELLED = 3;
    public static final int STATUS_CANCEL_FAILED = 6;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_DEPOSITED_TO_INTERNET = 1;
    public static final int STATUS_DUPLICATE_MESSAGE = 9;
    public static final int STATUS_INVALID_DESTINATION = 10;
    public static final int STATUS_MESSAGE_EXPIRED = 13;
    public static final int STATUS_NETWORK_CONGESTION = 4;
    public static final int STATUS_NETWORK_ERROR = 5;
    public static final int STATUS_TEXT_TOO_LONG = 8;
    public static final int STATUS_UNDEFINED = 255;
    public static final int STATUS_UNKNOWN_ERROR = 31;
    public static final int UNKNOWN_NETWORK_TYPE = 0;
    private int mContentType;
    private int mErrorCause;
    private int mErrorClass;
    private int mMessageId;
    private int mReasonCode;
    private byte[] mTpdu;

    public SmsResponse(int msgId, int code, byte[] tpdu, int contentType) {
        this.mMessageId = msgId;
        this.mReasonCode = code;
        this.mTpdu = tpdu;
        this.mContentType = contentType;
        convertSipResponsetoErrorCause(code);
    }

    public int getMessageId() {
        return this.mMessageId;
    }

    public int getErrorCause() {
        return this.mErrorCause;
    }

    public int getErrorClass() {
        return this.mErrorClass;
    }

    public void setErrorCause(int errorCause) {
        this.mErrorCause = errorCause;
    }

    public void setErrorClass(int errorClass) {
        this.mErrorClass = errorClass;
    }

    public void setMessageRef(int messageId) {
        this.mMessageId = messageId;
    }

    public byte[] getTpdu() {
        byte[] responsePdu = this.mTpdu;
        if (responsePdu != null) {
            return responsePdu;
        }
        return new byte[]{0};
    }

    public int getReasonCode() {
        return this.mReasonCode;
    }

    public int getContentType() {
        return this.mContentType;
    }

    private void convertSipResponsetoErrorCause(int responseCode) {
        if (responseCode >= 400 && responseCode <= 499) {
            this.mErrorClass = 3;
            if (responseCode == 403) {
                this.mErrorClass = 2;
                this.mErrorCause = 5;
                this.mReasonCode = 17;
            } else if (responseCode != 408) {
                this.mErrorCause = 31;
                this.mReasonCode = 17;
            } else {
                this.mErrorCause = 4;
                this.mReasonCode = 17;
            }
        } else if (responseCode < 500 || responseCode > 599) {
            int i = this.mContentType;
            if (i == 1) {
                if (responseCode == 200 || responseCode == 202) {
                    this.mErrorClass = 0;
                    this.mErrorCause = 0;
                    this.mReasonCode = 0;
                    return;
                }
                this.mErrorClass = 3;
                this.mErrorCause = 31;
                this.mReasonCode = 17;
            } else if (i != 2) {
            } else {
                if (responseCode == 0) {
                    this.mErrorClass = 0;
                    this.mErrorCause = 0;
                    this.mReasonCode = 0;
                    return;
                }
                this.mErrorClass = (responseCode >> 8) & 255;
                this.mErrorCause = responseCode & 255;
                this.mReasonCode = 17;
            }
        } else {
            this.mErrorClass = 3;
            if (responseCode != 500) {
                this.mErrorCause = 31;
                this.mReasonCode = 17;
                return;
            }
            this.mErrorCause = 5;
            this.mReasonCode = 17;
        }
    }
}
