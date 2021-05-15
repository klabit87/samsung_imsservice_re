package com.sec.internal.ims.cmstore.utils;

import android.util.Log;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import java.util.Map;
import java.util.TreeMap;

public class NotificationListContainer {
    private static final int MAX_SIZE = 60;
    private static final String TAG = NotificationListContainer.class.getSimpleName();
    private static NotificationListContainer sInstance = new NotificationListContainer();
    private TreeMap<Long, NotificationList[]> container = new TreeMap<>();

    private NotificationListContainer() {
    }

    public static NotificationListContainer getInstance() {
        return sInstance;
    }

    public synchronized void insertContainer(Long index, NotificationList[] notificationList) {
        String str = TAG;
        Log.d(str, "insertContainer, index=" + index + ",container.size()= " + this.container.size());
        if (this.container.size() < 60) {
            this.container.put(index, notificationList);
        }
    }

    public synchronized long peekFirstIndex() {
        if (this.container.isEmpty()) {
            return -1;
        }
        long index = this.container.firstKey().longValue();
        String str = TAG;
        Log.d(str, "peekFirstIndex, index=" + index);
        return index;
    }

    public synchronized Map.Entry<Long, NotificationList[]> popFirstEntry() {
        if (this.container.isEmpty()) {
            return null;
        }
        Map.Entry<Long, NotificationList[]> firstEntry = this.container.firstEntry();
        String str = TAG;
        Log.d(str, "popFirstEntry, index=" + firstEntry.getKey());
        this.container.remove(firstEntry.getKey());
        return firstEntry;
    }

    public synchronized boolean isEmpty() {
        return this.container.isEmpty();
    }

    public synchronized void clear() {
        this.container.clear();
    }
}
