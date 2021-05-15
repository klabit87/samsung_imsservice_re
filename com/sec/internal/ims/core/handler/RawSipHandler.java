package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sec.internal.interfaces.ims.core.handler.ISipDialogInterface;

public abstract class RawSipHandler extends BaseHandler implements ISipDialogInterface {
    protected RawSipHandler(Looper looper) {
        super(looper);
    }

    public void registerForEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForEvent(Handler h) {
    }

    public boolean sendSip(int regId, String sipMessage, Message result) {
        return false;
    }

    public void openSipDialog(boolean isRequired) {
    }
}
