package com.google.android.gms.common;

import com.google.android.gms.common.util.zza;
import com.google.android.gms.common.util.zzm;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;

final class zzr extends zzp {
    private final String packageName;
    private final zzh zzfro;
    private final boolean zzfrp;
    private final boolean zzfrq;

    private zzr(String str, zzh zzh, boolean z, boolean z2) {
        super(false, (String) null, (Throwable) null);
        this.packageName = str;
        this.zzfro = zzh;
        this.zzfrp = z;
        this.zzfrq = z2;
    }

    /* access modifiers changed from: package-private */
    public final String getErrorMessage() {
        String str = this.zzfrq ? "debug cert rejected" : "not whitelisted";
        String str2 = this.packageName;
        String zzn = zzm.zzn(zza.zzeq(Constants.DIGEST_ALGORITHM_SHA1).digest(this.zzfro.getBytes()));
        boolean z = this.zzfrp;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 44 + String.valueOf(str2).length() + String.valueOf(zzn).length());
        sb.append(str);
        sb.append(": pkg=");
        sb.append(str2);
        sb.append(", sha1=");
        sb.append(zzn);
        sb.append(", atk=");
        sb.append(z);
        sb.append(", ver=12211278.false");
        return sb.toString();
    }
}
