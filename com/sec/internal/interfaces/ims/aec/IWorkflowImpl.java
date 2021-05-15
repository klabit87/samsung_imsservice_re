package com.sec.internal.interfaces.ims.aec;

public interface IWorkflowImpl {
    void changeConnectivity();

    void clearAkaToken();

    void clearResource();

    void dump();

    String getAkaToken();

    boolean getEntitlementForVoLte();

    boolean getEntitlementForVoWiFi();

    boolean getEntitlementInitFromApp();

    boolean getSMSoIpEntitlementStatus();

    boolean getVoLteEntitlementStatus();

    boolean getVoWiFiEntitlementStatus();

    void initWorkflow(int i, String str, String str2);

    boolean isEntitlementOngoing();

    boolean isReadyToNotifyApp();

    boolean isSharedAkaToken();

    void performEntitlement(Object obj);

    void receivedFcmNotification(String str, String str2, String str3);

    void receivedSmsNotification(String str);

    void refreshFcmToken();

    void setPsDataRoaming(boolean z);

    void setReadyToNotifyApp(boolean z);

    void setSharedAkaToken(boolean z);

    void setValidEntitlement(boolean z);

    void updateFcmToken(String str, String str2);
}
