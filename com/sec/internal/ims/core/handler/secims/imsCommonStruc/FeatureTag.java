package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;

public final class FeatureTag {
    public static final int BURN_MSG = 34;
    public static final int CALL_COMPOSER = 14;
    public static final int CARD_MSG = 41;
    public static final int CDMALESS = 8;
    public static final int CDPN = 7;
    public static final int CHAT = 20;
    public static final int CHATBOT_CHAT_SESSION = 44;
    public static final int CHATBOT_ROLE = 51;
    public static final int CHATBOT_STANDALONE_MSG = 45;
    public static final int CHATBOT_VERSION = 52;
    public static final int CHAT_CPM = 12;
    public static final int CLOUD_FILE = 32;
    public static final int CPIMEXT = 54;
    public static final int DEFERRED_CPM = 11;
    public static final int EXTENDED_BOT_MSG = 46;
    public static final int FT = 22;
    public static final int FT_CPM = 13;
    public static final int FT_HTTP = 25;
    public static final int FT_HTTP_EXTRA = 33;
    public static final int FT_STORE = 24;
    public static final int FT_THUMBNAIL = 23;
    public static final int FT_VIA_SMS = 38;
    public static final int GEOLOCATION_PULL = 29;
    public static final int GEOLOCATION_PULL_FT = 30;
    public static final int GEOLOCATION_PUSH = 31;
    public static final int GEO_VIA_SMS = 39;
    public static final int GROUP_MANAGEMENT = 36;
    public static final int INTEGRATED_MSG = 27;
    public static final int IPCALL = 0;
    public static final int IPCALL_VIDEO = 1;
    public static final int IPCALL_VIDEO_ONLY = 2;
    public static final int ISH = 18;
    public static final int LAST_SEEN_ACTIVE = 43;
    public static final int MMTEL = 9;
    public static final int MMTEL_AUDIO = 5;
    public static final int MMTEL_CALL_COMPOSER = 53;
    public static final int MMTEL_VIDEO = 6;
    public static final int MSG_REVOKE = 40;
    public static final int POST_CALL = 16;
    public static final int PRESENCE_DISCOVERY = 19;
    public static final int PUBLIC_MSG = 42;
    public static final int RCS_TELEPHONY_CS = 48;
    public static final int RCS_TELEPHONY_VOLTE = 47;
    public static final int SF_GROUP_CHAT = 21;
    public static final int SHARED_MAP = 15;
    public static final int SHARED_SKETCH = 17;
    public static final int SMS = 4;
    public static final int SOCIAL_PRESENCE = 28;
    public static final int SRVCC = 49;
    public static final int STANDALONE_MSG = 10;
    public static final int STICKER = 37;
    public static final int TEXT = 50;
    public static final int VEMOTICON = 35;
    public static final int VSH = 3;
    public static final int VSH_OUTSIDE_CALL = 26;
    public static final String[] names = {"IPCALL", "IPCALL_VIDEO", "IPCALL_VIDEO_ONLY", "VSH", "SMS", "MMTEL_AUDIO", "MMTEL_VIDEO", "CDPN", "CDMALESS", "MMTEL", "STANDALONE_MSG", "DEFERRED_CPM", "CHAT_CPM", "FT_CPM", "CALL_COMPOSER", "SHARED_MAP", "POST_CALL", "SHARED_SKETCH", "ISH", "PRESENCE_DISCOVERY", CloudMessageProviderContract.DataTypes.CHAT, "SF_GROUP_CHAT", "FT", "FT_THUMBNAIL", "FT_STORE", "FT_HTTP", "VSH_OUTSIDE_CALL", "INTEGRATED_MSG", "SOCIAL_PRESENCE", "GEOLOCATION_PULL", "GEOLOCATION_PULL_FT", "GEOLOCATION_PUSH", "CLOUD_FILE", "FT_HTTP_EXTRA", "BURN_MSG", "VEMOTICON", "GROUP_MANAGEMENT", "STICKER", "FT_VIA_SMS", "GEO_VIA_SMS", "MSG_REVOKE", "CARD_MSG", "PUBLIC_MSG", "LAST_SEEN_ACTIVE", "CHATBOT_CHAT_SESSION", "CHATBOT_STANDALONE_MSG", "EXTENDED_BOT_MSG", "RCS_TELEPHONY_VOLTE", "RCS_TELEPHONY_CS", "SRVCC", "TEXT", "CHATBOT_ROLE", "CHATBOT_VERSION", "MMTEL_CALL_COMPOSER", "CPIMEXT"};

    private FeatureTag() {
    }

    public static String name(int e) {
        return names[e];
    }
}
