package com.samsung.android.cmcp2phelper.transport.internal;

import android.os.Handler;
import com.samsung.android.cmcp2phelper.BuildConstants;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CphUnicastSender extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphUnicastSender.class.getSimpleName());

    public CphUnicastSender(String ip, int port, byte[] message, int length) {
        this.mIp = ip;
        this.mPort = port;
        this.mMessage = message;
        this.mLength = length;
    }

    public CphUnicastSender(String ip, int port, byte[] message, int length, Handler callbackHandler, int what) {
        this.mIp = ip;
        this.mPort = port;
        this.mMessage = message;
        this.mLength = length;
        this.mCallbackHandler = callbackHandler;
        this.mCallbackwhat = what;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(this.mMessage, this.mLength, InetAddress.getByName(this.mIp), this.mPort);
            if (!BuildConstants.isUserBinary()) {
                print("[U-->](" + this.mIp + ":" + this.mPort + ")" + new String(this.mMessage, StandardCharsets.UTF_8));
            }
            socket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }
}
