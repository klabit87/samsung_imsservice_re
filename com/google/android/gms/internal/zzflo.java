package com.google.android.gms.internal;

public final class zzflo implements Cloneable {
    private static final zzflp zzpvn = new zzflp();
    private int mSize;
    private boolean zzpvo;
    private int[] zzpvp;
    private zzflp[] zzpvq;

    zzflo() {
        this(10);
    }

    private zzflo(int i) {
        this.zzpvo = false;
        int idealIntArraySize = idealIntArraySize(i);
        this.zzpvp = new int[idealIntArraySize];
        this.zzpvq = new zzflp[idealIntArraySize];
        this.mSize = 0;
    }

    private static int idealIntArraySize(int i) {
        int i2 = i << 2;
        int i3 = 4;
        while (true) {
            if (i3 >= 32) {
                break;
            }
            int i4 = (1 << i3) - 12;
            if (i2 <= i4) {
                i2 = i4;
                break;
            }
            i3++;
        }
        return i2 / 4;
    }

    private final int zznb(int i) {
        int i2 = this.mSize - 1;
        int i3 = 0;
        while (i3 <= i2) {
            int i4 = (i3 + i2) >>> 1;
            int i5 = this.zzpvp[i4];
            if (i5 < i) {
                i3 = i4 + 1;
            } else if (i5 <= i) {
                return i4;
            } else {
                i2 = i4 - 1;
            }
        }
        return ~i3;
    }

    public final /* synthetic */ Object clone() throws CloneNotSupportedException {
        int i = this.mSize;
        zzflo zzflo = new zzflo(i);
        System.arraycopy(this.zzpvp, 0, zzflo.zzpvp, 0, i);
        for (int i2 = 0; i2 < i; i2++) {
            zzflp[] zzflpArr = this.zzpvq;
            if (zzflpArr[i2] != null) {
                zzflo.zzpvq[i2] = (zzflp) zzflpArr[i2].clone();
            }
        }
        zzflo.mSize = i;
        return zzflo;
    }

    public final boolean equals(Object obj) {
        boolean z;
        boolean z2;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzflo)) {
            return false;
        }
        zzflo zzflo = (zzflo) obj;
        int i = this.mSize;
        if (i != zzflo.mSize) {
            return false;
        }
        int[] iArr = this.zzpvp;
        int[] iArr2 = zzflo.zzpvp;
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
            zzflp[] zzflpArr = this.zzpvq;
            zzflp[] zzflpArr2 = zzflo.zzpvq;
            int i3 = this.mSize;
            int i4 = 0;
            while (true) {
                if (i4 >= i3) {
                    z2 = true;
                    break;
                } else if (!zzflpArr[i4].equals(zzflpArr2[i4])) {
                    z2 = false;
                    break;
                } else {
                    i4++;
                }
            }
            if (z2) {
                return true;
            }
        }
        return false;
    }

    public final int hashCode() {
        int i = 17;
        for (int i2 = 0; i2 < this.mSize; i2++) {
            i = (((i * 31) + this.zzpvp[i2]) * 31) + this.zzpvq[i2].hashCode();
        }
        return i;
    }

    public final boolean isEmpty() {
        return this.mSize == 0;
    }

    /* access modifiers changed from: package-private */
    public final int size() {
        return this.mSize;
    }

    /* access modifiers changed from: package-private */
    public final void zza(int i, zzflp zzflp) {
        int zznb = zznb(i);
        if (zznb >= 0) {
            this.zzpvq[zznb] = zzflp;
            return;
        }
        int i2 = ~zznb;
        if (i2 < this.mSize) {
            zzflp[] zzflpArr = this.zzpvq;
            if (zzflpArr[i2] == zzpvn) {
                this.zzpvp[i2] = i;
                zzflpArr[i2] = zzflp;
                return;
            }
        }
        int i3 = this.mSize;
        if (i3 >= this.zzpvp.length) {
            int idealIntArraySize = idealIntArraySize(i3 + 1);
            int[] iArr = new int[idealIntArraySize];
            zzflp[] zzflpArr2 = new zzflp[idealIntArraySize];
            int[] iArr2 = this.zzpvp;
            System.arraycopy(iArr2, 0, iArr, 0, iArr2.length);
            zzflp[] zzflpArr3 = this.zzpvq;
            System.arraycopy(zzflpArr3, 0, zzflpArr2, 0, zzflpArr3.length);
            this.zzpvp = iArr;
            this.zzpvq = zzflpArr2;
        }
        int i4 = this.mSize;
        if (i4 - i2 != 0) {
            int[] iArr3 = this.zzpvp;
            int i5 = i2 + 1;
            System.arraycopy(iArr3, i2, iArr3, i5, i4 - i2);
            zzflp[] zzflpArr4 = this.zzpvq;
            System.arraycopy(zzflpArr4, i2, zzflpArr4, i5, this.mSize - i2);
        }
        this.zzpvp[i2] = i;
        this.zzpvq[i2] = zzflp;
        this.mSize++;
    }

    /* access modifiers changed from: package-private */
    public final zzflp zzmz(int i) {
        int zznb = zznb(i);
        if (zznb < 0) {
            return null;
        }
        zzflp[] zzflpArr = this.zzpvq;
        if (zzflpArr[zznb] == zzpvn) {
            return null;
        }
        return zzflpArr[zznb];
    }

    /* access modifiers changed from: package-private */
    public final zzflp zzna(int i) {
        return this.zzpvq[i];
    }
}
