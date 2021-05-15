package com.google.firebase.iid;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class zzy extends Handler {
    private /* synthetic */ zzx zzoli;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    zzy(zzx zzx, Looper looper) {
        super(looper);
        this.zzoli = zzx;
    }

    public final void handleMessage(Message message) {
        this.zzoli.zze(message);
    }
}
