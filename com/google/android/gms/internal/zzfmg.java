package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import java.io.IOException;

public final class zzfmg extends zzfhu<zzfmg, zza> implements zzfje {
    private static volatile zzfjl<zzfmg> zzbbm;
    /* access modifiers changed from: private */
    public static final zzfmg zzpxv;
    private int zzmjb;
    private int zzpxs;
    private String zzpxt = "";
    private zzfid<zzfgp> zzpxu = zzczs();

    public static final class zza extends zzfhu.zza<zzfmg, zza> implements zzfje {
        private zza() {
            super(zzfmg.zzpxv);
        }

        /* synthetic */ zza(zzfmh zzfmh) {
            this();
        }
    }

    static {
        zzfmg zzfmg = new zzfmg();
        zzpxv = zzfmg;
        zzfmg.zza(zzfhu.zzg.zzppw, (Object) null, (Object) null);
        zzfmg.zzpph.zzbkr();
    }

    private zzfmg() {
    }

    public static zzfmg zzdcu() {
        return zzpxv;
    }

    public final int getCode() {
        return this.zzpxs;
    }

    public final String getMessage() {
        return this.zzpxt;
    }

    /* access modifiers changed from: protected */
    public final Object zza(int i, Object obj, Object obj2) {
        boolean z = false;
        switch (zzfmh.zzbbk[i - 1]) {
            case 1:
                return new zzfmg();
            case 2:
                return zzpxv;
            case 3:
                this.zzpxu.zzbkr();
                return null;
            case 4:
                return new zza((zzfmh) null);
            case 5:
                zzfhu.zzh zzh = (zzfhu.zzh) obj;
                zzfmg zzfmg = (zzfmg) obj2;
                boolean z2 = this.zzpxs != 0;
                int i2 = this.zzpxs;
                if (zzfmg.zzpxs != 0) {
                    z = true;
                }
                this.zzpxs = zzh.zza(z2, i2, z, zzfmg.zzpxs);
                this.zzpxt = zzh.zza(!this.zzpxt.isEmpty(), this.zzpxt, true ^ zzfmg.zzpxt.isEmpty(), zzfmg.zzpxt);
                this.zzpxu = zzh.zza(this.zzpxu, zzfmg.zzpxu);
                if (zzh == zzfhu.zzf.zzppq) {
                    this.zzmjb |= zzfmg.zzmjb;
                }
                return this;
            case 6:
                zzfhb zzfhb = (zzfhb) obj;
                zzfhm zzfhm = (zzfhm) obj2;
                if (zzfhm != null) {
                    while (!z) {
                        try {
                            int zzcxx = zzfhb.zzcxx();
                            if (zzcxx != 0) {
                                if (zzcxx == 8) {
                                    this.zzpxs = zzfhb.zzcya();
                                } else if (zzcxx == 18) {
                                    this.zzpxt = zzfhb.zzcye();
                                } else if (zzcxx == 26) {
                                    if (!this.zzpxu.zzcxk()) {
                                        zzfid<zzfgp> zzfid = this.zzpxu;
                                        int size = zzfid.size();
                                        this.zzpxu = zzfid.zzmo(size == 0 ? 10 : size << 1);
                                    }
                                    this.zzpxu.add((zzfgp) zzfhb.zza(zzfgp.zzcxo(), zzfhm));
                                } else if (!zza(zzcxx, zzfhb)) {
                                }
                            }
                            z = true;
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
                    synchronized (zzfmg.class) {
                        if (zzbbm == null) {
                            zzbbm = new zzfhu.zzb(zzpxv);
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
        return zzpxv;
    }

    public final void zza(zzfhg zzfhg) throws IOException {
        int i = this.zzpxs;
        if (i != 0) {
            zzfhg.zzad(1, i);
        }
        if (!this.zzpxt.isEmpty()) {
            zzfhg.zzp(2, this.zzpxt);
        }
        for (int i2 = 0; i2 < this.zzpxu.size(); i2++) {
            zzfhg.zza(3, (zzfjc) this.zzpxu.get(i2));
        }
        this.zzpph.zza(zzfhg);
    }

    public final int zzhs() {
        int i = this.zzppi;
        if (i != -1) {
            return i;
        }
        int i2 = this.zzpxs;
        int zzag = i2 != 0 ? zzfhg.zzag(1, i2) + 0 : 0;
        if (!this.zzpxt.isEmpty()) {
            zzag += zzfhg.zzq(2, this.zzpxt);
        }
        for (int i3 = 0; i3 < this.zzpxu.size(); i3++) {
            zzag += zzfhg.zzc(3, (zzfjc) this.zzpxu.get(i3));
        }
        int zzhs = zzag + this.zzpph.zzhs();
        this.zzppi = zzhs;
        return zzhs;
    }
}
