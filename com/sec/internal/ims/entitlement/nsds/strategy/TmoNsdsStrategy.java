package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy;

public class TmoNsdsStrategy extends DefaultNsdsMnoStrategy {
    public TmoNsdsStrategy(Context ctx) {
        super(ctx);
        this.mStrategyType = DefaultNsdsMnoStrategy.NsdsStrategyType.TMOUS;
    }
}
