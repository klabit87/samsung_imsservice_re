package com.sec.internal.constants.ims.servicemodules.im;

public final class ImContract {
    public static final int ALL_MESSAGE = 19;
    public static final int ALL_PARTICIPANT = 20;
    public static final int ALL_SESSION = 18;
    public static final int AUTO_ACCEPT_FT = 12;
    public static final int BOT_SETTING = 37;
    public static final int BUFFERDB_DELETE_MESSAGE = 23;
    public static final int BUFFERDB_DELETE_PARTICIPANT = 26;
    public static final int BUFFERDB_INSERT_MESSAGE = 21;
    public static final int BUFFERDB_INSERT_NOTIFICATION = 39;
    public static final int BUFFERDB_INSERT_PARTICIPANT = 24;
    public static final int BUFFERDB_INSERT_SESSION = 30;
    public static final int BUFFERDB_QUERY_MESSAGE_CHATID = 28;
    public static final int BUFFERDB_QUERY_MESSAGE_IMDNID = 38;
    public static final int BUFFERDB_QUERY_MESSAGE_ROWID = 27;
    public static final int BUFFERDB_QUERY_PARTICIPANT = 29;
    public static final int BUFFERDB_QUERY_SESSION_CHATID = 31;
    public static final int BUFFERDB_QUERY_SESSION_ID = 32;
    public static final int BUFFERDB_UPDATE_MESSAGE = 22;
    public static final int BUFFERDB_UPDATE_NOTIFICATION = 40;
    public static final int BUFFERDB_UPDATE_PARTICIPANT = 25;
    public static final int BUFFERDB_UPDATE_SESSION = 36;
    public static final int CHAT = 4;
    public static final int CHATS = 3;
    public static final int CHAT_IDS_BY_CONTENT_TYPE = 17;
    public static final int ENRICHED_CHATS = 13;
    public static final int FILE_TRANSFER = 10;
    public static final int FILE_TRANSFERS = 9;
    public static final int MESSAGE = 1;
    public static final int MESSAGES = 0;
    public static final int MESSAGES_WITH_FT = 11;
    public static final int MESSAGES_WITH_FT_COUNT = 16;
    public static final int MESSAGE_COUNT = 2;
    public static final int MESSAGE_NOTIFICATIONS = 15;
    public static final int PARTICIPANTS = 5;
    public static final String PROVIDER_NAME = "com.samsung.rcs.im";
    public static final int RELIABLE_IMAGE = 35;
    public static final int SETTINGS = 14;
    public static final int UNREAD_MESSAGES = 6;
    public static final int UNREAD_MESSAGES_COUNT = 7;
    public static final int UNREAD_MESSAGES_COUNT_BY_ID = 8;

    public static final class AutoAcceptFt {
        public static final String ID = "_id";
        public static final String SETTING_VALUE = "setting_value";
    }

    public static final class BotUserAgent {
        public static final String BOT_USER_AGENT = "bot_user_agent";
    }

    public static class ChatItem {
        public static final String CHAT_ID = "chat_id";
        public static final String CONTENT_TYPE = "content_type";
        public static final String DELIVERED_TIMESTAMP = "delivered_timestamp";
        public static final String DIRECTION = "direction";
        public static final String EXT_INFO = "ext_info";
        public static final String ID = "_id";
        public static final String INSERTED_TIMESTAMP = "inserted_timestamp";
        public static final String IS_FILE_TRANSFER = "is_filetransfer";
        public static final String REMOTE_URI = "remote_uri";
        public static final String USER_ALIAS = "sender_alias";
    }

    public static final class CsSession extends ChatItem {
        public static final String BYTES_TRANSFERED = "bytes_transf";
        public static final String DATA_URL = "data_url";
        public static final String EXTRA_FT = "extra_ft";
        public static final String FILE_DISPOSITION = "file_disposition";
        public static final String FILE_NAME = "file_name";
        public static final String FILE_PATH = "file_path";
        public static final String FILE_SIZE = "file_size";
        public static final String FILE_TRANSFER_ID = "file_transfer_id";
        public static final String IS_RESIZABLE = "is_resizable";
        public static final String IS_RESUMABLE = "is_resumable";
        public static final String PLAYING_LENGTH = "playing_length";
        public static final String REASON = "reason";
        public static final String STATE = "state";
        public static final String STATUS = "ft_status";
        public static final String THUMBNAIL_PATH = "thumbnail_path";
        public static final String TRANSFER_MECH = "transfer_mech";
        public static final String TYPE = "type";
    }

