package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy;

import android.content.Context;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MnoNsdsConfigStrategyCreator {
    private static final String LOG_TAG = MnoNsdsConfigStrategyCreator.class.getSimpleName();
    private static Map<Mno, Class<?>> sMnoSpecificStrategyGenerator;
    private static Map<Integer, IMnoNsdsConfigStrategy> sMnoStrategy = new ConcurrentHashMap();

    static {
        initMnoSpecificStrategy();
    }

    private static void initMnoSpecificStrategy() {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        sMnoSpecificStrategyGenerator = concurrentHashMap;
        concurrentHashMap.put(Mno.TMOUS, TmoNsdsConfigStrategy.class);
    }

    public static synchronized void updateMnoStrategy(Context ctx, int phoneId) {
        synchronized (MnoNsdsConfigStrategyCreator.class) {
            IMSLog.i(LOG_TAG, phoneId, "updateMnoStrategy : onSimReady");
            sMnoStrategy.remove(Integer.valueOf(phoneId));
            sMnoStrategy.put(Integer.valueOf(phoneId), createMnoStrategy(ctx, phoneId));
        }
    }

    public static synchronized IMnoNsdsConfigStrategy getMnoStrategy(int phoneId) {
        IMnoNsdsConfigStrategy mnoNsdsConfigStrategy;
        synchronized (MnoNsdsConfigStrategyCreator.class) {
            mnoNsdsConfigStrategy = sMnoStrategy.get(Integer.valueOf(phoneId));
            if (mnoNsdsConfigStrategy == null) {
                IMSLog.i(LOG_TAG, phoneId, "MnoStrategy is not exist. Return null..");
            }
        }
        return mnoNsdsConfigStrategy;
    }

    private static synchronized IMnoNsdsConfigStrategy createMnoStrategy(Context ctx, int phoneId) {
        synchronized (MnoNsdsConfigStrategyCreator.class) {
            try {
                Mno mno = SimUtil.getSimMno(phoneId);
                String str = LOG_TAG;
                IMSLog.i(str, phoneId, "createMnoStrategy: Mno=" + mno);
                if (sMnoSpecificStrategyGenerator.containsKey(mno)) {
                    IMnoNsdsConfigStrategy iMnoNsdsConfigStrategy = (IMnoNsdsConfigStrategy) sMnoSpecificStrategyGenerator.get(mno).getConstructor(new Class[]{Context.class}).newInstance(new Object[]{ctx});
                    return iMnoNsdsConfigStrategy;
                }
            } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                String str2 = LOG_TAG;
                IMSLog.s(str2, phoneId, "Caught : " + e.getMessage());
            }
            DefaultNsdsConfigStrategy defaultNsdsConfigStrategy = new DefaultNsdsConfigStrategy(ctx);
            return defaultNsdsConfigStrategy;
        }
    }
}
