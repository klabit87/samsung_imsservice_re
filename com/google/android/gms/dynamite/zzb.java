package com.google.android.gms.dynamite;

import android.content.Context;
import com.google.android.gms.dynamite.DynamiteModule;

final class zzb implements DynamiteModule.zzd {
    zzb() {
    }

    public final zzj zza(Context context, String str, zzi zzi) throws DynamiteModule.zzc {
        zzj zzj = new zzj();
        zzj.zzhdt = zzi.zzc(context, str, true);
        if (zzj.zzhdt != 0) {
            zzj.zzhdu = 1;
        } else {
            zzj.zzhds = zzi.zzx(context, str);
            if (zzj.zzhds != 0) {
                zzj.zzhdu = -1;
            }
        }
        return zzj;
    }
}
