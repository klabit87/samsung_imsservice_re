package com.sec.internal.ims.gba;

public class BootstrappedSa {
    private String mBtid;
    private String mGbaKey;

    public BootstrappedSa(String gbaKey, String btid) {
        this.mGbaKey = gbaKey;
        this.mBtid = btid;
    }

    public String getGbaKey() {
        return this.mGbaKey;
    }

    public String getBtid() {
        return this.mBtid;
    }

    public String toString() {
        return "btid:" + getBtid() + ", gbaKey:" + getGbaKey();
    }
}
