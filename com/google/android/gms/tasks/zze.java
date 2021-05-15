package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zze<TResult> implements zzm<TResult> {
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final Executor zzkou;
    /* access modifiers changed from: private */
    public OnCompleteListener<TResult> zzleb;

    public zze(Executor executor, OnCompleteListener<TResult> onCompleteListener) {
        this.zzkou = executor;
        this.zzleb = onCompleteListener;
    }

    public final void cancel() {
        synchronized (this.mLock) {
            this.zzleb = null;
        }
    }

    public final void onComplete(Task<TResult> task) {
        synchronized (this.mLock) {
            if (this.zzleb != null) {
                this.zzkou.execute(new zzf(this, task));
            }
        }
    }
}
