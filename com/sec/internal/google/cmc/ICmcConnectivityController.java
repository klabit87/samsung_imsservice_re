package com.sec.internal.google.cmc;

public interface ICmcConnectivityController {
    void changeWifiDirectConnection(boolean z);

    boolean isEnabledWifiDirectFeature();

    void setDeviceIdInfo(String str, String str2);
}
