package com.google.android.gms.internal;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.util.Map;

final class zzfkd implements Comparable<zzfkd>, Map.Entry<K, V> {
    private V value;
    private final K zzpsr;
    private /* synthetic */ zzfjy zzpss;

    zzfkd(zzfjy zzfjy, K k, V v) {
        this.zzpss = zzfjy;
        this.zzpsr = k;
        this.value = v;
    }

    zzfkd(zzfjy zzfjy, Map.Entry<K, V> entry) {
        this(zzfjy, (Comparable) entry.getKey(), entry.getValue());
    }

    private static boolean equals(Object obj, Object obj2) {
        return obj == null ? obj2 == null : obj.equals(obj2);
    }

    public final /* synthetic */ int compareTo(Object obj) {
        return ((Comparable) getKey()).compareTo((Comparable) ((zzfkd) obj).getKey());
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map.Entry)) {
            return false;
        }
        Map.Entry entry = (Map.Entry) obj;
        return equals(this.zzpsr, entry.getKey()) && equals(this.value, entry.getValue());
    }

    public final /* synthetic */ Object getKey() {
        return this.zzpsr;
    }

    public final V getValue() {
        return this.value;
    }

    public final int hashCode() {
        K k = this.zzpsr;
        int i = 0;
        int hashCode = k == null ? 0 : k.hashCode();
        V v = this.value;
        if (v != null) {
            i = v.hashCode();
        }
        return hashCode ^ i;
    }

    public final V setValue(V v) {
        this.zzpss.zzdbr();
        V v2 = this.value;
        this.value = v;
        return v2;
    }

    public final String toString() {
        String valueOf = String.valueOf(this.zzpsr);
        String valueOf2 = String.valueOf(this.value);
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 1 + String.valueOf(valueOf2).length());
        sb.append(valueOf);
        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
        sb.append(valueOf2);
        return sb.toString();
    }
}
