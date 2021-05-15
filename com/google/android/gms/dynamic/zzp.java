package com.google.android.gms.dynamic;

import android.content.Context;
import android.os.IBinder;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.zzs;

public abstract class zzp<T> {
    private final String zzhcz;
    private T zzhda;

    protected zzp(String str) {
        this.zzhcz = str;
    }

    /* access modifiers changed from: protected */
    public final T zzdg(Context context) throws zzq {
        if (this.zzhda == null) {
            zzbq.checkNotNull(context);
            Context remoteContext = zzs.getRemoteContext(context);
            if (remoteContext != null) {
                try {
                    this.zzhda = zze((IBinder) remoteContext.getClassLoader().loadClass(this.zzhcz).newInstance());
                } catch (ClassNotFoundException e) {
                    throw new zzq("Could not load creator class.", e);
                } catch (InstantiationException e2) {
                    throw new zzq("Could not instantiate creator.", e2);
                } catch (IllegalAccessException e3) {
                    throw new zzq("Could not access creator.", e3);
                }
            } else {
                throw new zzq("Could not get remote context.");
            }
        }
        return this.zzhda;
    }

    /* access modifiers changed from: protected */
    public abstract T zze(IBinder iBinder);
}
