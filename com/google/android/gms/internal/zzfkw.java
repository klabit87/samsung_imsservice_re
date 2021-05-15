package com.google.android.gms.internal;

final class zzfkw extends zzfkt {
    zzfkw() {
    }

    private static int zza(byte[] bArr, int i, long j, int i2) {
        if (i2 == 0) {
            return zzfks.zzmu(i);
        }
        if (i2 == 1) {
            return zzfks.zzam(i, zzfkq.zzb(bArr, j));
        }
        if (i2 == 2) {
            return zzfks.zzi(i, zzfkq.zzb(bArr, j), zzfkq.zzb(bArr, j + 1));
        }
        throw new AssertionError();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0052, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00a9, code lost:
        return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int zzb(byte[] r11, long r12, int r14) {
        /*
            r0 = 0
            r1 = 1
            r3 = 16
            if (r14 >= r3) goto L_0x0009
            r3 = r0
            goto L_0x001b
        L_0x0009:
            r4 = r12
            r3 = r0
        L_0x000b:
            if (r3 >= r14) goto L_0x001a
            long r6 = r4 + r1
            byte r4 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r4)
            if (r4 >= 0) goto L_0x0016
            goto L_0x001b
        L_0x0016:
            int r3 = r3 + 1
            r4 = r6
            goto L_0x000b
        L_0x001a:
            r3 = r14
        L_0x001b:
            int r14 = r14 - r3
            long r3 = (long) r3
            long r12 = r12 + r3
        L_0x001e:
            r3 = r0
        L_0x001f:
            if (r14 <= 0) goto L_0x0032
            long r3 = r12 + r1
            byte r12 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r12)
            if (r12 < 0) goto L_0x002f
            int r14 = r14 + -1
            r9 = r3
            r3 = r12
            r12 = r9
            goto L_0x001f
        L_0x002f:
            r9 = r3
            r3 = r12
            r12 = r9
        L_0x0032:
            if (r14 != 0) goto L_0x0035
            return r0
        L_0x0035:
            int r14 = r14 + -1
            r4 = -32
            r5 = -65
            r6 = -1
            if (r3 >= r4) goto L_0x0053
            if (r14 != 0) goto L_0x0041
            return r3
        L_0x0041:
            int r14 = r14 + -1
            r4 = -62
            if (r3 < r4) goto L_0x0052
            long r3 = r12 + r1
            byte r12 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r12)
            if (r12 <= r5) goto L_0x0050
            goto L_0x0052
        L_0x0050:
            r12 = r3
            goto L_0x001e
        L_0x0052:
            return r6
        L_0x0053:
            r7 = -16
            if (r3 >= r7) goto L_0x007e
            r7 = 2
            if (r14 >= r7) goto L_0x005f
            int r11 = zza(r11, r3, r12, r14)
            return r11
        L_0x005f:
            int r14 = r14 + -2
            long r7 = r12 + r1
            byte r12 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r12)
            if (r12 > r5) goto L_0x007d
            r13 = -96
            if (r3 != r4) goto L_0x006f
            if (r12 < r13) goto L_0x007d
        L_0x006f:
            r4 = -19
            if (r3 != r4) goto L_0x0075
            if (r12 >= r13) goto L_0x007d
        L_0x0075:
            long r12 = r7 + r1
            byte r3 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r7)
            if (r3 <= r5) goto L_0x001e
        L_0x007d:
            return r6
        L_0x007e:
            r4 = 3
            if (r14 >= r4) goto L_0x0086
            int r11 = zza(r11, r3, r12, r14)
            return r11
        L_0x0086:
            int r14 = r14 + -3
            long r7 = r12 + r1
            byte r12 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r12)
            if (r12 > r5) goto L_0x00a9
            int r13 = r3 << 28
            int r12 = r12 + 112
            int r13 = r13 + r12
            int r12 = r13 >> 30
            if (r12 != 0) goto L_0x00a9
            long r12 = r7 + r1
            byte r3 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r7)
            if (r3 > r5) goto L_0x00a9
            long r3 = r12 + r1
            byte r12 = com.google.android.gms.internal.zzfkq.zzb((byte[]) r11, (long) r12)
            if (r12 <= r5) goto L_0x0050
        L_0x00a9:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfkw.zzb(byte[], long, int):int");
    }

    /* access modifiers changed from: package-private */
    public final int zzb(int i, byte[] bArr, int i2, int i3) {
        if ((i2 | i3 | (bArr.length - i3)) >= 0) {
            long j = (long) i2;
            return zzb(bArr, j, (int) (((long) i3) - j));
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Array length=%d, index=%d, limit=%d", new Object[]{Integer.valueOf(bArr.length), Integer.valueOf(i2), Integer.valueOf(i3)}));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033 A[LOOP:1: B:13:0x0033->B:37:0x00fc, LOOP_START, PHI: r2 r3 r4 r11 
      PHI: (r2v4 int) = (r2v3 int), (r2v6 int) binds: [B:10:0x002f, B:37:0x00fc] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r3v3 char) = (r3v2 char), (r3v4 char) binds: [B:10:0x002f, B:37:0x00fc] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r4v3 long) = (r4v2 long), (r4v5 long) binds: [B:10:0x002f, B:37:0x00fc] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r11v3 long) = (r11v2 long), (r11v5 long) binds: [B:10:0x002f, B:37:0x00fc] A[DONT_GENERATE, DONT_INLINE]] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final int zzb(java.lang.CharSequence r21, byte[] r22, int r23, int r24) {
        /*
            r20 = this;
            r0 = r21
            r1 = r22
            r2 = r23
            r3 = r24
            long r4 = (long) r2
            long r6 = (long) r3
            long r6 = r6 + r4
            int r8 = r21.length()
            java.lang.String r9 = " at index "
            java.lang.String r10 = "Failed writing "
            if (r8 > r3) goto L_0x0146
            int r11 = r1.length
            int r11 = r11 - r3
            if (r11 < r2) goto L_0x0146
            r2 = 0
        L_0x001a:
            r3 = 128(0x80, float:1.794E-43)
            r11 = 1
            if (r2 >= r8) goto L_0x002f
            char r13 = r0.charAt(r2)
            if (r13 >= r3) goto L_0x002f
            long r11 = r11 + r4
            byte r3 = (byte) r13
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r3)
            int r2 = r2 + 1
            r4 = r11
            goto L_0x001a
        L_0x002f:
            if (r2 != r8) goto L_0x0033
            int r0 = (int) r4
            return r0
        L_0x0033:
            if (r2 >= r8) goto L_0x0144
            char r13 = r0.charAt(r2)
            if (r13 >= r3) goto L_0x004a
            int r14 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r14 >= 0) goto L_0x004a
            long r14 = r4 + r11
            byte r13 = (byte) r13
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r13)
            r4 = r11
            r12 = r14
            r11 = r3
            goto L_0x00fc
        L_0x004a:
            r14 = 2048(0x800, float:2.87E-42)
            if (r13 >= r14) goto L_0x0074
            r14 = 2
            long r14 = r6 - r14
            int r14 = (r4 > r14 ? 1 : (r4 == r14 ? 0 : -1))
            if (r14 > 0) goto L_0x0074
            long r14 = r4 + r11
            int r3 = r13 >>> 6
            r3 = r3 | 960(0x3c0, float:1.345E-42)
            byte r3 = (byte) r3
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r3)
            long r3 = r14 + r11
            r5 = r13 & 63
            r13 = 128(0x80, float:1.794E-43)
            r5 = r5 | r13
            byte r5 = (byte) r5
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r14, (byte) r5)
            r18 = r11
            r11 = 128(0x80, float:1.794E-43)
            r12 = r3
            r4 = r18
            goto L_0x00fc
        L_0x0074:
            r3 = 57343(0xdfff, float:8.0355E-41)
            r14 = 55296(0xd800, float:7.7486E-41)
            if (r13 < r14) goto L_0x007e
            if (r3 >= r13) goto L_0x00af
        L_0x007e:
            r15 = 3
            long r15 = r6 - r15
            int r15 = (r4 > r15 ? 1 : (r4 == r15 ? 0 : -1))
            if (r15 > 0) goto L_0x00af
            long r14 = r4 + r11
            int r3 = r13 >>> 12
            r3 = r3 | 480(0x1e0, float:6.73E-43)
            byte r3 = (byte) r3
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r3)
            long r3 = r14 + r11
            int r5 = r13 >>> 6
            r5 = r5 & 63
            r11 = 128(0x80, float:1.794E-43)
            r5 = r5 | r11
            byte r5 = (byte) r5
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r14, (byte) r5)
            r14 = 1
            long r16 = r3 + r14
            r5 = r13 & 63
            r5 = r5 | r11
            byte r5 = (byte) r5
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r3, (byte) r5)
            r12 = r16
            r4 = 1
            r11 = 128(0x80, float:1.794E-43)
            goto L_0x00fc
        L_0x00af:
            r11 = 4
            long r11 = r6 - r11
            int r11 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r11 > 0) goto L_0x010f
            int r3 = r2 + 1
            if (r3 == r8) goto L_0x0107
            char r2 = r0.charAt(r3)
            boolean r11 = java.lang.Character.isSurrogatePair(r13, r2)
            if (r11 == 0) goto L_0x0106
            int r2 = java.lang.Character.toCodePoint(r13, r2)
            r11 = 1
            long r13 = r4 + r11
            int r15 = r2 >>> 18
            r15 = r15 | 240(0xf0, float:3.36E-43)
            byte r15 = (byte) r15
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r15)
            long r4 = r13 + r11
            int r15 = r2 >>> 12
            r15 = r15 & 63
            r11 = 128(0x80, float:1.794E-43)
            r12 = r15 | 128(0x80, float:1.794E-43)
            byte r12 = (byte) r12
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r13, (byte) r12)
            r12 = 1
            long r14 = r4 + r12
            int r16 = r2 >>> 6
            r12 = r16 & 63
            r12 = r12 | r11
            byte r12 = (byte) r12
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r4, (byte) r12)
            r4 = 1
            long r12 = r14 + r4
            r2 = r2 & 63
            r2 = r2 | r11
            byte r2 = (byte) r2
            com.google.android.gms.internal.zzfkq.zza((byte[]) r1, (long) r14, (byte) r2)
            r2 = r3
        L_0x00fc:
            int r2 = r2 + 1
            r3 = r11
            r18 = r4
            r4 = r12
            r11 = r18
            goto L_0x0033
        L_0x0106:
            r2 = r3
        L_0x0107:
            com.google.android.gms.internal.zzfkv r0 = new com.google.android.gms.internal.zzfkv
            int r2 = r2 + -1
            r0.<init>(r2, r8)
            throw r0
        L_0x010f:
            if (r14 > r13) goto L_0x0127
            if (r13 > r3) goto L_0x0127
            int r1 = r2 + 1
            if (r1 == r8) goto L_0x0121
            char r0 = r0.charAt(r1)
            boolean r0 = java.lang.Character.isSurrogatePair(r13, r0)
            if (r0 != 0) goto L_0x0127
        L_0x0121:
            com.google.android.gms.internal.zzfkv r0 = new com.google.android.gms.internal.zzfkv
            r0.<init>(r2, r8)
            throw r0
        L_0x0127:
            java.lang.ArrayIndexOutOfBoundsException r0 = new java.lang.ArrayIndexOutOfBoundsException
            r1 = 46
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>(r1)
            r2.append(r10)
            r2.append(r13)
            r2.append(r9)
            r2.append(r4)
            java.lang.String r1 = r2.toString()
            r0.<init>(r1)
            throw r0
        L_0x0144:
            int r0 = (int) r4
            return r0
        L_0x0146:
            java.lang.ArrayIndexOutOfBoundsException r1 = new java.lang.ArrayIndexOutOfBoundsException
            int r8 = r8 + -1
            char r0 = r0.charAt(r8)
            int r2 = r2 + r3
            r3 = 37
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r3)
            r4.append(r10)
            r4.append(r0)
            r4.append(r9)
            r4.append(r2)
            java.lang.String r0 = r4.toString()
            r1.<init>(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfkw.zzb(java.lang.CharSequence, byte[], int, int):int");
    }
}
