package javax.mail.internet;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.io.PrintStream;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MailDateFormat extends SimpleDateFormat {
    private static Calendar cal = new GregorianCalendar(tz);
    static boolean debug = false;
    private static final long serialVersionUID = -8148227605210628779L;
    private static TimeZone tz = TimeZone.getTimeZone("GMT");

    public MailDateFormat() {
        super("EEE, d MMM yyyy HH:mm:ss 'XXXXX' (z)", Locale.US);
    }

    public StringBuffer format(Date date, StringBuffer dateStrBuf, FieldPosition fieldPosition) {
        int pos;
        int start = dateStrBuf.length();
        super.format(date, dateStrBuf, fieldPosition);
        int pos2 = start + 25;
        while (dateStrBuf.charAt(pos2) != 'X') {
            pos2++;
        }
        this.calendar.clear();
        this.calendar.setTime(date);
        int offset = this.calendar.get(15) + this.calendar.get(16);
        if (offset < 0) {
            pos = pos2 + 1;
            dateStrBuf.setCharAt(pos2, '-');
            offset = -offset;
        } else {
            pos = pos2 + 1;
            dateStrBuf.setCharAt(pos2, '+');
        }
        int rawOffsetInMins = (offset / 60) / 1000;
        int offsetInHrs = rawOffsetInMins / 60;
        int offsetInMins = rawOffsetInMins % 60;
        int pos3 = pos + 1;
        dateStrBuf.setCharAt(pos, Character.forDigit(offsetInHrs / 10, 10));
        int pos4 = pos3 + 1;
        dateStrBuf.setCharAt(pos3, Character.forDigit(offsetInHrs % 10, 10));
        int pos5 = pos4 + 1;
        dateStrBuf.setCharAt(pos4, Character.forDigit(offsetInMins / 10, 10));
        int i = pos5 + 1;
        dateStrBuf.setCharAt(pos5, Character.forDigit(offsetInMins % 10, 10));
        return dateStrBuf;
    }

    public Date parse(String text, ParsePosition pos) {
        return parseDate(text.toCharArray(), pos, isLenient());
    }

    private static Date parseDate(char[] orig, ParsePosition pos, boolean lenient) {
        int year;
        char[] cArr = orig;
        ParsePosition parsePosition = pos;
        int seconds = 0;
        int offset = 0;
        try {
            MailDateParser p = new MailDateParser(cArr);
            p.skipUntilNumber();
            int day = p.parseNumber();
            if (!p.skipIfChar('-')) {
                p.skipWhiteSpace();
            }
            int month = p.parseMonth();
            if (!p.skipIfChar('-')) {
                p.skipWhiteSpace();
            }
            int year2 = p.parseNumber();
            if (year2 < 50) {
                year = year2 + 2000;
            } else if (year2 < 100) {
                year = year2 + NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_PUSH_TOKEN_GEN_FAILURE;
            } else {
                year = year2;
            }
            p.skipWhiteSpace();
            int hours = p.parseNumber();
            p.skipChar(':');
            int minutes = p.parseNumber();
            if (p.skipIfChar(':')) {
                seconds = p.parseNumber();
            }
            try {
                p.skipWhiteSpace();
                offset = p.parseTimeZone();
            } catch (ParseException e) {
                if (debug) {
                    PrintStream printStream = System.out;
                    printStream.println("No timezone? : '" + new String(cArr) + "'");
                }
            }
            parsePosition.setIndex(p.getIndex());
            return ourUTC(year, month, day, hours, minutes, seconds, offset, lenient);
        } catch (Exception e2) {
            if (debug) {
                PrintStream printStream2 = System.out;
                printStream2.println("Bad date: '" + new String(cArr) + "'");
                e2.printStackTrace();
            }
            parsePosition.setIndex(1);
            return null;
        }
    }

    private static synchronized Date ourUTC(int year, int mon, int mday, int hour, int min, int sec, int tzoffset, boolean lenient) {
        Date time;
        synchronized (MailDateFormat.class) {
            cal.clear();
            cal.setLenient(lenient);
            cal.set(1, year);
            cal.set(2, mon);
            cal.set(5, mday);
            cal.set(11, hour);
            cal.set(12, min + tzoffset);
            cal.set(13, sec);
            time = cal.getTime();
        }
        return time;
    }

    public void setCalendar(Calendar newCalendar) {
        throw new RuntimeException("Method setCalendar() shouldn't be called");
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        throw new RuntimeException("Method setNumberFormat() shouldn't be called");
    }
}
