package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;

class PushMessageData {
    public NmsEventList nmsEventList;
    @SerializedName("pns-subtype")
    public String pnsSubtype;
    @SerializedName("pns-time")
    public String pnsTime;
    @SerializedName("pns-type")
    public String pnsType;
    @SerializedName("recipients")
    public Attribute[] recipients;
    public String serviceName;

    PushMessageData() {
    }
}
