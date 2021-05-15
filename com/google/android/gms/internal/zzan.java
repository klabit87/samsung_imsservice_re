package com.google.android.gms.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

final class zzan {
    final String key;
    final String zza;
    final long zzb;
    final long zzc;
    long zzca;
    final long zzd;
    final long zze;
    final List<zzl> zzg;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    zzan(java.lang.String r13, com.google.android.gms.internal.zzc r14) {
        /*
            r12 = this;
            java.lang.String r2 = r14.zza
            long r3 = r14.zzb
            long r5 = r14.zzc
            long r7 = r14.zzd
            long r9 = r14.zze
            java.util.List<com.google.android.gms.internal.zzl> r0 = r14.zzg
            if (r0 == 0) goto L_0x0011
            java.util.List<com.google.android.gms.internal.zzl> r0 = r14.zzg
            goto L_0x0017
        L_0x0011:
            java.util.Map<java.lang.String, java.lang.String> r0 = r14.zzf
            java.util.List r0 = com.google.android.gms.internal.zzap.zza((java.util.Map<java.lang.String, java.lang.String>) r0)
        L_0x0017:
            r11 = r0
            r0 = r12
            r1 = r13
            r0.<init>(r1, r2, r3, r5, r7, r9, r11)
            byte[] r13 = r14.data
            int r13 = r13.length
            long r13 = (long) r13
            r12.zzca = r13
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzan.<init>(java.lang.String, com.google.android.gms.internal.zzc):void");
    }

    private zzan(String str, String str2, long j, long j2, long j3, long j4, List<zzl> list) {
        this.key = str;
        this.zza = "".equals(str2) ? null : str2;
        this.zzb = j;
        this.zzc = j2;
        this.zzd = j3;
        this.zze = j4;
        this.zzg = list;
    }

    static zzan zzc(zzao zzao) throws IOException {
        if (zzam.zzb((InputStream) zzao) == 538247942) {
            return new zzan(zzam.zza(zzao), zzam.zza(zzao), zzam.zzc(zzao), zzam.zzc(zzao), zzam.zzc(zzao), zzam.zzc(zzao), zzam.zzb(zzao));
        }
        throw new IOException();
    }

    /* access modifiers changed from: package-private */
    public final boolean zza(OutputStream outputStream) {
        try {
            zzam.zza(outputStream, 538247942);
            zzam.zza(outputStream, this.key);
            zzam.zza(outputStream, this.zza == null ? "" : this.zza);
            zzam.zza(outputStream, this.zzb);
            zzam.zza(outputStream, this.zzc);
            zzam.zza(outputStream, this.zzd);
            zzam.zza(outputStream, this.zze);
            List<zzl> list = this.zzg;
            if (list != null) {
                zzam.zza(outputStream, list.size());
                for (zzl next : list) {
                    zzam.zza(outputStream, next.getName());
                    zzam.zza(outputStream, next.getValue());
                }
            } else {
                zzam.zza(outputStream, 0);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            zzaf.zzb("%s", e.toString());
            return false;
        }
    }
}
