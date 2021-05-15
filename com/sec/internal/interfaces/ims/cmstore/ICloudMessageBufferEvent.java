package com.sec.internal.interfaces.ims.cmstore;

public interface ICloudMessageBufferEvent {
    public static final int EVENT_BUFFERDBREADBATCHMESSAGE_JSONSUMMARY = 25;
    public static final int EVENT_BUFFERDBREADMESSAGE_JSONSUMMARY = 24;
    public static final int EVENT_DELETEMESSAGE_JSONSUMMARY = 19;
    public static final int EVENT_DOWNLOADMESSAGE_JSONSUMMARY = 21;
    public static final int EVENT_GCM_RECEIVED = 26;
    public static final int EVENT_INITSYNC_TO_BUFFER = 1;
    public static final int EVENT_INITSYNC_TO_CHECKDUPLICATE = 2;
    public static final int EVENT_LINE_ACTIVATED = 12;
    public static final int EVENT_LINE_DEACTIVATED = 13;
    public static final int EVENT_NOTIFY_DATA = 10;
    public static final int EVENT_QUERY_FROM_MSGAPP_FAILED_MSG = 30;
    public static final int EVENT_RCS_DB_READY = 11;
    public static final int EVENT_READMESSAGE_JSONSUMMARY = 17;
    public static final int EVENT_RECEIVEDMESSAGE_JSONSUMMARY = 15;
    public static final int EVENT_RESTARTSERVICE = 14;
    public static final int EVENT_RESYNC_PENDING_MSG = 31;
    public static final int EVENT_SENDINGFAILMESSAGE_JSONSUMMARY = 23;
    public static final int EVENT_SENTMESSAGE_JSONSUMMARY = 16;
    public static final int EVENT_START_SYNC = 27;
    public static final int EVENT_STOP_SYNC = 28;
    public static final int EVENT_UNREADMESSAGE_JSONSUMMARY = 18;
    public static final int EVENT_UPDATE_FROM_CLOUD = 3;
    public static final int EVENT_UPDATE_FROM_DEVICE_IMFT = 5;
    public static final int EVENT_UPDATE_FROM_DEVICE_LEGACY = 6;
    public static final int EVENT_UPDATE_FROM_DEVICE_MSGAPP_FAILED = 29;
    public static final int EVENT_UPDATE_FROM_DEVICE_MSGAPP_FETCHED = 8;
    public static final int EVENT_UPDATE_FROM_DEVICE_SESSION_PARTCPTS = 7;
    public static final int EVENT_UPDATE_NETAPI_WORKINGSTATUS = 4;
    public static final int EVENT_UPLOADMESSAGE_JSONSUMMARY = 20;
    public static final int EVENT_WIPEOUTMESSAGE_JSONSUMMARY = 22;
}
