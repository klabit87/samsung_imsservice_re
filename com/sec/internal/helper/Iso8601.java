package com.sec.internal.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Iso8601 {
    private static final List<DateFormat> ALL_FORMATS;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final DateFormat MILLISECONDS_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
    private static final DateFormat MILLISECONDS_FORMAT_GMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final DateFormat MINUTES_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.getDefault());
    private static final DateFormat MINUTES_FORMAT_GMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault());
    private static final DateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final DateFormat SECONDS_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
    private static final DateFormat SECONDS_FORMAT_GMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private static final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());

    static {
        ArrayList arrayList = new ArrayList();
        ALL_FORMATS = arrayList;
        arrayList.add(SECONDS_FORMAT);
        ALL_FORMATS.add(SECONDS_FORMAT_GMT);
        ALL_FORMATS.add(MILLISECONDS_FORMAT_GMT);
        ALL_FORMATS.add(MILLISECONDS_FORMAT);
        ALL_FORMATS.add(MINUTES_FORMAT_GMT);
        ALL_FORMATS.add(MINUTES_FORMAT);
        ALL_FORMATS.add(DATE_FORMAT);
        ALL_FORMATS.add(MONTH_FORMAT);
        ALL_FORMATS.add(YEAR_FORMAT);
        for (DateFormat df : ALL_FORMATS) {
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    public static synchronized String format(Date date) throws NullPointerException, IllegalArgumentException {
        String format;
        synchronized (Iso8601.class) {
            format = SECONDS_FORMAT_GMT.format(date);
        }
        return format;
    }

    public static synchronized String formatMillis(Date date) throws NullPointerException, IllegalArgumentException {
        String format;
        synchronized (Iso8601.class) {
            format = MILLISECONDS_FORMAT_GMT.format(date);
        }
        return format;
    }

    public static synchronized Date parse(String date) throws NullPointerException, ParseException {
        Date parse;
        synchronized (Iso8601.class) {
            int timezoneStart = Math.max(date.lastIndexOf(45), date.lastIndexOf(43));
            int indexOfT = date.lastIndexOf(84);
            int lastColon = date.lastIndexOf(58);
            if (timezoneStart > -1 && lastColon > timezoneStart && timezoneStart > indexOfT) {
                date = date.substring(0, lastColon) + date.substring(lastColon + 1, date.length());
            }
            for (DateFormat format : ALL_FORMATS) {
                try {
                    parse = format.parse(date);
                } catch (ParseException e) {
                }
            }
            throw new ParseException(String.format("unsupported format for date %s", new Object[]{date}), 0);
        }
        return parse;
    }
}
