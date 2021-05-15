package com.google.android.gms.internal;

import com.google.android.gms.internal.zzflm;
import java.io.IOException;

public abstract class zzflm<M extends zzflm<M>> extends zzfls {
    protected zzflo zzpvl;

    public final <T> T zza(zzfln<M, T> zzfln) {
        zzflp zzmz;
        zzflo zzflo = this.zzpvl;
        if (zzflo == null || (zzmz = zzflo.zzmz(zzfln.tag >>> 3)) == null) {
            return null;
        }
        return zzmz.zzb(zzfln);
    }

    public void zza(zzflk zzflk) throws IOException {
        if (this.zzpvl != null) {
            for (int i = 0; i < this.zzpvl.size(); i++) {
                this.zzpvl.zzna(i).zza(zzflk);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final boolean zza(zzflj zzflj, int i) throws IOException {
        int position = zzflj.getPosition();
        if (!zzflj.zzlg(i)) {
            return false;
        }
        int i2 = i >>> 3;
        zzflu zzflu = new zzflu(i, zzflj.zzao(position, zzflj.getPosition() - position));
        zzflp zzflp = null;
        zzflo zzflo = this.zzpvl;
        if (zzflo == null) {
            this.zzpvl = new zzflo();
        } else {
            zzflp = zzflo.zzmz(i2);
        }
        if (zzflp == null) {
            zzflp = new zzflp();
            this.zzpvl.zza(i2, zzflp);
        }
        zzflp.zza(zzflu);
        return true;
    }

    /* renamed from: zzdck */
    public M clone() throws CloneNotSupportedException {
        M m = (zzflm) super.clone();
        zzflq.zza(this, (zzflm) m);
        return m;
    }

    public /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzflm) clone();
    }

    /* access modifiers changed from: protected */
    public int zzq() {
        if (this.zzpvl == null) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < this.zzpvl.size(); i2++) {
            i += this.zzpvl.zzna(i2).zzq();
        }
        return i;
    }
}
