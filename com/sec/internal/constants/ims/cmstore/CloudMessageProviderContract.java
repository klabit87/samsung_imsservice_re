package com.sec.internal.constants.ims.cmstore;

public final class CloudMessageProviderContract {
    public static final int ALL_MMSPDU = 32;
    public static final int ALL_SMS = 31;
    public static final int CALL_LOG = 16;
    public static final String CONTENTPRDR_ALL_MMSPDUMESSAGE = "allmmspdumessage";
    public static final String CONTENTPRDR_ALL_SMSMESSAGES = "allsmsmessages";
    public static final String CONTENTPRDR_CALLLOG = "calllog";
    public static final String CONTENTPRDR_FAX = "faxmessages";
    public static final String CONTENTPRDR_LATESTMESSAGE = "latestmessage";
    public static final String CONTENTPRDR_MIGRATE_SUCCESS = "migrate_success";
    public static final String CONTENTPRDR_MMSADDRMESSAGES = "mmsaddrmessages";
    public static final String CONTENTPRDR_MMSPARTMESSAGES_PARTID = "mmspartmessages_partid";
    public static final String CONTENTPRDR_MMSPARTMESSAGES_PDUID = "mmspartmessages";
    public static final String CONTENTPRDR_MMSPDUMESSAGE = "mmspdumessage";
    public static final String CONTENTPRDR_MULTILINESTATUS = "multilinestatus";
    public static final String CONTENTPRDR_NOTIFICATION = "notification";
    public static final String CONTENTPRDR_PENDING_CALLLOG = "pendingcalllog";
    public static final String CONTENTPRDR_PENDING_FAX = "pendingfaxmessages";
    public static final String CONTENTPRDR_PENDING_MMSPDUMESSAGE = "pendingmmspdumessage";
    public static final String CONTENTPRDR_PENDING_RCSCHATMESSAGE = "pendingrcschatmessage";
    public static final String CONTENTPRDR_PENDING_RCSFTMESSAGE = "pendingrcsftmessage";
    public static final String CONTENTPRDR_PENDING_SMSMESSAGES = "pendingsmsmessages";
    public static final String CONTENTPRDR_PENDING_VVMMESSAGES = "pendingvvmmessages";
    public static final String CONTENTPRDR_RCSCHATMESSAGE = "rcschatmessage";
    public static final String CONTENTPRDR_RCSFTMESSAGE = "rcsftmessage";
    public static final String CONTENTPRDR_RCSMESSAGEIMDN = "rcsmessageimdn";
    public static final String CONTENTPRDR_RCSMESSAGES = "rcsmessages";
    public static final String CONTENTPRDR_RCSPARTICIPANTS = "rcsparticipants";
    public static final String CONTENTPRDR_RCSSESSION = "rcssession";
    public static final String CONTENTPRDR_SMSMESSAGES = "smsmessages";
    public static final String CONTENTPRDR_SUMMARYTABLE = "summarytable";
    public static final String CONTENTPRDR_USER_DEBUG_FLAG = "userdebugflag";
    public static final String CONTENTPRDR_VVMGREETING = "vvmgreeting";
    public static final String CONTENTPRDR_VVMMESSAGES = "vvmmessages";
    public static final String CONTENTPRDR_VVMPIN = "vvmpin";
    public static final String CONTENTPRDR_VVMPROFILE = "vvmprofile";
    public static final int FAX_MESSAGE = 21;
    public static final int FAX_RECEIVER = 22;
    public static final int LATEST_MESSAGE = 33;
    public static final int MIGRATE_SUCCESS = 35;
    public static final int MMS_ADDR_MESSAGES = 5;
    public static final int MMS_PART_ID = 8;
    public static final int MMS_PART_MESSAGES = 6;
    public static final int MMS_PDU_MESSAGES = 4;
    public static final int MULTI_LINE_STATUS = 23;
    public static final int NONE = 0;
    public static final int PENDING_CALLLOG = 30;
    public static final int PENDING_CHAT = 26;
    public static final int PENDING_FAX = 29;
    public static final int PENDING_FT = 27;
    public static final int PENDING_MMS = 25;
    public static final int PENDING_SMS = 24;
    public static final int PENDING_VVM = 28;
    public static final String PROVIDER_NAME = "com.samsung.rcs.cmstore";
    public static final int RCS_GROUP_STATE = 34;
    public static final int RCS_MESSAGES = 1;
    public static final int RCS_MESSAGES_CHAT = 11;
    public static final int RCS_MESSAGES_FT = 12;
    public static final int RCS_MESSAGES_IMDN = 13;
    public static final int RCS_MESSAGES_QUERY_USEIMDN = 15;
    public static final int RCS_MESSAGES_SLM = 14;
    public static final int RCS_MESSAGE_ID = 9;
    public static final int RCS_PARTICIPANT = 2;
    public static final int RCS_SESSION = 10;
    public static final int SMS_MESSAGES = 3;
    public static final int SUMMARY_TABLE = 7;
    public static final int USER_DEBUG_FLAG = 99;
    public static final int VVM_GREETING = 18;
    public static final int VVM_MESSAGES = 17;
    public static final int VVM_PIN = 19;
    public static final int VVM_PROFILE = 20;

