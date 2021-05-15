package com.sec.internal.interfaces.ims.entitlement.config;

public interface IMnoNsdsConfigStrategy {
    String getEntitlementServerUrl(String str);

    int getNextOperation(int i, int i2);

    boolean isDeviceProvisioned();

    void scheduleRefreshDeviceConfig(int i);
}
