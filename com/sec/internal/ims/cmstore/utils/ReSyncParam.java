package com.sec.internal.ims.cmstore.utils;

import android.util.Log;
import com.sec.ims.util.IMSLog;

public class ReSyncParam {
    public static final String TAG = ReSyncParam.class.getSimpleName();
    private static String mChannelResUrl;
    private static String mChannelURL;
    private static String mNotifyURL;
    private static String mRestartToken;
    private static ReSyncParam sInstance = new ReSyncParam();

    private ReSyncParam() {
    }

    public static ReSyncParam getInstance() {
        return sInstance;
    }

    public static void update() {
        CloudMessagePreferenceManager preference = CloudMessagePreferenceManager.getInstance();
        mRestartToken = preference.getOMASSubscriptionRestartToken();
        mNotifyURL = preference.getOMACallBackURL();
        mChannelURL = preference.getOMAChannelURL();
        mChannelResUrl = preference.getOMASubscriptionResUrl();
        String str = TAG;
        Log.i(str, "ReSyncParam: mRestartToken:: " + IMSLog.checker(mRestartToken) + ",ReSyncParam: mNotifyURL:: " + IMSLog.checker(mNotifyURL) + ",ReSyncParam: mChannelURL:: " + IMSLog.checker(mChannelURL) + ",ReSyncParam: mChannelResUrl:: " + IMSLog.checker(mChannelResUrl));
    }

    public String getRestartToken() {
        return mRestartToken;
    }

    public String getNotifyURL() {
        return mNotifyURL;
    }

    public String getChannelURL() {
        return mChannelURL;
    }

    public String getChannelResURL() {
        return mChannelResUrl;
    }
}