    public static class ApplicationTypes {
        public static final String CALLLOGDATA = "CALLLOGDATA";
        public static final String MSGDATA = "MessageApp";
        public static final String RCSDATA = "RCSDATA";
        public static final String VVMDATA = "VVMDATA";
    }

    public static final class BufferCallLog {
        public static final String ANSWERED_BY = "answeredby";
        public static final String COUNTRYISO = "countryiso";
        public static final String DATA_USAGE = "data_usage";
        public static final String DATE = "date";
        public static final String DEVICE_NAME = "device_name";
        public static final String DURATION = "duration";
        public static final String FREQUENT = "frequent";
        public static final String GEOCODE = "geocoded_location";
        public static final String LOGTYPE = "logtype";
        public static final String NUMBER = "number";
        public static final String PRESENTATION = "presentation";
        public static final String SEEN = "seen";
        public static final String STARTTIME = "starttime";
        public static final String TYPE = "type";
        public static final String _ID = "_id";
    }

    public static class BufferDBExtensionBase {
        public static final String BUFFERDBID = "_bufferdbid";
        public static final String CORRELATION_ID = "correlation_id";
        public static final String CORRELATION_TAG = "correlation_tag";
        public static final String FLAGRESOURCEURL = "flagresourceurl";
        public static final String INITSYNCCURSOR = "initsync_cusor";
        public static final String INITSYNCSTATUS = "initsync_status";
        public static final String LASTMODSEQ = "lastmodseq";
        public static final String MESSAGETYPE = "messagetype";
        public static final String PARENTFOLDER = "parentfolder";
        public static final String PARENTFOLDERPATH = "parentfolderpath";
        public static final String PATH = "path";
        public static final String PAYLOADENCODING = "payloadencoding";
        public static final String PAYLOADPARTFULL = "payloadpartFull";
        public static final String PAYLOADPARTTHUMB = "payloadpartThumb";
        public static final String PAYLOADPARTTHUMB_FILENAME = "payloadpartThumb_filename";
        public static final String PAYLOADURL = "payloadurl";
        public static final String PREFERRED_URI = "linenum";
        public static final String RES_URL = "res_url";
        public static final String SYNCACTION = "syncaction";
        public static final String SYNCDIRECTION = "syncdirection";
    }

    public static final class BufferDBMMSaddr {
        public static final String ADDRESS = "address";
        public static final String CHARSET = "charset";
        public static final String CONTACT_ID = "contact_id";
        public static final String MSG_ID = "msg_id";
        public static final String TYPE = "type";
        public static final String _ID = "_id";
    }

    public static final class BufferDBMMSpart {
        public static final String CD = "cd";
        public static final String CHSET = "chset";
        public static final String CID = "cid";
        public static final String CL = "cl";
        public static final String CT = "ct";
        public static final String CTT_S = "ctt_s";
        public static final String CTT_T = "ctt_t";
        public static final String FN = "fn";
        public static final String MID = "mid";
        public static final String NAME = "name";
        public static final String SEQ = "seq";
        public static final String TEXT = "text";
        public static final String _DATA = "_data";
        public static final String _ID = "_id";
    }

