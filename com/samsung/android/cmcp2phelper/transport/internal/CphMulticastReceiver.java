package com.samsung.android.cmcp2phelper.transport.internal;

import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CphMulticastReceiver extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphMulticastSender.class.getSimpleName());
    private static final int MAX_PACKET_LENGTH = 1400;
    private boolean isRun;
    InetAddress mGroup;
    private MulticastSocket mReceiveSocket;
    MulticastSocket mSocket;

    public CphMulticastReceiver(String ip, int port, MdmnServiceInfo serviceInfo) {
        this.mIp = ip;
        this.mPort = port;
        this.mServiceInfo = serviceInfo;
    }

    public void stop() {
        if (this.mSocket != null) {
            Log.d(LOG_TAG, "stop responder");
            this.mSocket.close();
            this.mSocket = null;
        }
    }

    public void run() {
        try {
            this.mGroup = InetAddress.getByName(this.mIp);
            MulticastSocket multicastSocket = new MulticastSocket(this.mPort);
            this.mSocket = multicastSocket;
            multicastSocket.setLoopbackMode(true);
            this.mSocket.joinGroup(this.mGroup);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "UnknownHostException");
        } catch (SocketException e2) {
            e2.printStackTrace();
            Log.d(LOG_TAG, "SocketException");
        } catch (IOException e3) {
            e3.printStackTrace();
            Log.d(LOG_TAG, "IOException");
        }
        print("Start Multicast Reponder : ip - " + this.mIp + ", port - " + this.mPort);
        while (true) {
            MulticastSocket multicastSocket2 = this.mSocket;
            if (multicastSocket2 == null || multicastSocket2.isClosed()) {
                print("Stop Multicast Reponder");
            } else {
                byte[] buf = new byte[1400];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    this.mSocket.receive(recv);
                    print("[M<--]" + new String(recv.getData(), StandardCharsets.UTF_8));
                    handleReceivedMessage(recv);
                } catch (IOException e4) {
                    e4.printStackTrace();
                    Log.d(LOG_TAG, "socket is closed");
                }
            }
        }
        print("Stop Multicast Reponder");
    }
}
