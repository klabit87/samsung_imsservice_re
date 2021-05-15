package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfgj;
import com.google.android.gms.internal.zzfgk;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class zzfgk<MessageType extends zzfgj<MessageType, BuilderType>, BuilderType extends zzfgk<MessageType, BuilderType>> implements zzfjd {
    protected static <T> void zza(Iterable<T> iterable, List<? super T> list) {
        zzfhz.checkNotNull(iterable);
        if (iterable instanceof zzfil) {
            List<?> zzdap = ((zzfil) iterable).zzdap();
            zzfil zzfil = (zzfil) list;
            int size = list.size();
            for (Object next : zzdap) {
                if (next == null) {
                    StringBuilder sb = new StringBuilder(37);
                    sb.append("Element at index ");
                    sb.append(zzfil.size() - size);
                    sb.append(" is null.");
                    String sb2 = sb.toString();
                    for (int size2 = zzfil.size() - 1; size2 >= size; size2--) {
                        zzfil.remove(size2);
                    }
                    throw new NullPointerException(sb2);
                } else if (next instanceof zzfgs) {
                    zzfil.zzba((zzfgs) next);
                } else {
                    zzfil.add((String) next);
                }
            }
        } else if (iterable instanceof zzfjm) {
            list.addAll((Collection) iterable);
        } else {
            zzb(iterable, list);
        }
    }

    private static <T> void zzb(Iterable<T> iterable, List<? super T> list) {
        if ((list instanceof ArrayList) && (iterable instanceof Collection)) {
            ((ArrayList) list).ensureCapacity(list.size() + ((Collection) iterable).size());
        }
        int size = list.size();
        for (T next : iterable) {
            if (next == null) {
                StringBuilder sb = new StringBuilder(37);
                sb.append("Element at index ");
                sb.append(list.size() - size);
                sb.append(" is null.");
                String sb2 = sb.toString();
                for (int size2 = list.size() - 1; size2 >= size; size2--) {
                    list.remove(size2);
                }
                throw new NullPointerException(sb2);
            }
            list.add(next);
        }
    }

    /* access modifiers changed from: protected */
    public abstract BuilderType zza(MessageType messagetype);

    /* renamed from: zza */
    public abstract BuilderType zzb(zzfhb zzfhb, zzfhm zzfhm) throws IOException;

    /* renamed from: zzcxj */
    public abstract BuilderType clone();

    public final /* synthetic */ zzfjd zzd(zzfjc zzfjc) {
        if (zzczu().getClass().isInstance(zzfjc)) {
            return zza((zzfgj) zzfjc);
        }
        throw new IllegalArgumentException("mergeFrom(MessageLite) can only merge messages of the same type.");
    }
}
