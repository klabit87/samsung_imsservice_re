package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.ims.servicemodules.csh.event.CshCancelSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshRejectSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IshAcceptSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.IshStartSessionParams;

public abstract class IshHandler extends BaseHandler implements IIshServiceInterface {
    public abstract void acceptIshSession(IshAcceptSessionParams ishAcceptSessionParams);

    public abstract void cancelIshSession(CshCancelSessionParams cshCancelSessionParams);

    public abstract void registerForIshIncomingSession(Handler handler, int i, Object obj);

    public abstract void registerForIshSessionEstablished(Handler handler, int i, Object obj);

    public abstract void registerForIshTransferComplete(Handler handler, int i, Object obj);

    public abstract void registerForIshTransferFailed(Handler handler, int i, Object obj);

    public abstract void registerForIshTransferProgress(Handler handler, int i, Object obj);

    public abstract void rejectIshSession(CshRejectSessionParams cshRejectSessionParams);

    public abstract void startIshSession(IshStartSessionParams ishStartSessionParams);

    public abstract void stopIshSession(CshCancelSessionParams cshCancelSessionParams);

    public abstract void unregisterForIshIncomingSession(Handler handler);

    public abstract void unregisterForIshSessionEstablished(Handler handler);

    public abstract void unregisterForIshTransferComplete(Handler handler);

    public abstract void unregisterForIshTransferFailed(Handler handler);

    public abstract void unregisterForIshTransferProgress(Handler handler);

    protected IshHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
