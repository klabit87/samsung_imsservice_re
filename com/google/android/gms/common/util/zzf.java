package com.google.android.gms.common.util;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.ArraySet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class zzf {
    private static <K, V> Map<K, V> zza(int i, boolean z, K[] kArr, V[] vArr) {
        Map<K, V> zzh = zzh(i, false);
        for (int i2 = 0; i2 < kArr.length; i2++) {
            zzh.put(kArr[i2], vArr[i2]);
        }
        return zzh;
    }

    public static <K, V> Map<K, V> zza(K k, V v, K k2, V v2, K k3, V v3) {
        Map zzh = zzh(3, false);
        zzh.put(k, v);
        zzh.put(k2, v2);
        zzh.put(k3, v3);
        return Collections.unmodifiableMap(zzh);
    }

    public static <K, V> Map<K, V> zza(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        Map zzh = zzh(6, false);
        zzh.put(k, v);
        zzh.put(k2, v2);
        zzh.put(k3, v3);
        zzh.put(k4, v4);
        zzh.put(k5, v5);
        zzh.put(k6, v6);
        return Collections.unmodifiableMap(zzh);
    }

    public static <T> Set<T> zza(T t, T t2, T t3) {
        Set zzg = zzg(3, false);
        zzg.add(t);
        zzg.add(t2);
        zzg.add(t3);
        return Collections.unmodifiableSet(zzg);
    }

    public static <K, V> Map<K, V> zzb(K[] kArr, V[] vArr) {
        if (kArr.length == vArr.length) {
            int length = kArr.length;
            return length != 0 ? length != 1 ? Collections.unmodifiableMap(zza(kArr.length, false, kArr, vArr)) : Collections.singletonMap(kArr[0], vArr[0]) : Collections.emptyMap();
        }
        int length2 = kArr.length;
        int length3 = vArr.length;
        StringBuilder sb = new StringBuilder(66);
        sb.append("Key and values array lengths not equal: ");
        sb.append(length2);
        sb.append(" != ");
        sb.append(length3);
        throw new IllegalArgumentException(sb.toString());
    }

    public static <T> Set<T> zzb(T... tArr) {
        int length = tArr.length;
        if (length == 0) {
            return Collections.emptySet();
        }
        if (length == 1) {
            return Collections.singleton(tArr[0]);
        }
        if (length == 2) {
            T t = tArr[0];
            T t2 = tArr[1];
            Set zzg = zzg(2, false);
            zzg.add(t);
            zzg.add(t2);
            return Collections.unmodifiableSet(zzg);
        } else if (length == 3) {
            return zza(tArr[0], tArr[1], tArr[2]);
        } else {
            if (length != 4) {
                Set zzg2 = zzg(tArr.length, false);
                Collections.addAll(zzg2, tArr);
                return Collections.unmodifiableSet(zzg2);
            }
            T t3 = tArr[0];
            T t4 = tArr[1];
            T t5 = tArr[2];
            T t6 = tArr[3];
            Set zzg3 = zzg(4, false);
            zzg3.add(t3);
            zzg3.add(t4);
            zzg3.add(t5);
            zzg3.add(t6);
            return Collections.unmodifiableSet(zzg3);
        }
    }

    private static <T> Set<T> zzg(int i, boolean z) {
        return i <= 256 ? new ArraySet(i) : new HashSet(i, 1.0f);
    }

    private static <K, V> Map<K, V> zzh(int i, boolean z) {
        return i <= 256 ? new ArrayMap(i) : new HashMap(i, 1.0f);
    }
}
