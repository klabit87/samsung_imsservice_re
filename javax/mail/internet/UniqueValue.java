package javax.mail.internet;

import javax.mail.Session;

class UniqueValue {
    private static int id = 0;

    UniqueValue() {
    }

    public static String getUniqueBoundaryValue() {
        StringBuffer s = new StringBuffer();
        s.append("----=_Part_");
        s.append(getUniqueId());
        s.append("_");
        s.append(s.hashCode());
        s.append('.');
        s.append(System.currentTimeMillis());
        return s.toString();
    }

    public static String getUniqueMessageIDValue(Session ssn) {
        String suffix;
        InternetAddress addr = InternetAddress.getLocalAddress(ssn);
        if (addr != null) {
            suffix = addr.getAddress();
        } else {
            suffix = "javamailuser@localhost";
        }
        StringBuffer s = new StringBuffer();
        s.append(s.hashCode());
        s.append('.');
        s.append(getUniqueId());
        s.append('.');
        s.append(System.currentTimeMillis());
        s.append('.');
        s.append("JavaMail.");
        s.append(suffix);
        return s.toString();
    }

    private static synchronized int getUniqueId() {
        int i;
        synchronized (UniqueValue.class) {
            i = id;
            id = i + 1;
        }
        return i;
    }
}
