package com.sec.internal.ims.servicemodules.presence;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PresenceSubscriptionController {
    private static final String LOG_TAG = "PresenceSubscriptionController";
    private static Queue<ImsUri> mLazySubscriptionQueue = new LinkedList();
    private static List<PresenceSubscription> mPendingSubscriptionList = new ArrayList();
    private static List<PresenceSubscription> mSubscriptionList = new ArrayList();

    PresenceSubscriptionController() {
    }

    public static class SubscriptionRequest {
        public boolean isAlwaysForce = false;
        public int phoneId;
        public CapabilityConstants.RequestType type;
        public ImsUri uri;

        public SubscriptionRequest(ImsUri uri2, CapabilityConstants.RequestType type2, boolean isAlwaysForce2, int phoneId2) {
            this.uri = uri2;
            this.type = type2;
            this.isAlwaysForce = isAlwaysForce2;
            this.phoneId = phoneId2;
        }
    }

    static void addSubscription(PresenceSubscription subscription) {
        mSubscriptionList.add(subscription);
    }

    static void addLazySubscription(ImsUri uri) {
        mLazySubscriptionQueue.add(uri);
    }

    static void addPendingSubscription(PresenceSubscription subscription) {
        mPendingSubscriptionList.add(subscription);
    }

    static List<PresenceSubscription> getPendingSubscription() {
        return mPendingSubscriptionList;
    }

    static void clearPendingSubscription() {
        mPendingSubscriptionList.clear();
    }

    static PresenceSubscription getSubscription(ImsUri uri, boolean isSingleFetch, int phoneId) {
        if (uri == null) {
            IMSLog.e(LOG_TAG, phoneId, "getSubscription: uri is null");
            return null;
        }
        for (PresenceSubscription sub : mSubscriptionList) {
            if (sub.isSingleFetch() == isSingleFetch && sub.getPhoneId() == phoneId) {
                if ((isSingleFetch || !sub.isExpired()) && sub.contains(uri)) {
                    return sub;
                }
            }
        }
        return null;
    }

    static PresenceSubscription getSubscription(String subscriptionId, int phoneId) {
        if (subscriptionId == null) {
            Log.e(LOG_TAG, "getSubscription: subscriptionId is null");
            return null;
        }
        for (PresenceSubscription sub : mSubscriptionList) {
            if (sub.getSubscriptionId().equals(subscriptionId) && sub.getPhoneId() == phoneId) {
                return sub;
            }
        }
        return null;
    }

    static boolean hasSubscription(ImsUri uri) {
        for (PresenceSubscription sub : mSubscriptionList) {
            if (!sub.isExpired() && sub.contains(uri)) {
                return true;
            }
        }
        return false;
    }

    static void cleanExpiredSubscription() {
        Iterator<PresenceSubscription> it = mSubscriptionList.iterator();
        while (it.hasNext()) {
            PresenceSubscription s = it.next();
            if (!s.isSingleFetch() && s.getState() == 2) {
                IMSLog.s(LOG_TAG, "cleanExpiredSubscription(): expired uri " + s.getUriList() + " (" + s.getTimestamp() + ")");
                it.remove();
            }
        }
    }

    static void removeSubscription(List<ImsUri> uris) {
        if (uris == null || uris.size() == 0) {
            Log.e(LOG_TAG, "removeSubscription: uris null");
        } else if (uris.size() > 10) {
            Log.e(LOG_TAG, "removeSubscription: uris size is over " + uris.size());
        } else {
            Iterator<PresenceSubscription> it = mSubscriptionList.iterator();
            while (it.hasNext()) {
                PresenceSubscription s = it.next();
                if (s.isSingleFetch()) {
                    Iterator<ImsUri> it2 = uris.iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (s.contains(it2.next())) {
                                it.remove();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    for (ImsUri uri : uris) {
                        if (s.contains(uri)) {
                            s.remove(uri);
                        }
                    }
                }
            }
        }
    }

    static boolean checkLazySubscription(ImsUri telUri, boolean isLongLivedSubscription) {
        if (!mLazySubscriptionQueue.isEmpty() && mLazySubscriptionQueue.peek() != null && !mLazySubscriptionQueue.peek().equals(telUri) && !isLongLivedSubscription) {
            return true;
        }
        if (mLazySubscriptionQueue.isEmpty()) {
            return false;
        }
        mLazySubscriptionQueue.remove();
        return false;
    }
}
