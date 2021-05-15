package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zzi<TResult> implements zzm<TResult> {
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final Executor zzkou;
    /* access modifiers changed from: private */
    public OnSuccessListener<? super TResult> zzlef;

    public zzi(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.zzkou = executor;
        this.zzlef = onSuccessListener;
    }

    public final void cancel() {
        synchronized (this.mLock) {
            this.zzlef = null;
        }
    }

    public final void onComplete(Task<TResult> task) {
        if (task.isSuccessful()) {
            synchronized (this.mLock) {
                if (this.zzlef != null) {
                    this.zzkou.execute(new zzj(this, task));
                }
            }
        }
    }
}
