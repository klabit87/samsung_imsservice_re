package com.sec.internal.constants.ims.gls;

import com.sec.internal.log.IMSLog;

public class LocationInfo {
    public String mA1 = "";
    public String mA3 = "";
    public String mA6 = "";
    public String mAccuracy = "";
    public String mAltitude = "";
    public String mCountry = "";
    public String mDeviceId = "";
    public String mHNO = "";
    public String mLatitude = "";
    public String mLocationTime = "";
    public String mLongitude = "";
    public String mOS = "";
    public String mPC = "";
    public String mProviderType = "";
    public String mRadiusUOM = "";
    public String mRetentionExpires = "";
    public String mSRSName = "";

    public String toString() {
        if (IMSLog.isShipBuild()) {
            return "mCountry = " + this.mCountry;
        }
        return "mLatitude = " + this.mLatitude + ", mLongitude = " + this.mLongitude + ", mAltitude = " + this.mAltitude + ", mAccuracy = " + this.mAccuracy + ", mProviderType = " + this.mProviderType + ", mRetentionExpires = " + this.mRetentionExpires + ", mSRSName = " + this.mSRSName + ", mRadiusUOM = " + this.mRadiusUOM + ", mOS = " + this.mOS + ", mDeviceId = " + this.mDeviceId + ", mCountry = " + this.mCountry + ", mA1 = " + this.mA1 + ", mA3 = " + this.mA3 + ", mA6 = " + this.mA6 + ", mHNO = " + this.mHNO + ", mPC = " + this.mPC + ", mLocationTime = " + this.mLocationTime;
    }
}
