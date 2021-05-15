package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;

public abstract class OptionsHandler extends BaseHandler implements IOptionsServiceInterface {
    protected OptionsHandler(Looper looper) {
        super(looper);
    }

    public void registerForOptionsEvent(Handler h, int what, Object obj) {
    }

    public void registerForCmcOptionsEvent(Handler h, int what, Object obj) {
    }

    public void registerForP2pOptionsEvent(Handler h, int what, Object obj) {
    }

    public void requestCapabilityExchange(ImsUri uri, long myFeatures, int phoneId, String extFeature) {
    }

    public void requestSendCmcCheckMsg(int phoneId, int regId, String uriStr) {
    }

    public void setOwnCapabilities(long features, int phoneId) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
