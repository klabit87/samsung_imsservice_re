package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data;

import com.google.gson.annotations.SerializedName;

public class RcsContent {
    public String charset;
    public String content;
    @SerializedName("content-duration")
    public String content_duration;
    @SerializedName("content-id")
    public String content_id;
    @SerializedName("content-size")
    public String content_size;
    @SerializedName("content-transfer-encoding")
    public String content_transfer_encoding;
    @SerializedName("content-type")
    public String content_type;
    @SerializedName("rcs-data")
    public RcsData rcsdata;
}
