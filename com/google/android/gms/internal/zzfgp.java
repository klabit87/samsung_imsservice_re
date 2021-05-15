package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;

public final class zzfgp extends zzfhu<zzfgp, zza> implements zzfje {
    private static volatile zzfjl<zzfgp> zzbbm;
    /* access modifiers changed from: private */
    public static final zzfgp zzpnv;
    private String zzmid = "";
    private zzfgs zzmie = zzfgs.zzpnw;

    public static final class zza extends zzfhu.zza<zzfgp, zza> implements zzfje {
        private zza() {
            super(zzfgp.zzpnv);
        }

        /* synthetic */ zza(zzfgq zzfgq) {
            this();
        }
    }

    static {
        zzfgp zzfgp = new zzfgp();
        zzpnv = zzfgp;
        zzfgp.zza(zzfhu.zzg.zzppw, (Object) null, (Object) null);
        zzfgp.zzpph.zzbkr();
    }

    private zzfgp() {
    }

    public static zzfgp zzcxo() {
        return zzpnv;
    }

    /* access modifiers changed from: protected */
    public final Object zza(int i, Object obj, Object obj2) {
        boolean z;
        boolean z2 = true;
        switch (zzfgq.zzbbk[i - 1]) {
            case 1:
                return new zzfgp();
            case 2:
                return zzpnv;
            case 3:
                return null;
            case 4:
                return new zza((zzfgq) null);
            case 5:
                zzfhu.zzh zzh = (zzfhu.zzh) obj;
                zzfgp zzfgp = (zzfgp) obj2;
                this.zzmid = zzh.zza(!this.zzmid.isEmpty(), this.zzmid, !zzfgp.zzmid.isEmpty(), zzfgp.zzmid);
                boolean z3 = this.zzmie != zzfgs.zzpnw;
                zzfgs zzfgs = this.zzmie;
                if (zzfgp.zzmie == zzfgs.zzpnw) {
                    z2 = false;
                }
                this.zzmie = zzh.zza(z3, zzfgs, z2, zzfgp.zzmie);
                return this;
            case 6:
                zzfhb zzfhb = (zzfhb) obj;
                if (((zzfhm) obj2) != null) {
                    boolean z4 = false;
                    while (!z4) {
                        try {
                            int zzcxx = zzfhb.zzcxx();
                            if (zzcxx != 0) {
                                if (zzcxx == 10) {
                                    this.zzmid = zzfhb.zzcye();
                                } else if (zzcxx != 18) {
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
                                    this.zzmie = zzfhb.zzcyf();
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
                    synchronized (zzfgp.class) {
                        if (zzbbm == null) {
                            zzbbm = new zzfhu.zzb(zzpnv);
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
        return zzpnv;
    }

    public final void zza(zzfhg zzfhg) throws IOException {
        if (!this.zzmid.isEmpty()) {
            zzfhg.zzp(1, this.zzmid);
        }
        if (!this.zzmie.isEmpty()) {
            zzfhg.zza(2, this.zzmie);
        }
        this.zzpph.zza(zzfhg);
    }

    public final int zzhs() {
        int i = this.zzppi;
        if (i != -1) {
            return i;
        }
        int i2 = 0;
        if (!this.zzmid.isEmpty()) {
            i2 = 0 + zzfhg.zzq(1, this.zzmid);
        }
        if (!this.zzmie.isEmpty()) {
            i2 += zzfhg.zzc(2, this.zzmie);
        }
        int zzhs = i2 + this.zzpph.zzhs();
        this.zzppi = zzhs;
        return zzhs;
    }
}
