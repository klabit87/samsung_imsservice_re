package com.samsung.android.cmcp2phelper.transport.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.samsung.android.cmcp2phelper.data.CphDeviceManager;
import com.samsung.android.cmcp2phelper.data.CphMessage;
import com.samsung.android.cmcp2phelper.transport.CphManager;
import java.net.DatagramPacket;

public class CphSenderReceiver implements Runnable {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphSenderReceiver.class.getSimpleName());
    public static final long TIME_OUT = 550;
    protected Handler mCallbackHandler;
    protected int mCallbackwhat;
    protected Context mContext;
    protected String mIp;
    protected int mLength;
    protected Handler mLogHandler;
    protected byte[] mMessage;
    protected int mPort;
    protected MdmnServiceInfo mServiceInfo;

    public void run() {
    }

    public void print(String log) {
        Log.d("cmcp2phelper", log);
        Handler handler = this.mLogHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.obj = System.currentTimeMillis() + " : " + log;
            this.mLogHandler.sendMessage(msg);
        }
    }

    public void enableApplicationLog(Handler handler) {
        this.mLogHandler = handler;
    }

    /* access modifiers changed from: protected */
    public void handleReceivedMessage(DatagramPacket recv) {
        CphMessage recvMsg = new CphMessage(recv);
        if (!recvMsg.isValid()) {
            Log.d(LOG_TAG, "invalid message");
        } else if (!this.mServiceInfo.getLineId().equalsIgnoreCase(recvMsg.getLineId())) {
            Log.d(LOG_TAG, "Line id not matched");
        } else if (recvMsg.getMsgType() == 1) {
            CphMessage cphMessage = new CphMessage(2, 2.0d, this.mServiceInfo.getDeviceId(), this.mServiceInfo.getLineId());
            CphUnicastSender sender = new CphUnicastSender(recv.getAddress().getHostAddress(), recvMsg.getResponderPort(), cphMessage.getByte(), cphMessage.getByte().length);
            Handler handler = this.mLogHandler;
            if (handler != null) {
                sender.enableApplicationLog(handler);
            }
            CphManager.execute(sender);
            CphDeviceManager.addToCache(recvMsg);
        } else if (recvMsg.getMsgType() == 2) {
            CphDeviceManager.addToCache(recvMsg);
        }
    }
}
