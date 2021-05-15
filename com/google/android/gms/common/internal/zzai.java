package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.common.stats.zza;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.HashMap;

final class zzai extends zzag implements Handler.Callback {
    /* access modifiers changed from: private */
    public final Context mApplicationContext;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final HashMap<zzah, zzaj> zzggw = new HashMap<>();
    /* access modifiers changed from: private */
    public final zza zzggx;
    private final long zzggy;
    /* access modifiers changed from: private */
    public final long zzggz;

    zzai(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        this.mHandler = new Handler(context.getMainLooper(), this);
        this.zzggx = zza.zzanm();
        this.zzggy = 5000;
        this.zzggz = 300000;
    }

    public final boolean handleMessage(Message message) {
        int i = message.what;
        if (i == 0) {
            synchronized (this.zzggw) {
                zzah zzah = (zzah) message.obj;
                zzaj zzaj = this.zzggw.get(zzah);
                if (zzaj != null && zzaj.zzamv()) {
                    if (zzaj.isBound()) {
                        zzaj.zzgs("GmsClientSupervisor");
                    }
                    this.zzggw.remove(zzah);
                }
            }
            return true;
        } else if (i != 1) {
            return false;
        } else {
            synchronized (this.zzggw) {
                zzah zzah2 = (zzah) message.obj;
                zzaj zzaj2 = this.zzggw.get(zzah2);
                if (zzaj2 != null && zzaj2.getState() == 3) {
                    String valueOf = String.valueOf(zzah2);
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 47);
                    sb.append("Timeout waiting for ServiceConnection callback ");
                    sb.append(valueOf);
                    Log.wtf("GmsClientSupervisor", sb.toString(), new Exception());
                    ComponentName componentName = zzaj2.getComponentName();
                    if (componentName == null) {
                        componentName = zzah2.getComponentName();
                    }
                    if (componentName == null) {
                        componentName = new ComponentName(zzah2.getPackage(), NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
                    }
                    zzaj2.onServiceDisconnected(componentName);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public final boolean zza(zzah zzah, ServiceConnection serviceConnection, String str) {
        boolean isBound;
        zzbq.checkNotNull(serviceConnection, "ServiceConnection must not be null");
        synchronized (this.zzggw) {
            zzaj zzaj = this.zzggw.get(zzah);
            if (zzaj == null) {
                zzaj = new zzaj(this, zzah);
                zzaj.zza(serviceConnection, str);
                zzaj.zzgr(str);
                this.zzggw.put(zzah, zzaj);
            } else {
                this.mHandler.removeMessages(0, zzah);
                if (!zzaj.zza(serviceConnection)) {
                    zzaj.zza(serviceConnection, str);
                    int state = zzaj.getState();
                    if (state == 1) {
                        serviceConnection.onServiceConnected(zzaj.getComponentName(), zzaj.getBinder());
                    } else if (state == 2) {
                        zzaj.zzgr(str);
                    }
                } else {
                    String valueOf = String.valueOf(zzah);
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 81);
                    sb.append("Trying to bind a GmsServiceConnection that was already connected before.  config=");
                    sb.append(valueOf);
                    throw new IllegalStateException(sb.toString());
                }
            }
            isBound = zzaj.isBound();
        }
        return isBound;
    }

    /* access modifiers changed from: protected */
    public final void zzb(zzah zzah, ServiceConnection serviceConnection, String str) {
        zzbq.checkNotNull(serviceConnection, "ServiceConnection must not be null");
        synchronized (this.zzggw) {
            zzaj zzaj = this.zzggw.get(zzah);
            if (zzaj == null) {
                String valueOf = String.valueOf(zzah);
                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 50);
                sb.append("Nonexistent connection status for service config: ");
                sb.append(valueOf);
                throw new IllegalStateException(sb.toString());
            } else if (zzaj.zza(serviceConnection)) {
                zzaj.zzb(serviceConnection, str);
                if (zzaj.zzamv()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, zzah), this.zzggy);
                }
            } else {
                String valueOf2 = String.valueOf(zzah);
                StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 76);
                sb2.append("Trying to unbind a GmsServiceConnection  that was not bound before.  config=");
                sb2.append(valueOf2);
                throw new IllegalStateException(sb2.toString());
            }
        }
    }
}
