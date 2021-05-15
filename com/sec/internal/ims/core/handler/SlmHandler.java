package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmLMMSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmLMMSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;

public abstract class SlmHandler extends BaseHandler implements ISlmServiceInterface {
    protected SlmHandler(Looper looper) {
        super(looper);
    }

    public void sendSlmMessage(SendSlmMessageParams params) {
    }

    public void sendFtSlmMessage(SendSlmFileTransferParams params) {
    }

    public void acceptFtSlmMessage(AcceptFtSessionParams params) {
    }

    public void rejectFtSlmMessage(RejectFtSessionParams params) {
    }

    public void cancelFtSlmMessage(RejectFtSessionParams params) {
    }

    public void registerForSlmIncomingMessage(Handler h, int what, Object obj) {
    }

    public void unregisterForSlmIncomingMessage(Handler h) {
    }

    public void registerForSlmIncomingFileTransfer(Handler h, int what, Object obj) {
    }

    public void unregisterForSlmIncomingFileTransfer(Handler h) {
    }

    public void registerForSlmTransferProgress(Handler h, int what, Object obj) {
    }

    public void unregisterForSlmTransferProgress(Handler h) {
    }

    public void registerForSlmImdnNotification(Handler h, int what, Object obj) {
    }

    public void unregisterForSlmImdnNotification(Handler h) {
    }

    public void unregisterAllSLMFileTransferProgress() {
    }

    public void acceptSlmLMMSession(AcceptSlmLMMSessionParams params) {
    }

    public void rejectSlmLMMSession(RejectSlmLMMSessionParams rejectParams) {
    }

    public void registerForSlmLMMIncomingSession(Handler h, int what, Object obj) {
    }

    public void unregisterForSlmLMMIncomingSession(Handler h) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
