package com.google.android.gms.internal;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class zzfjn {
    private static final zzfjn zzprm = new zzfjn();
    private final zzfjw zzprn;
    private final ConcurrentMap<Class<?>, zzfjv<?>> zzpro = new ConcurrentHashMap();

    private zzfjn() {
        String[] strArr = {"com.google.protobuf.AndroidProto3SchemaFactory"};
        zzfjw zzfjw = null;
        for (int i = 0; i <= 0; i++) {
            zzfjw = zzua(strArr[0]);
            if (zzfjw != null) {
                break;
            }
        }
        this.zzprn = zzfjw == null ? new zzfiq() : zzfjw;
    }

    public static zzfjn zzdbf() {
        return zzprm;
    }

    private static zzfjw zzua(String str) {
        try {
            return (zzfjw) Class.forName(str).getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Throwable th) {
            return null;
        }
    }

    public final <T> zzfjv<T> zzl(Class<T> cls) {
        zzfhz.zzc(cls, NSDSNamespaces.NSDSExtras.MESSAGE_TYPE);
        zzfjv<T> zzfjv = (zzfjv) this.zzpro.get(cls);
        if (zzfjv != null) {
            return zzfjv;
        }
        zzfjv<T> zzk = this.zzprn.zzk(cls);
        zzfhz.zzc(cls, NSDSNamespaces.NSDSExtras.MESSAGE_TYPE);
        zzfhz.zzc(zzk, "schema");
        zzfjv<T> putIfAbsent = this.zzpro.putIfAbsent(cls, zzk);
        return putIfAbsent != null ? putIfAbsent : zzk;
    }
}
