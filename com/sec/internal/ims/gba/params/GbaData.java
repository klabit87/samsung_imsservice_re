package com.sec.internal.ims.gba.params;

public class GbaData {
    String cipkey;
    String intkey;
    String password;
    int phoneId = 0;

    public GbaData(String password2, String cipKey, String intKey) {
        this.password = password2;
        this.cipkey = cipKey;
        this.intkey = intKey;
    }

    public String getPassword() {
        return this.password;
    }

    public String getCipkey() {
        return this.cipkey;
    }

    public String getIntkey() {
        return this.intkey;
    }

    public void setPhoneId(int phoneId2) {
        this.phoneId = phoneId2;
    }

    public int getPhoneId() {
        return this.phoneId;
    }
}
