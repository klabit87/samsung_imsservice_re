package com.google.android.gms.internal;

import android.content.Context;
import com.google.android.gms.common.util.zzs;

public final class zzbif {
    private static Context zzglq;
    private static Boolean zzglr;

    public static synchronized boolean zzdb(Context context) {
        boolean z;
        synchronized (zzbif.class) {
            Context applicationContext = context.getApplicationContext();
            if (zzglq == null || zzglr == null || zzglq != applicationContext) {
                zzglr = null;
                if (zzs.isAtLeastO()) {
                    z = Boolean.valueOf(applicationContext.getPackageManager().isInstantApp());
                } else {
                    try {
                        context.getClassLoader().loadClass("com.google.android.instantapps.supervisor.InstantAppsRuntime");
                        zzglr = true;
                    } catch (ClassNotFoundException e) {
                        z = false;
                    }
                    zzglq = applicationContext;
                    boolean booleanValue = zzglr.booleanValue();
                    return booleanValue;
                }
                zzglr = z;
                zzglq = applicationContext;
                boolean booleanValue2 = zzglr.booleanValue();
                return booleanValue2;
            }
            boolean booleanValue3 = zzglr.booleanValue();
            return booleanValue3;
        }
    }
}
