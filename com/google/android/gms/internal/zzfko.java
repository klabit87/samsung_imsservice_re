package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;
import java.util.Arrays;

public final class zzfko {
    private static final zzfko zzpta = new zzfko(0, new int[0], new Object[0], false);
    private int count;
    private boolean zzpnq;
    private int zzppi;
    private int[] zzptb;
    private Object[] zzptc;

    private zzfko() {
        this(0, new int[8], new Object[8], true);
    }

    private zzfko(int i, int[] iArr, Object[] objArr, boolean z) {
        this.zzppi = -1;
        this.count = i;
        this.zzptb = iArr;
        this.zzptc = objArr;
        this.zzpnq = z;
    }

    static zzfko zzb(zzfko zzfko, zzfko zzfko2) {
        int i = zzfko.count + zzfko2.count;
        int[] copyOf = Arrays.copyOf(zzfko.zzptb, i);
        System.arraycopy(zzfko2.zzptb, 0, copyOf, zzfko.count, zzfko2.count);
        Object[] copyOf2 = Arrays.copyOf(zzfko.zzptc, i);
        System.arraycopy(zzfko2.zzptc, 0, copyOf2, zzfko.count, zzfko2.count);
        return new zzfko(i, copyOf, copyOf2, true);
    }

    private void zzc(int i, Object obj) {
        zzdbr();
        int i2 = this.count;
        if (i2 == this.zzptb.length) {
            int i3 = this.count + (i2 < 4 ? 8 : i2 >> 1);
            this.zzptb = Arrays.copyOf(this.zzptb, i3);
            this.zzptc = Arrays.copyOf(this.zzptc, i3);
        }
        int[] iArr = this.zzptb;
        int i4 = this.count;
        iArr[i4] = i;
        this.zzptc[i4] = obj;
        this.count = i4 + 1;
    }

    private final void zzdbr() {
        if (!this.zzpnq) {
            throw new UnsupportedOperationException();
        }
    }

    public static zzfko zzdca() {
        return zzpta;
    }

    static zzfko zzdcb() {
        return new zzfko();
    }

