package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy;

public class O2ULabNsdsStrategy extends DefaultNsdsMnoStrategy {
    public O2ULabNsdsStrategy(Context ctx) {
        super(ctx);
        this.mStrategyType = DefaultNsdsMnoStrategy.NsdsStrategyType.O2U;
        this.sMapEntitlementServices.put("vowifi", 1);
        this.sMapEntitlementServices.put("volte", 2);
    }

    public String getEntitlementServerUrl(String imsi, String deviceUid) {
        return "https://ses-test.o2.co.uk:443/generic_devices";
    }
}
