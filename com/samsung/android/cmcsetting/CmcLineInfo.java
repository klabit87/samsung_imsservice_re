package com.samsung.android.cmcsetting;

import java.util.ArrayList;

public class CmcLineInfo {
    private String mImpu = "";
    private String mLineId = "";
    private String mLineName = "";
    private int mLineSlotIndex = -1;
    private String mMsisdn = "";
    private ArrayList<String> mNmsAddrList = null;
    private ArrayList<String> mPcscfAddrList = null;

    public void setLineId(String str) {
        this.mLineId = str;
    }

    public String getLineId() {
        return this.mLineId;
    }

    public void setLineSlotIndex(int i) {
        this.mLineSlotIndex = i;
    }

    public int getLineSlotIndex() {
        return this.mLineSlotIndex;
    }

    public void setLineName(String str) {
        this.mLineName = str;
    }

    public String getLineName() {
        return this.mLineName;
    }

    public void setMsisdn(String str) {
        this.mMsisdn = str;
    }

    public String getMsisdn() {
        return this.mMsisdn;
    }

    public void setImpu(String str) {
        this.mImpu = str;
    }

    public String getImpu() {
        return this.mImpu;
    }

    public void setNmsAddrList(ArrayList<String> arrayList) {
        this.mNmsAddrList = arrayList;
    }

    public ArrayList<String> getNmsAddrList() {
        return this.mNmsAddrList;
    }

    public void setPcscfAddrList(ArrayList<String> arrayList) {
        this.mPcscfAddrList = arrayList;
    }

    public ArrayList<String> getPcscfAddrList() {
        return this.mPcscfAddrList;
    }

    public String toString() {
        return (((((("{" + "lineId:" + this.mLineId) + ",lineSlotIndex:" + this.mLineSlotIndex) + ",lineName:" + this.mLineName) + ",impu:" + this.mImpu) + ",nmsAddr:" + this.mNmsAddrList) + ",pcscfAddrList:" + this.mPcscfAddrList) + "}";
    }
}
