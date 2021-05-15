package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;

public final class zzfkk extends zzfhu<zzfkk, zza> implements zzfje {
    private static volatile zzfjl<zzfkk> zzbbm;
    /* access modifiers changed from: private */
    public static final zzfkk zzpsy;
    private long zzpsw;
    private int zzpsx;

    public static final class zza extends zzfhu.zza<zzfkk, zza> implements zzfje {
        private zza() {
            super(zzfkk.zzpsy);
        }

        /* synthetic */ zza(zzfkl zzfkl) {
            this();
        }

        public final zza zzdh(long j) {
            zzczv();
            ((zzfkk) this.zzppl).zzdg(j);
            return this;
        }

        public final zza zzmt(int i) {
            zzczv();
            ((zzfkk) this.zzppl).setNanos(i);
            return this;
        }
    }

    static {
        zzfkk zzfkk = new zzfkk();
        zzpsy = zzfkk;
        zzfkk.zza(zzfhu.zzg.zzppw, (Object) null, (Object) null);
        zzfkk.zzpph.zzbkr();
    }

    private zzfkk() {
    }

    /* access modifiers changed from: private */
    public final void setNanos(int i) {
        this.zzpsx = i;
    }

    public static zza zzdbw() {
        return (zza) ((zzfhu.zza) zzpsy.zza(zzfhu.zzg.zzppy, (Object) null, (Object) null));
    }

    public static zzfkk zzdbx() {
        return zzpsy;
    }

    /* access modifiers changed from: private */
    public final void zzdg(long j) {
        this.zzpsw = j;
    }

    public final int getNanos() {
        return this.zzpsx;
    }

    public final long getSeconds() {
        return this.zzpsw;
    }

    /* access modifiers changed from: protected */
    public final Object zza(int i, Object obj, Object obj2) {
        boolean z;
        boolean z2 = true;
        switch (zzfkl.zzbbk[i - 1]) {
            case 1:
                return new zzfkk();
            case 2:
                return zzpsy;
            case 3:
                return null;
            case 4:
                return new zza((zzfkl) null);
            case 5:
                zzfhu.zzh zzh = (zzfhu.zzh) obj;
                zzfkk zzfkk = (zzfkk) obj2;
                this.zzpsw = zzh.zza(this.zzpsw != 0, this.zzpsw, zzfkk.zzpsw != 0, zzfkk.zzpsw);
                boolean z3 = this.zzpsx != 0;
                int i2 = this.zzpsx;
                if (zzfkk.zzpsx == 0) {
                    z2 = false;
                }
                this.zzpsx = zzh.zza(z3, i2, z2, zzfkk.zzpsx);
                return this;
            case 6:
                zzfhb zzfhb = (zzfhb) obj;
                if (((zzfhm) obj2) != null) {
                    boolean z4 = false;
                    while (!z4) {
                        try {
                            int zzcxx = zzfhb.zzcxx();
                            if (zzcxx != 0) {
                                if (zzcxx == 8) {
                                    this.zzpsw = zzfhb.zzcxz();
                                } else if (zzcxx != 16) {
                                    if ((zzcxx & 7) == 4) {
                                        z = false;
                                    } else {
                                        if (this.zzpph == zzfko.zzdca()) {
                                            this.zzpph = zzfko.zzdcb();
                                        }
                                        z = this.zzpph.zzb(zzcxx, zzfhb);
                                    }
                                    if (!z) {
                                    }
                                } else {
                                    this.zzpsx = zzfhb.zzcya();
                                }
                            }
                            z4 = true;
                        } catch (zzfie e) {
                            throw new RuntimeException(e.zzi(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new zzfie(e2.getMessage()).zzi(this));
                        }
                    }
                    break;
                } else {
                    throw null;
                }
            case 7:
                break;
            case 8:
                if (zzbbm == null) {
                    synchronized (zzfkk.class) {
                        if (zzbbm == null) {
                            zzbbm = new zzfhu.zzb(zzpsy);
                        }
                    }
                }
                return zzbbm;
            case 9:
                return (byte) 1;
            case 10:
                return null;
            default:
                throw new UnsupportedOperationException();
        }
        return zzpsy;
    }

    public final void zza(zzfhg zzfhg) throws IOException {
        long j = this.zzpsw;
        if (j != 0) {
            zzfhg.zza(1, j);
        }
        int i = this.zzpsx;
        if (i != 0) {
            zzfhg.zzad(2, i);
        }
        this.zzpph.zza(zzfhg);
    }

    public final int zzhs() {
        int i = this.zzppi;
        if (i != -1) {
            return i;
        }
        long j = this.zzpsw;
        int i2 = 0;
        if (j != 0) {
            i2 = 0 + zzfhg.zzc(1, j);
        }
        int i3 = this.zzpsx;
        if (i3 != 0) {
            i2 += zzfhg.zzag(2, i3);
        }
        int zzhs = i2 + this.zzpph.zzhs();
        this.zzppi = zzhs;
        return zzhs;
    }
}
