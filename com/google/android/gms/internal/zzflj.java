package com.google.android.gms.internal;

import java.io.IOException;

public final class zzflj {
    private final byte[] buffer;
    private int zzpoc;
    private int zzpod = 64;
    private int zzpoe = 67108864;
    private int zzpoh;
    private int zzpoj;
    private int zzpok = Integer.MAX_VALUE;
    private final int zzpom;
    private final int zzpvi;
    private int zzpvj;
    private int zzpvk;

    private zzflj(byte[] bArr, int i, int i2) {
        this.buffer = bArr;
        this.zzpvi = i;
        int i3 = i2 + i;
        this.zzpvj = i3;
        this.zzpom = i3;
        this.zzpvk = i;
    }

    public static zzflj zzbe(byte[] bArr) {
        return zzo(bArr, 0, bArr.length);
    }

    private final void zzcyu() {
        int i = this.zzpvj + this.zzpoh;
        this.zzpvj = i;
        int i2 = this.zzpok;
        if (i > i2) {
            int i3 = i - i2;
            this.zzpoh = i3;
            this.zzpvj = i - i3;
            return;
        }
        this.zzpoh = 0;
    }

    private final byte zzcyv() throws IOException {
        int i = this.zzpvk;
        if (i != this.zzpvj) {
            byte[] bArr = this.buffer;
            this.zzpvk = i + 1;
            return bArr[i];
        }
        throw zzflr.zzdcn();
    }

    private final void zzlk(int i) throws IOException {
        if (i >= 0) {
            int i2 = this.zzpvk;
            int i3 = i2 + i;
            int i4 = this.zzpok;
            if (i3 > i4) {
                zzlk(i4 - i2);
                throw zzflr.zzdcn();
            } else if (i <= this.zzpvj - i2) {
                this.zzpvk = i2 + i;
            } else {
                throw zzflr.zzdcn();
            }
        } else {
            throw zzflr.zzdco();
        }
    }

    public static zzflj zzo(byte[] bArr, int i, int i2) {
        return new zzflj(bArr, 0, i2);
    }

    public final int getPosition() {
        return this.zzpvk - this.zzpvi;
    }

    public final byte[] readBytes() throws IOException {
        int zzcym = zzcym();
        if (zzcym < 0) {
            throw zzflr.zzdco();
        } else if (zzcym == 0) {
            return zzflv.zzpwe;
        } else {
            int i = this.zzpvj;
            int i2 = this.zzpvk;
            if (zzcym <= i - i2) {
                byte[] bArr = new byte[zzcym];
                System.arraycopy(this.buffer, i2, bArr, 0, zzcym);
                this.zzpvk += zzcym;
                return bArr;
            }
            throw zzflr.zzdcn();
        }
    }

    public final String readString() throws IOException {
        int zzcym = zzcym();
        if (zzcym < 0) {
            throw zzflr.zzdco();
        } else if (zzcym <= this.zzpvj - this.zzpvk) {
            String str = new String(this.buffer, this.zzpvk, zzcym, zzflq.UTF_8);
            this.zzpvk += zzcym;
            return str;
        } else {
            throw zzflr.zzdcn();
        }
    }

    public final void zza(zzfls zzfls) throws IOException {
        int zzcym = zzcym();
        if (this.zzpoc < this.zzpod) {
            int zzli = zzli(zzcym);
            this.zzpoc++;
            zzfls.zza(this);
            zzlf(0);
            this.zzpoc--;
            zzlj(zzli);
            return;
        }
        throw zzflr.zzdcq();
    }

    public final void zza(zzfls zzfls, int i) throws IOException {
        int i2 = this.zzpoc;
        if (i2 < this.zzpod) {
            this.zzpoc = i2 + 1;
            zzfls.zza(this);
            zzlf((i << 3) | 4);
            this.zzpoc--;
            return;
        }
        throw zzflr.zzdcq();
    }

    public final byte[] zzao(int i, int i2) {
        if (i2 == 0) {
            return zzflv.zzpwe;
        }
        byte[] bArr = new byte[i2];
        System.arraycopy(this.buffer, this.zzpvi + i, bArr, 0, i2);
        return bArr;
    }

    /* access modifiers changed from: package-private */
    public final void zzap(int i, int i2) {
        int i3 = this.zzpvk;
        int i4 = this.zzpvi;
        if (i > i3 - i4) {
            StringBuilder sb = new StringBuilder(50);
            sb.append("Position ");
            sb.append(i);
            sb.append(" is beyond current ");
            sb.append(this.zzpvk - this.zzpvi);
            throw new IllegalArgumentException(sb.toString());
        } else if (i >= 0) {
            this.zzpvk = i4 + i;
            this.zzpoj = i2;
        } else {
            StringBuilder sb2 = new StringBuilder(24);
            sb2.append("Bad position ");
            sb2.append(i);
            throw new IllegalArgumentException(sb2.toString());
        }
    }

