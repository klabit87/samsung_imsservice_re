package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy;

public class O2UNsdsStrategy extends DefaultNsdsMnoStrategy {
    public O2UNsdsStrategy(Context ctx) {
        super(ctx);
        this.mStrategyType = DefaultNsdsMnoStrategy.NsdsStrategyType.O2U;
        this.sMapEntitlementServices.put("vowifi", 1);
        this.sMapEntitlementServices.put("volte", 2);
    }
}
