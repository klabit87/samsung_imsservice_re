package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MnoNsdsStrategyCreator {
    private static final String LOG_TAG = MnoNsdsStrategyCreator.class.getSimpleName();
    private static Map<Integer, MnoNsdsStrategyCreator> sInstanceMap;
    private static Map<Mno, Class<?>> sMnoSpecificStrategyGenerator;
    private IMnoNsdsStrategy sMnoStrategy = null;

    private MnoNsdsStrategyCreator(Context ctx, int slotId) {
        if (sMnoSpecificStrategyGenerator == null) {
            initMnoSpecificStrategy();
        }
        this.sMnoStrategy = createMnoStrategy(ctx, slotId);
    }

    public static synchronized void resetMnoStrategy() {
        synchronized (MnoNsdsStrategyCreator.class) {
            if (sInstanceMap != null) {
                sInstanceMap.clear();
            }
        }
    }

    public static synchronized MnoNsdsStrategyCreator getInstance(Context ctx, int slotId) {
        MnoNsdsStrategyCreator mnoNsdsStrategyCreator;
        synchronized (MnoNsdsStrategyCreator.class) {
            if (sInstanceMap == null) {
                sInstanceMap = new HashMap();
            }
            if (sInstanceMap.get(Integer.valueOf(slotId)) == null) {
                sInstanceMap.put(Integer.valueOf(slotId), new MnoNsdsStrategyCreator(ctx, slotId));
            }
            mnoNsdsStrategyCreator = sInstanceMap.get(Integer.valueOf(slotId));
        }
        return mnoNsdsStrategyCreator;
    }

    private static void initMnoSpecificStrategy() {
        HashMap hashMap = new HashMap();
        sMnoSpecificStrategyGenerator = hashMap;
        hashMap.put(Mno.TMOUS, TmoNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.ATT, AttNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.TELEFONICA_UK, O2UNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.TELEFONICA_UK_LAB, O2ULabNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.GCI, XaaNsdsStrategy.class);
    }

    private IMnoNsdsStrategy createMnoStrategy(Context ctx, int slotId) {
        try {
            Mno mno = SimUtil.getSimMno(slotId);
            String str = LOG_TAG;
            IMSLog.i(str, slotId, "createMnoStrategy: Mno = " + mno);
            if (!sMnoSpecificStrategyGenerator.containsKey(mno)) {
                return null;
            }
            return (IMnoNsdsStrategy) sMnoSpecificStrategyGenerator.get(mno).getConstructor(new Class[]{Context.class}).newInstance(new Object[]{ctx});
        } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "exception" + e.getMessage());
            return null;
        }
    }

    public IMnoNsdsStrategy getMnoStrategy() {
        return this.sMnoStrategy;
    }
}
