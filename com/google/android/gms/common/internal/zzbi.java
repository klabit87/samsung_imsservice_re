package com.google.android.gms.common.internal;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.util.ArrayList;
import java.util.List;

public final class zzbi {
    private final Object zzdht;
    private final List<String> zzgho;

    private zzbi(Object obj) {
        this.zzdht = zzbq.checkNotNull(obj);
        this.zzgho = new ArrayList();
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(this.zzdht.getClass().getSimpleName());
        sb.append('{');
        int size = this.zzgho.size();
        for (int i = 0; i < size; i++) {
            sb.append(this.zzgho.get(i));
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public final zzbi zzg(String str, Object obj) {
        List<String> list = this.zzgho;
        String str2 = (String) zzbq.checkNotNull(str);
        String valueOf = String.valueOf(obj);
        StringBuilder sb = new StringBuilder(String.valueOf(str2).length() + 1 + String.valueOf(valueOf).length());
        sb.append(str2);
        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
        sb.append(valueOf);
        list.add(sb.toString());
        return this;
    }
}
