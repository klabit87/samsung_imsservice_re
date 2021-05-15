package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Handler;

public interface IvshServiceInterface {
    void acceptVshSession(CshAcceptSessionParams cshAcceptSessionParams);

    void cancelVshSession(CshCancelSessionParams cshCancelSessionParams);

    void registerForVshIncomingSession(Handler handler, int i, Object obj);

    void registerForVshSessionEstablished(Handler handler, int i, Object obj);

    void registerForVshSessionTerminated(Handler handler, int i, Object obj);

    void rejectVshSession(CshRejectSessionParams cshRejectSessionParams);

    void resetVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams);

    void setVshPhoneOrientation(VshOrientation vshOrientation);

    void setVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams);

    void startVshSession(VshStartSessionParams vshStartSessionParams);

    void stopVshSession(CshCancelSessionParams cshCancelSessionParams);

    void switchCamera(VshSwitchCameraParams vshSwitchCameraParams);

    void unregisterForVshIncomingSession(Handler handler);

    void unregisterForVshSessionEstablished(Handler handler);

    void unregisterForVshSessionTerminated(Handler handler);
}
