package com.samsung.android.cmcp2phelper.transport;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.samsung.android.cmcp2phelper.data.CphMessage;
import com.samsung.android.cmcp2phelper.transport.internal.CphSenderReceiver;
import com.samsung.android.cmcp2phelper.transport.internal.CphUnicastReceiver;
import com.samsung.android.cmcp2phelper.transport.internal.CphUnicastSender;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CphManager {
    public static final String LOG_TAG = ("cmcp2phelper/" + CphManager.class.getSimpleName());
    static final int MAX_THREAD = 16;
    private static final String MULTICAST_IP = "239.255.255.250";
    private static final int MULTICAST_PORT = 9900;
    private static final int UNICAST_PORT = 51024;
    private static final int UNICAST_PORT2 = 52024;
    private static final ExecutorService executor = Executors.newFixedThreadPool(16);
    Context mContext;
    Handler mLogHandler;
    MdmnServiceInfo mServiceInfo;
    CphUnicastReceiver mUnicastReceiver;
    CphUnicastReceiver mUnicastReceiver2;

    public CphManager(Context context, MdmnServiceInfo serviceInfo) {
        this.mContext = context;
        this.mServiceInfo = serviceInfo;
    }

    public static void execute(Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (Exception e) {
        }
    }

    public void start() {
        Log.e("cmcp2phelper", "start NSD");
        if (this.mUnicastReceiver == null) {
            CphUnicastReceiver cphUnicastReceiver = new CphUnicastReceiver(this.mLogHandler, UNICAST_PORT, this.mServiceInfo);
            this.mUnicastReceiver = cphUnicastReceiver;
            cphUnicastReceiver.enableApplicationLog(this.mLogHandler);
            execute(this.mUnicastReceiver);
        }
        if (this.mUnicastReceiver2 == null) {
            CphUnicastReceiver cphUnicastReceiver2 = new CphUnicastReceiver(this.mLogHandler, UNICAST_PORT2, this.mServiceInfo);
            this.mUnicastReceiver2 = cphUnicastReceiver2;
            cphUnicastReceiver2.enableApplicationLog(this.mLogHandler);
            execute(this.mUnicastReceiver2);
        }
    }

    public void stop() {
        Log.e("cmcp2phelper", "stop NSD");
        CphUnicastReceiver cphUnicastReceiver = this.mUnicastReceiver;
        if (cphUnicastReceiver != null) {
            cphUnicastReceiver.stop();
            this.mUnicastReceiver = null;
        }
        CphUnicastReceiver cphUnicastReceiver2 = this.mUnicastReceiver2;
        if (cphUnicastReceiver2 != null) {
            cphUnicastReceiver2.stop();
            this.mUnicastReceiver2 = null;
        }
    }

    public void startDiscoveryUnicast(Handler callbackHandler, int what, String deviceId, String impu, ArrayList<String> ipList) {
        CphMessage sendMsg = new CphMessage(1, 2.0d, deviceId, impu, getLocalIpAddress(), UNICAST_PORT);
        Iterator<String> it = ipList.iterator();
        while (it.hasNext()) {
            CphSenderReceiver sender = new CphUnicastSender(it.next(), UNICAST_PORT, sendMsg.getByte(), sendMsg.getByte().length, callbackHandler, what);
            sender.enableApplicationLog(this.mLogHandler);
            execute(sender);
        }
        CphMessage cphMessage = new CphMessage(1, 2.0d, deviceId, impu, getLocalIpAddress(), UNICAST_PORT2);
        Iterator<String> it2 = ipList.iterator();
        while (it2.hasNext()) {
            CphSenderReceiver sender2 = new CphUnicastSender(it2.next(), UNICAST_PORT2, cphMessage.getByte(), cphMessage.getByte().length, callbackHandler, what);
            sender2.enableApplicationLog(this.mLogHandler);
            execute(sender2);
        }
    }

    public void setLogHandler(Handler logHandler) {
        this.mLogHandler = logHandler;
    }

    public String getLocalIpAddress() {
        int ipAddress = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        try {
            return InetAddress.getByAddress(BigInteger.valueOf((long) ipAddress).toByteArray()).getHostAddress();
        } catch (UnknownHostException e) {
            Log.e("cmcp2phelper", "Unable to get host address.");
            return null;
        }
    }
}