    public static final class BufferDBMMSpdu {
        public static final String APP_ID = "app_id";
        public static final String CALLBACK_SET = "callback_set";
        public static final String CT_CLS = "ct_cls";
        public static final String CT_L = "ct_l";
        public static final String CT_T = "ct_t";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String DELETABLE = "deletable";
        public static final String D_RPT = "d_rpt";
        public static final String D_TM = "d_tm";
        public static final String EXP = "exp";
        public static final String FROM_ADDRESS = "from_address";
        public static final String HIDDEN = "hidden";
        public static final String LOCKED = "locked";
        public static final String MSG_BOX = "msg_box";
        public static final String MSG_ID = "msg_id";
        public static final String M_CLS = "m_cls";
        public static final String M_ID = "m_id";
        public static final String M_SIZE = "m_size";
        public static final String M_TYPE = "m_type";
        public static final String PRI = "pri";
        public static final String READ = "read";
        public static final String READ_STATUS = "read_status";
        public static final String RESERVED = "reserved";
        public static final String RESP_ST = "resp_st";
        public static final String RESP_TXT = "resp_txt";
        public static final String RETR_ST = "retr_st";
        public static final String RETR_TXT = "retr_txt";
        public static final String RETR_TXT_CS = "retr_txt_cs";
        public static final String RPT_A = "rpt_a";
        public static final String RR = "rr";
        public static final String SAFE_MESSAGE = "safe_message";
        public static final String SEEN = "seen";
        public static final String SIM_IMSI = "sim_imsi";
        public static final String SIM_SLOT = "sim_slot";
        public static final String SPAM_REPORT = "spam_report";
        public static final String ST = "st";
        public static final String SUB = "sub";
        public static final String SUB_CS = "sub_cs";
        public static final String TEXT_ONLY = "text_only";
        public static final String THREAD_ID = "thread_id";
        public static final String TR_ID = "tr_id";
        public static final String V = "v";
        public static final String _ID = "_id";
    }

    public static final class BufferDBSMS {
        public static final String ADDRESS = "address";
        public static final String APP_ID = "app_id";
        public static final String BODY = "body";
        public static final String CALLBACK_NUMBER = "callback_number";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String DELETABLE = "deletable";
        public static final String DELIVERY_DATE = "delivery_date";
        public static final String ERROR_CODE = "error_code";
        public static final String FROM_ADDRESS = "from_address";
        public static final String GROUP_ID = "group_id";
        public static final String GROUP_TYPE = "group_type";
        public static final String HIDDEN = "hidden";
        public static final String LINK_URL = "link_url";
        public static final String LOCKED = "locked";
        public static final String MSG_ID = "msg_id";
        public static final String PERSON = "person";
        public static final String PRI = "pri";
        public static final String PROTOCOL = "protocol";
        public static final String READ = "read";
        public static final String REPLY_PATH_PRESENT = "reply_path_present";
        public static final String RESERVED = "reserved";
        public static final String ROAM_PENDING = "roam_pending";
        public static final String SAFE_MESSAGE = "safe_message";
        public static final String SEEN = "seen";
        public static final String SERVICE_CENTER = "service_center";
        public static final String SIM_IMSI = "sim_imsi";
        public static final String SIM_SLOT = "sim_slot";
        public static final String SPAM_REPORT = "spam_report";
        public static final String STATUS = "status";
        public static final String SUBJECT = "subject";
        public static final String SVC_CMD = "svc_cmd";
        public static final String SVC_CMD_CONTENT = "svc_cmd_content";
        public static final String TELESERVICE_ID = "teleservice_id";
        public static final String THREAD_ID = "thread_id";
        public static final String TYPE = "type";
        public static final String _ID = "_id";
    }

    public static class CmsEventTypeValue {
        public static final String CMS_PROFILE_DISABLE = "CmsProfileDisable";
        public static final String CMS_PROFILE_ENABLE = "CmsProfileEnable";
    }

    public static class DataTypes {
        public static final String ACTIVATE = "ACTIVATE";
        public static final String CALLLOGDATA = "CALLLOGDATA";
        public static final String CHAT = "CHAT";
        public static final String DEACTIVATE = "DEACTIVATE";
        public static final String FAX = "FAX";
        public static final String FT = "FT";
        public static final String GSO = "GSO";
        public static final String IMDN = "IMDN";
        public static final String MMS = "MMS";
        public static final String MSGAPP_ALL = "MSG_ALL";
        public static final String SESSION = "SESSION";
        public static final String SMS = "SMS";
        public static final String VOICEMAILTOTEXT = "VOICEMAILTOTEXT";
        public static final String VVMDATA = "VVMDATA";
        public static final String VVMGREETING = "GREETING";
        public static final String VVMPIN = "PIN";
        public static final String VVMPROFILE = "PROFILE";
    }

