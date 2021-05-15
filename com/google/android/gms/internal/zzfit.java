package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfit<K, V> {
    private final V value;
    private final K zzmbd;
    private final zzfiv<K, V> zzpqz;

    private zzfit(zzfky zzfky, K k, zzfky zzfky2, V v) {
        this.zzpqz = new zzfiv<>(zzfky, k, zzfky2, v);
        this.zzmbd = k;
        this.value = v;
    }

    static <K, V> int zza(zzfiv<K, V> zzfiv, K k, V v) {
        return zzfhq.zza(zzfiv.zzpra, 1, (Object) k) + zzfhq.zza(zzfiv.zzprc, 2, (Object) v);
    }

    public static <K, V> zzfit<K, V> zza(zzfky zzfky, K k, zzfky zzfky2, V v) {
        return new zzfit<>(zzfky, k, zzfky2, v);
    }

    private static <T> T zza(zzfhb zzfhb, zzfhm zzfhm, zzfky zzfky, T t) throws IOException {
        int i = zzfiu.zzppe[zzfky.ordinal()];
        if (i == 1) {
            zzfjd zzczt = ((zzfjc) t).zzczt();
            zzfhb.zza(zzczt, zzfhm);
            return zzczt.zzczy();
        } else if (i == 2) {
            return Integer.valueOf(zzfhb.zzcyh());
        } else {
            if (i != 3) {
                return zzfhq.zza(zzfhb, zzfky, true);
            }
            throw new RuntimeException("Groups are not allowed in maps.");
        }
    }

    static <K, V> void zza(zzfhg zzfhg, zzfiv<K, V> zzfiv, K k, V v) throws IOException {
        zzfhq.zza(zzfhg, zzfiv.zzpra, 1, k);
        zzfhq.zza(zzfhg, zzfiv.zzprc, 2, v);
    }

    public final void zza(zzfhg zzfhg, int i, K k, V v) throws IOException {
        zzfhg.zzac(i, 2);
        zzfhg.zzlt(zza(this.zzpqz, k, v));
        zza(zzfhg, this.zzpqz, k, v);
    }

    public final void zza(zzfiw<K, V> zzfiw, zzfhb zzfhb, zzfhm zzfhm) throws IOException {
        int zzli = zzfhb.zzli(zzfhb.zzcym());
        K k = this.zzpqz.zzprb;
        V v = this.zzpqz.zzinq;
        while (true) {
            int zzcxx = zzfhb.zzcxx();
            if (zzcxx == 0) {
                break;
            } else if (zzcxx == (this.zzpqz.zzpra.zzdcj() | 8)) {
                k = zza(zzfhb, zzfhm, this.zzpqz.zzpra, k);
            } else if (zzcxx == (this.zzpqz.zzprc.zzdcj() | 16)) {
                v = zza(zzfhb, zzfhm, this.zzpqz.zzprc, v);
            } else if (!zzfhb.zzlg(zzcxx)) {
                break;
            }
        }
        zzfhb.zzlf(0);
        zzfhb.zzlj(zzli);
        zzfiw.put(k, v);
    }

    public final int zzb(int i, K k, V v) {
        return zzfhg.zzlw(i) + zzfhg.zzmd(zza(this.zzpqz, k, v));
    }
}
