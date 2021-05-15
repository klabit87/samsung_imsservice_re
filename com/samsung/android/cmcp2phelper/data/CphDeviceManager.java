package com.samsung.android.cmcp2phelper.data;

import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class CphDeviceManager {
    public static final int DISCOVERY_FAIL = 0;
    public static final int DISCOVERY_SUCCESS = 1;
    public static final String LOG_TAG = ("cmcp2phelper/" + CphDeviceManager.class.getSimpleName());
    private static int MAX_DISCOVERY_TIME = 1500;
    private static ConcurrentHashMap<String, CphMessage> cacheMap = new ConcurrentHashMap<>();
    private static Handler sHandler;
    private static int sMaxPeer;
    private static int sWhat;

    public static synchronized void clearCache() {
        synchronized (CphDeviceManager.class) {
            cacheMap.clear();
            sHandler = null;
            sMaxPeer = 4;
        }
    }

    public static void addToCache(CphMessage message) {
        if (cacheMap.put(message.getDeviceId(), message) != null) {
            String str = LOG_TAG;
            Log.d(str, "add to cache : " + message.toString() + ", size : " + cacheMap.size());
            if (cacheMap.size() == sMaxPeer) {
                Log.d(LOG_TAG, "find all node");
                notify(true);
            }
        }
    }

    public static void addToMyInfo(String deviceId, String lineId) {
        cacheMap.put(deviceId, new CphMessage(deviceId, lineId));
    }

    public static Collection<MdmnServiceInfo> getDeviceList(String goupId) {
        Collection<MdmnServiceInfo> collection = new ArrayList<>();
        Log.d(LOG_TAG, "---Reachable contact list----");
        for (CphMessage info : cacheMap.values()) {
            if (info.getLineId().equalsIgnoreCase(goupId)) {
                MdmnServiceInfo msi = new MdmnServiceInfo(info.getDeviceId(), info.getLineId());
                String str = LOG_TAG;
                Log.d(str, "rechable contact : " + msi.toString());
                collection.add(msi);
            }
        }
        Log.d(LOG_TAG, "---end----");
        return collection;
    }

    public static synchronized void setCallback(Handler handler, int what) {
        synchronized (CphDeviceManager.class) {
            sHandler = handler;
            sWhat = what;
            if (handler != null) {
                handler.sendMessageDelayed(handler.obtainMessage(what, 1, 0), (long) MAX_DISCOVERY_TIME);
            }
        }
    }

    public static synchronized void setMaxPeer(int maxPeer) {
        synchronized (CphDeviceManager.class) {
            sMaxPeer = maxPeer;
        }
    }

    public static synchronized void notify(boolean result) {
        synchronized (CphDeviceManager.class) {
            if (sHandler != null) {
                sHandler.removeCallbacksAndMessages((Object) null);
                if (result) {
                    sHandler.sendMessage(sHandler.obtainMessage(sWhat, 1, 0));
                } else {
                    sHandler.sendMessage(sHandler.obtainMessage(sWhat, 0, 0));
                }
                sHandler = null;
            }
        }
    }
}
