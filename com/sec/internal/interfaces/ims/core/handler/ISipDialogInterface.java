package com.sec.internal.interfaces.ims.core.handler;

import android.os.Handler;
import android.os.Message;

public interface ISipDialogInterface {
    void openSipDialog(boolean z);

    void registerForEvent(Handler handler, int i, Object obj);

    boolean sendSip(int i, String str, Message message);

    void unregisterForEvent(Handler handler);
}
