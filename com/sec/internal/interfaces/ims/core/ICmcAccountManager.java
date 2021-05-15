package com.sec.internal.interfaces.ims.core;

import java.util.List;

public interface ICmcAccountManager {

    public enum ProfileUpdateResult {
        UPDATED,
        NOT_UPDATED,
        FAILED
    }

    IRegisterTask getCmcRegisterTask(int i);

    String getCmcRelayType();

    String getCmcSaServerUrl();

    String getCurrentLineOwnerDeviceId();

    int getCurrentLineSlotIndex();

    ProfileUpdateResult getProfileUpdatedResult();

    List<String> getRegiEventNotifyHostInfo();

    boolean hasSecondaryDevice();

    boolean isCmcActivated();

    boolean isCmcDeviceActivated();

    boolean isCmcEnabled();

    boolean isCmcProfileAdded();

    boolean isSecondaryDevice();

    boolean isWifiOnly();

    void notifyCmcDeviceChanged();

    void onSimRefresh(int i);

    void setRegiEventNotifyHostInfo(List<String> list);

    void startCmcRegistration();

    void startSAService(boolean z);
}
