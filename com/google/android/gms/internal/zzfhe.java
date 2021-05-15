package com.google.android.gms.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xbill.DNS.KEYRecord;

final class zzfhe extends zzfhb {
    private final byte[] buffer;
    private int pos;
    private int zzpoh;
    private int zzpoj;
    private int zzpok;
    private final InputStream zzpol;
    private int zzpom;
    private int zzpon;
    private zzfhf zzpoo;

    private zzfhe(InputStream inputStream, int i) {
        super();
        this.zzpok = Integer.MAX_VALUE;
        this.zzpoo = null;
        zzfhz.zzc(inputStream, "input");
        this.zzpol = inputStream;
        this.buffer = new byte[i];
        this.zzpom = 0;
        this.pos = 0;
        this.zzpon = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b0, code lost:
        if (((long) r2[r0]) >= 0) goto L_0x00b4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final long zzcyr() throws java.io.IOException {
        /*
            r11 = this;
            int r0 = r11.pos
            int r1 = r11.zzpom
            if (r1 == r0) goto L_0x00b8
            byte[] r2 = r11.buffer
            int r3 = r0 + 1
            byte r0 = r2[r0]
            if (r0 < 0) goto L_0x0012
            r11.pos = r3
            long r0 = (long) r0
            return r0
        L_0x0012:
            int r1 = r1 - r3
            r4 = 9
            if (r1 < r4) goto L_0x00b8
            int r1 = r3 + 1
            byte r3 = r2[r3]
            int r3 = r3 << 7
            r0 = r0 ^ r3
            if (r0 >= 0) goto L_0x0025
            r0 = r0 ^ -128(0xffffffffffffff80, float:NaN)
        L_0x0022:
            long r2 = (long) r0
            goto L_0x00b5
        L_0x0025:
            int r3 = r1 + 1
            byte r1 = r2[r1]
            int r1 = r1 << 14
            r0 = r0 ^ r1
            if (r0 < 0) goto L_0x0036
            r0 = r0 ^ 16256(0x3f80, float:2.278E-41)
            long r0 = (long) r0
            r9 = r0
            r1 = r3
            r2 = r9
            goto L_0x00b5
        L_0x0036:
            int r1 = r3 + 1
            byte r3 = r2[r3]
            int r3 = r3 << 21
            r0 = r0 ^ r3
            if (r0 >= 0) goto L_0x0044
            r2 = -2080896(0xffffffffffe03f80, float:NaN)
            r0 = r0 ^ r2
            goto L_0x0022
        L_0x0044:
            long r3 = (long) r0
            int r0 = r1 + 1
            byte r1 = r2[r1]
            long r5 = (long) r1
            r1 = 28
            long r5 = r5 << r1
            long r3 = r3 ^ r5
            r5 = 0
            int r1 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r1 < 0) goto L_0x005b
            r1 = 266354560(0xfe03f80, double:1.315966377E-315)
        L_0x0057:
            long r2 = r3 ^ r1
            r1 = r0
            goto L_0x00b5
        L_0x005b:
            int r1 = r0 + 1
            byte r0 = r2[r0]
            long r7 = (long) r0
            r0 = 35
            long r7 = r7 << r0
            long r3 = r3 ^ r7
            int r0 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x0070
            r5 = -34093383808(0xfffffff80fe03f80, double:NaN)
        L_0x006d:
            long r2 = r3 ^ r5
            goto L_0x00b5
        L_0x0070:
            int r0 = r1 + 1
            byte r1 = r2[r1]
            long r7 = (long) r1
            r1 = 42
            long r7 = r7 << r1
            long r3 = r3 ^ r7
            int r1 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r1 < 0) goto L_0x0083
            r1 = 4363953127296(0x3f80fe03f80, double:2.1560793202584E-311)
            goto L_0x0057
        L_0x0083:
            int r1 = r0 + 1
            byte r0 = r2[r0]
            long r7 = (long) r0
            r0 = 49
            long r7 = r7 << r0
            long r3 = r3 ^ r7
            int r0 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x0096
            r5 = -558586000294016(0xfffe03f80fe03f80, double:NaN)
            goto L_0x006d
        L_0x0096:
            int r0 = r1 + 1
            byte r1 = r2[r1]
            long r7 = (long) r1
            r1 = 56
            long r7 = r7 << r1
            long r3 = r3 ^ r7
            r7 = 71499008037633920(0xfe03f80fe03f80, double:6.838959413692434E-304)
            long r3 = r3 ^ r7
            int r1 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r1 >= 0) goto L_0x00b3
            int r1 = r0 + 1
            byte r0 = r2[r0]
            long r7 = (long) r0
            int r0 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
            if (r0 < 0) goto L_0x00b8
            goto L_0x00b4
        L_0x00b3:
            r1 = r0
        L_0x00b4:
            r2 = r3
        L_0x00b5:
            r11.pos = r1
            return r2
        L_0x00b8:
            long r0 = r11.zzcyn()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfhe.zzcyr():long");
    }

    private final int zzcys() throws IOException {
        int i = this.pos;
        if (this.zzpom - i < 4) {
            zzlm(4);
            i = this.pos;
        }
        byte[] bArr = this.buffer;
        this.pos = i + 4;
        return ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16);
    }

    private final long zzcyt() throws IOException {
        int i = this.pos;
        if (this.zzpom - i < 8) {
            zzlm(8);
            i = this.pos;
        }
        byte[] bArr = this.buffer;
        this.pos = i + 8;
        return ((((long) bArr[i + 7]) & 255) << 56) | (((long) bArr[i]) & 255) | ((((long) bArr[i + 1]) & 255) << 8) | ((((long) bArr[i + 2]) & 255) << 16) | ((((long) bArr[i + 3]) & 255) << 24) | ((((long) bArr[i + 4]) & 255) << 32) | ((((long) bArr[i + 5]) & 255) << 40) | ((((long) bArr[i + 6]) & 255) << 48);
    }

    private final void zzcyu() {
        int i = this.zzpom + this.zzpoh;
        this.zzpom = i;
        int i2 = this.zzpon + i;
        int i3 = this.zzpok;
        if (i2 > i3) {
            int i4 = i2 - i3;
            this.zzpoh = i4;
            this.zzpom = i - i4;
            return;
        }
        this.zzpoh = 0;
    }

    private final byte zzcyv() throws IOException {
        if (this.pos == this.zzpom) {
            zzlm(1);
        }
        byte[] bArr = this.buffer;
        int i = this.pos;
        this.pos = i + 1;
        return bArr[i];
    }

    private final void zzlm(int i) throws IOException {
        if (zzln(i)) {
            return;
        }
        if (i > (this.zzpoe - this.zzpon) - this.pos) {
            throw zzfie.zzdal();
        }
        throw zzfie.zzdae();
    }

    private final boolean zzln(int i) throws IOException {
        while (this.pos + i > this.zzpom) {
            int i2 = this.zzpoe;
            int i3 = this.zzpon;
            int i4 = this.pos;
            if (i > (i2 - i3) - i4 || i3 + i4 + i > this.zzpok) {
                return false;
            }
            if (i4 > 0) {
                int i5 = this.zzpom;
                if (i5 > i4) {
                    byte[] bArr = this.buffer;
                    System.arraycopy(bArr, i4, bArr, 0, i5 - i4);
                }
                this.zzpon += i4;
                this.zzpom -= i4;
                this.pos = 0;
            }
            InputStream inputStream = this.zzpol;
            byte[] bArr2 = this.buffer;
            int i6 = this.zzpom;
            int read = inputStream.read(bArr2, i6, Math.min(bArr2.length - i6, (this.zzpoe - this.zzpon) - this.zzpom));
            if (read == 0 || read < -1 || read > this.buffer.length) {
                StringBuilder sb = new StringBuilder(102);
                sb.append("InputStream#read(byte[]) returned invalid result: ");
                sb.append(read);
                sb.append("\nThe InputStream implementation is buggy.");
                throw new IllegalStateException(sb.toString());
            } else if (read <= 0) {
                return false;
            } else {
                this.zzpom += read;
                zzcyu();
                if (this.zzpom >= i) {
                    return true;
                }
            }
        }
        StringBuilder sb2 = new StringBuilder(77);
        sb2.append("refillBuffer() called when ");
        sb2.append(i);
        sb2.append(" bytes were already available in buffer");
        throw new IllegalStateException(sb2.toString());
    }

    private final byte[] zzlo(int i) throws IOException {
        byte[] zzlp = zzlp(i);
        if (zzlp != null) {
            return zzlp;
        }
        int i2 = this.pos;
        int i3 = this.zzpom;
        int i4 = i3 - i2;
        this.zzpon += i3;
        this.pos = 0;
        this.zzpom = 0;
        List<byte[]> zzlq = zzlq(i - i4);
        byte[] bArr = new byte[i];
        System.arraycopy(this.buffer, i2, bArr, 0, i4);
        for (byte[] next : zzlq) {
            System.arraycopy(next, 0, bArr, i4, next.length);
            i4 += next.length;
        }
        return bArr;
    }

    private final byte[] zzlp(int i) throws IOException {
        if (i == 0) {
            return zzfhz.EMPTY_BYTE_ARRAY;
        }
        if (i >= 0) {
            int i2 = this.zzpon + this.pos + i;
            if (i2 - this.zzpoe <= 0) {
                int i3 = this.zzpok;
                if (i2 <= i3) {
                    int i4 = this.zzpom - this.pos;
                    int i5 = i - i4;
                    if (i5 >= 4096 && i5 > this.zzpol.available()) {
                        return null;
                    }
                    byte[] bArr = new byte[i];
                    System.arraycopy(this.buffer, this.pos, bArr, 0, i4);
                    this.zzpon += this.zzpom;
                    this.pos = 0;
                    this.zzpom = 0;
                    while (i4 < i) {
                        int read = this.zzpol.read(bArr, i4, i - i4);
                        if (read != -1) {
                            this.zzpon += read;
                            i4 += read;
                        } else {
                            throw zzfie.zzdae();
                        }
                    }
                    return bArr;
                }
                zzlk((i3 - this.zzpon) - this.pos);
                throw zzfie.zzdae();
            }
            throw zzfie.zzdal();
        }
        throw zzfie.zzdaf();
    }

    private final List<byte[]> zzlq(int i) throws IOException {
        ArrayList arrayList = new ArrayList();
        while (i > 0) {
            int min = Math.min(i, KEYRecord.Flags.EXTEND);
            byte[] bArr = new byte[min];
            int i2 = 0;
            while (i2 < min) {
                int read = this.zzpol.read(bArr, i2, min - i2);
                if (read != -1) {
                    this.zzpon += read;
                    i2 += read;
                } else {
                    throw zzfie.zzdae();
                }
            }
            i -= min;
            arrayList.add(bArr);
        }
        return arrayList;
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(zzcyt());
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(zzcys());
    }

    public final String readString() throws IOException {
        int zzcym = zzcym();
        if (zzcym > 0 && zzcym <= this.zzpom - this.pos) {
            String str = new String(this.buffer, this.pos, zzcym, zzfhz.UTF_8);
            this.pos += zzcym;
            return str;
        } else if (zzcym == 0) {
            return "";
        } else {
            if (zzcym > this.zzpom) {
                return new String(zzlo(zzcym), zzfhz.UTF_8);
            }
            zzlm(zzcym);
            String str2 = new String(this.buffer, this.pos, zzcym, zzfhz.UTF_8);
            this.pos += zzcym;
            return str2;
        }
    }

    public final <T extends zzfhu<T, ?>> T zza(T t, zzfhm zzfhm) throws IOException {
        int zzcym = zzcym();
        if (this.zzpoc < this.zzpod) {
            int zzli = zzli(zzcym);
            this.zzpoc++;
            T zza = zzfhu.zza(t, (zzfhb) this, zzfhm);
            zzlf(0);
            this.zzpoc--;
            zzlj(zzli);
            return zza;
        }
        throw zzfie.zzdak();
    }

    public final void zza(zzfjd zzfjd, zzfhm zzfhm) throws IOException {
        int zzcym = zzcym();
        if (this.zzpoc < this.zzpod) {
            int zzli = zzli(zzcym);
            this.zzpoc++;
            zzfjd.zzb(this, zzfhm);
            zzlf(0);
            this.zzpoc--;
            zzlj(zzli);
            return;
        }
        throw zzfie.zzdak();
    }

    public final int zzcxx() throws IOException {
        if (zzcyp()) {
            this.zzpoj = 0;
            return 0;
        }
        int zzcym = zzcym();
        this.zzpoj = zzcym;
        if ((zzcym >>> 3) != 0) {
            return zzcym;
        }
        throw zzfie.zzdah();
    }

    public final long zzcxy() throws IOException {
        return zzcyr();
    }

    public final long zzcxz() throws IOException {
        return zzcyr();
    }

    public final int zzcya() throws IOException {
        return zzcym();
    }

    public final long zzcyb() throws IOException {
        return zzcyt();
    }

    public final int zzcyc() throws IOException {
        return zzcys();
    }

    public final boolean zzcyd() throws IOException {
        return zzcyr() != 0;
    }

    public final String zzcye() throws IOException {
        byte[] bArr;
        int zzcym = zzcym();
        int i = this.pos;
        if (zzcym <= this.zzpom - i && zzcym > 0) {
            bArr = this.buffer;
            this.pos = i + zzcym;
        } else if (zzcym == 0) {
            return "";
        } else {
            if (zzcym <= this.zzpom) {
                zzlm(zzcym);
                bArr = this.buffer;
                this.pos = zzcym;
            } else {
                bArr = zzlo(zzcym);
            }
            i = 0;
        }
        if (zzfks.zzl(bArr, i, i + zzcym)) {
            return new String(bArr, i, zzcym, zzfhz.UTF_8);
        }
        throw zzfie.zzdam();
    }

    public final zzfgs zzcyf() throws IOException {
        int zzcym = zzcym();
        int i = this.zzpom;
        int i2 = this.pos;
        if (zzcym <= i - i2 && zzcym > 0) {
            zzfgs zzf = zzfgs.zzf(this.buffer, i2, zzcym);
            this.pos += zzcym;
            return zzf;
        } else if (zzcym == 0) {
            return zzfgs.zzpnw;
        } else {
            byte[] zzlp = zzlp(zzcym);
            if (zzlp != null) {
                return zzfgs.zzba(zzlp);
            }
            int i3 = this.pos;
            int i4 = this.zzpom;
            int i5 = i4 - i3;
            this.zzpon += i4;
            this.pos = 0;
            this.zzpom = 0;
            List<byte[]> zzlq = zzlq(zzcym - i5);
            ArrayList arrayList = new ArrayList(zzlq.size() + 1);
            arrayList.add(zzfgs.zzf(this.buffer, i3, i5));
            for (byte[] zzba : zzlq) {
                arrayList.add(zzfgs.zzba(zzba));
            }
            return zzfgs.zzg(arrayList);
        }
    }

    public final int zzcyg() throws IOException {
        return zzcym();
    }

    public final int zzcyh() throws IOException {
        return zzcym();
    }

    public final int zzcyi() throws IOException {
        return zzcys();
    }

    public final long zzcyj() throws IOException {
        return zzcyt();
    }

    public final int zzcyk() throws IOException {
        return zzll(zzcym());
    }

    public final long zzcyl() throws IOException {
        return zzct(zzcyr());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0066, code lost:
        if (r2[r3] >= 0) goto L_0x0068;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final int zzcym() throws java.io.IOException {
        /*
            r5 = this;
            int r0 = r5.pos
            int r1 = r5.zzpom
            if (r1 == r0) goto L_0x006b
            byte[] r2 = r5.buffer
            int r3 = r0 + 1
            byte r0 = r2[r0]
            if (r0 < 0) goto L_0x0011
            r5.pos = r3
            return r0
        L_0x0011:
            int r1 = r1 - r3
            r4 = 9
            if (r1 < r4) goto L_0x006b
            int r1 = r3 + 1
            byte r3 = r2[r3]
            int r3 = r3 << 7
            r0 = r0 ^ r3
            if (r0 >= 0) goto L_0x0022
            r0 = r0 ^ -128(0xffffffffffffff80, float:NaN)
            goto L_0x0068
        L_0x0022:
            int r3 = r1 + 1
            byte r1 = r2[r1]
            int r1 = r1 << 14
            r0 = r0 ^ r1
            if (r0 < 0) goto L_0x002f
            r0 = r0 ^ 16256(0x3f80, float:2.278E-41)
        L_0x002d:
            r1 = r3
            goto L_0x0068
        L_0x002f:
            int r1 = r3 + 1
            byte r3 = r2[r3]
            int r3 = r3 << 21
            r0 = r0 ^ r3
            if (r0 >= 0) goto L_0x003d
            r2 = -2080896(0xffffffffffe03f80, float:NaN)
            r0 = r0 ^ r2
            goto L_0x0068
        L_0x003d:
            int r3 = r1 + 1
            byte r1 = r2[r1]
            int r4 = r1 << 28
            r0 = r0 ^ r4
            r4 = 266354560(0xfe03f80, float:2.2112565E-29)
            r0 = r0 ^ r4
            if (r1 >= 0) goto L_0x002d
            int r1 = r3 + 1
            byte r3 = r2[r3]
            if (r3 >= 0) goto L_0x0068
            int r3 = r1 + 1
            byte r1 = r2[r1]
            if (r1 >= 0) goto L_0x002d
            int r1 = r3 + 1
            byte r3 = r2[r3]
            if (r3 >= 0) goto L_0x0068
            int r3 = r1 + 1
            byte r1 = r2[r1]
            if (r1 >= 0) goto L_0x002d
            int r1 = r3 + 1
            byte r2 = r2[r3]
            if (r2 < 0) goto L_0x006b
        L_0x0068:
            r5.pos = r1
            return r0
        L_0x006b:
            long r0 = r5.zzcyn()
            int r0 = (int) r0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfhe.zzcym():int");
    }

    /* access modifiers changed from: package-private */
    public final long zzcyn() throws IOException {
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            byte zzcyv = zzcyv();
            j |= ((long) (zzcyv & Byte.MAX_VALUE)) << i;
            if ((zzcyv & 128) == 0) {
                return j;
            }
        }
        throw zzfie.zzdag();
    }

    public final int zzcyo() {
        int i = this.zzpok;
        if (i == Integer.MAX_VALUE) {
            return -1;
        }
        return i - (this.zzpon + this.pos);
    }

    public final boolean zzcyp() throws IOException {
        return this.pos == this.zzpom && !zzln(1);
    }

    public final int zzcyq() {
        return this.zzpon + this.pos;
    }

    public final void zzlf(int i) throws zzfie {
        if (this.zzpoj != i) {
            throw zzfie.zzdai();
        }
    }

    public final boolean zzlg(int i) throws IOException {
        int zzcxx;
        int i2 = i & 7;
        int i3 = 0;
        if (i2 == 0) {
            if (this.zzpom - this.pos >= 10) {
                while (i3 < 10) {
                    byte[] bArr = this.buffer;
                    int i4 = this.pos;
                    this.pos = i4 + 1;
                    if (bArr[i4] < 0) {
                        i3++;
                    }
                }
                throw zzfie.zzdag();
            }
            while (i3 < 10) {
                if (zzcyv() < 0) {
                    i3++;
                }
            }
            throw zzfie.zzdag();
            return true;
        } else if (i2 == 1) {
            zzlk(8);
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
                zzlk(4);
                return true;
            }
            throw zzfie.zzdaj();
        }
    }

