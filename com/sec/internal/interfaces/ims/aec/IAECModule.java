package com.sec.internal.interfaces.ims.aec;

import com.sec.internal.interfaces.ims.core.ISequentialInitializable;

public interface IAECModule extends ISequentialInitializable {
    void dump();

    String getAkaToken(int i);

    boolean getSMSoIpEntitlementStatus(int i);

    boolean getVoLteEntitlementStatus(int i);

    boolean getVoWiFiEntitlementStatus(int i);

    boolean isEntitlementDisabled(int i);

    boolean isEntitlementRequired(int i);

    void triggerAutoConfigForApp(int i);
}
