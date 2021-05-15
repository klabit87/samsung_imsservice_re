package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zzg<TResult> implements zzm<TResult> {
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final Executor zzkou;
    /* access modifiers changed from: private */
    public OnFailureListener zzled;

    public zzg(Executor executor, OnFailureListener onFailureListener) {
        this.zzkou = executor;
        this.zzled = onFailureListener;
    }

    public final void cancel() {
        synchronized (this.mLock) {
            this.zzled = null;
        }
    }

    public final void onComplete(Task<TResult> task) {
        if (!task.isSuccessful()) {
            synchronized (this.mLock) {
                if (this.zzled != null) {
                    this.zzkou.execute(new zzh(this, task));
                }
            }
        }
    }
}
