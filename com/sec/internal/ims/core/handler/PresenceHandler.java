package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.presence.IPresenceStackInterface;
import java.util.List;

public abstract class PresenceHandler extends BaseHandler implements IPresenceStackInterface {
    protected PresenceHandler(Looper looper) {
        super(looper);
    }

    public void registerForWatcherInfo(Handler h, int what, Object obj) {
    }

    public void registerForPresenceInfo(Handler h, int what, Object obj) {
    }

    public void registerForPublishFailure(Handler h, int what, Object obj) {
    }

    public void subscribeList(List<ImsUri> list, boolean isAnonymousFetch, Message onComplete, String subscriptionId, boolean isGzipEnabled, int expiry, int phoneId) {
    }

    public void subscribe(ImsUri uri, boolean isAnonymousFetch, Message onComplete, String subscriptionId, int phoneId) {
    }

    public void publish(PresenceInfo presenceInfo, Message onComplete, int phoneId) {
    }

    public void unpublish(int phoneId) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
