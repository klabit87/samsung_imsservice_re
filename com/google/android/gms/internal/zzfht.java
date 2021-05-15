package com.google.android.gms.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

final class zzfht implements zzfjb {
    private static final zzfht zzppf = new zzfht();
    private final Map<Class<?>, Method> zzppg = new HashMap();

    private zzfht() {
    }

    public static zzfht zzczp() {
        return zzppf;
    }

    public final boolean zzi(Class<?> cls) {
        return zzfhu.class.isAssignableFrom(cls);
    }

    public final zzfja zzj(Class<?> cls) {
        if (!zzfhu.class.isAssignableFrom(cls)) {
            String valueOf = String.valueOf(cls.getName());
            throw new IllegalArgumentException(valueOf.length() != 0 ? "Unsupported message type: ".concat(valueOf) : new String("Unsupported message type: "));
        }
        try {
            Method method = this.zzppg.get(cls);
            if (method == null) {
                method = cls.getDeclaredMethod("buildMessageInfo", new Class[0]);
                method.setAccessible(true);
                this.zzppg.put(cls, method);
            }
            return (zzfja) method.invoke((Object) null, new Object[0]);
        } catch (Exception e) {
            String valueOf2 = String.valueOf(cls.getName());
            throw new RuntimeException(valueOf2.length() != 0 ? "Unable to get message info for ".concat(valueOf2) : new String("Unable to get message info for "), e);
        }
    }
}
