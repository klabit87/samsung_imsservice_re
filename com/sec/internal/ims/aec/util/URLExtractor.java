package com.sec.internal.ims.aec.util;

import android.net.Network;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.AECLog;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class URLExtractor {
    private static final String LOG_TAG = URLExtractor.class.getSimpleName();

    public static synchronized String getHttpUrl(int phoneId) {
        synchronized (URLExtractor.class) {
            String httpUrl = ExternalStorage.getLabHttpUrl();
            if (!TextUtils.isEmpty(httpUrl)) {
                return httpUrl;
            }
            String defaultHttpUrl = getDefaultHttpUrl(phoneId);
            return defaultHttpUrl;
        }
    }

    private static String getDefaultHttpUrl(int phoneId) {
        String mnc;
        String mcc;
        ISimManager simMgr = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (simMgr == null) {
            AECLog.e(LOG_TAG, "getDefaultHttpUrl: sim manager not ready", phoneId);
            return null;
        }
        String operator = simMgr.getSimOperator();
        if (operator.length() == 5) {
            mcc = operator.substring(0, 3);
            mnc = "0" + operator.substring(3, 5);
        } else if (operator.length() == 6) {
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3, 6);
        } else {
            AECLog.e(LOG_TAG, "getDefaultHttpUrl: invalid operator", phoneId);
            return null;
        }
        return String.format(AECNamespace.Template.AES_URL, new Object[]{mnc, mcc});
    }

    public static synchronized String getHostName(String httpUrl) {
        String hostName;
        synchronized (URLExtractor.class) {
            hostName = httpUrl.replaceFirst("https?://", "");
            if (hostName.indexOf(47) > 0) {
                hostName = hostName.substring(0, hostName.indexOf(47));
            }
        }
        return hostName;
    }

    public static synchronized Queue getIpAddress(int phoneId, String httpUrl, Network network) {
        Queue ipAddressQ;
        synchronized (URLExtractor.class) {
            ipAddressQ = new LinkedList();
            InetAddress[] ipAddressArray = getAllByName(phoneId, httpUrl, network);
            if (ipAddressArray != null) {
                if (ipAddressArray.length != 1) {
                    for (InetAddress inetAddress : ipAddressArray) {
                        ipAddressQ.offer(getHostAddress(inetAddress));
                    }
                    AECLog.i(LOG_TAG, "getIpAddress: " + ipAddressQ.toString(), phoneId);
                }
            }
            ipAddressQ.offer("https://" + getHostName(httpUrl));
            AECLog.i(LOG_TAG, "getIpAddress: " + ipAddressQ.toString(), phoneId);
        }
        return ipAddressQ;
    }

    private static InetAddress[] getAllByName(int phoneId, String httpUrl, Network network) {
        if (network != null) {
            return network.getAllByName(getDomainName(httpUrl));
        }
        try {
            return InetAddress.getAllByName(getDomainName(httpUrl));
        } catch (UnknownHostException e) {
            String str = LOG_TAG;
            AECLog.e(str, "UnknownHostException: " + e.getMessage(), phoneId);
            return null;
        }
    }

    private static String getDomainName(String httpUrl) {
        String domainName = httpUrl.replaceFirst("https?://", "");
        if (domainName.indexOf(58) > 0) {
            return domainName.substring(0, domainName.indexOf(58));
        }
        return domainName;
    }

    private static String getHostAddress(InetAddress ipAddress) {
        if (ipAddress instanceof Inet6Address) {
            return "https://[" + ipAddress.getHostAddress() + "]";
        }
        return "https://" + ipAddress.getHostAddress();
    }
}
