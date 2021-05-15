package com.sec.internal.interfaces.ims.core.handler;

import android.os.Handler;

public interface IMiscHandler {
    void registerForEcholocateEvent(Handler handler, int i, Object obj);

    void registerForXqMtripEvent(Handler handler, int i, Object obj);

    void unregisterForEcholocateEvent(Handler handler);

    void unregisterForXqMtripEvent(Handler handler);
}