    public static final class FAXMessages {
        public static final String DELIVER_STATUS = "deliverstatus";
        public static final String DIRECTION = "direction";
        public static final String ERRORMESSAGE = "error_message";
        public static final String FAXID = "transaction_id";
        public static final String FILENAME = "file_name";
        public static final String FILE_PATH = "file_path";
        public static final String FLAG_READ = "flagRead";
        public static final String MIME_TYPE = "content_type";
        public static final String RECEIVERS = "recipients";
        public static final String SENDER = "sender";
        public static final String SIZE = "file_size";
        public static final String TIMESTAMP = "date";
        public static final String UPLOADSTATUS = "uploadstatus";
        public static final String _ID = "_id";
    }

    public static final class FAXReceipents {
        public static final String FAXMSGBUFFERID = "_id";
        public static final String RECEIVER = "receiver";
    }

    public static final class JsonData {
        public static final String CHAT_ID = "chatid";
        public static final String CORRELATION_ID = "correlationId";
        public static final String CORRELATION_TAG = "correlationTag";
        public static final String ID = "id";
        public static final String IS_LOCAL_ONLY = "islocalonly";
        public static final String PREFERRED_LINE = "preferred_line";
        public static final String TRUE = "true";
        public static final String TYPE = "type";
    }

    public static class JsonParamTags {
        public static final String CMS_PROFILE_EVENT = "cms_profile_event";
        public static final String SIM_STATUS = "sim_status";
    }

    public static final class MultiLineStatus {
        public static final String MESSAGETYPE = "messagetype";
    }

    public static class SimStatusValue {
        public static final String SIM_READY = "SimReady";
        public static final String SIM_REMOVED = "SimRemoved";
    }

    public static final class VVMAccountInfoColumns {
        public static final String COS = "cos";
        public static final String EMAIL_ADDR1 = "email_addr1";
        public static final String EMAIL_ADDR2 = "email_addr2";
        public static final String ERRORMESSAGE = "error_message";
        public static final String GREETING_TYPE = "greeting_type";
        public static final String ISBLOCKED = "isblocked";
        public static final String LANGUAGE = "language";
        public static final String LINE_NUMBER = "line_number";
        public static final String NUT = "nut";
        public static final String PASSWORD = "password";
        public static final String PROFILE_CHANGETYPE = "profile_changetype";
        public static final String STATUS = "status";
        public static final String UPLOADSTATUS = "uploadstatus";
        public static final String USER_AUTHENTICATED = "user_authenticated";
        public static final String VVMON = "vvmon";
        public static final String _ID = "_id";
    }

    public static final class VVMGreetingColumns {
        public static final String ACCOUNT_NUMBER = "account_number";
        public static final String DURATION = "duration";
        public static final String ERRORMESSAGE = "error_message";
        public static final String FILENAME = "fileName";
        public static final String FILE_PATH = "filepath";
        public static final String FLAGS = "flags";
        public static final String GREETINGTYPE = "greetingtype";
        public static final String MESSAGE_ID = "messageId";
        public static final String MIME_TYPE = "mimeType";
        public static final String SIZE = "size";
        public static final String UPLOADSTATUS = "uploadstatus";
        public static final String _ID = "_id";
    }

    public static final class VVMMessageColumns {
        public static final String FILENAME = "fileName";
        public static final String FILE_PATH = "filepath";
        public static final String FLAGS = "flags";
        public static final String FLAG_READ = "flagRead";
        public static final String MESSAGE_ID = "messageId";
        public static final String MESSAGE_KEY = "messageKey";
        public static final String MIME_TYPE = "mimeType";
        public static final String RECIPIENT = "recipient";
        public static final String SENDER = "sender";
        public static final String SIZE = "size";
        public static final String TEXT = "text";
        public static final String TIMESTAMP = "timeStamp";
        public static final String _ID = "_id";
    }

    public static final class VVMPin {
        public static final String ERRORMESSAGE = "error_message";
        public static final String NEWPWD = "newpwd";
        public static final String OLDPWD = "oldpwd";
        public static final String UPLOADSTATUS = "uploadstatus";
        public static final String _ID = "_id";
    }
}
