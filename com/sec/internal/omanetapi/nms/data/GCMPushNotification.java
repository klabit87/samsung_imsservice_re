package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoPushNotificationRecipients;

public class GCMPushNotification {
    public String mOrigNotification;
    public NmsEventList nmsEventList;
    @SerializedName("pns-subtype")
    public String pnsSubtype;
    @SerializedName("pns-time")
    public String pnsTime;
    @SerializedName("pns-type")
    public String pnsType;
    @SerializedName("recipients")
    public TmoPushNotificationRecipients[] recipients;
    public String serviceName;
}
