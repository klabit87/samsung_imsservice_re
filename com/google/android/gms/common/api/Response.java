package com.google.android.gms.common.api;

import com.google.android.gms.common.api.Result;

public class Response<T extends Result> {
    private T zzftm;

    public Response() {
    }

    protected Response(T t) {
        this.zzftm = t;
    }

    /* access modifiers changed from: protected */
    public T getResult() {
        return this.zzftm;
    }

    public void setResult(T t) {
        this.zzftm = t;
    }
}
