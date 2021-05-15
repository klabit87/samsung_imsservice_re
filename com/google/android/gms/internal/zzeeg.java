package com.google.android.gms.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class zzeeg<A, B, C> {
    private final Map<B, C> values;
    private final List<A> zzmyy;
    private final zzedt<A, B> zzmyz;
    private zzeed<A, C> zzmza;
    private zzeed<A, C> zzmzb;

    private zzeeg(List<A> list, Map<B, C> map, zzedt<A, B> zzedt) {
        this.zzmyy = list;
        this.values = map;
        this.zzmyz = zzedt;
    }

    private final C zzbt(A a) {
        return this.values.get(this.zzmyz.zzbo(a));
    }

    public static <A, B, C> zzeee<A, C> zzc(List<A> list, Map<B, C> map, zzedt<A, B> zzedt, Comparator<A> comparator) {
        int i;
        zzeeg zzeeg = new zzeeg(list, map, zzedt);
        Collections.sort(list, comparator);
        Iterator<zzeej> it = new zzeeh(list.size()).iterator();
        int size = list.size();
        while (it.hasNext()) {
            zzeej next = it.next();
            size -= next.zzmzf;
            if (next.zzmze) {
                i = zzeea.zzmyt;
            } else {
                zzeeg.zzf(zzeea.zzmyt, next.zzmzf, size);
                size -= next.zzmzf;
                i = zzeea.zzmys;
            }
            zzeeg.zzf(i, next.zzmzf, size);
        }
        zzedz zzedz = zzeeg.zzmza;
        if (zzedz == null) {
            zzedz = zzedy.zzbvx();
        }
        return new zzeee<>(zzedz, comparator);
    }

    private final void zzf(int i, int i2, int i3) {
        zzedz zzx = zzx(i3 + 1, i2 - 1);
        A a = this.zzmyy.get(i3);
        zzeed<A, C> zzeec = i == zzeea.zzmys ? new zzeec<>(a, zzbt(a), (zzedz) null, zzx) : new zzedx<>(a, zzbt(a), (zzedz) null, zzx);
        if (this.zzmza == null) {
            this.zzmza = zzeec;
        } else {
            this.zzmzb.zza(zzeec);
        }
        this.zzmzb = zzeec;
    }

    private final zzedz<A, C> zzx(int i, int i2) {
        if (i2 == 0) {
            return zzedy.zzbvx();
        }
        if (i2 == 1) {
            A a = this.zzmyy.get(i);
            return new zzedx(a, zzbt(a), (zzedz) null, (zzedz) null);
        }
        int i3 = i2 / 2;
        int i4 = i + i3;
        zzedz zzx = zzx(i, i3);
        zzedz zzx2 = zzx(i4 + 1, i3);
        A a2 = this.zzmyy.get(i4);
        return new zzedx(a2, zzbt(a2), zzx, zzx2);
    }
}
