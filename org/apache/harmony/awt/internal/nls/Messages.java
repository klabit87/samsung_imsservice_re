package org.apache.harmony.awt.internal.nls;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static ResourceBundle bundle;

    static {
        bundle = null;
        try {
            bundle = setLocale(Locale.getDefault(), "org.apache.harmony.awt.internal.nls.messages");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String getString(String msg) {
        ResourceBundle resourceBundle = bundle;
        if (resourceBundle == null) {
            return msg;
        }
        try {
            return resourceBundle.getString(msg);
        } catch (MissingResourceException e) {
            return "Missing message: " + msg;
        }
    }

    public static String getString(String msg, Object arg) {
        return getString(msg, new Object[]{arg});
    }

    public static String getString(String msg, int arg) {
        return getString(msg, new Object[]{Integer.toString(arg)});
    }

    public static String getString(String msg, char arg) {
        return getString(msg, new Object[]{String.valueOf(arg)});
    }

    public static String getString(String msg, Object arg1, Object arg2) {
        return getString(msg, new Object[]{arg1, arg2});
    }

    public static String getString(String msg, Object[] args) {
        String format = msg;
        ResourceBundle resourceBundle = bundle;
        if (resourceBundle != null) {
            try {
                format = resourceBundle.getString(msg);
            } catch (MissingResourceException e) {
            }
        }
        return format(format, args);
    }

    public static String format(String format, Object[] args) {
        StringBuilder answer = new StringBuilder(format.length() + (args.length * 20));
        String[] argStrings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                argStrings[i] = "<null>";
            } else {
                argStrings[i] = args[i].toString();
            }
        }
        int lastI = 0;
        int i2 = format.indexOf(123, 0);
        while (i2 >= 0) {
            if (i2 != 0 && format.charAt(i2 - 1) == '\\') {
                if (i2 != 1) {
                    answer.append(format.substring(lastI, i2 - 1));
                }
                answer.append('{');
                lastI = i2 + 1;
            } else if (i2 > format.length() - 3) {
                answer.append(format.substring(lastI, format.length()));
                lastI = format.length();
            } else {
                int argnum = (byte) Character.digit(format.charAt(i2 + 1), 10);
                if (argnum < 0 || format.charAt(i2 + 2) != '}') {
                    answer.append(format.substring(lastI, i2 + 1));
                    lastI = i2 + 1;
                } else {
                    answer.append(format.substring(lastI, i2));
                    if (argnum >= argStrings.length) {
                        answer.append("<missing argument>");
                    } else {
                        answer.append(argStrings[argnum]);
                    }
                    lastI = i2 + 3;
                }
            }
            i2 = format.indexOf(123, lastI);
        }
        if (lastI < format.length()) {
            answer.append(format.substring(lastI, format.length()));
        }
        return answer.toString();
    }

    public static ResourceBundle setLocale(final Locale locale, final String resource) {
        try {
            return (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction<Object>((ClassLoader) null) {
                public Object run() {
                    String str = resource;
                    Locale locale = locale;
                    ClassLoader classLoader = null;
                    if (classLoader == null) {
                        classLoader = ClassLoader.getSystemClassLoader();
                    }
                    return ResourceBundle.getBundle(str, locale, classLoader);
                }
            });
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
