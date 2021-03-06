package org.xbill.DNS;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final class FormattedTime {
    private static NumberFormat w2;
    private static NumberFormat w4;

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        w2 = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(2);
        DecimalFormat decimalFormat2 = new DecimalFormat();
        w4 = decimalFormat2;
        decimalFormat2.setMinimumIntegerDigits(4);
        w4.setGroupingUsed(false);
    }

    private FormattedTime() {
    }

    public static String format(Date date) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        StringBuffer sb = new StringBuffer();
        c.setTime(date);
        sb.append(w4.format((long) c.get(1)));
        sb.append(w2.format((long) (c.get(2) + 1)));
        sb.append(w2.format((long) c.get(5)));
        sb.append(w2.format((long) c.get(11)));
        sb.append(w2.format((long) c.get(12)));
        sb.append(w2.format((long) c.get(13)));
        return sb.toString();
    }

    public static Date parse(String s) throws TextParseException {
        if (s.length() == 14) {
            Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            c.clear();
            try {
                c.set(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)) - 1, Integer.parseInt(s.substring(6, 8)), Integer.parseInt(s.substring(8, 10)), Integer.parseInt(s.substring(10, 12)), Integer.parseInt(s.substring(12, 14)));
                return c.getTime();
            } catch (NumberFormatException e) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid time encoding: ");
                stringBuffer.append(s);
                throw new TextParseException(stringBuffer.toString());
            }
        } else {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("Invalid time encoding: ");
            stringBuffer2.append(s);
            throw new TextParseException(stringBuffer2.toString());
        }
    }
}