    public final int zzcxx() throws IOException {
        if (this.zzpvk == this.zzpvj) {
            this.zzpoj = 0;
            return 0;
        }
        int zzcym = zzcym();
        this.zzpoj = zzcym;
        if (zzcym != 0) {
            return zzcym;
        }
        throw new zzflr("Protocol message contained an invalid tag (zero).");
    }

    public final long zzcxz() throws IOException {
        return zzcyr();
    }

    public final int zzcya() throws IOException {
        return zzcym();
    }

    public final boolean zzcyd() throws IOException {
        return zzcym() != 0;
    }

    public final long zzcyl() throws IOException {
        long zzcyr = zzcyr();
        return (-(zzcyr & 1)) ^ (zzcyr >>> 1);
    }

    public final int zzcym() throws IOException {
        int i;
        byte zzcyv = zzcyv();
        if (zzcyv >= 0) {
            return zzcyv;
        }
        byte b = zzcyv & Byte.MAX_VALUE;
        byte zzcyv2 = zzcyv();
        if (zzcyv2 >= 0) {
            i = zzcyv2 << 7;
        } else {
            b |= (zzcyv2 & Byte.MAX_VALUE) << 7;
            byte zzcyv3 = zzcyv();
            if (zzcyv3 >= 0) {
                i = zzcyv3 << 14;
            } else {
                b |= (zzcyv3 & Byte.MAX_VALUE) << 14;
                byte zzcyv4 = zzcyv();
                if (zzcyv4 >= 0) {
                    i = zzcyv4 << 21;
                } else {
                    byte b2 = b | ((zzcyv4 & Byte.MAX_VALUE) << 21);
                    byte zzcyv5 = zzcyv();
                    byte b3 = b2 | (zzcyv5 << 28);
                    if (zzcyv5 >= 0) {
                        return b3;
                    }
                    for (int i2 = 0; i2 < 5; i2++) {
                        if (zzcyv() >= 0) {
                            return b3;
                        }
                    }
                    throw zzflr.zzdcp();
                }
            }
        }
        return b | i;
    }

    public final int zzcyo() {
        int i = this.zzpok;
        if (i == Integer.MAX_VALUE) {
            return -1;
        }
        return i - this.zzpvk;
    }

    public final long zzcyr() throws IOException {
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            byte zzcyv = zzcyv();
            j |= ((long) (zzcyv & Byte.MAX_VALUE)) << i;
            if ((zzcyv & 128) == 0) {
                return j;
            }
        }
        throw zzflr.zzdcp();
    }

    public final int zzcys() throws IOException {
        return (zzcyv() & 255) | ((zzcyv() & 255) << 8) | ((zzcyv() & 255) << 16) | ((zzcyv() & 255) << 24);
    }

    public final long zzcyt() throws IOException {
        byte zzcyv = zzcyv();
        byte zzcyv2 = zzcyv();
        return ((((long) zzcyv2) & 255) << 8) | (((long) zzcyv) & 255) | ((((long) zzcyv()) & 255) << 16) | ((((long) zzcyv()) & 255) << 24) | ((((long) zzcyv()) & 255) << 32) | ((((long) zzcyv()) & 255) << 40) | ((((long) zzcyv()) & 255) << 48) | ((((long) zzcyv()) & 255) << 56);
    }

    public final void zzlf(int i) throws zzflr {
        if (this.zzpoj != i) {
            throw new zzflr("Protocol message end-group tag did not match expected tag.");
        }
    }

    public final boolean zzlg(int i) throws IOException {
        int zzcxx;
        int i2 = i & 7;
        if (i2 == 0) {
            zzcym();
            return true;
        } else if (i2 == 1) {
            zzcyt();
            return true;
        } else if (i2 == 2) {
            zzlk(zzcym());
            return true;
        } else if (i2 == 3) {
            do {
                zzcxx = zzcxx();
                if (zzcxx == 0 || !zzlg(zzcxx)) {
                    zzlf(((i >>> 3) << 3) | 4);
                }
                zzcxx = zzcxx();
                break;
            } while (!zzlg(zzcxx));
            zzlf(((i >>> 3) << 3) | 4);
            return true;
        } else if (i2 == 4) {
            return false;
        } else {
            if (i2 == 5) {
                zzcys();
                return true;
            }
            throw new zzflr("Protocol message tag had invalid wire type.");
        }
    }

    public final int zzli(int i) throws zzflr {
        if (i >= 0) {
            int i2 = i + this.zzpvk;
            int i3 = this.zzpok;
            if (i2 <= i3) {
                this.zzpok = i2;
                zzcyu();
                return i3;
            }
            throw zzflr.zzdcn();
        }
        throw zzflr.zzdco();
    }

    public final void zzlj(int i) {
        this.zzpok = i;
        zzcyu();
    }

    public final void zzmw(int i) {
        zzap(i, this.zzpoj);
    }
}
