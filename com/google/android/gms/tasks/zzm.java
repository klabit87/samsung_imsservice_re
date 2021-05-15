package com.google.android.gms.tasks;

interface zzm<TResult> {
    void cancel();

    void onComplete(Task<TResult> task);
}
