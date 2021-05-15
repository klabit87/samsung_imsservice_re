package com.google.android.gms.internal;

import java.io.IOException;
import java.io.InputStream;
import org.xbill.DNS.TTL;

final class zzfju extends InputStream {
    private int mark;
    private zzfjt zzpsa;
    private zzfgy zzpsb;
    private int zzpsc;
    private int zzpsd;
    private int zzpse;
    private /* synthetic */ zzfjq zzpsf;

    public zzfju(zzfjq zzfjq) {
        this.zzpsf = zzfjq;
        initialize();
    }

    private final void initialize() {
        zzfjt zzfjt = new zzfjt(this.zzpsf);
        this.zzpsa = zzfjt;
        zzfgy zzfgy = (zzfgy) zzfjt.next();
        this.zzpsb = zzfgy;
        this.zzpsc = zzfgy.size();
        this.zzpsd = 0;
        this.zzpse = 0;
    }

    private final void zzdbj() {
        int i;
        if (this.zzpsb != null && this.zzpsd == (i = this.zzpsc)) {
            this.zzpse += i;
            this.zzpsd = 0;
            if (this.zzpsa.hasNext()) {
                zzfgy zzfgy = (zzfgy) this.zzpsa.next();
                this.zzpsb = zzfgy;
                this.zzpsc = zzfgy.size();
                return;
            }
            this.zzpsb = null;
            this.zzpsc = 0;
        }
    }

    private final int zzk(byte[] bArr, int i, int i2) {
        int i3 = i2;
        while (true) {
            if (i3 <= 0) {
                break;
            }
            zzdbj();
            if (this.zzpsb != null) {
                int min = Math.min(this.zzpsc - this.zzpsd, i3);
                if (bArr != null) {
                    this.zzpsb.zza(bArr, this.zzpsd, i, min);
                    i += min;
                }
                this.zzpsd += min;
                i3 -= min;
            } else if (i3 == i2) {
                return -1;
            }
        }
        return i2 - i3;
    }

    public final int available() throws IOException {
        return this.zzpsf.size() - (this.zzpse + this.zzpsd);
    }

    public final void mark(int i) {
        this.mark = this.zzpse + this.zzpsd;
    }

    public final boolean markSupported() {
        return true;
    }

    public final int read() throws IOException {
        zzdbj();
        zzfgy zzfgy = this.zzpsb;
        if (zzfgy == null) {
            return -1;
        }
        int i = this.zzpsd;
        this.zzpsd = i + 1;
        return zzfgy.zzld(i) & 255;
    }

    public final int read(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw null;
        } else if (i >= 0 && i2 >= 0 && i2 <= bArr.length - i) {
            return zzk(bArr, i, i2);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public final synchronized void reset() {
        initialize();
        zzk((byte[]) null, 0, this.mark);
    }

    public final long skip(long j) {
        if (j >= 0) {
            if (j > TTL.MAX_VALUE) {
                j = 2147483647L;
            }
            return (long) zzk((byte[]) null, 0, (int) j);
        }
        throw new IndexOutOfBoundsException();
    }
}
