package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class NotificationStatus {
    public static final int NOTIFICATION_DELIVERED = 0;
    public static final int NOTIFICATION_DISPLAYED = 1;
    public static final int NOTIFICATION_INTERWORKING_MMS = 3;
    public static final int NOTIFICATION_INTERWORKING_SMS = 2;
    public static final String[] names = {"NOTIFICATION_DELIVERED", "NOTIFICATION_DISPLAYED", "NOTIFICATION_INTERWORKING_SMS", "NOTIFICATION_INTERWORKING_MMS"};

    private NotificationStatus() {
    }

    public static String name(int e) {
        return names[e];
    }
}
