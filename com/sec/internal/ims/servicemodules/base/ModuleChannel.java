package com.sec.internal.ims.servicemodules.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.ims.registry.ImsRegistry;

public class ModuleChannel {
    public static final String CAPDISCOVERY = "CapabilityDiscoveryModule";
    public static final int EVT_CAPDISCOVERY_DISABLE_FEATURE = 8002;
    public static final int EVT_CAPDISCOVERY_ENABLE_FEATURE = 8001;
    public static final int EVT_MODULE_CHANNEL_BASE = 8000;
    public static final int EVT_MODULE_CHANNEL_RESPONSE = 8999;
    protected Handler mDst = null;
    protected Handler mSrc = null;

    public interface Listener {
        void onFinished(int i, Object obj);
    }

    public static ModuleChannel createChannel(String service, Handler src) {
        return new ModuleChannel(src, ImsRegistry.getServiceModuleManager().getServiceModuleHandler(service));
    }

    private ModuleChannel(Handler src, Handler dst) {
        this.mSrc = src;
        this.mDst = dst;
    }

    public void sendEvent(int what, Object object, Listener listener) {
        Message msg = Message.obtain(this.mDst, what, object);
        Handler handler = this.mSrc;
        if (!(handler == null || listener == null)) {
            Message resp = Message.obtain(handler, EVT_MODULE_CHANNEL_RESPONSE, listener);
            Bundle data = new Bundle();
            data.putParcelable("callback_msg", resp);
            msg.setData(data);
        }
        msg.sendToTarget();
    }

    public void disableFeature(long feature) {
        Message.obtain(this.mDst, EVT_CAPDISCOVERY_DISABLE_FEATURE, Long.valueOf(feature)).sendToTarget();
    }
}
