package com.google.android.gms.common.api.internal;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import com.google.android.gms.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzbf;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.internal.zzca;

@Deprecated
public final class zzbz {
    private static final Object sLock = new Object();
    private static zzbz zzgah;
    private final String mAppId;
    private final Status zzgai;
    private final boolean zzgaj;
    private final boolean zzgak;

    private zzbz(Context context) {
        Resources resources = context.getResources();
        int identifier = resources.getIdentifier("google_app_measurement_enable", "integer", resources.getResourcePackageName(R.string.common_google_play_services_unknown_issue));
        boolean z = false;
        boolean z2 = true;
        if (identifier != 0) {
            z = resources.getInteger(identifier) != 0 ? true : z;
            this.zzgak = !z;
            z2 = z;
        } else {
            this.zzgak = false;
        }
        this.zzgaj = z2;
        String zzcr = zzbf.zzcr(context);
        zzcr = zzcr == null ? new zzca(context).getString("google_app_id") : zzcr;
        if (TextUtils.isEmpty(zzcr)) {
            this.zzgai = new Status(10, "Missing google app id value from from string resources with name google_app_id.");
            this.mAppId = null;
            return;
        }
        this.mAppId = zzcr;
        this.zzgai = Status.zzftq;
    }

    public static String zzakq() {
        return zzgi("getGoogleAppId").mAppId;
    }

    public static boolean zzakr() {
        return zzgi("isMeasurementExplicitlyDisabled").zzgak;
    }

    public static Status zzcl(Context context) {
        Status status;
        zzbq.checkNotNull(context, "Context must not be null.");
        synchronized (sLock) {
            if (zzgah == null) {
                zzgah = new zzbz(context);
            }
            status = zzgah.zzgai;
        }
        return status;
    }

    private static zzbz zzgi(String str) {
        zzbz zzbz;
        synchronized (sLock) {
            if (zzgah != null) {
                zzbz = zzgah;
            } else {
                StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 34);
                sb.append("Initialize must be called before ");
                sb.append(str);
                sb.append(".");
                throw new IllegalStateException(sb.toString());
            }
        }
        return zzbz;
    }
}
