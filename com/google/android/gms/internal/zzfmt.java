package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfmt extends zzflm<zzfmt> implements Cloneable {
    private int zzpzp = -1;
    private int zzpzq = 0;

    public zzfmt() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzbo */
    public final zzfmt zza(zzflj zzflj) throws IOException {
        int i;
        while (true) {
            int zzcxx = zzflj.zzcxx();
            if (zzcxx == 0) {
                return this;
            }
            if (zzcxx == 8) {
                i = zzflj.getPosition();
                int zzcya = zzflj.zzcya();
                switch (zzcya) {
                    case -1:
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                        this.zzpzp = zzcya;
                        break;
                    default:
                        StringBuilder sb = new StringBuilder(43);
                        sb.append(zzcya);
                        sb.append(" is not a valid enum NetworkType");
                        throw new IllegalArgumentException(sb.toString());
                }
            } else if (zzcxx == 16) {
                i = zzflj.getPosition();
                try {
                    int zzcya2 = zzflj.zzcya();
                    if (zzcya2 != 100) {
                        switch (zzcya2) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                                break;
                            default:
                                StringBuilder sb2 = new StringBuilder(45);
                                sb2.append(zzcya2);
                                sb2.append(" is not a valid enum MobileSubtype");
                                throw new IllegalArgumentException(sb2.toString());
                        }
                    }
                    this.zzpzq = zzcya2;
                } catch (IllegalArgumentException e) {
                    zzflj.zzmw(i);
                    zza(zzflj, zzcxx);
                }
            } else if (!super.zza(zzflj, zzcxx)) {
                return this;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: zzddh */
    public zzfmt clone() {
        try {
            return (zzfmt) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfmt)) {
            return false;
        }
        zzfmt zzfmt = (zzfmt) obj;
        if (this.zzpzp == zzfmt.zzpzp && this.zzpzq == zzfmt.zzpzq) {
            return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfmt.zzpvl == null || zzfmt.zzpvl.isEmpty() : this.zzpvl.equals(zzfmt.zzpvl);
        }
        return false;
    }

    public final int hashCode() {
        return ((((((getClass().getName().hashCode() + 527) * 31) + this.zzpzp) * 31) + this.zzpzq) * 31) + ((this.zzpvl == null || this.zzpvl.isEmpty()) ? 0 : this.zzpvl.hashCode());
    }

    public final void zza(zzflk zzflk) throws IOException {
        int i = this.zzpzp;
        if (i != -1) {
            zzflk.zzad(1, i);
        }
        int i2 = this.zzpzq;
        if (i2 != 0) {
            zzflk.zzad(2, i2);
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfmt) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfmt) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int zzq = super.zzq();
        int i = this.zzpzp;
        if (i != -1) {
            zzq += zzflk.zzag(1, i);
        }
        int i2 = this.zzpzq;
        return i2 != 0 ? zzq + zzflk.zzag(2, i2) : zzq;
    }
}
