package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AddParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ImSendComposingParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;

public abstract class ImHandler extends BaseHandler implements IImServiceInterface {
    public abstract void acceptFtSession(AcceptFtSessionParams acceptFtSessionParams);

    public abstract void acceptImSession(AcceptImSessionParams acceptImSessionParams);

    public abstract void addImParticipants(AddParticipantsParams addParticipantsParams);

    public abstract void cancelFtSession(RejectFtSessionParams rejectFtSessionParams);

    public abstract void extendToGroupChat(StartImSessionParams startImSessionParams);

    public abstract void registerForComposingNotification(Handler handler, int i, Object obj);

    public abstract void registerForConferenceInfoUpdate(Handler handler, int i, Object obj);

    public abstract void registerForImIncomingFileTransfer(Handler handler, int i, Object obj);

    public abstract void registerForImIncomingMessage(Handler handler, int i, Object obj);

    public abstract void registerForImIncomingSession(Handler handler, int i, Object obj);

    public abstract void registerForImSessionClosed(Handler handler, int i, Object obj);

    public abstract void registerForImSessionEstablished(Handler handler, int i, Object obj);

    public abstract void registerForImdnFailed(Handler handler, int i, Object obj);

    public abstract void registerForImdnNotification(Handler handler, int i, Object obj);

    public abstract void registerForImdnResponse(Handler handler, int i, Object obj);

    public abstract void registerForMessageFailed(Handler handler, int i, Object obj);

    public abstract void registerForTransferProgress(Handler handler, int i, Object obj);

    public abstract void rejectFtSession(RejectFtSessionParams rejectFtSessionParams);

    public abstract void sendComposingNotification(ImSendComposingParams imSendComposingParams);

    public abstract void sendDeliveredNotification(SendImdnParams sendImdnParams);

    public abstract void sendDisplayedNotification(SendImdnParams sendImdnParams);

    public abstract void sendFtDeliveredNotification(SendImdnParams sendImdnParams);

    public abstract void sendFtDisplayedNotification(SendImdnParams sendImdnParams);

    public abstract void sendFtSession(SendFtSessionParams sendFtSessionParams);

    public abstract void sendImMessage(SendMessageParams sendMessageParams);

    public abstract void startImSession(StartImSessionParams startImSessionParams);

    public abstract void stopImSession(StopImSessionParams stopImSessionParams);

    public abstract void unregisterForImIncomingMessage(Handler handler);

    public abstract void unregisterForImIncomingSession(Handler handler);

    public abstract void unregisterForImSessionClosed(Handler handler);

    public abstract void unregisterForImSessionEstablished(Handler handler);

    public abstract void unregisterForImdnResponse(Handler handler);

    public abstract void unregisterForMessageFailed(Handler handler);

    protected ImHandler(Looper looper) {
        super(looper);
    }

    public void setFtMessageId(Object rawHandle, int msgId) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }
}
