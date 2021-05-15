package com.sec.internal.ims.cmstore.helper;

import java.util.HashMap;
import java.util.Map;

public class DebugFlag {
    public static final String APP_ID = "app_id";
    public static final String AUTH_HOST_NAME = "auth_host_name";
    public static final String CPS_HOST_NAME = "cps_host_name";
    public static final String DEBUG_FLAG = "AMBS_DEBUG";
    public static boolean DEBUG_RETRY_TIMELINE_FLAG = false;
    public static final boolean ENABLE_ADVANCE_DEBUG = false;
    public static final String NC_HOST_NAME = "nc_host_name";
    public static final String RETRY_TIME = "retry_time";
    public static String debugRetryTimeLine = "10000,10000,10000,10000,10000";
    protected static Map<Integer, Integer> mRetrySchedule = new HashMap();

    static {
        initRetryTimeLine();
    }

    public static void initRetryTimeLine() {
        mRetrySchedule.clear();
        for (int i = 0; i < 5; i++) {
            mRetrySchedule.put(Integer.valueOf(i), 10000);
        }
    }

    public static void setRetryTimeLine(String timeValue) {
        mRetrySchedule.clear();
        debugRetryTimeLine = timeValue;
        String[] times = timeValue.split(",");
        for (int i = 0; i < times.length; i++) {
            mRetrySchedule.put(Integer.valueOf(i), Integer.valueOf(Integer.parseInt(times[i])));
        }
    }

    public static int getRetryTimeLine(int counter) {
        if (mRetrySchedule.containsKey(Integer.valueOf(counter))) {
            return mRetrySchedule.get(Integer.valueOf(counter)).intValue();
        }
        return 10;
    }
}
