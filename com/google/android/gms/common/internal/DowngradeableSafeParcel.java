package com.google.android.gms.common.internal;

import com.google.android.gms.internal.zzbgl;

public abstract class DowngradeableSafeParcel extends zzbgl implements ReflectedParcelable {
    private static final Object zzgfx = new Object();
    private static ClassLoader zzgfy = null;
    private static Integer zzgfz = null;
    private boolean zzgga = false;

    private static ClassLoader zzamp() {
        synchronized (zzgfx) {
        }
        return null;
    }

    protected static Integer zzamq() {
        synchronized (zzgfx) {
        }
        return null;
    }

    protected static boolean zzgq(String str) {
        zzamp();
        return true;
    }
}
