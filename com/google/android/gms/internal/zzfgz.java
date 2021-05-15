package com.google.android.gms.internal;

import java.io.IOException;

class zzfgz extends zzfgy {
    protected final byte[] zzjwl;

    zzfgz(byte[] bArr) {
        this.zzjwl = bArr;
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfgs) || size() != ((zzfgs) obj).size()) {
            return false;
        }
        if (size() == 0) {
            return true;
        }
        if (!(obj instanceof zzfgz)) {
            return obj.equals(this);
        }
        zzfgz zzfgz = (zzfgz) obj;
        int zzcxt = zzcxt();
        int zzcxt2 = zzfgz.zzcxt();
        if (zzcxt == 0 || zzcxt2 == 0 || zzcxt == zzcxt2) {
            return zza(zzfgz, 0, size());
        }
        return false;
    }

    public int size() {
        return this.zzjwl.length;
    }

    /* access modifiers changed from: package-private */
    public final void zza(zzfgr zzfgr) throws IOException {
        zzfgr.zze(this.zzjwl, zzcxu(), size());
    }

    /* access modifiers changed from: package-private */
    public final boolean zza(zzfgs zzfgs, int i, int i2) {
        if (i2 <= zzfgs.size()) {
            int i3 = i + i2;
            if (i3 > zzfgs.size()) {
                int size = zzfgs.size();
                StringBuilder sb = new StringBuilder(59);
                sb.append("Ran off end of other: ");
                sb.append(i);
                sb.append(", ");
                sb.append(i2);
                sb.append(", ");
                sb.append(size);
                throw new IllegalArgumentException(sb.toString());
            } else if (!(zzfgs instanceof zzfgz)) {
                return zzfgs.zzaa(i, i3).equals(zzaa(0, i2));
            } else {
                zzfgz zzfgz = (zzfgz) zzfgs;
                byte[] bArr = this.zzjwl;
                byte[] bArr2 = zzfgz.zzjwl;
                int zzcxu = zzcxu() + i2;
                int zzcxu2 = zzcxu();
                int zzcxu3 = zzfgz.zzcxu() + i;
                while (zzcxu2 < zzcxu) {
                    if (bArr[zzcxu2] != bArr2[zzcxu3]) {
                        return false;
                    }
                    zzcxu2++;
                    zzcxu3++;
                }
                return true;
            }
        } else {
            int size2 = size();
            StringBuilder sb2 = new StringBuilder(40);
            sb2.append("Length too large: ");
            sb2.append(i2);
            sb2.append(size2);
            throw new IllegalArgumentException(sb2.toString());
        }
    }

    public final zzfgs zzaa(int i, int i2) {
        int zzh = zzh(i, i2, size());
        return zzh == 0 ? zzfgs.zzpnw : new zzfgv(this.zzjwl, zzcxu() + i, zzh);
    }

    /* access modifiers changed from: protected */
    public void zzb(byte[] bArr, int i, int i2, int i3) {
        System.arraycopy(this.zzjwl, i, bArr, i2, i3);
    }

    public final zzfhb zzcxq() {
        return zzfhb.zzb(this.zzjwl, zzcxu(), size(), true);
    }

    /* access modifiers changed from: protected */
    public int zzcxu() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public final int zzg(int i, int i2, int i3) {
        return zzfhz.zza(i, this.zzjwl, zzcxu() + i2, i3);
    }

    public byte zzld(int i) {
        return this.zzjwl[i];
    }
}
