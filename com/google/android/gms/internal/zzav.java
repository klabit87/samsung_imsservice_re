package com.google.android.gms.internal;

import java.io.UnsupportedEncodingException;

public class zzav extends zzr<String> {
    private final Object mLock = new Object();
    private zzz<String> zzci;

    public zzav(int i, String str, zzz<String> zzz, zzy zzy) {
        super(i, str, zzy);
        this.zzci = zzz;
    }

    /* access modifiers changed from: protected */
    public final zzx<String> zza(zzp zzp) {
        String str;
        try {
            str = new String(zzp.data, zzap.zzb(zzp.zzab));
        } catch (UnsupportedEncodingException e) {
            str = new String(zzp.data);
        }
        return zzx.zza(str, zzap.zzb(zzp));
    }

    /* access modifiers changed from: protected */
    /* renamed from: zzh */
    public void zza(String str) {
        zzz<String> zzz;
        synchronized (this.mLock) {
            zzz = this.zzci;
        }
        if (zzz != null) {
            zzz.zzb(str);
        }
    }
}
