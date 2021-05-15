package com.sec.internal.ims.cmstore.utils;

import android.net.Network;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckCaptivePortal {
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int WALLED_GARDEN_RETRY_COUNT = 3;
    private static final int WALLED_GARDEN_RETRY_INTERVAL = 3000;
    private static final String WALLED_GARDEN_URL = "http://clients3.google.com/generate_204";

    public static boolean isGoodWifi(Network wifi) {
        for (int i = 0; i <= 3; i++) {
            if (!checkWifiWorksFineWithWalledGardenUrl(wifi)) {
                return false;
            }
            sleepHelper(3000);
        }
        return true;
    }

    private static boolean checkWifiWorksFineWithWalledGardenUrl(Network wifi) {
        HttpURLConnection urlConnection = null;
        boolean z = false;
        try {
            HttpURLConnection urlConnection2 = (HttpURLConnection) wifi.openConnection(new URL(WALLED_GARDEN_URL));
            urlConnection2.setInstanceFollowRedirects(false);
            urlConnection2.setConnectTimeout(10000);
            urlConnection2.setReadTimeout(10000);
            urlConnection2.setUseCaches(false);
            urlConnection2.getInputStream();
            if (urlConnection2.getResponseCode() == 204) {
                z = true;
            }
            if (urlConnection2 != null) {
                urlConnection2.disconnect();
            }
            return z;
        } catch (IOException e) {
            e.printStackTrace();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return false;
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw th;
        }
    }

    private static void sleepHelper(int sleepTime) {
        try {
            Thread.sleep((long) sleepTime);
        } catch (InterruptedException e) {
            Log.e("Utils", "sleepHelper", e);
        }
    }
}
