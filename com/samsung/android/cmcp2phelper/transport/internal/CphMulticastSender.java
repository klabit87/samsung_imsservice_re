package com.samsung.android.cmcp2phelper.transport.internal;

import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.data.CphDeviceManager;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CphMulticastSender extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphMulticastSender.class.getSimpleName());

    public CphMulticastSender(String ip, int port, byte[] message, int length, Handler callbackHandler, int what) {
        this.mIp = ip;
        this.mPort = port;
        this.mMessage = message;
        this.mLength = length;
        this.mCallbackHandler = callbackHandler;
        this.mCallbackwhat = what;
    }

    public void run() {
        Log.d(LOG_TAG, "send multicast");
        InetAddress group = null;
        MulticastSocket socket = null;
        try {
            group = InetAddress.getByName(this.mIp);
            socket = new MulticastSocket(this.mPort);
            socket.setLoopbackMode(true);
            socket.joinGroup(group);
        } catch (UnknownHostException e) {
            Log.d(LOG_TAG, "UnknownHostException");
            e.printStackTrace();
            CphDeviceManager.notify(false);
        } catch (SocketException e2) {
            Log.d(LOG_TAG, "SocketException");
            e2.printStackTrace();
            CphDeviceManager.notify(false);
        } catch (IOException e3) {
            Log.d(LOG_TAG, "IOException");
            e3.printStackTrace();
            CphDeviceManager.notify(false);
        }
        DatagramPacket packet = new DatagramPacket(this.mMessage, this.mLength, group, this.mPort);
        try {
            String str = LOG_TAG;
            Log.d(str, "[M-->]" + new String(this.mMessage, StandardCharsets.UTF_8));
            print("[M-->]" + new String(this.mMessage, StandardCharsets.UTF_8));
            int i = 0;
            while (i < 9) {
                socket.send(packet);
                try {
                    Thread.sleep(100);
                    i++;
                } catch (InterruptedException e4) {
                    e4.printStackTrace();
                    CphDeviceManager.notify(false);
                    if (socket != null) {
                        socket.close();
                        return;
                    }
                    return;
                }
            }
        } catch (IOException e5) {
            e5.printStackTrace();
        }
    }
}
