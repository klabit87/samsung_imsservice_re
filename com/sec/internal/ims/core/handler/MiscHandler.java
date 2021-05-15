package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.interfaces.ims.core.handler.IMiscHandler;

public class MiscHandler extends BaseHandler implements IMiscHandler {
    protected MiscHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }

    public void registerForEcholocateEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForEcholocateEvent(Handler h) {
    }

    public void registerForXqMtripEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForXqMtripEvent(Handler h) {
    }
}
