package com.google.android.gms.internal;

import java.util.Iterator;
import java.util.Map;

final class zzfjh<T> implements zzfjv<T> {
    private final zzfjc zzprg;
    private final zzfkn<?, ?> zzprh;
    private final boolean zzpri;
    private final zzfhn<?> zzprj;

    private zzfjh(Class<T> cls, zzfkn<?, ?> zzfkn, zzfhn<?> zzfhn, zzfjc zzfjc) {
        this.zzprh = zzfkn;
        this.zzpri = zzfhn.zzh(cls);
        this.zzprj = zzfhn;
        this.zzprg = zzfjc;
    }

    static <T> zzfjh<T> zza(Class<T> cls, zzfkn<?, ?> zzfkn, zzfhn<?> zzfhn, zzfjc zzfjc) {
        return new zzfjh<>(cls, zzfkn, zzfhn, zzfjc);
    }

    public final void zza(T t, zzfli zzfli) {
        Iterator<Map.Entry<?, Object>> it = this.zzprj.zzcr(t).iterator();
        while (it.hasNext()) {
            Map.Entry next = it.next();
            zzfhs zzfhs = (zzfhs) next.getKey();
            if (zzfhs.zzczm() != zzfld.MESSAGE || zzfhs.zzczn() || zzfhs.zzczo()) {
                throw new IllegalStateException("Found invalid MessageSet item.");
            }
            zzfli.zzb(zzfhs.zzhu(), next instanceof zzfii ? ((zzfii) next).zzdao().toByteString() : next.getValue());
        }
        zzfkn<?, ?> zzfkn = this.zzprh;
        zzfkn.zzb(zzfkn.zzcu(t), zzfli);
    }

    public final int zzct(T t) {
        zzfkn<?, ?> zzfkn = this.zzprh;
        int zzcv = zzfkn.zzcv(zzfkn.zzcu(t)) + 0;
        return this.zzpri ? zzcv + this.zzprj.zzcr(t).zzczk() : zzcv;
    }
}
