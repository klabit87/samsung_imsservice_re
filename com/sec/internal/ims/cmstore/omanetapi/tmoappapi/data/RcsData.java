package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data;

import com.google.gson.annotations.SerializedName;

public class RcsData {
    @SerializedName("contribution-id")
    public String contribution_id;
    @SerializedName("conversation-id")
    public String conversation_id;
    @SerializedName("feature-tag")
    public String feature_tag;
    @SerializedName("p-asserted-service")
    public String p_asserted_service;
    @SerializedName("sip-call-id")
    public String sip_call_id;
}
