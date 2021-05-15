package com.sec.internal.ims.cmstore.callHandling.successfullCall;

import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;

public class SuccessCallFlow {
    String mFlow = null;
    EnumProvision.ProvisionEventType mProvisionEventType = null;

    public SuccessCallFlow(String flow, EnumProvision.ProvisionEventType event) {
        this.mFlow = flow;
        this.mProvisionEventType = event;
    }
}
