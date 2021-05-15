package com.samsung.android.cmcp2phelper;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.data.CphDeviceManager;
import com.samsung.android.cmcp2phelper.transport.CphManager;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class MdmnNsdWrapper {
    public static final String LOG_TAG = ("cmcp2phelper/" + MdmnNsdWrapper.class.getSimpleName());
    Context mContext;
    Handler mLogHandler;
    MdmnServiceInfo mServiceInfo;
    CphManager mTransportManager;
    WifiManager.WifiLock mWifiLock;

    public MdmnNsdWrapper(Context context, MdmnServiceInfo service) {
        Log.d(LOG_TAG, "MdmnNsdWrapper Version 1.1.10");
        this.mServiceInfo = service;
        this.mContext = context;
        this.mTransportManager = new CphManager(context, this.mServiceInfo);
    }

    public Collection<MdmnServiceInfo> getSupportDevices() {
        return CphDeviceManager.getDeviceList(this.mServiceInfo.getLineId());
    }

    public void printCache() {
    }

    public void setServiceInfo(MdmnServiceInfo serviceInfo) {
        this.mServiceInfo = serviceInfo;
        this.mTransportManager.stop();
        CphManager cphManager = new CphManager(this.mContext, this.mServiceInfo);
        this.mTransportManager = cphManager;
        cphManager.start();
    }

    public void start() {
        if (!((WifiManager) this.mContext.getSystemService("wifi")).isWifiEnabled()) {
            Log.d(LOG_TAG, "wifi is not enabled");
            this.mTransportManager.stop();
            return;
        }
        WifiManager wifi = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifi != null) {
            if (this.mWifiLock == null) {
                this.mWifiLock = wifi.createWifiLock("cmcp2phelper");
            }
            this.mWifiLock.acquire();
        }
        this.mTransportManager.start();
    }

    public void stop() {
        this.mTransportManager.stop();
        WifiManager.WifiLock wifiLock = this.mWifiLock;
        if (wifiLock != null) {
            wifiLock.release();
            this.mWifiLock = null;
        }
    }

    public int startDiscovery(Handler callbackHanlder, int what, ArrayList<String> ipList) {
        CphDeviceManager.clearCache();
        if (this.mServiceInfo == null) {
            Log.d(LOG_TAG, "service info is not avaliable");
            return 0;
        } else if (!((WifiManager) this.mContext.getSystemService("wifi")).isWifiEnabled()) {
            Log.d(LOG_TAG, "wifi is not enabled");
            this.mTransportManager.stop();
            return 0;
        } else {
            CphManager cphManager = this.mTransportManager;
            if (cphManager == null) {
                Log.d(LOG_TAG, "NSDWrapper is not started");
                return 0;
            }
            cphManager.start();
            CphDeviceManager.setCallback(callbackHanlder, what);
            if (ipList != null) {
                CphDeviceManager.setMaxPeer(ipList.size());
                String str = LOG_TAG;
                Log.d(str, "Try discovery : " + ipList);
                this.mTransportManager.startDiscoveryUnicast(callbackHanlder, what, this.mServiceInfo.getDeviceId(), this.mServiceInfo.getLineId(), ipList);
                return 1;
            }
            Log.d(LOG_TAG, "No ip list");
            return 0;
        }
    }

    public int startDiscovery(ArrayList<String> ipList) {
        CphDeviceManager.clearCache();
        if (this.mServiceInfo == null) {
            Log.d(LOG_TAG, "service info is not avaliable");
            return 0;
        }
        CphDeviceManager.setCallback((Handler) null, 0);
        if (ipList != null) {
            CphDeviceManager.setMaxPeer(ipList.size());
            String str = LOG_TAG;
            Log.d(str, "Try discovery : " + ipList);
            if (!((WifiManager) this.mContext.getSystemService("wifi")).isWifiEnabled()) {
                Log.d(LOG_TAG, "wifi is not enabled");
                return 0;
            }
            CphManager cphManager = this.mTransportManager;
            if (cphManager == null) {
                Log.d(LOG_TAG, "NSDWrapper is not started");
                return 0;
            }
            cphManager.startDiscoveryUnicast((Handler) null, 0, this.mServiceInfo.getDeviceId(), this.mServiceInfo.getLineId(), ipList);
            return 1;
        }
        Log.d(LOG_TAG, "No ip list");
        return 0;
    }

    @Deprecated
    public void setLogHandler(Handler logHandler) {
        this.mLogHandler = logHandler;
        this.mTransportManager.setLogHandler(logHandler);
    }

    @Deprecated
    private static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
                while (true) {
                    if (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return null;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
