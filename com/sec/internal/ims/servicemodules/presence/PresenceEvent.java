package com.sec.internal.ims.servicemodules.presence;

import android.os.Message;
import android.util.Log;
import com.sec.ims.presence.PresenceInfo;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController;

public class PresenceEvent {
    static final int EVT_BAD_EVENT_TIMEOUT = 14;
    static final int EVT_NEW_PRESENCE_INFO = 10;
    static final int EVT_NEW_PRESENCE_INFO_DELAYED = 11;
    static final int EVT_NEW_WATCHER_INFO = 12;
    static final int EVT_PERIODIC_PUBLISH = 4;
    static final int EVT_PUBLISH_COMPLETE = 2;
    public static final int EVT_PUBLISH_REQUEST = 1;
    static final int EVT_SUBSCRIBE_COMPLETE = 6;
    static final int EVT_SUBSCRIBE_LIST_REQUESTED = 7;
    static final int EVT_SUBSCRIBE_REQUESTED = 5;
    static final int EVT_SUBSCRIBE_RETRY = 8;
    static final int EVT_SUBSCRIPTION_TERMINATED = 9;
    static final int EVT_UNPUBLISH_REQUEST = 3;
    static final int EVT_WAKE_LOCK_TIMEOUT = 13;
    private static final String LOG_TAG = "PresenceEvent";

    static boolean handleEvent(Message msg, PresenceModule presenceModule, int phoneId) {
        Log.i(LOG_TAG, "handleEvent: evt " + msg.what);
        switch (msg.what) {
            case 1:
                int phoneId2 = ((Integer) msg.obj).intValue();
                presenceModule.publish(presenceModule.getOwnPresenceInfo(phoneId2), phoneId2);
                return true;
            case 2:
                PresenceResponse presenceResponse = (PresenceResponse) ((AsyncResult) msg.obj).result;
                presenceModule.onPublishComplete(presenceResponse, presenceResponse.getPhoneId());
                return true;
            case 3:
                presenceModule.unpublish(((Integer) msg.obj).intValue());
                return true;
            case 4:
                presenceModule.onPeriodicPublish(((Integer) msg.obj).intValue());
                return true;
            case 5:
                presenceModule.onSubscribeRequested((PresenceSubscriptionController.SubscriptionRequest) msg.obj);
                return true;
            case 6:
                AsyncResult ar = (AsyncResult) msg.obj;
                presenceModule.onSubscribeComplete((PresenceSubscription) ar.userObj, (PresenceResponse) ar.result);
                return true;
            case 7:
                presenceModule.onSubscribeListRequested((CapabilityConstants.RequestType) msg.obj, msg.arg1);
                return true;
            case 8:
                presenceModule.onSubscribeRetry((PresenceSubscription) msg.obj);
                return true;
            case 9:
                presenceModule.onSubscriptionTerminated((PresenceSubscription) msg.obj);
                return true;
            case 10:
                PresenceInfo presenceInfo = (PresenceInfo) ((AsyncResult) msg.obj).result;
                presenceModule.onNewPresenceInformation(presenceInfo, presenceInfo.getPhoneId());
                return true;
            case 11:
                presenceModule.onNewPresenceInformation((PresenceInfo) msg.obj, msg.arg1);
                return true;
            case 12:
                presenceModule.onNewWatcherInformation((PresenceInfo) ((AsyncResult) msg.obj).result, phoneId);
                return true;
            case 13:
                presenceModule.clearWakeLock();
                return true;
            case 14:
                presenceModule.onBadEventTimeout(((Integer) msg.obj).intValue());
                return true;
            default:
                return false;
        }
    }
}
