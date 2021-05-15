package com.sec.internal.interfaces.ims.cmstore;

import android.net.Uri;
import android.provider.BaseColumns;

public interface ITelephonyDBColumns extends BaseColumns {
    public static final int BCC = 129;
    public static final int CC = 130;
    public static final Uri CONTENT_FAX = Uri.parse("content://im/ft");
    public static final Uri CONTENT_MMS = Uri.parse("content://mms");
    public static final Uri CONTENT_SMS = Uri.parse("content://sms");
    public static final int FROM = 137;
    public static final String FROM_INSERT_ADDRESS_TOKEN_STR = "insert-address-token";
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_RETRIEVE_CONF = 132;
    public static final int MESSAGE_TYPE_SEND_REQ = 128;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int READ_MSG = 1;
    public static final Uri SPAM_MMSSMS_CONTENT_URI = Uri.parse("content://mms-sms/spam-messages");
    public static final Uri SPAM_MMS_CONTENT_URI = Uri.parse("content://spammms");
    public static final Uri SPAM_SMS_CONTENT_URI = Uri.parse("content://spamsms");
    public static final int TO = 151;
    public static final String TYPE_DISCRIMINATOR_COLUMN = "transport_type";
    public static final int UNREAD_MSG = 0;
    public static final String xml_smil_type = "application/smil";
}
