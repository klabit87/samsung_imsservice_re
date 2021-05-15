package com.google.android.gms.internal;

public class zzfik {
    private static final zzfhm zzpns = zzfhm.zzczf();
    private zzfgs zzpqq;
    private volatile zzfjc zzpqr;
    private volatile zzfgs zzpqs;

    private zzfjc zzj(zzfjc zzfjc) {
        if (this.zzpqr == null) {
            synchronized (this) {
                if (this.zzpqr == null) {
                    try {
                        this.zzpqr = zzfjc;
                        this.zzpqs = zzfgs.zzpnw;
                    } catch (zzfie e) {
                        this.zzpqr = zzfjc;
                        this.zzpqs = zzfgs.zzpnw;
                    }
                }
            }
        }
        return this.zzpqr;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof zzfik)) {
            return false;
        }
        zzfik zzfik = (zzfik) obj;
        zzfjc zzfjc = this.zzpqr;
        zzfjc zzfjc2 = zzfik.zzpqr;
        return (zzfjc == null && zzfjc2 == null) ? toByteString().equals(zzfik.toByteString()) : (zzfjc == null || zzfjc2 == null) ? zzfjc != null ? zzfjc.equals(zzfik.zzj(zzfjc.zzczu())) : zzj(zzfjc2.zzczu()).equals(zzfjc2) : zzfjc.equals(zzfjc2);
    }

    public int hashCode() {
        return 1;
    }

    public final zzfgs toByteString() {
        if (this.zzpqs != null) {
            return this.zzpqs;
        }
        synchronized (this) {
            if (this.zzpqs != null) {
                zzfgs zzfgs = this.zzpqs;
                return zzfgs;
            }
            this.zzpqs = this.zzpqr == null ? zzfgs.zzpnw : this.zzpqr.toByteString();
            zzfgs zzfgs2 = this.zzpqs;
            return zzfgs2;
        }
    }

    public final int zzhs() {
        if (this.zzpqs != null) {
            return this.zzpqs.size();
        }
        if (this.zzpqr != null) {
            return this.zzpqr.zzhs();
        }
        return 0;
    }

    public final zzfjc zzk(zzfjc zzfjc) {
        zzfjc zzfjc2 = this.zzpqr;
        this.zzpqq = null;
        this.zzpqs = null;
        this.zzpqr = zzfjc;
        return zzfjc2;
    }
}
