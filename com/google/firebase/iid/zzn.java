package com.google.firebase.iid;

import android.os.Handler;
import android.os.Message;

final /* synthetic */ class zzn implements Handler.Callback {
    private final zzm zzola;

    zzn(zzm zzm) {
        this.zzola = zzm;
    }

    public final boolean handleMessage(Message message) {
        return this.zzola.zzc(message);
    }
}
