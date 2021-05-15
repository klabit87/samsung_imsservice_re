package com.sec.internal.ims.cmstore.utils;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;

public class SchedulerHelper {
    public static final String TAG = SchedulerHelper.class.getSimpleName();
    private static Handler mHandler = null;
    private static ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    private static SchedulerHelper sInstance = null;

    private SchedulerHelper(Handler handler) {
        mHandler = handler;
    }

    public static SchedulerHelper getInstance(Handler handler) {
        if (sInstance == null) {
            sInstance = new SchedulerHelper(handler);
        }
        return sInstance;
    }

    public void deleteNotificationSubscriptionResource() {
        Log.i(TAG, "deleteNotificationSubscriptionResource");
        String subscriptionChannelResUrl = mReSyncParam.getChannelResURL();
        if (!TextUtils.isEmpty(subscriptionChannelResUrl)) {
            mHandler.sendMessage(mHandler.obtainMessage(OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL.getId(), subscriptionChannelResUrl));
            CloudMessagePreferenceManager.getInstance().saveOMASubscriptionResUrl("");
        }
        String omaChannelUrl = CloudMessagePreferenceManager.getInstance().getOMAChannelResURL();
        if (!TextUtils.isEmpty(omaChannelUrl)) {
            mHandler.sendMessage(mHandler.obtainMessage(OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.getId(), omaChannelUrl));
            CloudMessagePreferenceManager.getInstance().saveOMAChannelResURL("");
            CloudMessagePreferenceManager.getInstance().saveOMACallBackURL("");
            CloudMessagePreferenceManager.getInstance().saveOMAChannelURL("");
        }
    }

    public boolean isSubscriptionChannelGoingExpired() {
        long subscriptionTime = CloudMessagePreferenceManager.getInstance().getOMASubscriptionTime();
        long channelDuration = ((long) CloudMessagePreferenceManager.getInstance().getOMASubscriptionChannelDuration()) * 1000;
        long currentTime = System.currentTimeMillis();
        long life = (ScheduleConstant.POLLING_TIME_OUT + currentTime) - (subscriptionTime + channelDuration);
        String str = TAG;
        Log.i(str, "subscriptionTime : " + subscriptionTime + ", channelDuration : " + channelDuration + ", currentTime : " + currentTime + ", life : " + life);
        return life >= 0;
    }
}
