package com.google.android.gms.common.util;

public final class zzn {
    public static String zza(byte[] bArr, int i, int i2, boolean z) {
        String str;
        if (bArr == null || bArr.length == 0 || i2 <= 0 || i2 > bArr.length) {
            return null;
        }
        StringBuilder sb = new StringBuilder((((i2 + 16) - 1) / 16) * 57);
        int i3 = 0;
        int i4 = 0;
        int i5 = i2;
        while (i5 > 0) {
            if (i3 != 0) {
                if (i3 == 8) {
                    str = " -";
                }
                sb.append(String.format(" %02X", new Object[]{Integer.valueOf(bArr[i4] & 255)}));
                i5--;
                i3++;
                if (i3 != 16 || i5 == 0) {
                    sb.append(10);
                    i3 = 0;
                }
                i4++;
            } else if (i2 < 65536) {
                str = String.format("%04X:", new Object[]{Integer.valueOf(i4)});
            } else {
                str = String.format("%08X:", new Object[]{Integer.valueOf(i4)});
            }
            sb.append(str);
            sb.append(String.format(" %02X", new Object[]{Integer.valueOf(bArr[i4] & 255)}));
            i5--;
            i3++;
            if (i3 != 16) {
            }
            sb.append(10);
            i3 = 0;
            i4++;
        }
        return sb.toString();
    }
}