    public final boolean equals(Object obj) {
        boolean z;
        boolean z2;
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof zzfko)) {
            return false;
        }
        zzfko zzfko = (zzfko) obj;
        int i = this.count;
        if (i == zzfko.count) {
            int[] iArr = this.zzptb;
            int[] iArr2 = zzfko.zzptb;
            int i2 = 0;
            while (true) {
                if (i2 >= i) {
                    z = true;
                    break;
                } else if (iArr[i2] != iArr2[i2]) {
                    z = false;
                    break;
                } else {
                    i2++;
                }
            }
            if (z) {
                Object[] objArr = this.zzptc;
                Object[] objArr2 = zzfko.zzptc;
                int i3 = this.count;
                int i4 = 0;
                while (true) {
                    if (i4 >= i3) {
                        z2 = true;
                        break;
                    } else if (!objArr[i4].equals(objArr2[i4])) {
                        z2 = false;
                        break;
                    } else {
                        i4++;
                    }
                }
                return z2;
            }
        }
    }

    public final int hashCode() {
        return ((((this.count + 527) * 31) + Arrays.hashCode(this.zzptb)) * 31) + Arrays.deepHashCode(this.zzptc);
    }

    public final void zza(zzfhg zzfhg) throws IOException {
        for (int i = 0; i < this.count; i++) {
            int i2 = this.zzptb[i];
            int i3 = i2 >>> 3;
            int i4 = i2 & 7;
            if (i4 == 0) {
                zzfhg.zza(i3, ((Long) this.zzptc[i]).longValue());
            } else if (i4 == 1) {
                zzfhg.zzb(i3, ((Long) this.zzptc[i]).longValue());
            } else if (i4 == 2) {
                zzfhg.zza(i3, (zzfgs) this.zzptc[i]);
            } else if (i4 == 3) {
                zzfhg.zzac(i3, 3);
                ((zzfko) this.zzptc[i]).zza(zzfhg);
                zzfhg.zzac(i3, 4);
            } else if (i4 == 5) {
                zzfhg.zzaf(i3, ((Integer) this.zzptc[i]).intValue());
            } else {
                throw zzfie.zzdaj();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void zza(zzfli zzfli) {
        if (zzfli.zzcyz() == zzfhu.zzg.zzpqg) {
            for (int i = this.count - 1; i >= 0; i--) {
                zzfli.zzb(this.zzptb[i] >>> 3, this.zzptc[i]);
            }
            return;
        }
        for (int i2 = 0; i2 < this.count; i2++) {
            zzfli.zzb(this.zzptb[i2] >>> 3, this.zzptc[i2]);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean zzb(int i, zzfhb zzfhb) throws IOException {
        int zzcxx;
        zzdbr();
        int i2 = i >>> 3;
        int i3 = i & 7;
        if (i3 == 0) {
            zzc(i, Long.valueOf(zzfhb.zzcxz()));
            return true;
        } else if (i3 == 1) {
            zzc(i, Long.valueOf(zzfhb.zzcyb()));
            return true;
        } else if (i3 == 2) {
            zzc(i, zzfhb.zzcyf());
            return true;
        } else if (i3 == 3) {
            zzfko zzfko = new zzfko();
            do {
                zzcxx = zzfhb.zzcxx();
                if (zzcxx == 0 || !zzfko.zzb(zzcxx, zzfhb)) {
                    zzfhb.zzlf((i2 << 3) | 4);
                    zzc(i, zzfko);
                }
                zzcxx = zzfhb.zzcxx();
                break;
            } while (!zzfko.zzb(zzcxx, zzfhb));
            zzfhb.zzlf((i2 << 3) | 4);
            zzc(i, zzfko);
            return true;
        } else if (i3 == 4) {
            return false;
        } else {
            if (i3 == 5) {
                zzc(i, Integer.valueOf(zzfhb.zzcyc()));
                return true;
            }
            throw zzfie.zzdaj();
        }
    }

    public final void zzbkr() {
        this.zzpnq = false;
    }

    /* access modifiers changed from: package-private */
    public final void zzd(StringBuilder sb, int i) {
        for (int i2 = 0; i2 < this.count; i2++) {
            zzfjf.zzb(sb, i, String.valueOf(this.zzptb[i2] >>> 3), this.zzptc[i2]);
        }
    }

    public final int zzdcc() {
        int i = this.zzppi;
        if (i != -1) {
            return i;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < this.count; i3++) {
            i2 += zzfhg.zzd(this.zzptb[i3] >>> 3, (zzfgs) this.zzptc[i3]);
        }
        this.zzppi = i2;
        return i2;
    }

    public final int zzhs() {
        int i;
        int i2 = this.zzppi;
        if (i2 != -1) {
            return i2;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < this.count; i4++) {
            int i5 = this.zzptb[i4];
            int i6 = i5 >>> 3;
            int i7 = i5 & 7;
            if (i7 == 0) {
                i = zzfhg.zzd(i6, ((Long) this.zzptc[i4]).longValue());
            } else if (i7 == 1) {
                i = zzfhg.zze(i6, ((Long) this.zzptc[i4]).longValue());
            } else if (i7 == 2) {
                i = zzfhg.zzc(i6, (zzfgs) this.zzptc[i4]);
            } else if (i7 == 3) {
                i = (zzfhg.zzlw(i6) << 1) + ((zzfko) this.zzptc[i4]).zzhs();
            } else if (i7 == 5) {
                i = zzfhg.zzai(i6, ((Integer) this.zzptc[i4]).intValue());
            } else {
                throw new IllegalStateException(zzfie.zzdaj());
            }
            i3 += i;
        }
        this.zzppi = i3;
        return i3;
    }
}
