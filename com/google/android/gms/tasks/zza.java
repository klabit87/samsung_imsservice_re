package com.google.android.gms.tasks;

import java.util.concurrent.Executor;

final class zza<TResult, TContinuationResult> implements zzm<TResult> {
    private final Executor zzkou;
    /* access modifiers changed from: private */
    public final Continuation<TResult, TContinuationResult> zzldw;
    /* access modifiers changed from: private */
    public final zzp<TContinuationResult> zzldx;

    public zza(Executor executor, Continuation<TResult, TContinuationResult> continuation, zzp<TContinuationResult> zzp) {
        this.zzkou = executor;
        this.zzldw = continuation;
        this.zzldx = zzp;
    }

    public final void cancel() {
        throw new UnsupportedOperationException();
    }

    public final void onComplete(Task<TResult> task) {
        this.zzkou.execute(new zzb(this, task));
    }
}
