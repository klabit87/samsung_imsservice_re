package com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds;

import com.google.gson.annotations.SerializedName;

public class EventType {
    @SerializedName("type")
    String type;

    public String toString() {
        return this.type;
    }
}
