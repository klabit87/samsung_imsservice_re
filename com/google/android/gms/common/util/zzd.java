package com.google.android.gms.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.gms.internal.zzbih;

public final class zzd {
    public static int zzt(Context context, String str) {
        Bundle bundle;
        PackageInfo zzu = zzu(context, str);
        if (zzu == null || zzu.applicationInfo == null || (bundle = zzu.applicationInfo.metaData) == null) {
            return -1;
        }
        return bundle.getInt("com.google.android.gms.version", -1);
    }

    private static PackageInfo zzu(Context context, String str) {
        try {
            return zzbih.zzdd(context).getPackageInfo(str, 128);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean zzv(Context context, String str) {
        "com.google.android.gms".equals(str);
        try {
            return (zzbih.zzdd(context).getApplicationInfo(str, 0).flags & 2097152) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
