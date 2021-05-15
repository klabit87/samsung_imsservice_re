package com.google.android.gms.internal;

public final class zzbhg {
    private static zzbhi zzgih;

    public static synchronized zzbhi zzanc() {
        zzbhi zzbhi;
        synchronized (zzbhg.class) {
            if (zzgih == null) {
                zzgih = new zzbhh();
            }
            zzbhi = zzgih;
        }
        return zzbhi;
    }
}
