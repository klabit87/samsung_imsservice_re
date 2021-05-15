package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.RawSipHandler;
import com.sec.internal.interfaces.ims.IImsFramework;

public class ResipRawSipHandler extends RawSipHandler {
    private static final int EVENT_SIP_DIALOG_MESSAGE = 100;
    private static final String LOG_TAG = ResipRawSipHandler.class.getSimpleName();
    private final IImsFramework mImsFramework;
    private final RegistrantList mRawSipRegistrantList = new RegistrantList();
    private StackIF mStackIf = null;

    protected ResipRawSipHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerRawSipEvent(this, 100, (Object) null);
    }

    public void registerForEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForSipDialogEvent");
        this.mRawSipRegistrantList.add(new Registrant(h, what, obj));
    }

    public void unregisterForEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForSipDialogEvent");
        this.mRawSipRegistrantList.remove(h);
    }

    public boolean sendSip(int regId, String sipMessage, Message result) {
        UserAgent ua = getUa(regId);
        if (ua == null) {
            Log.e(LOG_TAG, "sendSip: UserAgent not found");
            return false;
        }
        this.mStackIf.sendSip(ua.getHandle(), sipMessage, result);
        return true;
    }

    public void openSipDialog(boolean isRequired) {
        this.mStackIf.openSipDialog(isRequired);
    }

    private UserAgent getUa(int regId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(regId);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "handleMessage: event: " + msg.what);
        if (msg.what == 100) {
            this.mRawSipRegistrantList.notifyResult((String) ((AsyncResult) msg.obj).result);
        }
    }
}
