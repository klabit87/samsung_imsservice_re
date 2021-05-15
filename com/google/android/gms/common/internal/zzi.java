package com.google.android.gms.common.internal;

import android.util.Log;

public abstract class zzi<TListener> {
    private TListener zzgat;
    private /* synthetic */ zzd zzgfk;
    private boolean zzgfl = false;

    public zzi(zzd zzd, TListener tlistener) {
        this.zzgfk = zzd;
        this.zzgat = tlistener;
    }

    public final void removeListener() {
        synchronized (this) {
            this.zzgat = null;
        }
    }

    public final void unregister() {
        removeListener();
        synchronized (this.zzgfk.zzgey) {
            this.zzgfk.zzgey.remove(this);
        }
    }

    /* access modifiers changed from: protected */
    public abstract void zzamb();

    public final void zzamc() {
        TListener tlistener;
        synchronized (this) {
            tlistener = this.zzgat;
            if (this.zzgfl) {
                String valueOf = String.valueOf(this);
                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 47);
                sb.append("Callback proxy ");
                sb.append(valueOf);
                sb.append(" being reused. This is not safe.");
                Log.w("GmsClient", sb.toString());
            }
        }
        if (tlistener != null) {
            try {
                zzw(tlistener);
            } catch (RuntimeException e) {
                zzamb();
                throw e;
            }
        } else {
            zzamb();
        }
        synchronized (this) {
            this.zzgfl = true;
        }
        unregister();
    }

    /* access modifiers changed from: protected */
    public abstract void zzw(TListener tlistener);
}
