package org.xbill.DNS;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class Options {
    private static Map table;

    static {
        try {
            refresh();
        } catch (SecurityException e) {
        }
    }

    private Options() {
    }

    public static void refresh() {
        String s = System.getProperty("dnsjava.options");
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int index = token.indexOf(61);
                if (index == -1) {
                    set(token);
                } else {
                    set(token.substring(0, index), token.substring(index + 1));
                }
            }
        }
    }

    public static void clear() {
        table = null;
    }

    public static void set(String option) {
        if (table == null) {
            table = new HashMap();
        }
        table.put(option.toLowerCase(), CloudMessageProviderContract.JsonData.TRUE);
    }

    public static void set(String option, String value) {
        if (table == null) {
            table = new HashMap();
        }
        table.put(option.toLowerCase(), value.toLowerCase());
    }

    public static void unset(String option) {
        Map map = table;
        if (map != null) {
            map.remove(option.toLowerCase());
        }
    }

    public static boolean check(String option) {
        Map map = table;
        if (map == null || map.get(option.toLowerCase()) == null) {
            return false;
        }
        return true;
    }

    public static String value(String option) {
        Map map = table;
        if (map == null) {
            return null;
        }
        return (String) map.get(option.toLowerCase());
    }

    public static int intValue(String option) {
        String s = value(option);
        if (s == null) {
            return -1;
        }
        try {
            int val = Integer.parseInt(s);
            if (val > 0) {
                return val;
            }
            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
