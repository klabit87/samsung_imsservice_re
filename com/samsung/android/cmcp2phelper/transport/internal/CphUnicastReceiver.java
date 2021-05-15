package com.samsung.android.cmcp2phelper.transport.internal;

import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.BuildConstants;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class CphUnicastReceiver extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphUnicastReceiver.class.getSimpleName());
    private static final int MAX_PACKET_LENGTH = 1400;
    int mRecvPort;
    DatagramSocket mSocket;

    public CphUnicastReceiver(Handler callbackHandler, MdmnServiceInfo serviceInfo) {
        this.mCallbackHandler = callbackHandler;
        this.mRecvPort = -1;
        this.mServiceInfo = serviceInfo;
    }

    public CphUnicastReceiver(Handler callbackHandler, int recvPort, MdmnServiceInfo serviceInfo) {
        this.mCallbackHandler = callbackHandler;
        this.mRecvPort = recvPort;
        this.mServiceInfo = serviceInfo;
    }

    public void run() {
        try {
            if (this.mRecvPort < 0) {
                this.mSocket = new DatagramSocket();
            } else {
                this.mSocket = new DatagramSocket(this.mRecvPort);
            }
            print("Start Unicast Reponder : port - " + this.mSocket.getLocalPort());
            while (this.mSocket != null && !this.mSocket.isClosed()) {
                byte[] buf = new byte[1400];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    this.mSocket.receive(recv);
                    if (!BuildConstants.isUserBinary()) {
                        print("[U<--](" + recv.getAddress().getHostAddress() + ")" + new String(recv.getData(), StandardCharsets.UTF_8));
                    }
                    handleReceivedMessage(recv);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "socket is closed");
                }
            }
            print("Stop Unicast Reponder");
        } catch (SocketException e2) {
            e2.printStackTrace();
            Log.d(LOG_TAG, "SocketException");
            print("SocketException- Unicast Receiver");
        }
    }

    public int getRecvPort() {
        DatagramSocket datagramSocket = this.mSocket;
        if (datagramSocket != null) {
            return datagramSocket.getLocalPort();
        }
        return 0;
    }

    public void stop() {
        if (this.mSocket != null) {
            Log.d(LOG_TAG, "stop responder");
            this.mSocket.close();
            this.mSocket = null;
        }
    }
}
