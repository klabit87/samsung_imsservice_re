package com.sec.internal.omanetapi.common.data;

import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.NmsSubscriptionUpdate;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;

public class OMAApiRequestParam {

    public static class AllObjectRequest {
        public Object object;
    }

    public static class AllSubscriptionRequest {
        public NmsSubscription nmsSubscription;
    }

    public static class BulkCreationRequest {
        public ObjectList objectList;
    }

    public static class BulkDeletionRequest {
        public BulkDelete bulkDelete;
    }

    public static class BulkUpdateRequest {
        public BulkUpdate bulkUpdate;
    }

    public static class NmsSubscriptionUpdateRequest {
        public NmsSubscriptionUpdate nmsSubscriptionUpdate;
    }

    public static class NotificationChannelLifetimeRequest {
        public NotificationChannelLifetime notificationChannelLifetime;
    }

    public static class NotificationChannels {
        public NotificationChannel notificationChannel;
    }

    public static class NotificationListRequest {
        public LongPollingRequestParameters longPollingRequestParameters;
    }

    public static class ObjectSearchRequest {
        public SelectionCriteria selectionCriteria;
    }

    public static class VvmServiceProfileRequest {
        public VvmServiceProfile vvmserviceProfile;
    }
}
