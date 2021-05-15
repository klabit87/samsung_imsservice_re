package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.log.AECLog;

public class DataConnectivity {
    private static String LOG_TAG = DataConnectivity.class.getSimpleName();

    public static boolean isDataConnected(Context context, boolean bPsDataOffExempt) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return false;
        }
        Network activeNetwork = cm.getActiveNetwork();
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        boolean isOnline = capabilities != null && capabilities.hasCapability(12) && capabilities.hasCapability(16);
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        String str = LOG_TAG;
        AECLog.d(str, "isDataConnected: online [" + isOnline + "], connected [" + isConnected + "], psDataOffExempt [" + bPsDataOffExempt + "]");
        if ((!isConnected || !isOnline) && !bPsDataOffExempt) {
            return false;
        }
        return true;
    }

    public static boolean isWifiConnected(Context context) {
        NetworkInfo ni;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null) {
            Network[] allNetworks = cm.getAllNetworks();
            int length = allNetworks.length;
            int i = 0;
            while (i < length) {
                Network network = allNetworks[i];
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (nc == null || !nc.hasTransport(1) || !nc.hasCapability(12) || !nc.hasCapability(16) || (ni = cm.getNetworkInfo(network)) == null) {
                    i++;
                } else {
                    AECLog.d(LOG_TAG, "isWifiConnected: " + ni);
                    return ni.isConnected();
                }
            }
        }
        return false;
    }

    public static boolean isMobileDataOn(Context context) {
        boolean z = true;
        if (Settings.Global.getInt(context.getContentResolver(), Extensions.Settings.Global.MOBILE_DATA, 1) != 1) {
            z = false;
        }
        boolean mobileDataOn = z;
        String str = LOG_TAG;
        AECLog.d(str, "checkMobileDataOn: " + mobileDataOn);
        return mobileDataOn;
    }
}
