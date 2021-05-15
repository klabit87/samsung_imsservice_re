package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data;

import com.google.gson.annotations.SerializedName;
import java.net.URL;

public class TmoGcmMessage {
    @SerializedName("call-duration")
    public String call_duration;
    @SerializedName("call-status")
    public String call_status;
    @SerializedName("call-timestamp")
    public String call_timestamp;
    @SerializedName("call-type")
    public String call_type;
    @SerializedName("client-correlator")
    public String client_correlator;
    @SerializedName("content")
    public RcsContent[] content;
    public String direction;
    public String emailAddress;
    @SerializedName("failed-rcpt-list")
    public String failed_rcpt_list;
    public String folderSyncPath;
    public URL folderURL;
    public String id;
    @SerializedName("imdn-message-id")
    public String imdn_message_id;
    @SerializedName("message-id")
    public String message_id;
    @SerializedName("message-time")
    public String message_time;
    public URL objectIconURL;
    public URL objectURL;
    @SerializedName("participating-device")
    public String participating_device;
    public String reassembled;
    public TmoPushNotificationRecipients[] recipients;
    public String sender;
    public String status;
    public String store;
    public String subject;
    @SerializedName("thread-id")
    public String thread_id;
    @SerializedName("pin")
    public String vvmPin;
}
