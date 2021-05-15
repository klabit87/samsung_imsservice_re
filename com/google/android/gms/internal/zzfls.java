package com.google.android.gms.internal;

import java.io.IOException;

public abstract class zzfls {
    protected volatile int zzpnr = -1;

    public static final <T extends zzfls> T zza(T t, byte[] bArr) throws zzflr {
        return zza(t, bArr, 0, bArr.length);
    }

    private static <T extends zzfls> T zza(T t, byte[] bArr, int i, int i2) throws zzflr {
        try {
            zzflj zzo = zzflj.zzo(bArr, 0, i2);
            t.zza(zzo);
            zzo.zzlf(0);
            return t;
        } catch (zzflr e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).", e2);
        }
    }

    public static final byte[] zzc(zzfls zzfls) {
        int zzhs = zzfls.zzhs();
        byte[] bArr = new byte[zzhs];
        try {
            zzflk zzp = zzflk.zzp(bArr, 0, zzhs);
            zzfls.zza(zzp);
            zzp.zzcyx();
            return bArr;
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public String toString() {
        return zzflt.zzd(this);
    }

    public abstract zzfls zza(zzflj zzflj) throws IOException;

    public void zza(zzflk zzflk) throws IOException {
    }

    /* renamed from: zzdcl */
    public zzfls clone() throws CloneNotSupportedException {
        return (zzfls) super.clone();
    }

    public final int zzdcr() {
        if (this.zzpnr < 0) {
            zzhs();
        }
        return this.zzpnr;
    }

    public final int zzhs() {
        int zzq = zzq();
        this.zzpnr = zzq;
        return zzq;
    }

    /* access modifiers changed from: protected */
    public int zzq() {
        return 0;
    }
}
