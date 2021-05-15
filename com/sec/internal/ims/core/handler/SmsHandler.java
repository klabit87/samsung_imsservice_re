package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.ims.servicemodules.sms.ISmsServiceInterface;

public class SmsHandler extends BaseHandler implements ISmsServiceInterface {
    protected SmsHandler(Looper looper) {
        super(looper);
    }

    public void sendSMSResponse(int phoneId, String responseStr, int statusCode) {
    }

    public void registerForSMSEvent(Handler h, int what, Object obj) {
    }

    public void registerForRrcConnectionEvent(Handler h, int what, Object obj) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }

    public void sendMessage(String smsc, String localUri, String contentType, byte[] data, boolean isDeliverReport, String callId, int msgId, int regId) {
    }

    public void setMsgAppInfoToSipUa(int phoneId, String info) {
    }
}
