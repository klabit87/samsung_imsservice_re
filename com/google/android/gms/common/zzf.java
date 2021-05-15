package com.google.android.gms.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.google.android.gms.common.internal.zzak;
import com.google.android.gms.common.util.zzj;
import com.google.android.gms.internal.zzbih;
import com.sec.internal.imscr.LogClass;

public class zzf {
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.google.android.gms";
    public static final int GOOGLE_PLAY_SERVICES_VERSION_CODE = zzs.GOOGLE_PLAY_SERVICES_VERSION_CODE;
    private static final zzf zzfqz = new zzf();

    zzf() {
    }

    public static Intent zza(Context context, int i, String str) {
        if (i == 1 || i == 2) {
            return (context == null || !zzj.zzcv(context)) ? zzak.zzt("com.google.android.gms", zzq(context, str)) : zzak.zzamw();
        }
        if (i != 3) {
            return null;
        }
        return zzak.zzgt("com.google.android.gms");
    }

    public static zzf zzahf() {
        return zzfqz;
    }

    public static int zzc(Context context, int i) {
        int zzc = zzs.zzc(context, i);
        if (zzs.zzd(context, zzc)) {
            return 18;
        }
        return zzc;
    }

    public static void zzce(Context context) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
        zzs.zzbo(context);
    }

    public static void zzcf(Context context) {
        zzs.zzcf(context);
    }

    public static int zzcg(Context context) {
        return zzs.zzcg(context);
    }

    public static boolean zzd(Context context, int i) {
        return zzs.zzd(context, i);
    }

    private static String zzq(Context context, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("gcore_");
        sb.append(GOOGLE_PLAY_SERVICES_VERSION_CODE);
        sb.append("-");
        if (!TextUtils.isEmpty(str)) {
            sb.append(str);
        }
        sb.append("-");
        if (context != null) {
            sb.append(context.getPackageName());
        }
        sb.append("-");
        if (context != null) {
            try {
                sb.append(zzbih.zzdd(context).getPackageInfo(context.getPackageName(), 0).versionCode);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return sb.toString();
    }

    public PendingIntent getErrorResolutionPendingIntent(Context context, int i, int i2) {
        return zza(context, i, i2, (String) null);
    }

    public String getErrorString(int i) {
        return zzs.getErrorString(i);
    }

    public int isGooglePlayServicesAvailable(Context context) {
        return zzc(context, -1);
    }

    public boolean isUserResolvableError(int i) {
        return zzs.isUserRecoverableError(i);
    }

    public final PendingIntent zza(Context context, int i, int i2, String str) {
        Intent zza = zza(context, i, str);
        if (zza == null) {
            return null;
        }
        return PendingIntent.getActivity(context, i2, zza, LogClass.SIM_EVENT);
    }

    @Deprecated
    public final Intent zzbo(int i) {
        return zza((Context) null, i, (String) null);
    }
}
