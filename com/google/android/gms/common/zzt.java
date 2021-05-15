package com.google.android.gms.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.internal.zzbih;

public class zzt {
    private static zzt zzfrx;
    private final Context mContext;

    private zzt(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private static zzh zza(PackageInfo packageInfo, zzh... zzhArr) {
        if (packageInfo.signatures == null) {
            return null;
        }
        if (packageInfo.signatures.length != 1) {
            Log.w("GoogleSignatureVerifier", "Package has more than one signature.");
            return null;
        }
        zzi zzi = new zzi(packageInfo.signatures[0].toByteArray());
        for (int i = 0; i < zzhArr.length; i++) {
            if (zzhArr[i].equals(zzi)) {
                return zzhArr[i];
            }
        }
        return null;
    }

    public static boolean zza(PackageInfo packageInfo, boolean z) {
        zzh zzh;
        if (!(packageInfo == null || packageInfo.signatures == null)) {
            if (z) {
                zzh = zza(packageInfo, zzk.zzfrh);
            } else {
                zzh = zza(packageInfo, zzk.zzfrh[0]);
            }
            if (zzh != null) {
                return true;
            }
        }
        return false;
    }

    public static zzt zzcj(Context context) {
        zzbq.checkNotNull(context);
        synchronized (zzt.class) {
            if (zzfrx == null) {
                zzg.zzch(context);
                zzfrx = new zzt(context);
            }
        }
        return zzfrx;
    }

    private final zzp zzgh(String str) {
        String str2;
        try {
            PackageInfo packageInfo = zzbih.zzdd(this.mContext).getPackageInfo(str, 64);
            boolean zzci = zzs.zzci(this.mContext);
            if (packageInfo == null) {
                str2 = "null pkg";
            } else if (packageInfo.signatures.length != 1) {
                str2 = "single cert required";
            } else {
                zzi zzi = new zzi(packageInfo.signatures[0].toByteArray());
                String str3 = packageInfo.packageName;
                zzp zza = zzg.zza(str3, zzi, zzci);
                if (!zza.zzfrm || packageInfo.applicationInfo == null || (packageInfo.applicationInfo.flags & 2) == 0 || (zzci && !zzg.zza(str3, zzi, false).zzfrm)) {
                    return zza;
                }
                str2 = "debuggable release cert app rejected";
            }
            return zzp.zzgg(str2);
        } catch (PackageManager.NameNotFoundException e) {
            String valueOf = String.valueOf(str);
            return zzp.zzgg(valueOf.length() != 0 ? "no pkg ".concat(valueOf) : new String("no pkg "));
        }
    }

    public final boolean zza(PackageInfo packageInfo) {
        if (packageInfo == null) {
            return false;
        }
        if (zza(packageInfo, false)) {
            return true;
        }
        if (zza(packageInfo, true)) {
            if (zzs.zzci(this.mContext)) {
                return true;
            }
            Log.w("GoogleSignatureVerifier", "Test-keys aren't accepted on this build.");
        }
        return false;
    }

    public final boolean zzbp(int i) {
        zzp zzp;
        String[] packagesForUid = zzbih.zzdd(this.mContext).getPackagesForUid(i);
        if (packagesForUid != null && packagesForUid.length != 0) {
            zzp = null;
            for (String zzgh : packagesForUid) {
                zzp = zzgh(zzgh);
                if (zzp.zzfrm) {
                    break;
                }
            }
        } else {
            zzp = zzp.zzgg("no pkgs");
        }
        if (!zzp.zzfrm) {
            if (zzp.cause != null) {
                Log.d("GoogleCertificatesRslt", zzp.getErrorMessage(), zzp.cause);
            } else {
                Log.d("GoogleCertificatesRslt", zzp.getErrorMessage());
            }
        }
        return zzp.zzfrm;
    }
}
