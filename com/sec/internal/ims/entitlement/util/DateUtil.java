package com.sec.internal.ims.entitlement.util;

import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private static final String LOG_TAG = DateUtil.class.getSimpleName();
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static Date parseIso8601Date(String iso8601) {
        String str = LOG_TAG;
        IMSLog.i(str, "parseIso8601Date: ISO8601 " + iso8601);
        if (iso8601 == null) {
            IMSLog.e(LOG_TAG, "parseIso8601Date: input is null");
            return null;
        }
        try {
            return fetchDateFormat().parse(iso8601);
        } catch (ParseException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "parseIso8601Date: " + e.getMessage());
            return null;
        }
    }

    private static SimpleDateFormat fetchDateFormat() {
        SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso8601.setTimeZone(UTC_TIME_ZONE);
        return iso8601;
    }
}
