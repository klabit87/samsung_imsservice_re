package com.google.android.gms.common.util;

import android.content.Context;

public final class zzj {
    private static Boolean zzgkq;
    private static Boolean zzgkr;
    private static Boolean zzgks;
    private static Boolean zzgkt;
    private static Boolean zzgku;

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        if (zzgkr.booleanValue() != false) goto L_0x003e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean zza(android.content.res.Resources r4) {
        /*
            r0 = 0
            if (r4 != 0) goto L_0x0004
            return r0
        L_0x0004:
            java.lang.Boolean r1 = zzgkq
            if (r1 != 0) goto L_0x0045
            android.content.res.Configuration r1 = r4.getConfiguration()
            int r1 = r1.screenLayout
            r1 = r1 & 15
            r2 = 3
            r3 = 1
            if (r1 <= r2) goto L_0x0016
            r1 = r3
            goto L_0x0017
        L_0x0016:
            r1 = r0
        L_0x0017:
            if (r1 != 0) goto L_0x003e
            java.lang.Boolean r1 = zzgkr
            if (r1 != 0) goto L_0x0036
            android.content.res.Configuration r4 = r4.getConfiguration()
            int r1 = r4.screenLayout
            r1 = r1 & 15
            if (r1 > r2) goto L_0x002f
            int r4 = r4.smallestScreenWidthDp
            r1 = 600(0x258, float:8.41E-43)
            if (r4 < r1) goto L_0x002f
            r4 = r3
            goto L_0x0030
        L_0x002f:
            r4 = r0
        L_0x0030:
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r4)
            zzgkr = r4
        L_0x0036:
            java.lang.Boolean r4 = zzgkr
            boolean r4 = r4.booleanValue()
            if (r4 == 0) goto L_0x003f
        L_0x003e:
            r0 = r3
        L_0x003f:
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r0)
            zzgkq = r4
        L_0x0045:
            java.lang.Boolean r4 = zzgkq
            boolean r4 = r4.booleanValue()
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.util.zzj.zza(android.content.res.Resources):boolean");
    }

    public static boolean zzcu(Context context) {
        if (zzgks == null) {
            zzgks = Boolean.valueOf(zzs.zzanw() && context.getPackageManager().hasSystemFeature("android.hardware.type.watch"));
        }
        return zzgks.booleanValue();
    }

    public static boolean zzcv(Context context) {
        return (!zzs.isAtLeastN() || zzcw(context)) && zzcu(context);
    }

    public static boolean zzcw(Context context) {
        if (zzgkt == null) {
            zzgkt = Boolean.valueOf(zzs.zzanx() && context.getPackageManager().hasSystemFeature("cn.google"));
        }
        return zzgkt.booleanValue();
    }

    public static boolean zzcx(Context context) {
        if (zzgku == null) {
            zzgku = Boolean.valueOf(context.getPackageManager().hasSystemFeature("android.hardware.type.iot") || context.getPackageManager().hasSystemFeature("android.hardware.type.embedded"));
        }
        return zzgku.booleanValue();
    }
}
