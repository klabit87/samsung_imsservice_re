package com.sec.internal.ims.entitlement.softphone;

import java.util.ArrayList;
import java.util.List;

public class ImsNetworkIdentity {
    private List<String> mAddressList = new ArrayList();
    private String mAppId = null;
    private String mImpi = null;
    private String mImpu = null;

    public ImsNetworkIdentity() {
    }

    public ImsNetworkIdentity(String impi, String impu, List<String> addressList, String appId) {
        this.mImpi = impi;
        this.mImpu = impu;
        this.mAddressList = addressList;
        this.mAppId = appId;
    }

    public String getImpi() {
        return this.mImpi;
    }

    public String getImpu() {
        return this.mImpu;
    }

    public List<String> getAddressList() {
        return this.mAddressList;
    }

    public String getAppId() {
        return this.mAppId;
    }

    public boolean impiEmpty() {
        return this.mImpi == null;
    }

    public void clear() {
        this.mImpi = null;
        this.mImpu = null;
        this.mAddressList.clear();
        this.mAppId = null;
    }

    public String toString() {
        return "[impi: " + this.mImpi + " impu: " + this.mImpu + " address: " + this.mAddressList + " app-id: " + this.mAppId + "]";
    }
}
