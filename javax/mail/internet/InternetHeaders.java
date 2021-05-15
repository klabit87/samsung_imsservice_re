package javax.mail.internet;

import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.helper.httpclient.HttpController;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders {
    protected List headers;

    protected static final class InternetHeader extends Header {
        String line;

        public InternetHeader(String l) {
            super("", "");
            int i = l.indexOf(58);
            if (i < 0) {
                this.name = l.trim();
            } else {
                this.name = l.substring(0, i).trim();
            }
            this.line = l;
        }

        public InternetHeader(String n, String v) {
            super(n, "");
            if (v != null) {
                this.line = String.valueOf(n) + ": " + v;
                return;
            }
            this.line = null;
        }

        public String getValue() {
            int i = this.line.indexOf(58);
            if (i < 0) {
                return this.line;
            }
            int j = i + 1;
            while (j < this.line.length() && ((c = this.line.charAt(j)) == ' ' || c == 9 || c == 13 || c == 10)) {
                j++;
            }
            return this.line.substring(j);
        }
    }

    static class matchEnum implements Enumeration {
        private Iterator e;
        private boolean match;
        private String[] names;
        private InternetHeader next_header = null;
        private boolean want_line;

        matchEnum(List v, String[] n, boolean m, boolean l) {
            this.e = v.iterator();
            this.names = n;
            this.match = m;
            this.want_line = l;
        }

        public boolean hasMoreElements() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            return this.next_header != null;
        }

        public Object nextElement() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            if (this.next_header != null) {
                InternetHeader h = this.next_header;
                this.next_header = null;
                if (this.want_line) {
                    return h.line;
                }
                return new Header(h.getName(), h.getValue());
            }
            throw new NoSuchElementException("No more headers");
        }

        private InternetHeader nextMatch() {
            while (this.e.hasNext()) {
                InternetHeader h = (InternetHeader) this.e.next();
                if (h.line != null) {
                    if (this.names != null) {
                        int i = 0;
                        while (true) {
                            String[] strArr = this.names;
                            if (i >= strArr.length) {
                                if (this.match == 0) {
                                    return h;
                                }
                            } else if (!strArr[i].equalsIgnoreCase(h.getName())) {
                                i++;
                            } else if (this.match) {
                                return h;
                            }
                        }
                    } else if (this.match) {
                        return null;
                    } else {
                        return h;
                    }
                }
            }
            return null;
        }
    }

    public InternetHeaders() {
        ArrayList arrayList = new ArrayList(40);
        this.headers = arrayList;
        arrayList.add(new InternetHeader("Return-Path", (String) null));
        this.headers.add(new InternetHeader("Received", (String) null));
        this.headers.add(new InternetHeader("Resent-Date", (String) null));
        this.headers.add(new InternetHeader("Resent-From", (String) null));
        this.headers.add(new InternetHeader("Resent-Sender", (String) null));
        this.headers.add(new InternetHeader("Resent-To", (String) null));
        this.headers.add(new InternetHeader("Resent-Cc", (String) null));
        this.headers.add(new InternetHeader("Resent-Bcc", (String) null));
        this.headers.add(new InternetHeader("Resent-Message-Id", (String) null));
        this.headers.add(new InternetHeader("Date", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.from, (String) null));
        this.headers.add(new InternetHeader("Sender", (String) null));
        this.headers.add(new InternetHeader("Reply-To", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.to, (String) null));
        this.headers.add(new InternetHeader(AttributeNames.cc, (String) null));
        this.headers.add(new InternetHeader(AttributeNames.bcc, (String) null));
        this.headers.add(new InternetHeader("Message-Id", (String) null));
        this.headers.add(new InternetHeader("In-Reply-To", (String) null));
        this.headers.add(new InternetHeader("References", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.subject, (String) null));
        this.headers.add(new InternetHeader("Comments", (String) null));
        this.headers.add(new InternetHeader("Keywords", (String) null));
        this.headers.add(new InternetHeader("Errors-To", (String) null));
        this.headers.add(new InternetHeader("MIME-Version", (String) null));
        this.headers.add(new InternetHeader("Content-Type", (String) null));
        this.headers.add(new InternetHeader(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, (String) null));
        this.headers.add(new InternetHeader("Content-MD5", (String) null));
        this.headers.add(new InternetHeader(":", (String) null));
        this.headers.add(new InternetHeader("Content-Length", (String) null));
        this.headers.add(new InternetHeader("Status", (String) null));
    }

    public InternetHeaders(InputStream is) throws MessagingException {
        this.headers = new ArrayList(40);
        load(is);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0030 A[Catch:{ IOException -> 0x0051 }] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0021 A[Catch:{ IOException -> 0x0051 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(java.io.InputStream r7) throws javax.mail.MessagingException {
        /*
            r6 = this;
            com.sun.mail.util.LineInputStream r0 = new com.sun.mail.util.LineInputStream
            r0.<init>(r7)
            r1 = 0
            java.lang.StringBuffer r2 = new java.lang.StringBuffer
            r2.<init>()
        L_0x000b:
            java.lang.String r3 = r0.readLine()     // Catch:{ IOException -> 0x0051 }
            if (r3 == 0) goto L_0x0030
            java.lang.String r4 = " "
            boolean r4 = r3.startsWith(r4)     // Catch:{ IOException -> 0x0051 }
            if (r4 != 0) goto L_0x0021
            java.lang.String r4 = "\t"
            boolean r4 = r3.startsWith(r4)     // Catch:{ IOException -> 0x0051 }
            if (r4 == 0) goto L_0x0030
        L_0x0021:
            if (r1 == 0) goto L_0x0027
            r2.append(r1)     // Catch:{ IOException -> 0x0051 }
            r1 = 0
        L_0x0027:
            java.lang.String r4 = "\r\n"
            r2.append(r4)     // Catch:{ IOException -> 0x0051 }
            r2.append(r3)     // Catch:{ IOException -> 0x0051 }
            goto L_0x0048
        L_0x0030:
            if (r1 == 0) goto L_0x0036
            r6.addHeaderLine(r1)     // Catch:{ IOException -> 0x0051 }
            goto L_0x0047
        L_0x0036:
            int r4 = r2.length()     // Catch:{ IOException -> 0x0051 }
            if (r4 <= 0) goto L_0x0047
            java.lang.String r4 = r2.toString()     // Catch:{ IOException -> 0x0051 }
            r6.addHeaderLine(r4)     // Catch:{ IOException -> 0x0051 }
            r4 = 0
            r2.setLength(r4)     // Catch:{ IOException -> 0x0051 }
        L_0x0047:
            r1 = r3
        L_0x0048:
            if (r3 == 0) goto L_0x0050
            int r4 = r3.length()     // Catch:{ IOException -> 0x0051 }
            if (r4 > 0) goto L_0x000b
        L_0x0050:
            return
        L_0x0051:
            r3 = move-exception
            javax.mail.MessagingException r4 = new javax.mail.MessagingException
            java.lang.String r5 = "Error in input stream"
            r4.<init>(r5, r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetHeaders.load(java.io.InputStream):void");
    }

    public String[] getHeader(String name) {
        List v = new ArrayList();
        for (InternetHeader h : this.headers) {
            if (name.equalsIgnoreCase(h.getName()) && h.line != null) {
                v.add(h.getValue());
            }
        }
        if (v.size() == 0) {
            return null;
        }
        return (String[]) v.toArray(new String[v.size()]);
    }

    public String getHeader(String name, String delimiter) {
        String[] s = getHeader(name);
        if (s == null) {
            return null;
        }
        if (s.length == 1 || delimiter == null) {
            return s[0];
        }
        StringBuffer r = new StringBuffer(s[0]);
        for (int i = 1; i < s.length; i++) {
            r.append(delimiter);
            r.append(s[i]);
        }
        return r.toString();
    }

    public void setHeader(String name, String value) {
        boolean found = false;
        int i = 0;
        while (i < this.headers.size()) {
            InternetHeader h = (InternetHeader) this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (!found) {
                    if (h.line != null) {
                        int indexOf = h.line.indexOf(58);
                        int j = indexOf;
                        if (indexOf >= 0) {
                            h.line = String.valueOf(h.line.substring(0, j + 1)) + " " + value;
                            found = true;
                        }
                    }
                    h.line = String.valueOf(name) + ": " + value;
                    found = true;
                } else {
                    this.headers.remove(i);
                    i--;
                }
            }
            i++;
        }
        if (!found) {
            addHeader(name, value);
        }
    }

    public void addHeader(String name, String value) {
        int pos = this.headers.size();
        boolean addReverse = name.equalsIgnoreCase("Received") || name.equalsIgnoreCase("Return-Path");
        if (addReverse) {
            pos = 0;
        }
        for (int i = this.headers.size() - 1; i >= 0; i--) {
            InternetHeader h = (InternetHeader) this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (addReverse) {
                    pos = i;
                } else {
                    this.headers.add(i + 1, new InternetHeader(name, value));
                    return;
                }
            }
            if (h.getName().equals(":")) {
                pos = i;
            }
        }
        this.headers.add(pos, new InternetHeader(name, value));
    }

    public void removeHeader(String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            InternetHeader h = (InternetHeader) this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                h.line = null;
            }
        }
    }

    public Enumeration getAllHeaders() {
        return new matchEnum(this.headers, (String[]) null, false, false);
    }

    public Enumeration getMatchingHeaders(String[] names) {
        return new matchEnum(this.headers, names, true, false);
    }

    public Enumeration getNonMatchingHeaders(String[] names) {
        return new matchEnum(this.headers, names, false, false);
    }

    public void addHeaderLine(String line) {
        try {
            char c = line.charAt(0);
            if (c != ' ') {
                if (c != 9) {
                    this.headers.add(new InternetHeader(line));
                    return;
                }
            }
            InternetHeader h = (InternetHeader) this.headers.get(this.headers.size() - 1);
            h.line = String.valueOf(h.line) + "\r\n" + line;
        } catch (StringIndexOutOfBoundsException e) {
        } catch (NoSuchElementException e2) {
        }
    }

    public Enumeration getAllHeaderLines() {
        return getNonMatchingHeaderLines((String[]) null);
    }

    public Enumeration getMatchingHeaderLines(String[] names) {
        return new matchEnum(this.headers, names, true, true);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) {
        return new matchEnum(this.headers, names, false, true);
    }
}
