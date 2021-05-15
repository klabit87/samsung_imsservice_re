package com.samsung.android.cmcsetting;

import com.samsung.android.cmcsetting.CmcSettingManagerConstants;

public class CmcDeviceInfo {
    private CmcSettingManagerConstants.DeviceCategory mDeviceCategory = null;
    private String mDeviceId = "";
    private String mDeviceName = "";
    private CmcSettingManagerConstants.DeviceType mDeviceType = null;
    private boolean mIsActivation = false;
    private boolean mIsCallActivation = false;
    private boolean mIsCallAllowedSdByPd = false;
    private boolean mIsMessageActivation = false;
    private boolean mIsMessageAllowedSdByPd = false;

    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceName(String str) {
        this.mDeviceName = str;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceCategory(CmcSettingManagerConstants.DeviceCategory deviceCategory) {
        this.mDeviceCategory = deviceCategory;
    }

    public CmcSettingManagerConstants.DeviceCategory getDeviceCategory() {
        return this.mDeviceCategory;
    }

    public void setDeviceType(CmcSettingManagerConstants.DeviceType deviceType) {
        this.mDeviceType = deviceType;
    }

    public CmcSettingManagerConstants.DeviceType getDeviceType() {
        return this.mDeviceType;
    }

    public void setMessageAllowedSdByPd(boolean z) {
        this.mIsMessageAllowedSdByPd = z;
    }

    public boolean isMessageAllowedSdByPd() {
        return this.mIsMessageAllowedSdByPd;
    }

    public void setCallAllowedSdByPd(boolean z) {
        this.mIsCallAllowedSdByPd = z;
    }

    public boolean isCallAllowedSdByPd() {
        return this.mIsCallAllowedSdByPd;
    }

    public void setActivation(boolean z) {
        this.mIsActivation = z;
    }

    public boolean isActivation() {
        return this.mIsActivation;
    }

    public void setMessageActivation(boolean z) {
        this.mIsMessageActivation = z;
    }

    public boolean isMessageActivation() {
        return this.mIsMessageActivation;
    }

    public void setCallActivation(boolean z) {
        this.mIsCallActivation = z;
    }

    public boolean isCallActivation() {
        return this.mIsCallActivation;
    }

    public String toString() {
        return ((((((((("{" + "deviceId:" + this.mDeviceId) + ",deviceName:" + this.mDeviceName) + ",deviceCategory:" + this.mDeviceCategory) + ",deviceType:" + this.mDeviceType) + ",isCallAllowedSdByPd:" + this.mIsCallAllowedSdByPd) + ",isMessageAllowedSdByPd:" + this.mIsMessageAllowedSdByPd) + ",isActivation:" + this.mIsActivation) + ",isMessageActivation:" + this.mIsMessageActivation) + ",isCallActivation:" + this.mIsCallActivation) + "}";
    }
}