    public static final class ImSession {
        public static final String CHAT_ID = "chat_id";
        public static final String CHAT_MODE = "chat_mode";
        public static final String CHAT_TYPE = "chat_type";
        public static final String CONTRIBUTION_ID = "contribution_id";
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String DIRECTION = "direction";
        public static final String ICON_PARTICIPANT = "icon_participant";
        public static final String ICON_PATH = "icon_path";
        public static final String ICON_TIMESTAMP = "icon_timestamp";
        public static final String ICON_URI = "icon_uri";
        public static final String ID = "_id";
        public static final String IMDN_NOTIFICATIONS_AVAILABILITY = "imdn_notifications_availability";
        public static final String INSERTED_TIMESTAMP = "inserted_time_stamp";
        public static final String IS_BROADCAST_MSG = "is_broadcast_msg";
        public static final String IS_CHATBOT_ROLE = "is_chatbot_role";
        public static final String IS_FT_GROUP_CHAT = "is_ft_group_chat";
        public static final String IS_GROUP_CHAT = "is_group_chat";
        public static final String IS_MUTED = "is_muted";
        public static final String IS_REUSABLE = "is_reusable";
        public static final String MAX_PARTICIPANTS_COUNT = "max_participants_count";
        public static final String OWN_GROUP_ALIAS = "own_group_alias";
        public static final String OWN_PHONE_NUMBER = "own_sim_imsi";
        public static final String PREFERRED_URI = "preferred_uri";
        public static final String SESSION_URI = "session_uri";
        public static final String SIM_IMSI = "sim_imsi";
        public static final String STATUS = "status";
        public static final String SUBJECT = "subject";
        public static final String SUBJECT_PARTICIPANT = "subject_participant";
        public static final String SUBJECT_TIMESTAMP = "subject_timestamp";
    }

    public static final class ImdnRecRoute {
        public static final String ALIAS = "alias";
        public static final String ID = "_id";
        public static final String IMDN_ID = "imdn_id";
        public static final String MESSAGE_ID = "message_id";
        public static final String URI = "uri";
    }

    public static final class Message extends ChatItem {
        public static final String BODY = "body";
        public static final String CONTRIBUTION_ID = "contribution_id";
        public static final String CONVERSATION_ID = "conversation_id";
        public static final String DEVICE_ID = "device_id";
        public static final String DISPLAYED_TIMESTAMP = "displayed_timestamp";
        public static final String DISPOSITION_NOTIFICATION_STATUS = "disposition_notification_status";
        public static final String FLAG_MASK = "flag_mask";
        public static final String ID = "_id";
        public static final String IMDN_MESSAGE_ID = "imdn_message_id";
        public static final String IMDN_ORIGINAL_TO = "imdn_original_to";
        public static final String IS_BROADCAST_MSG = "is_broadcast_msg";
        public static final String IS_VM2TXT_MSG = "is_vm2txt_msg";
        public static final String MAAP_TRAFFIC_TYPE = "maap_traffic_type";
        public static final String MESSAGE_ISSLM = "message_isslm";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String MESSAGING_TECH = "messaging_tech";
        public static final String NOTIFICATION_DISPOSITION_MASK = "notification_disposition_mask";
        public static final String NOTIFICATION_STATUS = "notification_status";
        public static final String NOT_DISPLAYED_COUNTER = "not_displayed_counter";
        public static final String REFERENCE_ID = "reference_id";
        public static final String REFERENCE_TYPE = "reference_type";
        public static final String REFERENCE_VALUE = "reference_value";
        public static final String REQUEST_MESSAGE_ID = "request_message_id";
        public static final String REVOCATION_STATUS = "revocation_status";
        public static final String SENT_TIMESTAMP = "sent_timestamp";
        public static final String SIM_IMSI = "sim_imsi";
        public static final String STATUS = "status";
        public static final String SUGGESTION = "suggestion";
    }

    public static final class MessageNotification {
        public static final String ID = "id";
        public static final String IMDN_ID = "imdn_id";
        public static final String MESSAGE_ID = "message_id";
        public static final String SENDER_URI = "sender_uri";
        public static final String STATUS = "status";
        public static final String TIMESTAMP = "timestamp";
    }

    public static final class Participant {
        public static final String ALIAS = "alias";
        public static final String CHAT_ID = "chat_id";
        public static final String ID = "_id";
        public static final String PARTICIPANT_STATUS = "status";
        public static final String TYPE = "type";
        public static final String URI = "uri";
    }
}
