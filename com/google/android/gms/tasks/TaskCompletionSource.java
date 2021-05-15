package com.google.android.gms.tasks;

public class TaskCompletionSource<TResult> {
    private final zzp<TResult> zzlel = new zzp<>();

    public Task<TResult> getTask() {
        return this.zzlel;
    }

    public void setException(Exception exc) {
        this.zzlel.setException(exc);
    }

    public void setResult(TResult tresult) {
        this.zzlel.setResult(tresult);
    }

    public boolean trySetException(Exception exc) {
        return this.zzlel.trySetException(exc);
    }

    public boolean trySetResult(TResult tresult) {
        return this.zzlel.trySetResult(tresult);
    }
}
