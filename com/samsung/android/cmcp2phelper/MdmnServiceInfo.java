package com.samsung.android.cmcp2phelper;

public class MdmnServiceInfo {
    String deviceId;
    String lineId;
    String serviceName;

    public String toString() {
        return "com.samsung.android.cmcp2phelper.MdmnServiceInfo{serviceName='" + this.serviceName + '\'' + ", deviceId='" + this.deviceId + '\'' + '}';
    }

    public MdmnServiceInfo(String deviceId2, String lineId2) {
        this.serviceName = "samsung_cmc";
        this.lineId = lineId2;
        this.deviceId = deviceId2;
    }

    public MdmnServiceInfo(String serviceName2, String lineId2, String deviceId2) {
        this.serviceName = serviceName2;
        this.lineId = lineId2;
        this.deviceId = deviceId2;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getLineId() {
        return this.lineId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }
}
