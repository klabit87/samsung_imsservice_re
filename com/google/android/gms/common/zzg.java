package com.google.android.gms.common;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.internal.zzba;
import com.google.android.gms.common.internal.zzbb;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.dynamic.zzn;
import com.google.android.gms.dynamite.DynamiteModule;

final class zzg {
    private static volatile zzba zzfra;
    private static final Object zzfrb = new Object();
    private static Context zzfrc;

    static zzp zza(String str, zzh zzh, boolean z) {
        String str2;
        try {
            if (zzfra == null) {
                zzbq.checkNotNull(zzfrc);
                synchronized (zzfrb) {
                    if (zzfra == null) {
                        zzfra = zzbb.zzan(DynamiteModule.zza(zzfrc, DynamiteModule.zzhdl, "com.google.android.gms.googlecertificates").zzhk("com.google.android.gms.common.GoogleCertificatesImpl"));
                    }
                }
            }
            zzbq.checkNotNull(zzfrc);
            try {
                if (zzfra.zza(new zzn(str, zzh, z), zzn.zzz(zzfrc.getPackageManager()))) {
                    return zzp.zzahj();
                }
                boolean z2 = true;
                if (z || !zza(str, zzh, true).zzfrm) {
                    z2 = false;
                }
                return zzp.zza(str, zzh, z, z2);
            } catch (RemoteException e) {
                e = e;
                Log.e("GoogleCertificates", "Failed to get Google certificates from remote", e);
                str2 = "module call";
                return zzp.zzd(str2, e);
            }
        } catch (DynamiteModule.zzc e2) {
            e = e2;
            str2 = "module init";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0019, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized void zzch(android.content.Context r2) {
        /*
            java.lang.Class<com.google.android.gms.common.zzg> r0 = com.google.android.gms.common.zzg.class
            monitor-enter(r0)
            android.content.Context r1 = zzfrc     // Catch:{ all -> 0x001a }
            if (r1 != 0) goto L_0x0011
            if (r2 == 0) goto L_0x0018
            android.content.Context r2 = r2.getApplicationContext()     // Catch:{ all -> 0x001a }
            zzfrc = r2     // Catch:{ all -> 0x001a }
            monitor-exit(r0)
            return
        L_0x0011:
            java.lang.String r2 = "GoogleCertificates"
            java.lang.String r1 = "GoogleCertificates has been initialized already"
            android.util.Log.w(r2, r1)     // Catch:{ all -> 0x001a }
        L_0x0018:
            monitor-exit(r0)
            return
        L_0x001a:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.zzg.zzch(android.content.Context):void");
    }
}