    public final int zzli(int i) throws zzfie {
        if (i >= 0) {
            int i2 = i + this.zzpon + this.pos;
            int i3 = this.zzpok;
            if (i2 <= i3) {
                this.zzpok = i2;
                zzcyu();
                return i3;
            }
            throw zzfie.zzdae();
        }
        throw zzfie.zzdaf();
    }

    public final void zzlj(int i) {
        this.zzpok = i;
        zzcyu();
    }

    public final void zzlk(int i) throws IOException {
        int i2 = this.zzpom;
        int i3 = this.pos;
        if (i <= i2 - i3 && i >= 0) {
            this.pos = i3 + i;
        } else if (i >= 0) {
            int i4 = this.zzpon;
            int i5 = this.pos;
            int i6 = i4 + i5 + i;
            int i7 = this.zzpok;
            if (i6 <= i7) {
                int i8 = this.zzpom;
                int i9 = i8 - i5;
                this.pos = i8;
                while (true) {
                    zzlm(1);
                    int i10 = i - i9;
                    int i11 = this.zzpom;
                    if (i10 > i11) {
                        i9 += i11;
                        this.pos = i11;
                    } else {
                        this.pos = i10;
                        return;
                    }
                }
            } else {
                zzlk((i7 - i4) - i5);
                throw zzfie.zzdae();
            }
        } else {
            throw zzfie.zzdaf();
        }
    }
}
