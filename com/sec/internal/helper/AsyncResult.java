package com.sec.internal.helper;

import android.os.Message;

public class AsyncResult {
    public Throwable exception;
    public Object result;
    public Object userObj;

    public static AsyncResult forMessage(Message m, Object r, Throwable ex) {
        AsyncResult ret = new AsyncResult(m.obj, r, ex);
        m.obj = ret;
        return ret;
    }

    public AsyncResult(Object uo, Object r, Throwable ex) {
        this.userObj = uo;
        this.result = r;
        this.exception = ex;
    }
}
