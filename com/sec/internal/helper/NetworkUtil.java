package com.sec.internal.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Patterns;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.log.IMSLog;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NetworkUtil {
    private static final String LOG_TAG = "NetworkUtil";

    public static boolean isIPv4Address(String ip) {
        try {
            if (InetAddress.getByName(ip) instanceof Inet4Address) {
                return true;
            }
            return false;
        } catch (UnknownHostException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("getIPversion : invalid ip : ");
            sb.append(IMSLog.isShipBuild() ? "xxx" : ip);
            Log.e(LOG_TAG, sb.toString());
            return false;
        }
    }

    public static boolean isIPv6Address(String ip) {
        try {
            if (InetAddress.getByName(ip) instanceof Inet6Address) {
                return true;
            }
            return false;
        } catch (UnknownHostException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("getIPversion : invalid ip : ");
            sb.append(IMSLog.isShipBuild() ? "xxx" : ip);
            Log.e(LOG_TAG, sb.toString());
            return false;
        }
    }

    public static boolean isMobileDataOn(Context context) {
        return ImsConstants.SystemSettings.MOBILE_DATA.get(context, 1) == 1;
    }

    public static boolean isMobileDataPressed(Context context) {
        return ImsConstants.SystemSettings.MOBILE_DATA_PRESSED.get(context, 1) == 1;
    }

    public static boolean isLegacy3gppNetwork(int network) {
        if (!(network == 1 || network == 2 || network == 3 || network == 15 || network == 16)) {
            switch (network) {
                case 8:
                case 9:
                case 10:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean isValidPcscfAddress(String pcscf) {
        if (pcscf == null || pcscf.isEmpty()) {
            return false;
        }
        if (Patterns.DOMAIN_NAME.matcher(pcscf).matches() || isIPv4Address(pcscf) || isIPv6Address(pcscf)) {
            return true;
        }
        return false;
    }

    public static boolean isConnected(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
