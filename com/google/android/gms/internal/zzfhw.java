package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;

public final class zzfhw extends zzfhu<zzfhw, zza> implements zzfje {
    private static volatile zzfjl<zzfhw> zzbbm;
    /* access modifiers changed from: private */
    public static final zzfhw zzpqj;
    private int zzpqi;

    public static final class zza extends zzfhu.zza<zzfhw, zza> implements zzfje {
        private zza() {
            super(zzfhw.zzpqj);
        }

        /* synthetic */ zza(zzfhx zzfhx) {
            this();
        }

        public final zza zzmj(int i) {
            zzczv();
            ((zzfhw) this.zzppl).setValue(i);
            return this;
        }
    }

    static {
        zzfhw zzfhw = new zzfhw();
        zzpqj = zzfhw;
        zzfhw.zza(zzfhu.zzg.zzppw, (Object) null, (Object) null);
        zzfhw.zzpph.zzbkr();
    }

    private zzfhw() {
    }

    /* access modifiers changed from: private */
    public final void setValue(int i) {
        this.zzpqi = i;
    }

    public static zza zzdaa() {
        return (zza) ((zzfhu.zza) zzpqj.zza(zzfhu.zzg.zzppy, (Object) null, (Object) null));
    }

    public static zzfhw zzdab() {
        return zzpqj;
    }

    public final int getValue() {
        return this.zzpqi;
    }

    /* access modifiers changed from: protected */
    public final Object zza(int i, Object obj, Object obj2) {
        boolean z;
        boolean z2 = true;
        switch (zzfhx.zzbbk[i - 1]) {
            case 1:
                return new zzfhw();
            case 2:
                return zzpqj;
            case 3:
                return null;
            case 4:
                return new zza((zzfhx) null);
            case 5:
                zzfhu.zzh zzh = (zzfhu.zzh) obj;
                zzfhw zzfhw = (zzfhw) obj2;
                boolean z3 = this.zzpqi != 0;
                int i2 = this.zzpqi;
                if (zzfhw.zzpqi == 0) {
                    z2 = false;
                }
                this.zzpqi = zzh.zza(z3, i2, z2, zzfhw.zzpqi);
                return this;
            case 6:
                zzfhb zzfhb = (zzfhb) obj;
                if (((zzfhm) obj2) != null) {
                    boolean z4 = false;
                    while (!z4) {
                        try {
                            int zzcxx = zzfhb.zzcxx();
                            if (zzcxx != 0) {
                                if (zzcxx != 8) {
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
                                    this.zzpqi = zzfhb.zzcya();
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
                    synchronized (zzfhw.class) {
                        if (zzbbm == null) {
                            zzbbm = new zzfhu.zzb(zzpqj);
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
        return zzpqj;
    }

    public final void zza(zzfhg zzfhg) throws IOException {
        int i = this.zzpqi;
        if (i != 0) {
            zzfhg.zzad(1, i);
        }
        this.zzpph.zza(zzfhg);
    }

    public final int zzhs() {
        int i = this.zzppi;
        if (i != -1) {
            return i;
        }
        int i2 = this.zzpqi;
        int i3 = 0;
        if (i2 != 0) {
            i3 = 0 + zzfhg.zzag(1, i2);
        }
        int zzhs = i3 + this.zzpph.zzhs();
        this.zzppi = zzhs;
        return zzhs;
    }
}
