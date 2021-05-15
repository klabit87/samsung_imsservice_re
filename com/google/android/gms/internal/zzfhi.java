package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;

final class zzfhi implements zzfli {
    private final zzfhg zzpob;

    private zzfhi(zzfhg zzfhg) {
        zzfhg zzfhg2 = (zzfhg) zzfhz.zzc(zzfhg, "output");
        this.zzpob = zzfhg2;
        zzfhg2.zzpoq = this;
    }

    public static zzfhi zzb(zzfhg zzfhg) {
        return zzfhg.zzpoq != null ? zzfhg.zzpoq : new zzfhi(zzfhg);
    }

    public final void zzb(int i, Object obj) {
        try {
            if (obj instanceof zzfgs) {
                this.zzpob.zzb(i, (zzfgs) obj);
            } else {
                this.zzpob.zzb(i, (zzfjc) obj);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final int zzcyz() {
        return zzfhu.zzg.zzpqf;
    }
}
