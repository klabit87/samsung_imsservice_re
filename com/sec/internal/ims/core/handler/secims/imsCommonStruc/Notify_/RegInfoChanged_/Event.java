package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_;

import com.sec.internal.constants.ims.ImsConstants;

public final class Event {
    public static final int CREATED = 1;
    public static final int DEACTIVATED = 5;
    public static final int EXPIRED = 4;
    public static final int PROBATION = 6;
    public static final int REFRESHED = 2;
    public static final int REGISTERED = 0;
    public static final int SHORTENED = 3;
    public static final String[] names = {ImsConstants.Intents.EXTRA_REGISTERED, "CREATED", "REFRESHED", "SHORTENED", "EXPIRED", "DEACTIVATED", "PROBATION"};

    private Event() {
    }

    public static String name(int e) {
        return names[e];
    }
}
