package javax.mail.internet;

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Address;

public class NewsAddress extends Address {
    private static final long serialVersionUID = -4203797299824684143L;
    protected String host;
    protected String newsgroup;

    public NewsAddress() {
    }

    public NewsAddress(String newsgroup2) {
        this(newsgroup2, (String) null);
    }

    public NewsAddress(String newsgroup2, String host2) {
        this.newsgroup = newsgroup2;
        this.host = host2;
    }

    public String getType() {
        return "news";
    }

    public void setNewsgroup(String newsgroup2) {
        this.newsgroup = newsgroup2;
    }

    public String getNewsgroup() {
        return this.newsgroup;
    }

    public void setHost(String host2) {
        this.host = host2;
    }

    public String getHost() {
        return this.host;
    }

    public String toString() {
        return this.newsgroup;
    }

    public boolean equals(Object a) {
        String str;
        if (!(a instanceof NewsAddress)) {
            return false;
        }
        NewsAddress s = (NewsAddress) a;
        if (this.newsgroup.equals(s.newsgroup)) {
            if (this.host == null && s.host == null) {
                return true;
            }
            String str2 = this.host;
            if (str2 == null || (str = s.host) == null || !str2.equalsIgnoreCase(str)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        int hash = 0;
        String str = this.newsgroup;
        if (str != null) {
            hash = 0 + str.hashCode();
        }
        String str2 = this.host;
        if (str2 != null) {
            return hash + str2.toLowerCase(Locale.ENGLISH).hashCode();
        }
        return hash;
    }

    public static String toString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuffer s = new StringBuffer(addresses[0].toString());
        for (int i = 1; i < addresses.length; i++) {
            s.append(",");
            s.append(addresses[i].toString());
        }
        return s.toString();
    }

    public static NewsAddress[] parse(String newsgroups) throws AddressException {
        StringTokenizer st = new StringTokenizer(newsgroups, ",");
        Vector nglist = new Vector();
        while (st.hasMoreTokens()) {
            nglist.addElement(new NewsAddress(st.nextToken()));
        }
        int size = nglist.size();
        NewsAddress[] na = new NewsAddress[size];
        if (size > 0) {
            nglist.copyInto(na);
        }
        return na;
    }
}
