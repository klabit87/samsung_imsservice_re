package com.google.android.gms.internal;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public final class zzh implements zzab {
    private int zzr;
    private int zzs;
    private final int zzt;
    private final float zzu;

    public zzh() {
        this(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_CANCEL_CODE, 1, 1.0f);
    }

    private zzh(int i, int i2, float f) {
        this.zzr = NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_CANCEL_CODE;
        this.zzt = 1;
        this.zzu = 1.0f;
    }

    public final void zza(zzae zzae) throws zzae {
        boolean z = true;
        int i = this.zzs + 1;
        this.zzs = i;
        int i2 = this.zzr;
        this.zzr = (int) (((float) i2) + (((float) i2) * this.zzu));
        if (i > this.zzt) {
            z = false;
        }
        if (!z) {
            throw zzae;
        }
    }

    public final int zzb() {
        return this.zzr;
    }

    public final int zzc() {
        return this.zzs;
    }
}
