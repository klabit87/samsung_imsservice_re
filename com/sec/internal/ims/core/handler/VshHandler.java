package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.ims.servicemodules.csh.event.CshAcceptSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshCancelSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshRejectSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshStartSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.VshSwitchCameraParams;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;

public abstract class VshHandler extends BaseHandler implements IvshServiceInterface {
    public abstract void acceptVshSession(CshAcceptSessionParams cshAcceptSessionParams);

    public abstract void cancelVshSession(CshCancelSessionParams cshCancelSessionParams);

    public abstract void registerForVshIncomingSession(Handler handler, int i, Object obj);

    public abstract void registerForVshSessionEstablished(Handler handler, int i, Object obj);

    public abstract void registerForVshSessionTerminated(Handler handler, int i, Object obj);

    public abstract void rejectVshSession(CshRejectSessionParams cshRejectSessionParams);

    public abstract void resetVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams);

    public abstract void setVshPhoneOrientation(VshOrientation vshOrientation);

    public abstract void setVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams);

    public abstract void startVshSession(VshStartSessionParams vshStartSessionParams);

    public abstract void stopVshSession(CshCancelSessionParams cshCancelSessionParams);

    public abstract void switchCamera(VshSwitchCameraParams vshSwitchCameraParams);

    public abstract void unregisterForVshIncomingSession(Handler handler);

    public abstract void unregisterForVshSessionEstablished(Handler handler);

    public abstract void unregisterForVshSessionTerminated(Handler handler);

    protected VshHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
