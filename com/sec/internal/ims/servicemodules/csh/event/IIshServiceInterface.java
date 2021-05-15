package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Handler;

public interface IIshServiceInterface {
    void acceptIshSession(IshAcceptSessionParams ishAcceptSessionParams);

    void cancelIshSession(CshCancelSessionParams cshCancelSessionParams);

    void registerForIshIncomingSession(Handler handler, int i, Object obj);

    void registerForIshSessionEstablished(Handler handler, int i, Object obj);

    void registerForIshTransferComplete(Handler handler, int i, Object obj);

    void registerForIshTransferFailed(Handler handler, int i, Object obj);

    void registerForIshTransferProgress(Handler handler, int i, Object obj);

    void rejectIshSession(CshRejectSessionParams cshRejectSessionParams);

    void startIshSession(IshStartSessionParams ishStartSessionParams);

    void stopIshSession(CshCancelSessionParams cshCancelSessionParams);

    void unregisterForIshIncomingSession(Handler handler);

    void unregisterForIshSessionEstablished(Handler handler);

    void unregisterForIshTransferComplete(Handler handler);

    void unregisterForIshTransferFailed(Handler handler);

    void unregisterForIshTransferProgress(Handler handler);
}
