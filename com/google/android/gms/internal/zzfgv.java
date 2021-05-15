package com.google.android.gms.internal;

final class zzfgv extends zzfgz {
    private final int zzpnz;
    private final int zzpoa;

    zzfgv(byte[] bArr, int i, int i2) {
        super(bArr);
        zzh(i, i + i2, bArr.length);
        this.zzpnz = i;
        this.zzpoa = i2;
    }

    public final int size() {
        return this.zzpoa;
    }

    /* access modifiers changed from: protected */
    public final void zzb(byte[] bArr, int i, int i2, int i3) {
        System.arraycopy(this.zzjwl, zzcxu() + i, bArr, i2, i3);
    }

    /* access modifiers changed from: protected */
    public final int zzcxu() {
        return this.zzpnz;
    }

    public final byte zzld(int i) {
        zzab(i, size());
        return this.zzjwl[this.zzpnz + i];
    }
}
