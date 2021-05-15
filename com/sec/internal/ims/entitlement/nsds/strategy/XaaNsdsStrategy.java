package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy;

public class XaaNsdsStrategy extends DefaultNsdsMnoStrategy {
    public XaaNsdsStrategy(Context ctx) {
        super(ctx);
        this.mStrategyType = DefaultNsdsMnoStrategy.NsdsStrategyType.XAA;
    }
}
