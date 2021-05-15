package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;

final class zzfiq implements zzfjw {
    private static final zzfjb zzpqx = new zzfir();
    private final zzfjb zzpqw;

    public zzfiq() {
        this(new zzfis(zzfht.zzczp(), zzdas()));
    }

    private zzfiq(zzfjb zzfjb) {
        this.zzpqw = (zzfjb) zzfhz.zzc(zzfjb, "messageInfoFactory");
    }

    private static boolean zza(zzfja zzfja) {
        return zzfja.zzdaz() == zzfhu.zzg.zzpqc;
    }

    private static zzfjb zzdas() {
        try {
            return (zzfjb) Class.forName("com.google.protobuf.DescriptorMessageInfoFactory").getDeclaredMethod("getInstance", new Class[0]).invoke((Object) null, new Object[0]);
        } catch (Exception e) {
            return zzpqx;
        }
    }

    public final <T> zzfjv<T> zzk(Class<T> cls) {
        zzfjx.zzm(cls);
        zzfja zzj = this.zzpqw.zzj(cls);
        if (zzj.zzdba()) {
            return zzfhu.class.isAssignableFrom(cls) ? zzfjh.zza(cls, zzfjx.zzdbm(), zzfhp.zzczh(), zzj.zzdbb()) : zzfjh.zza(cls, zzfjx.zzdbk(), zzfhp.zzczi(), zzj.zzdbb());
        }
        if (zzfhu.class.isAssignableFrom(cls)) {
            boolean zza = zza(zzj);
            zzfji zzdbd = zzfjk.zzdbd();
            zzfim zzdar = zzfim.zzdar();
            zzfkn<?, ?> zzdbm = zzfjx.zzdbm();
            if (zza) {
                return zzfjg.zza(cls, zzj, zzdbd, zzdar, zzdbm, zzfhp.zzczh(), zzfiz.zzdax());
            }
            return zzfjg.zza(cls, zzj, zzdbd, zzdar, zzdbm, (zzfhn<?>) null, zzfiz.zzdax());
        }
        boolean zza2 = zza(zzj);
        zzfji zzdbc = zzfjk.zzdbc();
        zzfim zzdaq = zzfim.zzdaq();
        if (zza2) {
            return zzfjg.zza(cls, zzj, zzdbc, zzdaq, zzfjx.zzdbk(), zzfhp.zzczi(), zzfiz.zzdaw());
        }
        return zzfjg.zza(cls, zzj, zzdbc, zzdaq, zzfjx.zzdbl(), (zzfhn<?>) null, zzfiz.zzdaw());
    }
}
