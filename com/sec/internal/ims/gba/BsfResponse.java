package com.sec.internal.ims.gba;

import com.google.gson.annotations.SerializedName;

public class BsfResponse {
    @SerializedName("btid")
    private String mBtid;
    @SerializedName("lifetime")
    private String mLifetime;
    @SerializedName("@xmlns")
    private String mXmlns;

    public String getmXmlns() {
        return this.mXmlns;
    }

    public String getBtid() {
        return this.mBtid;
    }

    public String getLifetime() {
        return this.mLifetime;
    }
}
